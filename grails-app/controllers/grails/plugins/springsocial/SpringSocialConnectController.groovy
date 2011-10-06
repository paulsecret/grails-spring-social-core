/* Copyright 2011 the original author or authors.
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

import grails.plugins.springsocial.connect.web.GrailsConnectSupport
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.social.connect.ConnectionKey
import org.springframework.social.connect.DuplicateConnectionException
import org.springframework.web.context.request.RequestAttributes

class SpringSocialConnectController {

  private static final String DUPLICATE_CONNECTION_EXCEPTION_ATTRIBUTE = "_duplicateConnectionException"
  private static final String DUPLICATE_CONNECTION_ATTRIBUTE = "social.addConnection.duplicate"

  def connectionFactoryLocator
  def connectionRepository

  def webSupport = new GrailsConnectSupport(home: g.createLink(uri: "/", absolute: true))

  static allowedMethods = [connect: 'POST', oauthCallback: 'GET', disconnect: 'DELETE']

  def connect = {
    String result
    if (isLoggedIn()) {
      def providerId = params.providerId
      def connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
      def nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
      result = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest)
      redirect url: result
    } else {
      result = SpringSecurityUtils.securityConfig.auth.loginFormUrl
      redirect uri: result
    }
  }

  def oauthCallback = {
    def providerId = params.providerId
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
    def connection = webSupport.completeConnection(connectionFactory, new GrailsWebRequest(request, response, servletContext))

    addConnection(connection, connectionFactory, request)
    redirect(uri: uri)
  }

  def disconnect = {
    def providerId = params.providerId
    def providerUserId = params.providerUserId
    assert providerId, "The providerId is required"

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
      //postConnect(connectionFactory, connection, request)
    } catch (DuplicateConnectionException e) {
      request.setAttribute(DUPLICATE_CONNECTION_ATTRIBUTE, e, RequestAttributes.SCOPE_SESSION);
    }
  }
}
