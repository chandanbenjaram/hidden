package droid.samepinch.co.rest;

import org.springframework.web.client.RestTemplate;

/**
 * Created by imaginationcoder on 7/16/15.
 */
public enum RestClient {
    INSTANCE;
    private final RestTemplate restTemplate;
    private RestClient(){
        restTemplate = new RestTemplate();
    }

    public RestTemplate handle() {
        return restTemplate;
    }
}
