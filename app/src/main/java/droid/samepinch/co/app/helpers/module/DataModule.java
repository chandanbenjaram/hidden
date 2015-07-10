package droid.samepinch.co.app.helpers.module;

import org.springframework.web.client.RestTemplate;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import droid.samepinch.co.rest.ReqPosts;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Module
public class DataModule {
    public DataModule() {
    }

    @Provides ReqPosts provideReqPosts(){
        return new ReqPosts();
    }

    @Provides @Singleton RestTemplate provideRestTemplate(){
        return new RestTemplate();
    }
}
