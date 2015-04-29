package com.example.christos.embeddedscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentTxt = (TextView) findViewById(R.id.contentTextView);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        priceEditText = (EditText) findViewById(R.id.priceEditText);
        bttnAddToFavourites = (ImageButton) findViewById(R.id.bttnAddToFavourites);
        bttnAddToBasket = (ImageButton) findViewById(R.id.bttnAddToBasket);
        bttnSubmit = (Button) findViewById(R.id.bttnSubmit);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
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
                String productName=nameEditText.getText().toString().trim();
                if(FileManipulation.checkIfIn(productName, "favourites.txt", getApplicationContext())==false)
                {
                    if(productName.matches(".*\\w.*"))
                    {
                        FileWriteAsync async = new FileWriteAsync();
                        async.execute(productName, "favourites.txt");
                        Toast.makeText(getBaseContext(), "Saved to favourites", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Please name the product", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
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

        final Context context = this;
        bttnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(InternetConnectivity.checkInternet(getApplicationContext())==false)  //check for internet connection
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("Oops");
                    alertDialog.setMessage("No internet connection"+"\n"+"Cannot connect to database");
                    alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }});
                    alertDialog.show();
                }
                else
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
                            Intent newIntent = new Intent(getApplicationContext(), PricesActivity.class);
                            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(newIntent);
                            finish();
                            //communicate with database
                        }
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Insert price", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle!=null)
        {
            String s =(String) bundle.get("Barcode_content");
            contentTxt.setText(s);
        }
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
