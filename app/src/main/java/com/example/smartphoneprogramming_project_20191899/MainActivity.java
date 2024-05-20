package com.example.smartphoneprogramming_project_20191899;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 123;
    public static final String PREFS_NAME = "ServerPrefs";
    public static final String KEY_SERVER_ADDRESS = "serverAddress";
    public static final String DEFAULT_SERVER_ADDRESS = "192.168.1.1";
    private static final String CHANNEL_ID = "server_status_channel";
    private static final int NOTIFICATION_ID = 1;

    private TextView textView;
    private BroadcastReceiver receiver;
    private DatabaseHelper dbHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate: Activity Created");

        initializeViews();
        initializeDatabase();
        initializeNotifications();
        initializeReceivers();
        checkSmsPermission();
        setupButtonClickListeners();

        // 서버 주소 가져오기
        String serverAddress = getServerAddress();
        Log.d("MainActivity", "Received serverAddress: " + serverAddress);
    }

    private void initializeViews() {
        textView = findViewById(R.id.url);
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
        //insertDummyData();
    }

    private void initializeNotifications() {
        createNotificationChannel();
        checkNotificationPermission();
    }

    private void initializeReceivers() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(SMSReceiver.ACTION_UPDATE_TEXTVIEW)) {
                    String url = intent.getStringExtra(SMSReceiver.EXTRA_URL);
                    textView.setText(url);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(SMSReceiver.ACTION_UPDATE_TEXTVIEW));
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_RECEIVE_SMS);
        }
    }

    private void setupButtonClickListeners() {
        Button viewDatabaseButton = findViewById(R.id.view_database_button);
        viewDatabaseButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UrlDatabaseActivity.class);
            startActivity(intent);
        });

        Button clearDatabaseButton = findViewById(R.id.clear_database_button);
        clearDatabaseButton.setOnClickListener(v -> {
            dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_NAME, null, null);
            Toast.makeText(MainActivity.this, "데이터베이스가 초기화되었습니다.", Toast.LENGTH_SHORT).show();
        });

        Button recreateDatabaseButton = findViewById(R.id.recreate_database_button);
        recreateDatabaseButton.setOnClickListener(v -> {
            dbHelper.getWritableDatabase();
            dbHelper.close();
            Toast.makeText(this, "데이터베이스가 재생성되었습니다.", Toast.LENGTH_SHORT).show();
        });

        Button virustotal_test_page = findViewById(R.id.VirusTotal_Test_page);
        virustotal_test_page.setOnClickListener(v -> {
            String serverAddress = getServerAddress();
            Log.d("MainActivity", "Sending serverAddress to VirusTotal: " + serverAddress);
            Intent intent = new Intent(MainActivity.this, VirusTotal.class);
            intent.putExtra("serverAddress", serverAddress);
            intent.putExtra("url", textView.getText().toString());
            startActivity(intent);
        });

        Button virustotal_local_server = findViewById(R.id.VirusTotal_local_server);
        virustotal_local_server.setOnClickListener(v -> showServerAddressDialog());
    }

    private String getServerAddress() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SERVER_ADDRESS, DEFAULT_SERVER_ADDRESS);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Server Status";
            String description = "Notifications for server status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void checkNotificationPermission() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (!notificationManager.areNotificationsEnabled()) {
            new AlertDialog.Builder(this)
                    .setTitle("알림 권한 필요")
                    .setMessage("이 앱의 알림을 활성화해야 합니다. 설정으로 이동하시겠습니까?")
                    .setPositiveButton("설정", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                        startActivity(intent);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        }
    }

    private void showServerAddressDialog() {
        runOnUiThread(() -> {
            if (!isFinishing()) {
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                new AlertDialog.Builder(this)
                        .setTitle("서버 주소 입력")
                        .setMessage("서버의 IP 주소를 입력해주세요 (예: 192.168.1.1)")
                        .setView(input)
                        .setPositiveButton("확인", (dialog, which) -> {
                            String serverAddress = input.getText().toString();
                            if (!serverAddress.startsWith("http://") && !serverAddress.startsWith("https://")) {
                                serverAddress = "http://" + serverAddress;
                            }
                            if (sharedPreferences == null) {
                                sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                            }
                            Log.d("MainActivity", "Server address entered: " + serverAddress);
                            saveServerAddress(serverAddress);
                            checkServerStatus(serverAddress);
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });
    }

    private void checkServerStatus(String serverAddress) {
        OkHttpClient client = new OkHttpClient();
        String url = serverAddress + ":5000/health";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("MainActivity", "checkServerStatus: onFailure", e);
                runOnUiThread(() -> {
                    showNotification("서버와 연결이 끊어졌습니다. 새로운 서버 주소를 입력해주세요.");
                    showServerAddressDialog();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "서버와 정상적으로 연결되었습니다.", Toast.LENGTH_LONG).show();
                        saveServerAddress(serverAddress);
                    });
                } else {
                    Log.e("MainActivity", "checkServerStatus: onResponse - failure");
                    runOnUiThread(() -> {
                        showNotification("서버와 연결이 끊어졌습니다. 새로운 서버 주소를 입력해주세요.");
                        showServerAddressDialog();
                    });
                }
            }
        });
    }

    private void showNotification(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Server Status")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void saveServerAddress(String serverAddress) {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SERVER_ADDRESS, serverAddress);
        editor.apply();
        Log.d("MainActivity", "Server address saved: " + serverAddress);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private void insertDummyData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, "1234567890");
        values.put(DatabaseHelper.COLUMN_URL, "https://example.com");
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        values.clear();
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, "9876543210");
        values.put(DatabaseHelper.COLUMN_URL, "https://example.org");
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        db.close();
    }
}
