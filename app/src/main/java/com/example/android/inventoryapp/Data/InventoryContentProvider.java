package com.example.android.inventoryapp.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventoryapp.Data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.EditorActivity;

/**
 * Created by azozs on 11/12/2017.
 */

public class InventoryContentProvider extends ContentProvider {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = InventoryContentProvider.class.getSimpleName();

    /**
     * URI matcher code for the content URI for the INVENTORY table
     */
    private static final int WHOLE_TABLE = 100;

    /**
     * URI matcher code for the content URI for a single ITEM in the INVENTORY table
     */
    private static final int ITEM_ID = 101;
    private InventoryDbHelper inventoryHelper;
    SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        inventoryHelper = new InventoryDbHelper(getContext());
        return true;
    }

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH, WHOLE_TABLE);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH + "/#", ITEM_ID);
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs
            , @Nullable String sortOrder) {
        Cursor cursor;
        database = inventoryHelper.getReadableDatabase();
        int matchCheck = uriMatcher.match(uri);
        switch (matchCheck) {
            case WHOLE_TABLE:
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        sortOrder);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int matchChecker = uriMatcher.match(uri);
        switch (matchChecker) {
            case WHOLE_TABLE:
                return InventoryEntry.CONTENT_TABLE;
            case ITEM_ID:
                return InventoryEntry.CONTENT_ITEM;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WHOLE_TABLE:
                return saveItem(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri saveItem(Uri uri, ContentValues values) {
        String name = values.getAsString(InventoryEntry.NAME);
        byte [] view = values.getAsByteArray(InventoryEntry.PICTURE);
        Integer price = values.getAsInteger(InventoryEntry.PRICE);
        Integer quantity = values.getAsInteger(InventoryEntry.QUANTITY);
        if(view == null){
            throw new IllegalArgumentException("The Item should has a picture");
        }
        if (name == null) {
            throw new IllegalArgumentException("The Item should has a name");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("The price should has a price");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("The Item should has a name");
        }
        database = inventoryHelper.getWritableDatabase();
        long insertedItemRow = database.insert(InventoryEntry.TABLE_NAME, null, values);
        if (insertedItemRow == 0) {
            Log.e(LOG_TAG, "error with saving Item");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, insertedItemRow);

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int matchChecker = uriMatcher.match(uri);
        database = inventoryHelper.getWritableDatabase();
        int deletedRows;
        switch (matchChecker) {
            case WHOLE_TABLE:
                deletedRows = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                deletedRows = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot delete unknown URI " + uri);
        }
        if (deletedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int matchChecker = uriMatcher.match(uri);
        switch (matchChecker) {
            case WHOLE_TABLE:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update unknown URI " + uri);
        }

    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(InventoryEntry.NAME) && (values.getAsString(InventoryEntry.NAME)) == null) {
            throw new IllegalArgumentException("Item requires a name");
        }
        if (values.containsKey(InventoryEntry.PRICE) && ((values.getAsInteger(InventoryEntry.PRICE)) == null
                || (values.getAsInteger(InventoryEntry.PRICE)) <= 0)) {
            throw new IllegalArgumentException("ITEM MUST HAVE A PRICE");
        }
        if (values.containsKey(InventoryEntry.QUANTITY) && ((values.getAsInteger(InventoryEntry.QUANTITY)) == null
                || (values.getAsInteger(InventoryEntry.QUANTITY)) < 0)) {
            throw new IllegalArgumentException("ITEM MUST HAVE A QUANTITY");
        }
        if (values.size() == 0) {
            return 0;
        }
        database = inventoryHelper.getWritableDatabase();
        getContext().
                getContentResolver().
                notifyChange(uri, null);
        int updatedRows = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (updatedRows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updatedRows;
    }

}
