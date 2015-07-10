package droid.samepinch.co.app.helpers.module;

import org.springframework.web.client.RestTemplate;

import javax.inject.Singleton;

import dagger.Component;
import droid.samepinch.co.rest.ReqPosts;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Component(modules = {DataModule.class})
@Singleton
public interface StorageComponent {
    ReqPosts provideReqPosts();
    RestTemplate provideRestTemplate();

}
