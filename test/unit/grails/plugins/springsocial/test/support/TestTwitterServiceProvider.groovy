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
package grails.plugins.springsocial.test.support

import org.springframework.social.oauth1.*
import org.springframework.util.MultiValueMap

/**
 * Created with IntelliJ IDEA.
 * User: domix
 * Date: 02/05/13
 * Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
class TestTwitterServiceProvider implements OAuth1ServiceProvider<TestTwitterApi> {
  public static final authorizeUrl = 'http://a_auth_service_provider.com/oauth/authorize'

  OAuth1Operations getOAuthOperations() {
    return new OAuth1Operations() {

      OAuth1Version getVersion() {
        OAuth1Version.CORE_10_REVISION_A
      }

      OAuthToken fetchRequestToken(String callbackUrl, MultiValueMap<String, String> additionalParameters) {
        new OAuthToken('value', 'secret')
      }

      String buildAuthorizeUrl(String requestToken, OAuth1Parameters parameters) {
        "${authorizeUrl}?request_token=${requestToken}"
      }

      String buildAuthenticateUrl(String requestToken, OAuth1Parameters parameters) {
        buildAuthorizeUrl(requestToken, parameters)
      }

      OAuthToken exchangeForAccessToken(AuthorizedRequestToken authorizedRequestToken, MultiValueMap<String, String> stringStringMultiValueMap) {
        new OAuthToken('value', 'secret')
      }
    };
  }

  TestTwitterApi getApi(String accessToken, String secret) {
    return null
  }
}
