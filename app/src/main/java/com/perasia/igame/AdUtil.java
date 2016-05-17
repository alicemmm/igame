package com.perasia.igame;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;

import java.io.File;

public class AdUtil {
    private static final String TAG = AdUtil.class.getSimpleName();

    private Context mContext;

    private WebView mWebView;
    private WebSettings mWebSettings;

    private DownloadManager mDownloadManager;

    public interface OnWebViewGoneListener {
        void onGone();
    }

    private static OnWebViewGoneListener listener;

    public static void setOnWebViewGoneListener(OnWebViewGoneListener l) {
        listener = l;
    }

    private AdUtil(Context context, WebView webView) {
        mContext = context;
        mWebView = webView;
        if (mWebView != null) {
            mWebSettings = mWebView.getSettings();
        }

        if (mWebSettings != null) {
            initWebView();
        }
    }

    public static AdUtil setAds(Context context, WebView webView) {
        return new AdUtil(context, webView);
    }

    private void initWebView() {
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportZoom(true);
        mWebSettings.setBuiltInZoomControls(false);
        mWebView.requestFocus();

        MyWebViewClient myWebViewClient = new MyWebViewClient();
        mWebView.setWebViewClient(myWebViewClient);

        ChromeClient webChromeClient = new ChromeClient();
        mWebView.setWebChromeClient(webChromeClient);

        if (Build.VERSION.SDK_INT >= 19) {
            mWebSettings.setLoadsImagesAutomatically(true);
        } else {
            mWebSettings.setLoadsImagesAutomatically(false);
        }

        mWebSettings.setBlockNetworkImage(true);

        mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        setWebViewDownloadListener();

        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.loadUrl("file:///android_asset/banner.html");
    }

    private void setWebViewDownloadListener() {
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String s1, String s2, String s3, long l) {
                final String savePath = CommonUtils.getDownloadSavePath(mContext, url);

                mDownloadManager = DownloadManager.getInstance(mContext);

                mDownloadManager.execute(url, savePath, true, new DownloadManager.DownloadCallBack() {
                    @Override
                    public void onStart() {
                        Log.e(TAG, "download start");
                    }

                    @Override
                    public void onLoading(long total, long current, boolean isUploading) {

                    }

                    @Override
                    public void onSuccess(ResponseInfo<File> responseInfo) {
                        CommonUtils.installApk(mContext, savePath);
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        Log.e(TAG, msg);
                    }
                });

            }
        });
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!mWebSettings.getLoadsImagesAutomatically()) {
                mWebSettings.setLoadsImagesAutomatically(true);
            }
            mWebSettings.setBlockNetworkImage(false);

            countDown();

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }
    }

    /**
     * WebChromeClient自定义继承类,获取标题，获得当前进度
     */
    private class ChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
        }
    }

    private void countDown() {
        new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (listener != null) {
                    listener.onGone();
                }
            }
        }.start();
    }

}
