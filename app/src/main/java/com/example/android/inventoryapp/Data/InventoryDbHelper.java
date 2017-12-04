package com.example.android.inventoryapp.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.Data.InventoryContract.InventoryEntry;

/**
 * Created by azozs on 11/11/2017.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String CREATE = "CREATE TABLE " +
            InventoryEntry.TABLE_NAME + "( " +
            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryEntry.NAME + " TEXT, " +
            InventoryEntry.PRICE + " INTEGER, " +
            InventoryEntry.QUANTITY + " INTEGER, " +
            InventoryEntry.PICTURE +  " BLOB)";
    private static final String DELETE = "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE);
        onCreate(db);
    }
}
