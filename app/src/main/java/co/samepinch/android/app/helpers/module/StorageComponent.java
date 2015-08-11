package co.samepinch.android.app.helpers.module;

import javax.inject.Singleton;

import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqLogin;
import co.samepinch.android.rest.ReqPosts;
import co.samepinch.android.rest.ReqSetBody;
import dagger.Component;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Component(modules = {DataModule.class})
@Singleton
public interface StorageComponent {
    ReqPosts provideReqPosts();
    User provideAnonymousDot();
    ReqLogin provideReqLogin();
    ReqSetBody provideReqSetBody();
}
