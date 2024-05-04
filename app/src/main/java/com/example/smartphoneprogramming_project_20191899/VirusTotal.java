package com.example.smartphoneprogramming_project_20191899;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException
        ;
import java.util.Iterator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.FormBody;
public class VirusTotal extends AppCompatActivity {
    private static final String API_KEY = "";
    private static final String TAG = "VirusTotalActivity";
    private static final OkHttpClient client = new OkHttpClient();

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        resultTextView = findViewById(R.id.result_text_view);

        String scanUrl = "http://amaz0n.zhongxiaoyang.top/";
        startUrlScan(scanUrl);
    }

    private void startUrlScan(String scanUrl) {
        String url = "https://www.virustotal.com/vtapi/v2/url/scan";

        RequestBody formBody = new FormBody.Builder()
                .add("apikey", API_KEY)
                .add("url", scanUrl)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMessage = "Error: " + e.getMessage();
                Log.e(TAG, errorMessage);
                runOnUiThread(() -> resultTextView.setText(errorMessage));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Response: " + responseData);
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        String scanId = jsonObject.getString("scan_id");
                        getUrlReport(scanId);
                    } catch (JSONException e) {
                        String errorMessage = "Error parsing JSON: " + e.getMessage();
                        Log.e(TAG, errorMessage);
                        runOnUiThread(() -> resultTextView.setText(errorMessage));
                    }
                } else {
                    String errorMessage = "Error: " + response.code();
                    Log.e(TAG, errorMessage);
                    runOnUiThread(() -> resultTextView.setText(errorMessage));
                }
            }
        });
    }

    private void getUrlReport(String scanId) {
        String urlReport = "https://www.virustotal.com/vtapi/v2/url/report";
        int maxRetries = 5;
        int initialRetryCount = 0;

        Request request = new Request.Builder()
                .url(urlReport)
                .addHeader("apikey", API_KEY)
                .addHeader("resource", scanId)
                .build();

        pollScanResult(request, maxRetries, initialRetryCount);
    }

    private void pollScanResult(Request request, int maxRetries, final int retryCount) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String errorMessage = "Error: " + e.getMessage();
                Log.e(TAG, errorMessage);
                runOnUiThread(() -> resultTextView.setText(errorMessage));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "Response: " + responseData);
                        try {
                            JSONObject jsonObject = new JSONObject(responseData);
                            int responseCode = jsonObject.getInt("response_code");

                            if (responseCode == 1) {
                                JSONObject scanResultObject = jsonObject.getJSONObject("scans");
                                StringBuilder resultBuilder = new StringBuilder();

                                Iterator<String> vendors = scanResultObject.keys();
                                while (vendors.hasNext()) {
                                    String vendor = vendors.next();
                                    JSONObject scanResult = scanResultObject.getJSONObject(vendor);

                                    boolean detected = scanResult.getBoolean("detected");
                                    String result = scanResult.getString("result");

                                    resultBuilder.append("Vendor: ").append(vendor).append("\n");
                                    resultBuilder.append("Detected: ").append(detected).append("\n");
                                    resultBuilder.append("Result: ").append(result).append("\n\n");
                                }

                                String scanResult = resultBuilder.toString();
                                runOnUiThread(() -> resultTextView.setText(scanResult));
                            } else if (responseCode == -2 && retryCount < maxRetries) {
                                // 스캔 결과가 아직 준비되지 않은 경우, 10초 후에 다시 시도
                                int nextRetryCount = retryCount + 1;
                                Handler handler = new Handler();
                                handler.postDelayed(() -> pollScanResult(request, maxRetries, nextRetryCount), 10000);
                            } else {
                                String verboseMsg = jsonObject.getString("verbose_msg");
                                runOnUiThread(() -> resultTextView.setText(verboseMsg));
                            }
                        } catch (JSONException e) {
                            String errorMessage = "Error parsing JSON: " + e.getMessage();
                            Log.e(TAG, errorMessage);
                            runOnUiThread(() -> resultTextView.setText(errorMessage));
                        }
                    } else {
                        String errorMessage = "Error: " + response.code();
                        Log.e(TAG, errorMessage);
                        runOnUiThread(() -> resultTextView.setText(errorMessage));
                    }
                } finally {
                    response.close();
                }
            }
        });
    }
}