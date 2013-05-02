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

  void "user is not logged in and trying to connect"() {
    when:
    controller.connect()
    then:
    1 * mock.isLoggedIn() >> false
    assert controller.actionUri != null
    println controller.actionUri
  }
}