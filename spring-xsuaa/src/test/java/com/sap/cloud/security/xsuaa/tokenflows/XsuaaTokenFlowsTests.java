package com.sap.cloud.security.xsuaa.tokenflows;

import static org.junit.Assert.*;

import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

public class XsuaaTokenFlowsTests {

    Jwt mockJwt = buildMockJwt(Arrays.asList("read", "write"));
    
    
    @Test
    public void test_constructor() {
        new XsuaaTokenFlows(new RestTemplate(), new TokenDecoderMock(mockJwt));
    }

    @Test
    public void test_startRefreshTokenFlow() {
        XsuaaTokenFlows xsuaaTokenFlows = new XsuaaTokenFlows(new RestTemplate(), new TokenDecoderMock(mockJwt));
        RefreshTokenFlow flow = xsuaaTokenFlows.refreshTokenFlow(URI.create("http://base/"));
        assertNotNull("RefreshTokenFlow must not be null.", flow);
        
        flow = xsuaaTokenFlows.refreshTokenFlow(URI.create("http://token/"), URI.create("http://authz/"), URI.create("http://token_keys/"));
        assertNotNull("RefreshTokenFlow must not be null.", flow);
    }
    
    @Test
    public void test_startUserTokenFlow() {
        XsuaaTokenFlows xsuaaTokenFlows = new XsuaaTokenFlows(new RestTemplate(), new TokenDecoderMock(mockJwt));
        UserTokenFlow flow = xsuaaTokenFlows.userTokenFlow(URI.create("http://base/"));
        assertNotNull("UserTokenFlow must not be null.", flow);
        
        flow = xsuaaTokenFlows.userTokenFlow(URI.create("http://token/"), URI.create("http://authz/"), URI.create("http://token_keys/"));
        assertNotNull("UserTokenFlow must not be null.", flow);
    }
    
    @Test
    public void test_startClientCredentialsFlow() {
        XsuaaTokenFlows xsuaaTokenFlows = new XsuaaTokenFlows(new RestTemplate(), new TokenDecoderMock(mockJwt));
        ClientCredentialsTokenFlow flow = xsuaaTokenFlows.clientCredentialsTokenFlow(URI.create("http://base/"));
        assertNotNull("ClientCredentialsTokenFlow must not be null.", flow);
        
        flow = xsuaaTokenFlows.clientCredentialsTokenFlow(URI.create("http://token/"), URI.create("http://authz/"), URI.create("http://token_keys/"));
        assertNotNull("ClientCredentialsTokenFlow must not be null.", flow);
    }
    
    
    private Jwt buildMockJwt(List<String> scopes) {
        Map<String, Object> jwtHeaders = new HashMap<String, Object>();
        jwtHeaders.put("dummyHeader", "dummyHeaderValue");
        
        Map<String, Object> jwtClaims = new HashMap<String, Object>();
        jwtClaims.put("dummyClaim", "dummyClaimValue");
        jwtClaims.put("scope", scopes);
        
        return new Jwt("mockJwtValue", Instant.now(), Instant.now().plusMillis(100000), jwtHeaders, jwtClaims);
    }
}
