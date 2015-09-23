package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.R;

public class WebViewFragment extends Fragment {
    public static final String TAG = "WebViewFragment";
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.webview)
    WebView mWebView;

    private LocalHandler mHandler;

    private String mUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new LocalHandler(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.webview, container, false);
        ButterKnife.bind(this, view);

        mUrl = getArguments().getString(AppConstants.K.REMOTE_URL.name(), null);
        if (StringUtils.isBlank(mUrl)) {
            handleError("missing url. closing...");
        }


        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        setupData();
        return view;
    }

    private void setupData() {
        // Let's display the progress in the activity title bar, like the
        // browser app does.
//        getActivity().getWindow().requestFeature(Window.FEATURE_PROGRESS);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(Boolean.TRUE);

        final Activity activity = getActivity();
//        mWebView.setWebChromeClient(new WebChromeClient() {
//            public void onProgressChanged(WebView view, int progress) {
//                // Activities and WebViews measure progress with different scales.
//                // The progress meter will automatically disappear when we reach 100%
//                activity.setProgress(progress * 1000);
//            }
//        });
//        mWebView.setWebViewClient(new WebViewClient() {
//            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//                handleError("error opening url");
//            }
//        });

        mWebView.loadUrl(mUrl);
    }

    private void handleError(String errMsg) {
        Snackbar.make(getView(), errMsg, Snackbar.LENGTH_SHORT).show();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().finish();
            }
        }, 999);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private static final class LocalHandler extends Handler {
        private final WeakReference<WebViewFragment> mActivity;

        public LocalHandler(WebViewFragment parent) {
            mActivity = new WeakReference<WebViewFragment>(parent);
        }
    }

}