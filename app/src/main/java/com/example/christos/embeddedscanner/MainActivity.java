package com.example.christos.embeddedscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends ActionBarActivity {

    private TextView contentTxt;
    private EditText nameEditText;
    private EditText priceEditText;
    private ImageButton bttnAddToFavourites;
    private ImageButton bttnAddToBasket;
    private Button bttnSubmit;
    private DatabaseHelper dHelper;
    private String barcode;
    private Spinner spinner;
    private boolean barcodeExist=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        contentTxt = (TextView) findViewById(R.id.contentTextView);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        priceEditText = (EditText) findViewById(R.id.priceEditText);
        bttnAddToFavourites = (ImageButton) findViewById(R.id.bttnAddToFavourites);
        bttnAddToBasket = (ImageButton) findViewById(R.id.bttnAddToBasket);
        bttnSubmit = (Button) findViewById(R.id.bttnSubmit);

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null)
        {
            barcode =(String) bundle.get("Barcode_content");

            contentTxt.setText(barcode);
        }

        String barcodeExistQuery="select * from product where code="+barcode;
        dHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery(barcodeExistQuery, null);
        String productName=null;
        while(cursor.moveToNext())
        {
            productName=cursor.getString(0);
        }
        cursor.close();
        if(productName!=null)
        {
            nameEditText.setText(productName);
            nameEditText.setFocusable(false);
            barcodeExist=true;
        }
        else
        {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Unknown");
            alertDialog.setMessage("New product");
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }});
            alertDialog.show();
        }

        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.market_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt("Select Market");
        spinner.setAdapter(adapter);
        spinner.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapter,
                        R.layout.contact_spinner_row_nothing_selected,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        this));

        bttnAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName = nameEditText.getText().toString().trim();
                if (FileManipulation.checkIfIn(productName, "favourites.txt", getApplicationContext()) == false) {
                    if (productName.matches(".*\\w.*")) {
                        FileWriteAsync async = new FileWriteAsync();
                        async.execute(productName, "favourites.txt");
                        Toast.makeText(getBaseContext(), "Saved to favourites", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getBaseContext(), "Please name the product", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getBaseContext(), "Product already in favourites", Toast.LENGTH_SHORT).show();
                }

            }
        });

        bttnAddToBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String productName=nameEditText.getText().toString().trim();
                if(FileManipulation.checkIfIn(productName, "basket.txt", getApplicationContext())==false)
                {
                    if(productName.matches(".*\\w.*"))
                    {
                        FileWriteAsync async = new FileWriteAsync();
                        async.execute(productName, "basket.txt");
                        Toast.makeText(getBaseContext(), "Saved to basket", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Please name the product", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Product already in basket", Toast.LENGTH_SHORT).show();
                }
            }
        });

        bttnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                    boolean dotInSide=false;
                    String priceString=priceEditText.getText().toString();
                    if(priceString.length()>0)
                    {
                        if (priceString.charAt(0) == '.' || priceString.charAt(priceString.length() - 1) == '.') {
                            dotInSide = true;
                        }

                        if (dotInSide == true) {
                            Toast.makeText(getBaseContext(), "Wrong number format", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if(barcodeExist==false)
                            {
                                insertNewProduct();
                            }

                            if(checkIfProductInMarket())
                            {
                                updateDB();
                            }
                            else
                            {
                                insertPriceToDB();
                            }

                            Intent newIntent = new Intent(getApplicationContext(), PricesActivity.class);
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            newIntent.putExtra("Barcode", barcode);
                            newIntent.putExtra("From", "MainActivity");
                            startActivity(newIntent);
                            finish();
                        }
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Insert price", Toast.LENGTH_SHORT).show();
                    }
                }
        });
       }

    public boolean checkIfProductInMarket()
    {
        SQLiteDatabase db = dHelper.getWritableDatabase();
        String checkQuery = "select prod_name, m_name " +
                            "from sold, product " +
                            "where prod_name='"+nameEditText.getText().toString().trim()+"' and prod_code="+barcode+" and m_name='"+spinner.getSelectedItem().toString().trim()+"'";
        Cursor cursor = db.rawQuery(checkQuery, null);

        if(cursor.getCount()>0)
        {
            cursor.close();
            return true;
        }
        else
        {
            cursor.close();
            return false;
        }
    }

    public void insertNewProduct()
    {
        SQLiteDatabase db = dHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("prod_name", nameEditText.getText().toString().trim());
        contentValues.put("code", barcode);
        db.insert("product", null, contentValues);
    }

    public void insertPriceToDB()
    {
        SQLiteDatabase db = dHelper.getWritableDatabase();
        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("prod_code", barcode);
        contentValues2.put("m_name", spinner.getSelectedItem().toString().trim());
        contentValues2.put("price", Float.parseFloat(priceEditText.getText().toString().trim()));
        db.insert("sold", null, contentValues2);
        //String insert1 = "insert into product values("+nameEditText.getText().toString().trim()+", "+barcode+")";
        //String insert2 = "insert into sold values("+nameEditText.getText().toString().trim()+", "+ spinner.getSelectedItem().toString() +", "+priceEditText.getText().toString().trim()+")";
        //db.execSQL(insert1);
        //db.execSQL(insert2);
    }

    public void updateDB()
    {
        SQLiteDatabase db = dHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("price", Float.parseFloat(priceEditText.getText().toString().trim()));
        String[] args = new String[]{barcode, spinner.getSelectedItem().toString().trim()};
        db.update("sold", contentValues, "prod_code =? and m_name =?", args);
        //String updatePriceQuery="update sold set price="+priceEditText.getText().toString().trim()+" where prod_code="+barcode+" and m_name="+nameEditText.getText().toString().trim();
        //db.execSQL(updatePriceQuery);
    }

    private class FileWriteAsync extends AsyncTask<String, String, Void>
    {
        @Override
        protected Void doInBackground(String... args) {
        //args[0] to eiserxomeno string, args[1] to onoma tou arxeiou
            FileManipulation.writeToFile(args[0], args[1], getApplicationContext());
            ArrayList<String> sortedList = new ArrayList<String>(FileManipulation.getArrayListFromFile(args[1], getApplicationContext()));
            Collections.sort(sortedList);
            FileManipulation.writeMany(sortedList, args[1], getApplicationContext());

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
