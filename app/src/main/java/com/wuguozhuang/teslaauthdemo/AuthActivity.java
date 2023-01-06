package com.wuguozhuang.teslaauthdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AuthActivity extends AppCompatActivity {

    private WebView web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        web = findViewById(R.id.authWebview);

        web.getSettings().setJavaScriptEnabled(true);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.setAcceptCookie(true);

        String url = getIntent().getStringExtra("url");
        String redirectUrl = getIntent().getStringExtra("redirectUrl");

        web.setWebChromeClient(new WebChromeClient());
        web.setSaveEnabled(false);
        web.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String urlString) {
                if (urlString.startsWith(redirectUrl)) {
                    new Handler(getMainLooper()).postDelayed(()->{
                        String code = Uri.parse(urlString).getQueryParameter("code");
                        Intent i = new Intent();
                        i.putExtra("code", code);
                        setResult(RESULT_OK, i);
                        finish();
                    }, 200);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                }
            }
        });
        web.loadUrl(url);
    }
}