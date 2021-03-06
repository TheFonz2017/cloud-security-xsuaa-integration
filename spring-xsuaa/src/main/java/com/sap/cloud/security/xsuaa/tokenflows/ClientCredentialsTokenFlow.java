package com.sap.cloud.security.xsuaa.tokenflows;

import static com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlowsUtils.addAcceptHeader;
import static com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlowsUtils.addBasicAuthHeader;
import static com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlowsUtils.buildAuthorities;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A client credentials flow builder class.
 * Applications retrieve an instance of this builder from 
 * {@link XsuaaTokenFlows} and then create the flow request
 * using a builder pattern.
 */
public class ClientCredentialsTokenFlow {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String GRANT_TYPE = "grant_type";
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private static final String AUTHORITIES = "authorities";

    private RestTemplate restTemplate;
    private XsuaaTokenFlowRequest request;
    private VariableKeySetUriTokenDecoder tokenDecoder;

    /**
     * Creates a new instance. 
     * @param restTemplate - the {@link RestTemplate} used to execute the final request.
     * @param xsuaaBaseUri - the base URI of XSUAA. Based on the base URI the tokenEndpoint, authorize and key set URI will be derived.
     */
    ClientCredentialsTokenFlow(RestTemplate restTemplate, VariableKeySetUriTokenDecoder tokenDecoder, URI xsuaaBaseUri) {
        Assert.notNull(restTemplate, "RestTemplate must not be null.");
        Assert.notNull(xsuaaBaseUri, "XSUAA base URI must not be null.");
        Assert.notNull(tokenDecoder, "TokenDecoder must not be null.");

        this.restTemplate = restTemplate;
        this.tokenDecoder = tokenDecoder;
        
        URI tokenEndpoint     = UriComponentsBuilder.fromUri(xsuaaBaseUri).path("/oauth/token").build().toUri();
        URI authorizeEndpoint = UriComponentsBuilder.fromUri(xsuaaBaseUri).path("/oauth/authorize").build().toUri();
        URI keySetEndpoint    = UriComponentsBuilder.fromUri(xsuaaBaseUri).path("/token_keys").build().toUri();
        
        this.request = new XsuaaTokenFlowRequest(tokenEndpoint, authorizeEndpoint, keySetEndpoint);
    }
    
    /**
     * Creates a new instance.
     * @param restTemplate      - the {@link RestTemplate} used to execute the final request.
     * @param tokenEndpoint     - the token endpoint URI.
     * @param authorizeEndpoint - the authorize endpoint URI. 
     * @param keySetEndpoint    - the key set endpoint URI. 
     */
    ClientCredentialsTokenFlow(RestTemplate restTemplate, VariableKeySetUriTokenDecoder tokenDecoder, URI tokenEndpoint, URI authorizeEndpoint, URI keySetEndpoint) {
        Assert.notNull(restTemplate, "RestTemplate must not be null.");
        Assert.notNull(tokenDecoder, "TokenDecoder must not be null.");
        Assert.notNull(tokenEndpoint, "Token endpoint URI must not be null.");
        Assert.notNull(authorizeEndpoint, "Authorize endpoint URI must not be null.");
        Assert.notNull(keySetEndpoint, "Key set endpoint URI must not be null.");

        this.restTemplate = restTemplate;
        this.tokenDecoder = tokenDecoder;
        this.request = new XsuaaTokenFlowRequest(tokenEndpoint, authorizeEndpoint, keySetEndpoint);
    }

    /**
     * Adds the OAuth 2.0 client ID to the request.<br>
     * The ID needs to be that of the OAuth client that 
     * requests the token.
     * @param clientId - the ID of the OAuth 2.0 client requesting the token.
     * @return this builder.
     */
    public ClientCredentialsTokenFlow client(String clientId) {
        request.setClientId(clientId);
        return this;
    }

    /**
     * Adds the OAuth 2.0 client's secret to this request.<br>
     * The secret needs to be the one of the client that requests the token.
     * @param clientSecret - the secret of the OAuth 2.0 client requesting the token.  
     * @return this builder.
     */
    public ClientCredentialsTokenFlow secret(String clientSecret) {
        request.setClientSecret(clientSecret);
        return this;
    }

