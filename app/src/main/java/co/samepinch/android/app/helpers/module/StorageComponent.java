package co.samepinch.android.app.helpers.module;

import javax.inject.Singleton;

import co.samepinch.android.rest.ReqLogin;
import dagger.Component;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqPosts;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Component(modules = {DataModule.class})
@Singleton
public interface StorageComponent {
    ReqPosts provideReqPosts();
    User provideAnonymousDot();
    ReqLogin provideReqLogin();

}
