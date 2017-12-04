package com.example.android.inventoryapp.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by azozs on 11/11/2017.
 */

public final class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri INVENTORY = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {
        public static final String CONTENT_TABLE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final String CONTENT_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;
        public static final Uri CONTENT_URL = Uri.withAppendedPath(INVENTORY, PATH);
        public static final String _ID = BaseColumns._ID;
        public static final String TABLE_NAME = "inventory";
        public static final String NAME = "name";
        public static final String QUANTITY = "quantity";
        public static final String PRICE = "price";
        public static final String PICTURE = "picture";
    }
}
