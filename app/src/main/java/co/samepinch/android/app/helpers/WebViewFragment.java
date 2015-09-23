package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.samepinch.android.app.R;

public class WebViewFragment extends Fragment implements AdvancedWebView.Listener {
    public static final String TAG = "WebViewFragment";

    @Bind(R.id.webview)
    AdvancedWebView mWebView;

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

        mWebView.setListener(getActivity(), this);
        mWebView.loadUrl(mUrl);
        return view;
    }

    @OnClick(R.id.close)
    public void onCloseEvent() {
        getActivity().finish();
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

    @Override
    public void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    public void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(String url) {
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void onDownloadRequested(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
    }

    @Override
    public void onExternalPageRequest(String url) {
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

}