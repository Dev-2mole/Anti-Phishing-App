package com.example.smartphoneprogramming_project_20191899;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_RECEIVE_SMS = 123;
    private TextView textView;
    private BroadcastReceiver receiver;
    private DatabaseHelper dbHelper;  // Database helper 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // SMS 수신 권한 요청
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_RECEIVE_SMS);
        }

        textView = findViewById(R.id.url);
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

        dbHelper = new DatabaseHelper(this);
        insertDummyData(); // 더미 데이터 삽입

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
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            dbHelper.getWritableDatabase(); // 데이터베이스 열기 (버전 증가로 인해 onUpgrade 메서드가 호출됨)
            dbHelper.close();
            Toast.makeText(this, "데이터베이스가 재생성되었습니다.", Toast.LENGTH_SHORT).show();
        });
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

        // 첫 번째 더미 데이터
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, "1234567890");
        values.put(DatabaseHelper.COLUMN_URL, "https://example.com");
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        // 두 번째 더미 데이터
        values.clear();
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, "9876543210");
        values.put(DatabaseHelper.COLUMN_URL, "https://example.org");
        db.insert(DatabaseHelper.TABLE_NAME, null, values);

        db.close();
    }
}