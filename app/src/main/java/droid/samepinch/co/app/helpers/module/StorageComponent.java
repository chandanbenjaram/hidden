package droid.samepinch.co.app.helpers.module;

import javax.inject.Singleton;

import dagger.Component;
import droid.samepinch.co.data.dto.User;
import droid.samepinch.co.rest.ReqPosts;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Component(modules = {DataModule.class})
@Singleton
public interface StorageComponent {
    ReqPosts provideReqPosts();
    User provideAnonymousDot();

}
