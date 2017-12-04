package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.inventoryapp.Data.InventoryContract.InventoryEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PET_LOADER = 1;
    private boolean mItemHasChanged = false;
    private boolean pictureChanged = false;
    private EditText nameEditText;
    private EditText priceEditText;
    private Button selectImage;
    private Button order;
    private String[] items = {"Camera", "Gallery"};
    public static final int REQUEST_CODE_CAMERA = 0012;
    public static final int REQUEST_CODE_GALLERY = 0013;
    FileInputStream file;
    byte[] image = null;
    private ImageView img;
    int quantity;
    public Uri mContentUri;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mContentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        Intent s = getIntent();
        mContentUri = s.getData();
        if (mContentUri == null) {
            setTitle("Add Item");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }
        Button decrease = (Button) findViewById(R.id.decrease);
        Button increase = (Button) findViewById(R.id.increase);
        order = (Button) findViewById(R.id.order);
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    quantity--;
                }
                displayText(quantity);
            }
        });
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order();
            }
        });
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity < 100) {
                    quantity++;
                }
                displayText(quantity);
            }
        });
        img = (ImageView) findViewById(R.id.image_view);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        priceEditText = (EditText) findViewById(R.id.price_edit_text);
        selectImage = (Button) findViewById(R.id.select_imag);
        nameEditText.setOnTouchListener(mTouchListener);
        priceEditText.setOnTouchListener(mTouchListener);
        increase.setOnTouchListener(mTouchListener);
        decrease.setOnTouchListener(mTouchListener);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenImage();
            }
        });
    }

    private void OpenImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {
                    EasyImage.openCamera(EditorActivity.this, REQUEST_CODE_CAMERA);
                } else if (items[i].equals("Gallery")) {
                    EasyImage.openGallery(EditorActivity.this, REQUEST_CODE_GALLERY);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                switch (type) {
                    case REQUEST_CODE_CAMERA:
                        Glide.with(EditorActivity.this)
                                .load(imageFile)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(img);
                        saveImage(imageFile.getAbsolutePath());
                        break;
                    case REQUEST_CODE_GALLERY:
                        Glide.with(EditorActivity.this)
                                .load(imageFile)
                                .centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(img);
                        saveImage(imageFile.getAbsolutePath());
                        break;
                }
            }
        });
    }

    private void saveImage(String imagePath) {
        pictureChanged = true;
        try {
            file = new FileInputStream(imagePath);
            image = new byte[file.available()];
            file.read(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayText(int i) {
        TextView quantityText = (TextView) findViewById(R.id.text_quantity);
        quantityText.setText("" + i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void saveItem() {
        // Use trim to eliminate leading or trailing white space
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        if (mContentUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                quantity == 0 && img.getDrawable() == null) {
            Toast.makeText(this, R.string.fill_all,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (image == null && mContentUri == null) {
            Toast.makeText(this, R.string.no_picture,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || quantity == 0) {
            Toast.makeText(this, R.string.fill_all,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int price = Integer.parseInt(priceString);
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.NAME, nameString);
        values.put(InventoryEntry.PRICE, price);
        values.put(InventoryEntry.QUANTITY, quantity);
        if (pictureChanged) {
            values.put(InventoryEntry.PICTURE, image);
        }


        try {
            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mContentUri == null) {
            // Insert a new pet into the provider, returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URL, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mContentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, R.string.update_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, R.string.update_done,
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.save:
                saveItem();
                finish();
                return true;

            case R.id.delete:
                showDeleteConfirmationDialog();
                return true;

            case android.R.id.home:

                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_item_dialog);
        builder.setPositiveButton(R.string.delete_item, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the Item.
                deleteItem();
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

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Unsaved changes do you want to continue");
        builder.setPositiveButton("discard", discardButtonClickListener);
        builder.setNegativeButton("Keep me here", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    private void deleteItem() {
        // TODO: Implement this method
        long row = getContentResolver().delete(mContentUri, null, null);
        if (row == 0) {
            Toast.makeText(this, "delete Item didn't success",
                    Toast.LENGTH_SHORT).show();
        } else if (row == 1) {
            Toast.makeText(this, "delete Item success",
                    Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.NAME,
                InventoryEntry.PRICE,
                InventoryEntry.QUANTITY,
                InventoryEntry.PICTURE};
        return new CursorLoader(this,
                mContentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (data.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = data.getColumnIndex(InventoryEntry.NAME);
            int priceColumn = data.getColumnIndex(InventoryEntry.PRICE);
            int quantityColumn = data.getColumnIndex(InventoryEntry.QUANTITY);
            byte[] blob = data.getBlob(data.getColumnIndex(InventoryEntry.PICTURE));
            Bitmap image = BitmapFactory.decodeByteArray(blob, 0, blob.length);

            // Extract out the value from the Cursor for the given column index
            String name = data.getString(nameColumnIndex);
            int price = data.getInt(priceColumn);
            int quantityNum = data.getInt(quantityColumn);
            quantity = quantityNum;
            //int weight = data.getInt(weightColumnIndex);
            // Update the views on the screen with the values from the database
            nameEditText.setText(name);
            img.setImageBitmap(image);
            priceEditText.setText(Integer.toString(price));
            TextView quantityText = (TextView) findViewById(R.id.text_quantity);
            quantityText.setText(Integer.toString(quantityNum));

        }
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        TextView quantityText = (TextView) findViewById(R.id.text_quantity);
        quantityText.setText("0");
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private void order() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "inventory", "sales@yu.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order more ");
        String message = "Please send us more of the this product\n" + "Product name: " + nameEditText.getText() + "\n" +
                "Quantity: ";
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }
}
