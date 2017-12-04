package com.example.android.inventoryapp.Data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.MainActivity;
import com.example.android.inventoryapp.R;

/**
 * Created by azozs on 11/17/2017.
 */

public class InventoryAdapter extends CursorAdapter {
    Context mContext;
    View currentView;

    public InventoryAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.switch_item, parent, false);
    }

    @Override
    public void bindView(final View view, Context context, final Cursor cursor) {
        TextView name = view.findViewById(R.id.name);
        Button saleButton = view.findViewById(R.id.sale_button);
        TextView price = view.findViewById(R.id.price);
        saleButton.setTag(cursor.getPosition());
        TextView quantity = view.findViewById(R.id.quantity);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentView= v;
                Item(cursor, (int) v.getTag());
            }
        });

        name.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.NAME)));
        price.setText(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.PRICE))+ "");
        quantity.setText(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.QUANTITY))+"");
    }

    private void Item(Cursor cursor, int position) {
        cursor.moveToPosition(position);
        ContentValues values = new ContentValues();
        int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.QUANTITY));
        int id = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID));

        if (quantity <= 0) {
            Toast.makeText(currentView.getContext(), "stock is not enough", Toast.LENGTH_SHORT).show();
            return;
        }
        quantity--;
        values.put(InventoryContract.InventoryEntry.QUANTITY, quantity);
        Uri stockUrl = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URL, id);
        int rowsAffected = mContext.getContentResolver().update(stockUrl, values, null, null);
        Toast.makeText(currentView.getContext(), rowsAffected + mContext.getString(R.string.item_sold), Toast.LENGTH_SHORT).show();
    }
}
