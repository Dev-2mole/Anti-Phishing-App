package com.example.smartphoneprogramming_project_20191899;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VirusTotal extends AppCompatActivity {
    private PieChart pieChart;
    private OkHttpClient client;
    private static final String URL = "http://35.194.114.44:5000/scan";
    private TextView textViewResponse, textViewStatus;
    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_total);
        textViewResponse = findViewById(R.id.textViewResponse);
        pieChart = findViewById(R.id.chart);
        textViewStatus = findViewById(R.id.textViewStatus);

        initializeChart();
        initializeClient();
        fetchData();
    }

    private void initializeClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void fetchData() {
        new Thread(() -> {
            try {
                final String response = post(URL, new JSONObject().put("url", "http://amaz0n.zhongxiaoyang.top/"));
                final String analyzedResponse = analyzeResponse(response);
                uiHandler.post(() -> {
                    textViewResponse.setText(analyzedResponse);
                    updateStatus(analyzedResponse);
                    updateChartData(analyzedResponse);
                });
            } catch (Exception e) {
                Log.e("NetworkError", "Error sending request", e);
            }
        }).start();
    }

    private String post(String url, JSONObject json) throws IOException, JSONException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("HttpResponse", "Server returned error: " + response.code());
                return "Error: " + response.code();
            }
            return response.body().string();
        }
    }

    private String analyzeResponse(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        JSONObject scanResults = json.getJSONObject("scan_results");
        int clean = 0, phishing = 0, malicious = 0, suspicious = 0;

        Iterator<String> keys = scanResults.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = scanResults.getString(key);
            switch (value) {
                case "clean site": clean++; break;
                case "phishing site": phishing++; break;
                case "malicious site": malicious++; break;
                case "suspicious site": suspicious++; break;
            }
        }

        Log.d("DataCount", "Clean: " + clean + ", Phishing: " + phishing + ", Malicious: " + malicious + ", Suspicious: " + suspicious);
        String summary = String.format("Clean: %d\nPhishing: %d\nMalicious: %d\nSuspicious: %d\n", clean, phishing, malicious, suspicious);
        if (phishing + malicious + suspicious >= 5) {
            summary += "WARNING: This site is considered suspicious.";
        }
        return summary;
    }

    private void initializeChart() {
        pieChart.clear();
        pieChart.setNoDataText("Loading data...");
    }

    private void updateChartData(String analysis) {
        List<PieEntry> entries = parseChartData(analysis);
        PieDataSet dataSet = new PieDataSet(entries, "Site Status");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private List<PieEntry> parseChartData(String analysis) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(30, "Clean"));
        entries.add(new PieEntry(20, "Phishing"));
        entries.add(new PieEntry(50, "Malicious"));
        return entries;
    }

    private void updateStatus(String analysis) {
        if (analysis.contains("WARNING")) {
            textViewStatus.setText("WARNING: The site may be unsafe!");
        } else if (analysis.contains("DANGER")) {
            textViewStatus.setText("DANGER: The site is malicious!");
        } else if (analysis.contains("SAFE")) {
            textViewStatus.setText("SAFE: The site is safe.");
        } else {
            textViewStatus.setText("UNKNOWN: Proceed with caution.");
        }
    }

}
