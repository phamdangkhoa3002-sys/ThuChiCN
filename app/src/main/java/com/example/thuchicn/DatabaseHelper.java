package com.example.thuchicn;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "quanlythuchi.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "transactions";

    public static final String COL_ID = "id";
    public static final String COL_LOAI = "loai";
    public static final String COL_SOTIEN = "soTien";
    public static final String COL_PHANLOAI = "phanLoai";
    public static final String COL_NGAY = "ngay";
    public static final String COL_GIO = "gio";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create database table
    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_LOAI + " TEXT, " +
                COL_SOTIEN + " REAL, " +
                COL_PHANLOAI + " TEXT, " +
                COL_NGAY + " TEXT, " +
                COL_GIO + " TEXT)";

        db.execSQL(query);
    }

    // Upgrade database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert transaction
    public boolean insertTransaction(String loai, double soTien,
                                     String phanLoai, String ngay, String gio) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LOAI, loai);
        values.put(COL_SOTIEN, soTien);
        values.put(COL_PHANLOAI, phanLoai);
        values.put(COL_NGAY, ngay);
        values.put(COL_GIO, gio);

        long result = db.insert(TABLE_NAME, null, values);

        return result != -1;
    }

    // Get all transactions
    public Cursor getAllTransactions() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_NAME + " ORDER BY " +
                        COL_ID + " DESC",
                null
        );
    }

    // Delete transaction
    public boolean deleteTransaction(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_NAME,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    // Calculate total income
    public double getTotalIncome() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_SOTIEN + ") FROM " + TABLE_NAME +
                        " WHERE " + COL_LOAI + "=?",
                new String[]{"Thu"}
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }

    // Calculate total expense
    public double getTotalExpense() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_SOTIEN + ") FROM " + TABLE_NAME +
                        " WHERE " + COL_LOAI + "=?",
                new String[]{"Chi"}
        );

        double total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }

        cursor.close();

        return total;
    }
    // Update transaction
    public boolean updateTransaction(int id, String loai, double soTien,
                                     String phanLoai, String ngay, String gio) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_LOAI, loai);
        values.put(COL_SOTIEN, soTien);
        values.put(COL_PHANLOAI, phanLoai);
        values.put(COL_NGAY, ngay);
        values.put(COL_GIO, gio);

        int result = db.update(
                TABLE_NAME,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    // Get transactions by month and year
    public Cursor getTransactionsByMonthYear(String thang, String nam) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_NAME +
                        " WHERE substr(" + COL_NGAY + ", 4, 2) = ?" +
                        " AND substr(" + COL_NGAY + ", 7, 4) = ?" +
                        " ORDER BY " + COL_ID + " DESC",
                new String[]{thang, nam}
        );
    }
}