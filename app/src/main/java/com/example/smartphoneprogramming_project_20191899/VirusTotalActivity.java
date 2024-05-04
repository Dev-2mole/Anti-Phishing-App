package com.example.smartphoneprogramming_project_20191899;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class VirusTotalActivity extends AppCompatActivity {
    private WebView webView;
    private static final String VIRUSTOTAL_ID = "";
    private static final String VIRUSTOTAL_PW = "";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_total);

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("sign-in")) {
                    // 로그인 폼 자동 입력 및 로그인 버튼 클릭
                    String autoLoginScript = "document.getElementById('email').value='" + VIRUSTOTAL_ID + "';"
                            + "document.getElementById('password').value='" + VIRUSTOTAL_PW + "';"
                            + "document.getElementsByTagName('button')[0].click();";
                    view.evaluateJavascript(autoLoginScript, null);
                }
            }
        });

        // VirusTotal 로그인 페이지 로드
        webView.loadUrl("https://www.virustotal.com/gui/sign-in");
    }
}