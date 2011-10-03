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

class SpringSocialProviderSignInController {
  def connectionFactoryLocator
  def connectionRepository
  def signInService
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

    if (oauth_token) {
      def verifier = params.oauth_verifier

      def connectionFactory = getConnectionFactoryLocator().getConnectionFactory(providerId)
      def accessToken = connectionFactory.getOAuthOperations().exchangeForAccessToken(new AuthorizedRequestToken(extractCachedRequestToken(session), verifier), null)
      def connection = connectionFactory.createConnection(accessToken)
      //return handleSignIn(connection, request);
      redirect(url: handleSignIn(connection, session))
    } else if (code) {
      render "providerId: ${providerId}, pam: ${pam}"
    }
  }

  private ConnectionFactoryLocator getConnectionFactoryLocator() {
    return connectionFactoryLocatorProvider.get();
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

  private ConnectionRepository getConnectionRepository() {
    connectionRepositoryProvider.get()
  }

  private String handleSignIn(connection, session) {
    String localUserId = usersConnectionRepository.findUserIdWithConnection(connection)
    if (localUserId == null) {
      def signInAttempt = new ProviderSignInAttempt(connection, connectionFactoryLocatorProvider, connectionRepositoryProvider);
      session.setAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE, signInAttempt)
    } else {
      signInService.signIn(localUserId)
    }
    session.ss_last_user_profile = connection.fetchUserProfile()
    def postSignInUri = session.ss_oauth_return_url ?: g.createLink(uri: SpringSocialUtils.config.postSignInUri, absolute: true)
    postSignInUri
  }
}