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
import grails.test.mixin.TestFor

@TestFor(SpringSocialConnectController)
class SpringSocialConnectControllerSpec extends spock.lang.Specification {
  SpringSecurityService mock = Mock(SpringSecurityService)

  def setup() {
    controller.springSecurityService = mock

  }

  def cleanup() {
  }

  void "user redirected to default location because is not logged in"() {
    when:
    controller.connect()
    then:
    1 * mock.isLoggedIn() >> false
    controller.response.status == 302
    controller.response.header('Location').endsWith('/')
  }

  void "user redirected to specified location because is not logged in"() {
    def mockConfig = new ConfigObject()
    mockConfig.springsocial.loginUrl = "/loginuri"
    controller.grailsApplication.config = mockConfig
    when:
    controller.connect()
    then:
    1 * mock.isLoggedIn() >> false
    controller.response.status == 302
    controller.response.header('Location').endsWith('/loginuri')
  }
}