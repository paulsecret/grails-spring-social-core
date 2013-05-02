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

import static org.springframework.http.HttpStatus.MOVED_TEMPORARILY

@TestFor(SpringSocialConnectController)
class SpringSocialConnectControllerSpec extends spock.lang.Specification {
  SpringSecurityService mockSpringSecurityService = Mock(SpringSecurityService)
  ConnectionFactoryLocator mockConnectionFactoryLocator = Mock(ConnectionFactoryLocator)

  def setup() {
    controller.springSecurityService = mockSpringSecurityService
  }

  void "user redirected to default location because is not logged in"() {
    when:
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> false
      controller.response.status == MOVED_TEMPORARILY.value()
      controller.response.header('Location').endsWith('/')
  }

  void "user redirected to specified location because is not logged in"() {
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
    controller.connectionFactoryLocator = mockConnectionFactoryLocator

    when:
      controller.connect()
    then:
      1 * mockSpringSecurityService.isLoggedIn() >> true
      IllegalArgumentException ex = thrown()
      ex.message == 'The providerId is required'
  }

  void "redirected to OAuth Service provider when all settings are right"() {
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
      controller.response.header('Location') == TestTwitterServiceProvider.authorizeUrl
  }
}