package com.example.smartphoneprogramming_project_20191899;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "url_database.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "urls";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_RECEIVED_DATE = "received_date";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_REQUEST_CODE = "request_code";
    public static final String COLUMN_REDIRECTED_URL = "redirected_url";
    public static final String COLUMN_IS_ABNORMAL = "is_abnormal";
    public static final String COLUMN_MESSAGE_BODY = "message_body";
    public static final String COLUMN_TOTAL_SCANS = "total_scans";
    public static final String COLUMN_SCAN_DETAILS = "scan_details";
    public static final String COLUMN_SUSPICIOUS_SCANS = "suspicious_scans";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_RECEIVED_DATE + " TEXT,"
                    + COLUMN_PHONE_NUMBER + " TEXT,"
                    + COLUMN_URL + " TEXT,"
                    + COLUMN_REQUEST_CODE + " TEXT,"
                    + COLUMN_REDIRECTED_URL + " TEXT,"
                    + COLUMN_IS_ABNORMAL + " INTEGER,"
                    + COLUMN_MESSAGE_BODY + " TEXT,"
                    + COLUMN_TOTAL_SCANS + " INTEGER,"
                    + COLUMN_SCAN_DETAILS + " TEXT,"
                    + COLUMN_SUSPICIOUS_SCANS + " INTEGER"
                    + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}