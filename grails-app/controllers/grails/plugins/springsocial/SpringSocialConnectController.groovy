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
import org.springframework.web.context.request.RequestAttributes

class SpringSocialConnectController {

  private static final String DUPLICATE_CONNECTION_EXCEPTION_ATTRIBUTE = "_duplicateConnectionException"
  private static final String DUPLICATE_CONNECTION_ATTRIBUTE = "social.addConnection.duplicate"

  ConnectionFactoryLocator connectionFactoryLocator
  ConnectionRepository connectionRepository

  ConnectSupport webSupport = new GrailsConnectSupport(mapping: "springSocialConnect")
  SpringSecurityService springSecurityService

  static allowedMethods = [connect: 'POST', oauthCallback: 'GET', disconnect: 'DELETE']

  def connect() {
    String result
    if (springSecurityService.isLoggedIn()) {
      def providerId = params.providerId

      Assert.hasText(providerId, "The providerId is required")

      def connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
      MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
      //TODO: Handle preconnect filters
      //preConnect(connectionFactory, parameters, request);
      def nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
      result = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest, parameters)
      redirect url: result
    } else {
      log.warn("The connect feature only is available for Signed Users. New users perhaps can use SignIn feature.")

      //TODO: Document this parameters
      result = grailsApplication.config.springsocial.loginUrl ?: DefaultConfig.loginUrl
      log.info("Redirecting to $result")
      redirect uri: result
    }
  }

  def oauthCallback() {
    def providerId = params.providerId

    Assert.hasText(providerId, "The providerId is required")

    def config = SpringSocialUtils.config.get(providerId)
    def denied = params.denied

    if (denied) {
      //TODO: Document this parameters
      def uriRedirectOnDenied = session.ss_oauth_redirect_callback_on_denied ?: config.page.deniedHome
      if (log.isInfoEnabled()) {
        log.info("The user has denied accesss to ${providerId} profile. Redirecting to uri: ${uriRedirectOnDenied}")
      }
      redirect(uri: uriRedirectOnDenied)
      return
    }

    //TODO: Document this parameter
    def uriRedirect = session.ss_oauth_redirect_callback

    //TODO: Document this parameter
    def uri = uriRedirect ?: config.page.connectedHome

    def connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
    def nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
    def connection = webSupport.completeConnection(connectionFactory, nativeWebRequest)

    addConnection(connection, connectionFactory, nativeWebRequest)
    redirect(uri: uri)
  }

  def disconnect() {
    def providerId = params.providerId
    def providerUserId = params.providerUserId
    Assert.hasText(providerId, "The providerId is required")

    ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId);

    if (providerUserId) {
      if (log.isInfoEnabled()) {
        log.info("Disconecting from ${providerId} to ${providerUserId}")
      }
      connectionRepository.removeConnection(new ConnectionKey(providerId, providerUserId));
    } else {
      if (log.isInfoEnabled()) {
        log.info("Disconecting from ${providerId}")
      }
      connectionRepository.removeConnections(providerId)
    }

    def cfg = SpringSocialUtils.config.get(providerId)

    //TODO: Document this parameter
    def postDisconnectUri = params.ss_post_disconnect_uri ?: cfg.postDisconnectUri
    if (log.isInfoEnabled()) {
      log.info("redirecting to ${postDisconnectUri}")
    }
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
}
