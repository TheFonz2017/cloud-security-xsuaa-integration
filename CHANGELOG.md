# Change Log 

All notable changes to this project will be documented in this file.

## 2.0.0
* Entirely overhauled and simplified API.
* Support for token flows of Client Credentials, Authorization Code, Refresh Token and User Token Grant.
* Token Flow creation using builder pattern.
* Auto-Configuration support that automatically adds 
   * additional token validators 
   * `XsuaaServiceBindings`  as `@Autowired` bean.
   * `XsuaaTokenFlows`  as `@Autowired` bean.
* XsuaaToken now inherits from standard `Jwt` token and can be used both as a wrapper or replacement for `Jwt`.
* Added `DefaultSpringSecurityAuthoritiesExtractor` and `XsAppNameReplacingAuthoritiesExtractor` to deal more gracefully with XsAppName prefixes in token scopes.
* Better integration with Spring / Spring Boot
* Unit tested token flows
* Test Coverage over 95%

## 1.4.0
* API method to query [token validity](https://github.com/SAP/cloud-security-xsuaa-integration/blob/master/spring-xsuaa/src/main/java/com/sap/cloud/security/xsuaa/token/Token.java#L167)
* Bugfix in basic authentication support: allow  usage of JWT token or basic authentication with one configuration
* Allows overwrite / enhancement of XSUAA jwt token validators
* Allow applications to initialize of Spring SecurityContext for non HTTP requests. As documented [here](https://github.com/SAP/cloud-security-xsuaa-integration/blob/master/spring-xsuaa/README.md)

## 1.3.1
* Broker plan validation failed due to incorrect audience validation
## 1.3.0
* JwtGenerator offers enhancement options: custom claims and audience
* Test framework support for multi tenancy

## 1.2.0
* Eases enhancement of TokenAuthenticationConverter ([issue 23](https://github.com/SAP/cloud-security-xsuaa-integration/issues/23))
* Makes XsuaaAudienceValidator more robust ([issue 21](https://github.com/SAP/cloud-security-xsuaa-integration/issues/21))
* XSTokenRequest accepts custom RestTemplate ([issue 25](https://github.com/SAP/cloud-security-xsuaa-integration/issues/25))
* Provides spring-xsuaa-test library with JWTGenerator ([issue 29](https://github.com/SAP/cloud-security-xsuaa-integration/issues/29))
* Provides spring-xsuaa-mock library with XSUAA authentication mock web server for offline token key validation ([issue 30](https://github.com/SAP/cloud-security-xsuaa-integration/issues/30))


## 1.1.0

* Spring-Security 5 integration libraries. Added AudienceValidator
* Spring-Security 5 Support for basic authentication

## 1.1.0.RC1

* Initial version including spring-security 5 integration libraries


## 1.0.0

* Initial version of the api for SAP Java Buildpack

