package com.example.smartphoneprogramming_project_20191899;

import android.database.Cursor;
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
        Cursor cursor = dbHelper.getReadableDatabase().query(
                DatabaseHelper.TABLE_NAME,
                null, // all columns
                null, // columns for the "where" clause
                null, // values for the "where" clause
                null, // group by
                null, // filter by row groups
                null); // sort order

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2,
                cursor,
                new String[] {DatabaseHelper.COLUMN_PHONE_NUMBER, DatabaseHelper.COLUMN_URL},
                new int[] {android.R.id.text1, android.R.id.text2},
                0);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            try {
                Cursor itemCursor = (Cursor) parent.getItemAtPosition(position);

                int receivedDateColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_RECEIVED_DATE);
                int phoneNumberColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_PHONE_NUMBER);
                int urlColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_URL);
                int requestCodeColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_REQUEST_CODE);
                int redirectedUrlColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_REDIRECTED_URL);
                int isAbnormalColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ABNORMAL);
                int messageBodyColumnIndex = itemCursor.getColumnIndex(DatabaseHelper.COLUMN_MESSAGE_BODY);

                String receivedDate = receivedDateColumnIndex != -1 ? itemCursor.getString(receivedDateColumnIndex) : "";
                String phoneNumber = phoneNumberColumnIndex != -1 ? itemCursor.getString(phoneNumberColumnIndex) : "";
                String url = urlColumnIndex != -1 ? itemCursor.getString(urlColumnIndex) : "";
                String requestCode = requestCodeColumnIndex != -1 ? itemCursor.getString(requestCodeColumnIndex) : "";
                String redirectedUrl = redirectedUrlColumnIndex != -1 ? itemCursor.getString(redirectedUrlColumnIndex) : "";
                int isAbnormal = isAbnormalColumnIndex != -1 ? itemCursor.getInt(isAbnormalColumnIndex) : 0;
                String messageBody = messageBodyColumnIndex != -1 ? itemCursor.getString(messageBodyColumnIndex) : "";

                String details = "수신일시: " + receivedDate + "\n"
                        + "보낸 사람: " + phoneNumber + "\n"
                        + "URL: " + url + "\n"
                        + "Request Code: " + requestCode + "\n"
                        + "Redirected URL: " + redirectedUrl + "\n"
                        + "이상 여부: " + isAbnormal;

                View popupView = LayoutInflater.from(UrlDatabaseActivity.this).inflate(R.layout.popup_layout, null);
                TextView detailsTextView = popupView.findViewById(R.id.details_text_view);
                TextView messageBodyTextView = popupView.findViewById(R.id.message_body_text_view);

                detailsTextView.setText(details);
                messageBodyTextView.setText("문자 내용:\n" + messageBody);

                // 팝업 창 생성
                AlertDialog.Builder builder = new AlertDialog.Builder(UrlDatabaseActivity.this);
                builder.setView(popupView);
                builder.setPositiveButton("닫기", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(UrlDatabaseActivity.this, "데이터 로드 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}
