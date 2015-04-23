package com.example.christos.embeddedscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;


public class MainActivity extends ActionBarActivity {

    private TextView contentTxt;
    private EditText nameEditText;
    private ImageButton bttnAddToFavourites;
    private ImageButton bttnAddToBasket;
    private Button bttnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contentTxt = (TextView) findViewById(R.id.contentTextView);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        bttnAddToFavourites = (ImageButton) findViewById(R.id.bttnAddToFavourites);
        bttnAddToBasket = (ImageButton) findViewById(R.id.bttnAddToBasket);
        bttnSubmit = (Button) findViewById(R.id.bttnSubmit);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.market_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        bttnAddToFavourites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FileManipulation.checkIfIn(nameEditText.getText().toString(), "favourites.txt", getApplicationContext())==false)
                {
                    if(nameEditText.getText().toString().matches(".*\\w.*"))
                    {
                        FileManipulation.writeToFile(nameEditText.getText().toString(), "favourites.txt", getApplicationContext());
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
                if(FileManipulation.checkIfIn(nameEditText.getText().toString(), "basket.txt", getApplicationContext())==false)
                {
                    if(nameEditText.getText().toString().matches(".*\\w.*"))
                    {
                        FileManipulation.writeToFile(nameEditText.getText().toString(), "basket.txt", getApplicationContext());
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
                    Intent newIntent = new Intent(getApplicationContext(), PricesActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newIntent);

                    //communicate with database
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
