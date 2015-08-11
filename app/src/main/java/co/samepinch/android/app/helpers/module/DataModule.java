package co.samepinch.android.app.helpers.module;

import org.apache.commons.lang3.StringUtils;

import javax.inject.Singleton;

import co.samepinch.android.app.helpers.AppConstants;
import co.samepinch.android.data.dto.User;
import co.samepinch.android.rest.ReqLogin;
import co.samepinch.android.rest.ReqPosts;
import co.samepinch.android.rest.ReqSetBody;
import dagger.Module;
import dagger.Provides;

/**
 * Created by cbenjaram on 7/9/15.
 */
@Module
public class DataModule {
    public DataModule() {
    }

    @Provides
    ReqPosts provideReqPosts() {
        return new ReqPosts();
    }

    @Provides
    @Singleton
    User provideAnonymousDot() {
        String anonymousStr = "anonymous";
        User anonymous = new User();
        anonymous.setUid(String.valueOf(0));
        anonymous.setFname(anonymousStr);
        anonymous.setLname(StringUtils.EMPTY);
        anonymous.setPrefName(anonymousStr);
        anonymous.setPinchHandle(anonymousStr);
        return anonymous;
    }

    @Provides
    ReqLogin provideReqLogin() {
        ReqLogin instance = new ReqLogin();
        instance.setPlatform(AppConstants.KV.PLATFORM.getValue());
        instance.setDeviceToken(AppConstants.KV.CLIENT_SECRET.getValue());

        return instance;
    }

    @Provides
    ReqSetBody provideReqSetBody() {
        ReqSetBody instance = new ReqSetBody();
        return instance;
    }

}
