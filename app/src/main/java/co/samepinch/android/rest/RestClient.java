package co.samepinch.android.rest;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by imaginationcoder on 7/16/15.
 */
public enum RestClient {
    INSTANCE;
    private final RestTemplate restTemplate;

    private RestClient() {
        HttpComponentsClientHttpRequestFactory reqFac = new HttpComponentsClientHttpRequestFactory();
        reqFac.setReadTimeout(60000);
        reqFac.setConnectTimeout(300000);
        restTemplate = new RestTemplate(reqFac);
    }

    public RestTemplate handle() {
        return restTemplate;
    }
}
