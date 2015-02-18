/* Copyright 2013 Domingo Suarez Torres.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.springsocial

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugins.springsocial.config.DefaultConfig
import grails.plugins.springsocial.connect.web.GrailsConnectSupport
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.core.GenericTypeResolver
import org.springframework.social.connect.*
import org.springframework.social.connect.web.ConnectInterceptor
import org.springframework.social.connect.web.ConnectSupport
import org.springframework.social.connect.web.DisconnectInterceptor
import org.springframework.util.Assert
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.UrlPathHelper

/**
 * Generic UI controller for managing the account-to-service-provider connection flow.
 * <ul>
 * <li>GET /connect/{providerId}  - Get a web page showing connection status to {providerId}.</li>
 * <li>POST /connect/{providerId} - Initiate an connection with {providerId}.</li>
 * <li>GET /connect/{providerId}?oauth_verifier||code - Receive {providerId} authorization callback and establish the connection.</li>
 * <li>DELETE /connect/{providerId} - Disconnect from {providerId}.</li>
 * </ul>
 *
 * This code was borrowed from SpringSocial code and adapted to Grails.
 * @see org.springframework.social.connect.web.ConnectController
 *
 * @author Domingo Suarez Torres
 */
class SpringSocialConnectController {
  private static final String DUPLICATE_CONNECTION_ATTRIBUTE = "social_addConnection_duplicate";
  private static final String PROVIDER_ERROR_ATTRIBUTE = "social_provider_error";
  private static final String AUTHORIZATION_ERROR_ATTRIBUTE = "social_authorization_error";

  ConnectionFactoryLocator connectionFactoryLocator
  ConnectionRepository connectionRepository

  ConnectSupport webSupport = new GrailsConnectSupport(mapping: 'springSocialConnect')
  SpringSecurityService springSecurityService

  private MultiValueMap<Class<?>, ConnectInterceptor<?>> connectInterceptors = new LinkedMultiValueMap<Class<?>, ConnectInterceptor<?>>()
  MultiValueMap<Class<?>, DisconnectInterceptor<?>> disconnectInterceptors = new LinkedMultiValueMap<Class<?>, DisconnectInterceptor<?>>()
  UrlPathHelper urlPathHelper = new UrlPathHelper()
  String viewPath = "connect/"

  static allowedMethods = [connect: 'POST', oauthCallback: 'GET', disconnect: 'DELETE']

  /**
   * Configure the list of connect interceptors that should receive callbacks during the connection process.
   * Convenient when an instance of this class is configured using a tool that supports JavaBeans-based configuration.
   * @param interceptors the connect interceptors to add
   */
  void setConnectInterceptors(List<ConnectInterceptor<?>> interceptors) {
    interceptors.each { ConnectInterceptor<?> interceptor ->
      addInterceptor(interceptor);
    }
  }

  /**
   * Configure the list of discconnect interceptors that should receive callbacks when connections are removed.
   * Convenient when an instance of this class is configured using a tool that supports JavaBeans-based configuration.
   * @param interceptors the connect interceptors to add
   */
  public void setDisconnectInterceptors(List<DisconnectInterceptor<?>> interceptors) {
    interceptors.each { DisconnectInterceptor<?> interceptor ->
      addDisconnectInterceptor(interceptor)
    }
  }

  /**
   * Adds a ConnectInterceptor to receive callbacks during the connection process.
   * Useful for programmatic configuration.
   * @param interceptor the connect interceptor to add
   */
  public void addInterceptor(ConnectInterceptor<?> interceptor) {
    Class<?> serviceApiType = GenericTypeResolver.resolveTypeArgument(interceptor.getClass(), ConnectInterceptor)
    connectInterceptors.add(serviceApiType, interceptor)
  }

  /**
   * Adds a DisconnectInterceptor to receive callbacks during the disconnection process.
   * Useful for programmatic configuration.
   * @param interceptor the connect interceptor to add
   */
  public void addDisconnectInterceptor(DisconnectInterceptor<?> interceptor) {
    Class<?> serviceApiType = GenericTypeResolver.resolveTypeArgument(interceptor.getClass(), DisconnectInterceptor)
    disconnectInterceptors.add(serviceApiType, interceptor)
  }

