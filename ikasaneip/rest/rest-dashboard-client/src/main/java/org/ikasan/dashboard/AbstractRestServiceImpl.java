package org.ikasan.dashboard;

import org.ikasan.dashboard.model.JwtRequest;
import org.ikasan.dashboard.model.JwtResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractRestServiceImpl {
    Logger logger = LoggerFactory.getLogger(AbstractRestServiceImpl.class);

    protected RestTemplate restTemplate;

    protected String username;

    protected String password;

    protected String token;

    protected String authenticateUrl;

    protected String url;

    protected boolean authenticate(String userAgent)
    {
        HttpEntity<JwtRequest> entity = new HttpEntity(new JwtRequest(username, password), createHttpHeaders(userAgent));
        try
        {
            if ( username != null && password != null )
            {
                ResponseEntity<JwtResponse> response = restTemplate
                    .exchange(authenticateUrl, HttpMethod.POST, entity, JwtResponse.class);
                this.token = response.getBody().getToken();
                return true;
            }
            return false;
        }
        catch (HttpClientErrorException e)
        {
            logger.warn("Issue while authenticating to dashboard [" + authenticateUrl + "] with response [{" + e
                .getResponseBodyAsString() + "}]", e);
            return false;
        }
    }

    protected HttpHeaders createHttpHeaders(String userAgent)
    {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // todo sort out USER_AGENT
        headers.add(HttpHeaders.USER_AGENT, userAgent);
        if ( token != null )
        {
            headers.add("Authorization", "Bearer " + token);
        }
        return headers;
    }
}
