package com.example.smartphoneprogramming_project_20191899;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class UrlDatabaseActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.url_database);

        listView = findViewById(R.id.url_list);
        dbHelper = new DatabaseHelper(this);
        displayDatabaseInfo();
    }


    private void displayDatabaseInfo() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[]{DatabaseHelper.COLUMN_PHONE_NUMBER, DatabaseHelper.COLUMN_URL},
                new int[]{android.R.id.text1, android.R.id.text2},
                0
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Cursor itemCursor = (Cursor) parent.getItemAtPosition(position);
            int receivedDateColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIVED_DATE);
            int phoneNumberColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
            int urlColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_URL);
            int isAbnormalColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ABNORMAL);
            int messageBodyColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_BODY);
            int totalScansColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_TOTAL_SCANS);
            int suspiciousScansColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_SUSPICIOUS_SCANS);
            int scanDetailsColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_SCAN_DETAILS);

            String receivedDate = itemCursor.getString(receivedDateColumnIndex);
            String phoneNumber = itemCursor.getString(phoneNumberColumnIndex);
            String url = itemCursor.getString(urlColumnIndex);
            int isAbnormal = itemCursor.getInt(isAbnormalColumnIndex);
            String messageBody = itemCursor.getString(messageBodyColumnIndex);
            int totalScans = itemCursor.getInt(totalScansColumnIndex);
            int suspiciousScans = itemCursor.getInt(suspiciousScansColumnIndex);
            String scanDetails = itemCursor.getString(scanDetailsColumnIndex);

            String details = "수신일시: " + receivedDate + "\n"
                    + "보낸 사람: " + phoneNumber + "\n"
                    + "URL: " + url + "\n"
                    + "이상 여부: " + isAbnormal + "\n"
                    + "총 검사 수: " + totalScans + "\n"
                    + "의심스러운 검사 수: " + suspiciousScans;

            View popupView = LayoutInflater.from(UrlDatabaseActivity.this).inflate(R.layout.popup_layout, null);
            TextView detailsTextView = popupView.findViewById(R.id.details_text_view);
            TextView messageBodyTextView = popupView.findViewById(R.id.message_body_text_view);
            TextView scanDetailsTextView = popupView.findViewById(R.id.scan_details_text_view);

            detailsTextView.setText(details);
            messageBodyTextView.setText("문자 내용:\n" + messageBody);
            scanDetailsTextView.setText("상세 검사 결과:\n" + scanDetails);

            AlertDialog.Builder builder = new AlertDialog.Builder(UrlDatabaseActivity.this);
            builder.setView(popupView);
            builder.setPositiveButton("닫기", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });

        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
