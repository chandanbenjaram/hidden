package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.module.DaggerStorageComponent;
import co.samepinch.android.app.helpers.module.StorageComponent;
import co.samepinch.android.rest.ReqSetBody;
import co.samepinch.android.rest.Resp;
import co.samepinch.android.rest.RestClient;

public class ChooseHandleFragment extends android.support.v4.app.Fragment {
    public static final String TAG = "ChooseHandleFragment";

    @Bind(R.id.view_avatar)
    SimpleDraweeView mAvatar;

    @Bind(R.id.input_pinchHandle)
    EditText mInputPinchHandle;

    @Bind(R.id.status)
    TextView status;

    @Bind(R.id.btn_clear)
    Button mClearButton;

    @Bind(R.id.btn_done)
    Button mDoneButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_handle, container, false);
        ButterKnife.bind(this, view);

        Bundle iArgs = getArguments();
        if(iArgs !=null){
            if(StringUtils.isNotBlank(iArgs.getString("image", ""))) {
                Uri imgUri = Uri.parse(iArgs.getString("image"));
                mAvatar.setImageURI(imgUri);
            }
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.btn_clear)
    public void onCancel() {
        mInputPinchHandle.setText("");
    }

    @OnClick(R.id.btn_done)
    public void onDoneClick() {
        String pinchHandle = mInputPinchHandle.getText().toString();
        if (StringUtils.isBlank(pinchHandle)) {
            return;
        } else {
            new ValidateHandleTask().execute(pinchHandle);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private class ValidateHandleTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... args) {
            StorageComponent component = DaggerStorageComponent.create();
            ReqSetBody req = component.provideReqSetBody();
            // set base args
            req.setToken(Utils.getNonBlankAppToken());
            req.setCmd("validatePinchHandle");

            Map<String, String> body = new HashMap<>();
            body.put("pinch_handle", args[0]);
            req.setBody(body);

            //headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(RestClient.INSTANCE.jsonMediaType());
            try {
                HttpEntity<ReqSetBody> payloadEntity = new HttpEntity<>(req, headers);
                ResponseEntity<Resp> resp = RestClient.INSTANCE.handle().exchange(AppConstants.API.USERS.getValue(), HttpMethod.POST, payloadEntity, Resp.class);
                if (resp != null) {
                    return resp.getBody().getStatus() == 200;
                }
            } catch (Exception e) {
                // muted
                Resp resp = Utils.parseAsRespSilently(e);
                if (resp != null && resp.getStatus() == 400) {
                    return Boolean.FALSE;
                } else {
                    Log.e(TAG, "err validating...");
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result != null) {
                if (result.booleanValue()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("PINCH_HANDLE", mInputPinchHandle.getText().toString());
                    getActivity().setResult(Activity.RESULT_OK, resultIntent);
                    getActivity().finish();
                } else {
                    mInputPinchHandle.setError("already taken. choose another one.");
                }
                return;
            }

            status.setText("try again...");
        }
    }
}
