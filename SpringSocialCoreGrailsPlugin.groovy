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

import grails.plugins.springsocial.SpringSecuritySigninService

class SpringSocialCoreGrailsPlugin {
  // the plugin version
  String version = "0.1.16"
  String grailsVersion = "1.3.0 > *"
  Map dependsOn = ['springSecurityCore': '1.2.1 > *']

  List pluginExcludes = [
      "grails-app/views/error.gsp",
      "grails-app/conf/Config.groovy",
      "grails-app/conf/DataSource.groovy",
      "grails-app/i18n/**"
  ]

  String author = "Domingo Suarez Torres"
  String authorEmail = "domingo.suarez@gmail.com"
  String title = "Spring Social Core"
  String description = "Spring Social Core plugin."

  String documentation = "http://grails.org/plugin/spring-social-core"

  String license = "APACHE"
  String organization = [name: "SynergyJ", url: "http://synergyj.com/"]
  String developers = [[name: "Domingo Suarez Torres", email: "domingo.suarez@gmail.com"], [name: "Jose Juan Reyes Zuniga", email: "neodevelop@gmail.com"]]
  String scm = [url: "https://github.com/synergyj/grails-spring-social-core"]
  String issueManagement = [system: "GITHUB", url: "https://github.com/synergyj/grails-spring-social-core/issues"]

  def doWithSpring = {
    xmlns context: "http://www.springframework.org/schema/context"
    context.'component-scan'('base-package': "grails.plugins.springsocial.config.core")

    signInService(SpringSecuritySigninService)
  }

}
