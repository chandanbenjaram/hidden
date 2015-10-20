package co.samepinch.android.rest;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Created by imaginationcoder on 7/16/15.
 */
public enum RestClient {
    INSTANCE;
    private final RestTemplate restTemplate;

    private static final List<MediaType> LIST_MEDIA_TYPE_JSON = Arrays.asList(MediaType.APPLICATION_JSON);

    RestClient() {
        SimpleClientHttpRequestFactory reqFac = new SimpleClientHttpRequestFactory();
        reqFac.setReadTimeout(60000);
        reqFac.setConnectTimeout(300000);

//        template.setRequestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory());
        restTemplate = new RestTemplate(reqFac);
    }

    public RestTemplate handle() {
        return restTemplate;
    }

    public List<MediaType> jsonMediaType() {
        return LIST_MEDIA_TYPE_JSON;
    }
}
