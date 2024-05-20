package com.example.smartphoneprogramming_project_20191899;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity"; // 로그 태그
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        webView = findViewById(R.id.webview);
        configureWebView(); // WebView 설정 메소드 호출
        String url = getIntent().getStringExtra("url");

        // WebViewClient 설정
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.d(TAG, "Page loading started: " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page loading finished: " + url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                // 오류 로그 출력
                Log.e(TAG, "Error loading page: " + error.toString());
            }
        });

        webView.loadUrl(url); // URL 로딩
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // JavaScript 활성화
        settings.setDomStorageEnabled(true); // DOM Storage 활성화
        settings.setLoadWithOverviewMode(true); // 콘텐츠가 웹뷰에 정확히 맞도록 조정
        settings.setUseWideViewPort(true); // HTML 컨텐츠의 너비가 웹뷰의 너비에 맞도록 설정
        settings.setSupportZoom(true); // 줌 지원 활성화
        settings.setBuiltInZoomControls(true); // 내장 줌 컨트롤 사용
        settings.setDisplayZoomControls(false); // 줌 컨트롤 표시하지 않음
    }
}
