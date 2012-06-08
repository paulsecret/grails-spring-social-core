
n.n.n / 2012-06-08 
==================

  * when running on Grails < 2.* you need to provide explicitely the spring-security-core plugin in your app
  * renamed the plugin, again
  * When the app is running on Grails 1.3.4, prevent load the transitive plugins
  * renamed to prevent failing on Grails 1.3.4
  * removed jackson-mapper-asl dependency
  * some fucking fixes
  * formatted out the code, fixed the copyright year
  * added some plugins
  * added to ignore list
  * upgraded spring-security-core
  * upgraded Spring Social & Spring Security Crypto
  * added more directories to ignore list
  * upgraded to Grails 2.0.3, added some files to ignore list, updated copyright info
  * Update SpringSocialCoreGrailsPlugin.groovy
  * upgraded to Spring Social Core 1.0.1
  * setting version to 0.1.31
  * removed println's, added logs instead.
  * Merge branch 'master' of github.com:synergyj/grails-spring-social-core
  * initial load
  * improved current user validation
  * added convenient taglib
  * added SpringSocialCoreService
  * added validation on providerId
  * setting to mem the database config
  * upgraded to Srping Security Core 1.2.4
  * CSS improve and fix spring-security version
  * fixes on duplicate connection
  * setting version to 0.1.30
  * Merge pull request #3 from SpOOnman/master
  * Provide redirect after sign in.
  * Use named mappings in callbackUrl for both connect and signin controllers.
  * fixed 'No thread-bound request found' when building the appCtx, because we arent creating proxies
  * improvements to use correctly latest SpringSocial
  * improved the UrlMappings
  * prepare 0.1.27 release
  * added valition if user isLoggedIn, we only permit connect on authenticated users
  * deleted unused code, polish some code
  * polish code
  * improvements to handle signin
  * prepare 0.1.26 release
  * prepare 0.1.25 release
  * prepare 0.1.24 release
  * prepare 0.1.22 release
  * prepare 0.1.21 release
  * prepare 0.1.20 release
  * prepare 0.1.19 release
  * prepare 0.1.18 release
  * prepare 0.1.17 release
  * fixed bug, make the iteration on a map instead of list
  * prepare 0.1.16 release
  * added files to ignore list
  * deleted unused files
  * New achievement Author (Level 6)
  * removed the explicit cast
  * prepare the 0.1.15 release
  * refactor to find the ConnectionFactoryLpocator inside the BeanPostProcessor
  * preparing 0.1.14 release
  * refactor for prevent: groovy.lang.MissingFieldException: No such field: connectionFactoryLocator
  * preparing the 0.1.13 release
  * added automated registration for ConnectionFactories in the Registry
  * setting version to 0.1.12
  * setting version to 0.1.11
  * removed unused log
  * added a ConnectionFactoryConfigurer
  * more code polish
  * minor code polish
  * polishing plugin descriptor
  * deleted unused diles
  * preparing 0.1.10 release
  * preparing 0.1.9 release
  * preparing 0.1.9 release
  * added dependency on spriung.security-core 1.2.1
  * setting version to 0.1.8
  * removed dependency, baceuse its specified in dependsOn
  * improved the depoendOn
  * deleted unused plugins
  * refactored some code
  * added more docs
  * deleted useless docs
  * added setting for documentation
  * added initial docs
  * deleted TODO tag
  * New achievement Garage Inventor
  * setting version to 0.1.7 and removed dependsOn SpringSecurityCore
  * upgraded to Spring-Security-Core 1.2.1
  * setting version to 0.1.6
  * added support when the user denied access to the profile, added old config stuff
  * added dependecies and load after SpringSecurityCore
  * removed plugin-config plugin
  * loaded after pluginConfig plugin
  * added more properties for pom generation
  * polish code
  * added support for disconnect a user from specific provider
  * adding optional parameter for userProviderId
  * polish code
  * removed dependency on SpringSocialUtils
  * load plugin after SpringSecurityCore
  * polish code
  * polish code
  * adding the default config
  * adding the plugin-config plugin
  * setting version to 0.1.5, added depends on plugins required
  * updated SpringSocial to 1.0.0.RELEASE and updating according libraries
  * seting versio to 0.1.4, added some resources to ignore in plugin config
  * Fix CSS
  * polish code
  * fixed error in OAuth2
  * updated jackson-mapper to 1.8.4
  * polish code
  * optimized imports
  * removed unused code
  * added IntelliJ IDEA to ignore list
  * adding support fot OAuth2
  * added a empty space :)
  * setting version to 0.1.3
  * added support for OAuth2ConnectionFactory
  * fixed scope plugin, setting version to 0.1.2
  * setting version to 0.1.1, removed component scan for twitter package
  * removed unused tests
  * added to ignore list Eclipse files
  * added to ignore list db files
  * added license file
  * fixed disconnect flow
  * added default param to redirect when the user disconnects
  * used a page property to render the left menu
  * moved all twitter stuff related to own plugin
  * removed twiiter and facebook related config
  * removed println
  * preparing to split the plugin
  * preparing to split the plugin
  * preparing to split the plugin
  * preparing to split the plugin
  * split the config
  * fixed package name
  * moved to a better package
  * renamed
  * renamed
  * ptimized code to handle the outh callback from providers
  * fixed config
  * fixed package name
  * moved to a better package
  * renamed
  * renamed
  * removed unnecesary config file
  * added support for setting in session the uri to redirect on callback, useful for creating dynamic redirects, polish code
  * code polish
  * upgraded to SpringSocial 1.0.0.RC2, fixed flows to handle twitter connections
  * fixed typo in variable name, fixed NPE
  * updated library
  * upgraded to Spring Social 1.0.0.RC1 and Spring Security Core Grails plugin to 1.2
  * used the new ConnectSupport class (introduced in SpringSocial 1.0.0.RC1) to create the urls. Vhanged the controller name to connecto to Social Services
  * adapting config from Spring Social 1.0.0..RC1
  * upgraded to Spring Social 1.0.0.RC1
  * added siginService
  * fixed the uri generation
  * added siginService
  * added initial facebook support
  * added support for signin
  * added spring security plugin as required dependency
  * initial support for facebook
  * commented out 'Post Tweet feature'
  * fixed typos
  * handle check for twitter config
  * adding search capabilities
  * added support for update status
  * fixed template location
  * fixed new template locations
  * moved files
  * renamed directory
  * deleted unnecessary files
  * added very basic support for Trending Topics
  * basic support for Twitter Anywhere
  * fixed controller name
  * renamed directory
  * renamed directory
  * Added default layout and styles
  * Added support for default views with twitter
  * Adding basic support for twitter
  * optimized imports and excluding some artifacts
  * deleted unnecessary files
  * deleted unnecessary files
  * added configuration defaults and support for custom postURIs
  * cleanup
  * deleted unnecesary files
  * renamed to be included in plugin distribution
  * general cleanup
  * fixed url mappings
  * refactor package name
  * added default config
  * Added support for override configuration in application
  * adding to ignore lists all zip files and target directory
  * adding the domain class to store the user conenctgions, added the templete to twitter connections, adding the apis config definition
  * added license header
  * initial commit
  * first commit
