/* Copyright 2013 the original author or authors.
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

import grails.plugins.springsecurity.SpringSecurityService
import grails.plugins.springsocial.config.DefaultConfig
import grails.plugins.springsocial.connect.web.GrailsConnectSupport
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.social.connect.*
import org.springframework.social.connect.web.ConnectSupport
import org.springframework.util.Assert
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes

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

  private static final String DUPLICATE_CONNECTION_ATTRIBUTE = "social.addConnection.duplicate"
  private static final String PROVIDER_ERROR_ATTRIBUTE = "social.provider.error";

  ConnectionFactoryLocator connectionFactoryLocator
  ConnectionRepository connectionRepository

  ConnectSupport webSupport = new GrailsConnectSupport(mapping: "springSocialConnect")
  SpringSecurityService springSecurityService

  static allowedMethods = [connect: 'POST', oauthCallback: 'GET', disconnect: 'DELETE']

  def connect() {
    String result
    if (springSecurityService.isLoggedIn()) {
      String providerId = params.providerId

      Assert.hasText(providerId, "The providerId is required")

      ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
      //TODO: Handle preconnect filters
      //preConnect(connectionFactory, parameters, request);
      NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
      result = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest, parameters)
      redirect url: result
    } else {
      log.warn("The connect feature only is available for Signed Users. New users perhaps can use SignIn feature.")

      result = getUrlLogin()
      log.info("Redirecting to $result")
      redirect uri: result
    }
  }

  def oauthCallback() {
    String providerId = params.providerId

    Assert.hasText(providerId, 'The providerId is required')

    def config = grailsApplication.config.springsocial.get(providerId)

    if(!config) {
      //TODO: Maybe send a http status 50x
      String result = getUrlLogin()
      redirect uri: result
      return
    }
    String denied = params.denied

    if (denied) {
      //TODO: Document this parameters
      def uriRedirectOnDenied = getPageDeniedHome(config)

      log.info("The user has denied accesss to ${providerId} profile. Redirecting to uri: ${uriRedirectOnDenied}")

      redirect(uri: uriRedirectOnDenied)
      return
    }

    String uri = getPageConnectedHome(config)

    ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
    NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
    Connection connection = webSupport.completeConnection(connectionFactory, nativeWebRequest)

    addConnection(connection, connectionFactory, nativeWebRequest)
    redirect(uri: uri)
  }

  def disconnect() {
    String providerId = params.providerId
    String providerUserId = params.providerUserId
    Assert.hasText(providerId, "The providerId is required")

    ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId);

    if (providerUserId) {
      log.info("Disconecting from ${providerId} to ${providerUserId}")
      connectionRepository.removeConnection(new ConnectionKey(providerId, providerUserId));
    } else {
      log.info("Disconecting from ${providerId}")
      connectionRepository.removeConnections(providerId)
    }

    def cfg = SpringSocialUtils.config.get(providerId)

    //TODO: Document this parameter
    def postDisconnectUri = params.ss_post_disconnect_uri ?: cfg.postDisconnectUri
    log.info("redirecting to ${postDisconnectUri}")
    redirect(uri: postDisconnectUri)
  }

  private void addConnection(connection, connectionFactory, request) {
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
}
