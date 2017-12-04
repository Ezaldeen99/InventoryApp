package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.Data.InventoryAdapter;
import com.example.android.inventoryapp.Data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor> {
    InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        ListView list = (ListView) findViewById(R.id.items_list);
        adapter = new InventoryAdapter(this, null);
        list.setAdapter(adapter);
        View emptyView = findViewById(R.id.empty_text);
        list.setEmptyView(emptyView);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URL, id);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showDeleteConfirmationDialog();
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog);
        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Item.
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.keep_me, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the Item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URL, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from Inverntory database");
    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME,
                InventoryEntry.PRICE,
                InventoryEntry.QUANTITY,
                InventoryEntry.PICTURE};
        return new android.content.CursorLoader(this,
                InventoryEntry.CONTENT_URL,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        adapter.swapCursor(null);

    }

}
