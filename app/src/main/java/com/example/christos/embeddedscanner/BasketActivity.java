package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;


public class BasketActivity extends ActionBarActivity {

    private ArrayList<String> basketProducts = new ArrayList<String>();
    private ArrayList<Boolean> thumbs = new ArrayList<Boolean>();
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        Button bttnBestPrice = (Button) findViewById(R.id.bestPriceButton);
        final Context context = this;
        bttnBestPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0; i<thumbs.size(); i++)
                {
                    thumbs.set(i, false);
                    adapter.notifyDataSetChanged();
                }
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
                    if(basketProducts.size()>0)
                    {
                        Intent newIntent = new Intent(getApplicationContext(), PricesActivity.class);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(newIntent);
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Basket is empty", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        FileManipulation.populateListFromFile("basket.txt", basketProducts, getApplicationContext());
        for(int i=0, j=basketProducts.size(); i<j; i++) thumbs.add(false);
        populateListView();
    }

    private void populateListView()
    {
        adapter = new MyListAdapter();
        de.timroes.android.listview.EnhancedListView list = (de.timroes.android.listview.EnhancedListView) findViewById(R.id.basketListView);
        list.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int i) {
                basketProducts.remove(i);
                thumbs.remove(i);
                FileDeleteAsync async = new FileDeleteAsync();
                async.execute(i);
                adapter.notifyDataSetChanged();
                return null;
            }
        });
        list.enableSwipeToDismiss();
        list.setAdapter(adapter);
    }

    private class FileDeleteAsync extends AsyncTask<Integer, String, Void>
    {
        @Override
        protected Void doInBackground(Integer... args) {

            Integer index = args[0];
            FileManipulation.delete("basket.txt", index, 1, getApplicationContext());

            return null;
        }
    }

    private static class ViewHolder
    {
        ImageButton thumbImageButton;
        TextView makeText;
    }

    private class MyListAdapter extends ArrayAdapter<String>
    {
        public MyListAdapter()
        {
            super(BasketActivity.this, R.layout.item_view_2, basketProducts);
        }

        private View itemView;

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            itemView = convertView;
            if(itemView==null)
            {
                itemView = getLayoutInflater().inflate(R.layout.item_view_2, parent, false);
                holder = new ViewHolder();
                holder.thumbImageButton = (ImageButton) itemView.findViewById(R.id.thumbImageButton);
                holder.makeText = (TextView) itemView.findViewById(R.id.productNameTextView);
                itemView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) itemView.getTag();
            }

            //find the string
            String currentString = basketProducts.get(position);
            Boolean currentThumb = thumbs.get(position);

            if(currentThumb==false)
            {
                holder.thumbImageButton.setImageResource(R.drawable.thumboff);
            }
            else
            {
                holder.thumbImageButton.setImageResource(R.drawable.thumbon);
            }
            holder.thumbImageButton.setTag(position);
            final ViewHolder finalHolder = holder;
            holder.thumbImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v)
                {
                   Integer index = (Integer) v.getTag();
                   if(thumbs.get(index)==false)
                   {
                       finalHolder.thumbImageButton.setImageResource(R.drawable.thumbon);
                       thumbs.set(index, !thumbs.get(index));
                       notifyDataSetChanged();
                   }
                    else
                   {
                       finalHolder.thumbImageButton.setImageResource(R.drawable.thumboff);
                       thumbs.set(index, !thumbs.get(index));
                       notifyDataSetChanged();
                   }
                }
            });

            //make
            holder.makeText.setText(currentString);

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favourites, menu);
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
