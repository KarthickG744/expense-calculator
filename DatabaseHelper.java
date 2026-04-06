package com.example.expensecalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.*;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME    = "expenses.db";
    private static final int    DB_VERSION = 1;
    private static final String TABLE      = "expenses";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "date TEXT NOT NULL," +
            "name TEXT NOT NULL," +
            "amount REAL NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void addExpense(String date, String name, double amount) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", date);
        cv.put("name", name);
        cv.put("amount", amount);
        db.insert(TABLE, null, cv);
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE, "id=?", new String[]{String.valueOf(id)});
    }

    public List<Expense> getExpensesForDate(String date) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE + " WHERE date=? ORDER BY id DESC",
            new String[]{date});
        while (c.moveToNext()) {
            list.add(new Expense(c.getInt(0), c.getString(1), c.getString(2), c.getDouble(3)));
        }
        c.close();
        return list;
    }

    public double getMonthTotal(String yearMonth) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT SUM(amount) FROM " + TABLE + " WHERE date LIKE ?",
            new String[]{yearMonth + "%"});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    // Returns map of expense name -> total amount for the month
    public Map<String, Double> getMonthlySummary(String yearMonth) {
        Map<String, Double> map = new LinkedHashMap<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
            "SELECT name, SUM(amount) FROM " + TABLE +
            " WHERE date LIKE ? GROUP BY LOWER(name) ORDER BY SUM(amount) DESC",
            new String[]{yearMonth + "%"});
        while (c.moveToNext()) {
            map.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        return map;
    }
}
