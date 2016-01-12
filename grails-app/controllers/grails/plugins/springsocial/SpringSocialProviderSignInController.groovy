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

import grails.plugins.springsocial.connect.web.GrailsConnectSupport
import grails.plugins.springsocial.signin.SpringSocialSimpleSignInAdapter
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.social.connect.Connection
import org.springframework.social.connect.ConnectionFactory
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionRepository
import org.springframework.social.connect.web.ConnectSupport
import org.springframework.social.connect.web.ProviderSignInAttempt
import org.springframework.social.connect.web.SignInAdapter
import org.springframework.util.Assert
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes

class SpringSocialProviderSignInController {

    ConnectionFactoryLocator connectionFactoryLocator

    ConnectSupport webSupport = new GrailsConnectSupport(mapping: 'springSocialSignIn')

    def usersConnectionRepository

    def requestCache

    def grailsApplication

    static allowedMethods = [signin: 'POST', oauthCallback: 'GET', disconnect: 'DELETE']

    def signin() {
        String providerId = params.providerId

        Assert.hasText(providerId, 'The providerId is required')

        ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId)
        NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
        String url = webSupport.buildOAuthUrl(connectionFactory, nativeWebRequest)

        redirect url: url
    }

    def oauthCallback() {
        String providerId = params.providerId

        Assert.hasText(providerId, 'The providerId is required')

        NativeWebRequest nativeWebRequest = new GrailsWebRequest(request, response, servletContext)
        ConfigObject config = getConfigByProviderId(providerId)
        ConnectionFactory connectionFactory = connectionFactoryLocator.getConnectionFactory(providerId);
        Connection connection = webSupport.completeConnection(connectionFactory, nativeWebRequest);
        String url = handleSignIn(connection, nativeWebRequest, config);

        redirect url: url
    }

    protected String handleSignIn(Connection connection, GrailsWebRequest request, config) {
        String result
        List<String> userIds = usersConnectionRepository.findUserIdsWithConnection(connection)
        if (userIds.size() == 0) {
            log.debug('No user found in the repository, creating a new one...')

            ProviderSignInAttempt signInAttempt = new ProviderSignInAttempt(connection, connectionFactoryLocator, usersConnectionRepository)
            request.setAttribute(ProviderSignInAttempt.SESSION_ATTRIBUTE, signInAttempt, RequestAttributes.SCOPE_SESSION)
            //TODO: Document this setting
            result = request.session.ss_oauth_redirect_on_signIn_attempt ?: config.page.handleSignIn
        } else if (userIds.size() == 1) {
            log.debug('User found in the repository...')

            usersConnectionRepository.createConnectionRepository(userIds.get(0)).updateConnection(connection)
            SignInAdapter signInAdapter = new SpringSocialSimpleSignInAdapter(requestCache)
            String originalUrl = signInAdapter.signIn(userIds.get(0), connection, request)
            log.debug("originalUrl: ${originalUrl}")
            //TODO: Document this setting
            result = originalUrl ?: config.postSignInUrl
        } else {
            log.error('Multiple Users found in the repository...')
            //TODO: handle redirect when multiple users found
            //result = redirect(URIBuilder.fromUri(signInUrl).queryParam("error", "multiple_users").build().toString());
        }
        
        return result
    }

    private ConfigObject getConfigByProviderId(String providerId) {
        grailsApplication.config.grails.plugins.springsocial?.get(providerId)
    }
}