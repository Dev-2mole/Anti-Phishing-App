package com.example.smartphoneprogramming_project_20191899;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SMSReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_TEXTVIEW = "com.example.smartphoneprogramming_project_20191899.UPDATE_TEXTVIEW";
    public static final String EXTRA_URL = "extra_url";
    private List<Pattern> urlPatterns = new ArrayList<>();

    public SMSReceiver() {
        urlPatterns.add(Pattern.compile("https?://[\\w.-]+(?:\\.[\\w\\.-]+)+"));
        urlPatterns.add(Pattern.compile("www\\d{0,3}\\.[\\w.-]+(?:\\.[\\w\\.-]+)+"));
        urlPatterns.add(Pattern.compile("\\b[a-z0-9-]+\\.[a-z]{2,4}\\b"));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                for (Object pdu : pdus) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = sms.getMessageBody();
                    String phoneNumber = sms.getOriginatingAddress();
                    String url = extractUrl(messageBody);
                    if (url != null) {
                        saveToDatabase(context, phoneNumber, url, messageBody);
                        Intent localIntent = new Intent(ACTION_UPDATE_TEXTVIEW);
                        localIntent.putExtra(EXTRA_URL, url);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
                    }
                }
            }
        }
    }

    private void saveToDatabase(Context context, String phoneNumber, String url, String messageBody) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_RECEIVED_DATE, getCurrentDateTime());
        values.put(DatabaseHelper.COLUMN_PHONE_NUMBER, phoneNumber);
        values.put(DatabaseHelper.COLUMN_URL, url);
        values.put(DatabaseHelper.COLUMN_REQUEST_CODE, "");
        values.put(DatabaseHelper.COLUMN_REDIRECTED_URL, "");
        values.put(DatabaseHelper.COLUMN_IS_ABNORMAL, 0);
        values.put(DatabaseHelper.COLUMN_MESSAGE_BODY, messageBody);

        long rowId = db.insert(DatabaseHelper.TABLE_NAME, null, values);
        if (rowId != -1) {
            Log.d("SMSReceiver", "데이터가 성공적으로 저장되었습니다. Row ID: " + rowId);
        } else {
            Log.e("SMSReceiver", "데이터 저장에 실패했습니다.");
        }

        db.close();
    }

    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String extractUrl(String message) {
        for (Pattern pattern : urlPatterns) {
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }
}
