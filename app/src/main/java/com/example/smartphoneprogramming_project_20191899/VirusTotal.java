package com.example.smartphoneprogramming_project_20191899;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VirusTotal extends AppCompatActivity {
    private CircularProgressBar circularProgressBar;
    private TextView textViewDetectionCount;
    private OkHttpClient client;
    private Button buttonViewWeb;
    private TextView textViewResponse, textViewStatus, textViewDetectionSummary, textViewDetectionDetails;
    private PieChart pieChart; // Initialize pieChart here
    private String reportUrl;
    private String serverAddress;
    private static final String CHANNEL_ID = "virustotal_notifications";
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private DatabaseHelper databaseHelper;
    private JSONObject jsonResponse;
    public VirusTotal() {
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_virus_total);
        initializeViews();
        initializeClient();
        getServerAddress();
        setButtonClickListener();
        handleReceivedUrl();
        checkNotificationPermission();
    }

    private void initializeViews() {
        circularProgressBar = findViewById(R.id.circularProgressBar);
        textViewDetectionCount = findViewById(R.id.textViewDetectionCount);
        textViewResponse = findViewById(R.id.textViewResponse);
        textViewStatus = findViewById(R.id.textViewStatus);
        buttonViewWeb = findViewById(R.id.buttonViewWeb);
        textViewDetectionSummary = findViewById(R.id.textViewDetectionSummary);
        textViewDetectionDetails = findViewById(R.id.textViewDetectionDetails);
    }

    private void initializeClient() {
        client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃
                .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃
                .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃
                .build();
    }

    private void getServerAddress() {
        serverAddress = getIntent().getStringExtra("serverAddress");
        if (serverAddress == null || serverAddress.isEmpty()) {
            serverAddress = MainActivity.DEFAULT_SERVER_ADDRESS;
        }
        Log.d("VirusTotal", "Received serverAddress: " + serverAddress);
    }

    private void setButtonClickListener() {
        buttonViewWeb.setOnClickListener(v -> {
            if (reportUrl == null || reportUrl.isEmpty()) {
                Log.e("VirusTotal", "reportUrl is null or empty");
                return;
            }
            openUrlInBrowser(reportUrl);
        });
    }

    private void handleReceivedUrl() {
        String url = getIntent().getStringExtra("url");
        Log.d("VirusTotal", "Received URL: " + url);
        if (url != null) {
            new FetchDataTask().execute(url);
        }
    }

    private void checkNotificationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.e("VirusTotal", "Notification permission denied.");
            }
        }
    }

    private class FetchDataTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String targetUrl = urls[0];
            Log.d("FetchDataTask", "URL for POST request: " + targetUrl);
            try {
                JSONObject requestBody = createRequestBody(targetUrl);
                String completeUrl = buildCompleteUrl();
                Log.d("FetchDataTask", "Complete URL for POST request: " + completeUrl);
                return sendPostRequest(completeUrl, requestBody);
            } catch (JSONException | IOException e) {
                Log.e("NetworkError", "Error in network operation", e);
                return null;
            } catch (Exception e) {
                Log.e("NetworkError", "Unexpected error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                try {
                    processResponse(response);
                } catch (JSONException e) {
                    Log.e("FetchDataTask", "Error processing JSON response", e);
                }
            } else {
                Log.e("FetchDataTask", "Received null response");
            }
        }

        private JSONObject createRequestBody(String targetUrl) throws JSONException {
            JSONObject json = new JSONObject();
            json.put("url", targetUrl);
            Log.d("FetchDataTask", "JSON for POST request: " + json.toString());
            return json;
        }

        private String buildCompleteUrl() {
            String completeUrl;
            if (serverAddress.startsWith("http://") || serverAddress.startsWith("https://")) {
                completeUrl = serverAddress + ":5000/scan";
            } else {
                completeUrl = "http://" + serverAddress + ":5000/scan";
            }
            Log.d("FetchDataTask", "Built complete URL: " + completeUrl);
            return completeUrl;
        }

        private String sendPostRequest(String url, JSONObject requestBody) throws IOException {
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e("HttpResponse", "Server returned error: " + response.code());
                    return "Error: " + response.code();
                }
                String responseBody = response.body().string();
                Log.d("FetchDataTask", "Response: " + responseBody); // Add this line to log the response
                return responseBody;
            }
        }

        private void processResponse(String response) throws JSONException {
            jsonResponse = new JSONObject(response);
            String analyzedResponse = analyzeResponse(jsonResponse);
            updateUI(analyzedResponse);
            showNotification(VirusTotal.this, "Scan Complete", "Click to view details");
            saveScanResultToDatabase(jsonResponse);
        }
    }

    private void updateUI(String analyzedResponse) {
        runOnUiThread(() -> {
            updateStatus(analyzedResponse);
            updateChartData(analyzedResponse);
            extractReportUrl(jsonResponse);
        });
    }

    private String analyzeResponse(JSONObject json) throws JSONException {
        JSONObject scanResults = json.optJSONObject("scan_results");
        int clean = 0, phishing = 0, malicious = 0, suspicious = 0, unrated = 0, others = 0;

        StringBuilder detectionDetailsBuilder = new StringBuilder();

        if (scanResults != null) {
            Log.d("analyzeResponse", "scanResults: " + scanResults.toString());
            Iterator<String> keys = scanResults.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject details = scanResults.optJSONObject(key);
                if (details != null) {
                    boolean detected = details.optBoolean("detected", false);
                    String result = details.optString("result", "");

                    Log.d("analyzeResponse", "key: " + key + ", detected: " + detected + ", result: " + result);

                    if (detected) {
                        detectionDetailsBuilder.append("Site: ").append(key).append(", Result: ").append(result).append("\n");
                        switch (result.toLowerCase()) {
                            case "clean":
                            case "clean site":
                                clean++;
                                break;
                            case "phishing":
                            case "phishing site":
                                phishing++;
                                break;
                            case "malicious":
                            case "malicious site":
                            case "malware site":
                                malicious++;
                                break;
                            case "suspicious":
                            case "suspicious site":
                                suspicious++;
                                break;
                            case "unrated site":
                                unrated++;
                                break;
                            default:
                                others++;
                                break;
                        }
                    } else {
                        if (result.equalsIgnoreCase("unrated site")) {
                            unrated++;
                        } else {
                            clean++;
                        }
                    }
                } else {
                    Log.d("analyzeResponse", "details is null for key: " + key);
                }
            }
        } else {
            Log.d("analyzeResponse", "scanResults is null");
        }

        int totalDetections = clean + phishing + malicious + suspicious + unrated + others;
        int totalScans = scanResults.length();
        String detectionSummary = String.format(Locale.getDefault(), "%d/%d detections", totalDetections, totalScans);

        runOnUiThread(() -> {
            textViewDetectionSummary.setText(detectionSummary);
            textViewDetectionDetails.setText(detectionDetailsBuilder.toString());
            circularProgressBar.setProgressMax(totalScans);
            circularProgressBar.setProgressWithAnimation((float) totalDetections, 1000L); // 1 second animation
            textViewDetectionCount.setText(String.valueOf(totalDetections));
        });

        return String.format(Locale.getDefault(), "Clean: %d\nPhishing: %d\nMalicious: %d\nSuspicious: %d\nUnrated: %d\nOthers: %d",
                clean, phishing, malicious, suspicious, unrated, others);
    }

    private void updateChartData(String response) {
        List<PieEntry> entries = new ArrayList<>();
        String[] lines = response.split("\n");
        for (String line : lines) {
            String[] parts = line.split(": ");
            if (parts.length == 2) {
                String label = parts[0];
                int value = Integer.parseInt(parts[1]);
                entries.add(new PieEntry(value, label));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Scan Results");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateStatus(String status) {
        runOnUiThread(() -> textViewStatus.setText(status));
    }

    private void extractReportUrl(JSONObject jsonResponse) {
        if (jsonResponse.has("permalink")) {
            reportUrl = jsonResponse.optString("permalink", null);
            Log.d("VirusTotal", "Report URL received: " + reportUrl);
            if (reportUrl != null && !reportUrl.startsWith("http://") && !reportUrl.startsWith("https://")) {
                if (serverAddress.startsWith("http://") || serverAddress.startsWith("https://")) {
                    reportUrl = serverAddress + reportUrl;
                } else {
                    reportUrl = "http://" + serverAddress + reportUrl;
                }
            }
        }
    }

    public void showNotification(Context context, String title, String content) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("VirusTotal", "Notification permission not granted.");
            return;
        }

        Intent intent = new Intent(context, MainActivity.class); // Use MainActivity or your target activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE // Ensure this flag is used
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "VirusTotal Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }

    private void openUrlInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private void saveScanResultToDatabase(JSONObject jsonResponse) throws JSONException {
        JSONObject scanResults = jsonResponse.optJSONObject("scan_results");
        int totalScans = scanResults != null ? scanResults.length() : 0;
        String scanDetails = jsonResponse.toString();

        int suspiciousScans = 0;
        if (scanResults != null) {
            Iterator<String> keys = scanResults.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject details = scanResults.optJSONObject(key);
                if (details != null && details.optBoolean("detected", false)) {
                    suspiciousScans++;
                }
            }
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TOTAL_SCANS, totalScans);
        values.put(DatabaseHelper.COLUMN_SCAN_DETAILS, scanDetails);
        values.put(DatabaseHelper.COLUMN_SUSPICIOUS_SCANS, suspiciousScans);
        db.insert(DatabaseHelper.TABLE_NAME, null, values);
        db.close();
    }
}