  def connect() {
    String result
    if (springSecurityService.isLoggedIn()) {
      String providerId = params.providerId

      Assert.hasText(providerId, 'The providerId is required')

      ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>()

      NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)

      preConnect(connectionFactory, parameters, nativeWebRequest)


      try {
        result = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest, parameters)
      } catch (e) {
        request.setAttribute(PROVIDER_ERROR_ATTRIBUTE, e, RequestAttributes.SCOPE_SESSION)
        result = connectionStatusRedirect(providerId, request)
      }
      redirect url: result
    } else {
      log.warn('The connect feature only is available for Signed Users. New users perhaps can use SignIn feature.')

      result = getUrlLogin()
      log.info("Redirecting to $result")
      redirect uri: result
    }
  }

  /**
   * Returns a RedirectView with the URL to redirect to after a connection is created or deleted.
   * Defaults to "/connect/{providerId}" relative to DispatcherServlet's path.
   * May be overridden to handle custom redirection needs.
   * @param providerId the ID of the provider for which a connection was created or deleted.
   * @param request the NativeWebRequest used to access the servlet path when constructing the redirect path.
   */
  protected String connectionStatusRedirect(String providerId, NativeWebRequest request) {
    /*HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest)
    String path = "/connect/" + providerId + getPathExtension(servletRequest)
    if (prependServletPath(servletRequest)) {
      path = servletRequest.getServletPath() + path
    }
    return new RedirectView(path, true)*/
    ""
  }

  def oauthCallback() {
    String providerId = params.providerId

    Assert.hasText(providerId, 'The providerId is required')

    ConfigObject config = getConfigByProviderId(providerId)

    if (!config) {
      //TODO: Maybe send a http status 50x
      String result = getUrlLogin()
      redirect uri: result
      return
    }
    String denied = params.denied

    if (denied) {
      //TODO: Document this parameters
      String uriRedirectOnDenied = getPageDeniedHome(config)

      log.info("The user has denied accesss to ${providerId} profile. Redirecting to uri: ${uriRedirectOnDenied}")

      redirect(uri: uriRedirectOnDenied)
      return
    }

    String uri = getPageConnectedHome(config)

    ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
    NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
    Connection connection = webSupport.completeConnection(connectionFactory, nativeWebRequest)

    //addConnection(connection, connectionFactory, nativeWebRequest)
    addConnection(connection, nativeWebRequest)
    redirect(uri: uri)
  }

  def disconnect() {
    String providerId = params.providerId
    String providerUserId = params.providerUserId
    Assert.hasText(providerId, 'The providerId is required')

    //TODO: implement this
    //ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
    //preDisconnect(connectionFactory, request)

    if (providerUserId) {
      log.info("Disconecting from ${providerId} to ${providerUserId}")
      connectionRepository.removeConnection(new ConnectionKey(providerId, providerUserId))
    } else {
      log.info("Disconecting from ${providerId}")
      connectionRepository.removeConnections(providerId)
    }

    //TODO: implement this
    //postDisconnect(connectionFactory, request)

    ConfigObject config = getConfigByProviderId(providerId)
    //TODO: Document this parameter
    String postDisconnectUri = getPagePostDisconnectHome(config)
    log.info("redirecting to ${postDisconnectUri}")
    redirect(uri: postDisconnectUri)
  }

  //private void addConnection(Connection<?> connection, ConnectionFactory<?> connectionFactory, WebRequest request) {
  private void addConnection(Connection<?> connection, WebRequest request) {
    try {
      connectionRepository.addConnection(connection)
      //TODO: handle post connections interceptors
      //postConnect(connectionFactory, connection, request)
    } catch (DuplicateConnectionException e) {
      request.setAttribute(DUPLICATE_CONNECTION_ATTRIBUTE, e, RequestAttributes.SCOPE_SESSION)
    }
  }

  private String getUrlLogin() {
    //TODO: Document this parameter
    grailsApplication.config.springsocial?.loginUrl ?: DefaultConfig.loginUrl
  }

  private String getPageConnectedHome(ConfigObject config) {
    //TODO: Document this parameter
    config.page?.connectedHome ?: DefaultConfig.pageConnectedHome
  }

  private String getPageDeniedHome(ConfigObject config) {
    //TODO: Document this parameter
    config.page?.deniedHome ?: DefaultConfig.pageDeniedHome
  }

  private String getPagePostDisconnectHome(ConfigObject config) {
    //TODO: Document this parameter
    config?.page?.postDisconnectHome ?: DefaultConfig.pagePostDisconnectHome
  }

    private ConfigObject getConfigByProviderId(String providerId) {
        grailsApplication.config.plugins.config.springsocial?.get(providerId)
    }

  @SuppressWarnings(["rawtypes", "unchecked"])
  private void preConnect(ConnectionFactory<?> connectionFactory, MultiValueMap<String, String> parameters, WebRequest request) {
    for (ConnectInterceptor interceptor : interceptingConnectionsTo(connectionFactory)) {
      interceptor.preConnect(connectionFactory, parameters, request)
    }
  }

  private List<ConnectInterceptor<?>> interceptingConnectionsTo(ConnectionFactory<?> connectionFactory) {
    Class<?> serviceType = GenericTypeResolver.resolveTypeArgument(connectionFactory.getClass(), ConnectionFactory)
    List<ConnectInterceptor<?>> typedInterceptors = connectInterceptors.get(serviceType)
    if (typedInterceptors == null) {
      typedInterceptors = Collections.emptyList()
    }
    typedInterceptors
  }
}
