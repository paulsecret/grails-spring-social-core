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
import grails.plugins.springsocial.test.support.TestTwitterConnectionFactory
import grails.plugins.springsocial.test.support.TestTwitterServiceProvider
import grails.test.mixin.TestFor
import org.springframework.social.connect.ConnectionFactory
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.ConnectionKey
import org.springframework.social.connect.ConnectionRepository

import static org.springframework.http.HttpStatus.MOVED_TEMPORARILY

@TestFor(SpringSocialConnectController)
class SpringSocialConnectControllerSpec extends spock.lang.Specification {
  SpringSecurityService mockSpringSecurityService = Mock()
  ConnectionFactoryLocator mockConnectionFactoryLocator = Mock()
  ConnectionRepository mockConnectionRepository = Mock()

  def setup() {
    controller.springSecurityService = mockSpringSecurityService
    controller.grailsApplication.config = new ConfigObject()
    controller.connectionFactoryLocator = mockConnectionFactoryLocator
    controller.connectionRepository = mockConnectionRepository
  }

  void "user redirected to default location because is not logged in"() {
    when:
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> false
      controller.response.status == MOVED_TEMPORARILY.value()
      isLoginHome()
  }

  void "user redirected to specified location because is not logged in"() {
    given:
      def mockConfig = new ConfigObject()
      String loginUri = "/loginuri"
      mockConfig.springsocial.loginUrl = loginUri
      controller.grailsApplication.config = mockConfig
    when:
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> false
      controller.response.status == MOVED_TEMPORARILY.value()
      controller.response.header('Location').endsWith(loginUri)
  }

  void "Exception when trying to connect and the providerId is missing"() {
    given:
      controller.connectionFactoryLocator = mockConnectionFactoryLocator
    when:
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> true
      IllegalArgumentException ex = thrown()
      ex.message == 'The providerId is required'
  }

  void "redirected to OAuth Service provider when all settings are right"() {
    given:
      String providerId = 'twitter2'
      ConnectionFactory mockConnectionFactory = new TestTwitterConnectionFactory()
      controller.connectionFactoryLocator = mockConnectionFactoryLocator
    when:
      controller.params.providerId = providerId
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> true
      1 * mockConnectionFactoryLocator.getConnectionFactory(providerId) >> mockConnectionFactory
      controller.response.status == MOVED_TEMPORARILY.value()
      controller.response.header('Location').startsWith(TestTwitterServiceProvider.authorizeUrl)
  }

  void "oauth callback without providerID"() {
    when:
      controller.oauthCallback()
    then:
      IllegalArgumentException ex = thrown()
      ex.message == 'The providerId is required'
  }

  void "oauth callback with a valid providerID and the config is missing for that provider"() {
    when:
      controller.params.providerId = 'twitter'
      controller.oauthCallback()
    then:
      controller.response.status == MOVED_TEMPORARILY.value()
      isLoginHome()
  }

  void "oauth callback sucessfully"() {
    given:
      String providerId = 'twitter'
      def mockConfig = new ConfigObject()
      mockConfig.springsocial.twitter.foo = 'foo'
      controller.grailsApplication.config = mockConfig
    when:
      controller.params.providerId = providerId
      controller.oauthCallback()
    then:
      1 * mockConnectionFactoryLocator.getConnectionFactory(providerId) >> new TestTwitterConnectionFactory()
      controller.response.status == MOVED_TEMPORARILY.value()
      isLoginHome()
  }

  void "oauth callback and provider or user denied access"() {
    given:
      String providerId = 'twitter'
      def mockConfig = new ConfigObject()
      mockConfig.springsocial.twitter.foo = 'foo'
      controller.grailsApplication.config = mockConfig
      controller.params.denied = 'true'
      controller.params.providerId = providerId
    when:
      controller.oauthCallback()
    then:
      controller.response.status == MOVED_TEMPORARILY.value()
      isLoginHome()
  }

  void "trying to disconnect with no providerId"() {
    when:
      controller.disconnect()
    then:
      IllegalArgumentException exception = thrown()
      exception.message == 'The providerId is required'
  }

  void "trying to disconnect with providerId"() {
    given:
      String providerId = 'twitter'
      controller.params.providerId = providerId
    when:
      controller.disconnect()
    then:
      1 * mockConnectionRepository.removeConnections(providerId)
      isLoginHome()
  }

  void "trying to disconnect with providerId and providerUserId"() {
    given:
      String providerId = 'twitter'
      String providerUserId = 'someuser'
      controller.params.providerId = providerId
      controller.params.providerUserId = providerUserId
    when:
      controller.disconnect()
    then:
      1 * mockConnectionRepository.removeConnection(new ConnectionKey(providerId, providerUserId))
      isLoginHome()
  }

  private boolean isLoginHome() {
    controller.response.header('Location') == 'http://localhost:8080/'
  }
}