    /**
     * Adds additional authorization attributes to the request. <br>
     * Clients can use this to request additional attributes in the 
     * {@code 'az_attr'} claim of the returned token.
     * @param additionalAuthorizationAttributes - the additional attributes.
     * @return this builder.
     */
    public ClientCredentialsTokenFlow attributes(Map<String, String> additionalAuthorizationAttributes) {
        this.request.setAdditionalAuthorizationAttributes(additionalAuthorizationAttributes);
        return this;
    }

    /**
     * Executes the token flow and returns a JWT token from XSUAA.
     * @return the JWT token generated by XSUAA. 
     * @throws TokenFlowException in case of token flow errors.
     */
    public Jwt execute() throws TokenFlowException {

        checkRequest(request);

        return requestTechnicalUserToken(request);
    }

    /**
     * Checks if the built request is valid. Throws an exception if not all mandatory fields are filled.
     * @param request - the token flow request.
     * @throws TokenFlowException in case the request does not have all mandatory fields set.
     */
    private void checkRequest(XsuaaTokenFlowRequest request) throws TokenFlowException {
        if (!request.isValid()) {
            throw new TokenFlowException("Client credentials flow request is not valid. Make sure all mandatory fields are set.");
        }
    }

    /**
     * Requests the client credentials token from XSUAA.
     * @param request - the token request.
     * @return the JWT token returned by XSUAA.
     * @throws TokenFlowException in case of an error during the flow.
     */
    private Jwt requestTechnicalUserToken(XsuaaTokenFlowRequest request) throws TokenFlowException {

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(request.getTokenEndpoint());

        // add grant type to URI
        builder.queryParam(GRANT_TYPE, CLIENT_CREDENTIALS);

        String authorities = buildAuthorities(request); // returns JSON!
        if (authorities != null) {
            builder.queryParam(AUTHORITIES, authorities); // places JSON inside the URI !?!
        }

        HttpHeaders headers = createHeadersForTechnicalUserTokenExchange(request);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        URI requestUri = builder.build().encode().toUri();

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity(requestUri, requestEntity, Map.class);

        HttpStatus responseStatusCode = responseEntity.getStatusCode();

        if (responseStatusCode == HttpStatus.UNAUTHORIZED) {
            throw new TokenFlowException(String.format("Error retrieving JWT token. Received status code %s. Call to XSUAA was not successful (grant_type: client_credentials). Client credentials invalid.", responseStatusCode));
        }

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new TokenFlowException(String.format("Error retrieving JWT token. Received status code %s. Call to XSUAA was not successful (grant_type: client_credentials).", responseStatusCode));
        }

        String encodedJwtTokenValue = responseEntity.getBody().get(ACCESS_TOKEN).toString();
                
        return decode(encodedJwtTokenValue, request.getKeySetEndpoint());
    }

    /**
     * Creates a set of headers required for the token exchange with XSUAA.
     * @param request - the token flow request. 
     * @return the set of headers.
     */
    private HttpHeaders createHeadersForTechnicalUserTokenExchange(XsuaaTokenFlowRequest request) {
        HttpHeaders headers = new HttpHeaders();
        addAcceptHeader(headers);
        addBasicAuthHeader(headers, request.getClientId(), request.getClientSecret());
        return headers;
    }
    
    /**
     * Decodes the returned JWT value.
     * @param encodedToken - the encoded JWT token value.  
     * @return the decoded JWT. 
     * @throws TokenFlowException in case of an exception decoding the token.
     */
    private Jwt decode(String encodedToken, URI keySetEndpoint) throws TokenFlowException {

        tokenDecoder.setJwksURI(keySetEndpoint);
        // validation is not required by the one who retrieves the token,
        // but by the one who receives it (e.g. the service it is sent to).
        // Hence, here we only decode, but do not validate.
        // decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(tokenValidators));
        Jwt jwt = tokenDecoder.decode(encodedToken); 
        return jwt;
    }
}
