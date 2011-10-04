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
package grails.plugins.springsocial.config.core

import javax.inject.Inject
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.social.connect.ConnectionFactory
import org.springframework.social.connect.ConnectionFactoryLocator
import org.springframework.social.connect.support.ConnectionFactoryRegistry

@Configuration
class ConnectionFactoryConfigurer {
  @Inject
  ConnectionFactoryLocator connectionFactoryLocator

  @Bean
  BeanDefinitionRegistryPostProcessor processConecctionF() {
    new BeanDefinitionRegistryPostProcessor() {
      void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) {}

      void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        //TODO: Document the automatic ConnectionFactory registration
        def connectionFactories = beanFactory.getBeansOfType(ConnectionFactory)
        connectionFactories.each {conectionFactory ->
          if (connectionFactoryLocator) {
            println "adding to the registry: " + conectionFactory.dump()
            ((ConnectionFactoryRegistry) connectionFactoryLocator).addConnectionFactory(conectionFactory)
          }

        }
      }

    }
  }
}
