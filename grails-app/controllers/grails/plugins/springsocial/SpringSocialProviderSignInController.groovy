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
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.DuplicateConnectionException
import org.springframework.social.connect.web.ProviderSignInAttempt
import org.springframework.social.oauth1.AuthorizedRequestToken
import org.springframework.social.oauth1.OAuthToken
import org.springframework.social.connect.Connection
import org.springframework.web.context.request.RequestAttributes
import org.springframework.social.support.URIBuilder
import org.springframework.web.context.request.NativeWebRequest
import grails.plugins.springsocial.signin.SpringSocialSimpleSignInAdapter

class SpringSocialProviderSignInController {
  def connectionFactoryLocator
  def connectionRepository
  def signInService
  def usersConnectionRepository
  def requestCache
  def webSupport = new GrailsConnectSupport(home: g.createLink(uri: "/", absolute: true))

  def signIn = {
    def providerId = params.providerId
    def connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
    def nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
    def url = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest)
    redirect url: url
  }

  def oauthCallback = {
    def providerId = params.providerId
    def oauth_token = params.oauth_token
    def code = params.code
    def pam = oauth_token ?: code

    def nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
    def config = SpringSocialUtils.config.get(providerId)

    if (oauth_token) {
      def connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId);
      def connection = webSupport.completeConnection(connectionFactory, nativeWebRequest);
      return handleSignIn(connection, nativeWebRequest, session, config);
    } else if (code) {
      render "providerId: ${providerId}, pam: ${pam}"
    }
  }

  String callbackUrl(provider) {
    g.createLink(mapping: 'springSocialSignIn', params: [providerId: provider], absolute: true)
  }


  private OAuthToken extractCachedRequestToken(session) {
    def requestToken = session.oauthToken
    session.removeAttribute('oauthToken')
    requestToken
  }

  private void addConnection(session, connectionFactory, connection) {
    try {
      getConnectionRepository().addConnection(connection)
      //postConnect(connectionFactory, connection, request)
    } catch (DuplicateConnectionException e) {
      session.addAttribute(DUPLICATE_CONNECTION_EXCEPTION_ATTRIBUTE, e)
    }
  }

  private String handleSignIn(Connection connection, NativeWebRequest request, session, config) {
    String result
		List<String> userIds = usersConnectionRepository.findUserIdsWithConnection(connection);
		if (userIds.size() == 0) {
			ProviderSignInAttempt signInAttempt = new ProviderSignInAttempt(connection, connectionFactoryLocator, usersConnectionRepository);
			request.setAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE, signInAttempt, RequestAttributes.SCOPE_SESSION)
      //TODO: Document this setting
      result = session.ss_oauth_redirect_on_signIn_attempt ?: config.page.handleSignIn
		} else if (userIds.size() == 1){
			usersConnectionRepository.createConnectionRepository(userIds.get(0)).updateConnection(connection)
      def signInAdapter = new SpringSocialSimpleSignInAdapter(requestCache)
			def originalUrl = signInAdapter.signIn(userIds.get(0), connection, request)
      println "originalUrl: ${originalUrl}"
      //TODO: Document this setting
			result = originalUrl ?: config.postSignInUrl
		} else {
      //TODO: handle redirect when multiple users found
			//result = redirect(URIBuilder.fromUri(signInUrl).queryParam("error", "multiple_users").build().toString());
		}
    result
	}
}