package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
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
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;


public class FavouritesActivity extends ActionBarActivity {

    private ArrayList<String> favouriteProducts = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        FileManipulation.populateListFromFile("favourites.txt", favouriteProducts, getApplicationContext());
        populateListView();
    }

    private void populateListView()
    {
        adapter = new MyListAdapter();
        de.timroes.android.listview.EnhancedListView list = (de.timroes.android.listview.EnhancedListView) findViewById(R.id.favouritesListView);
        list.setDismissCallback(new EnhancedListView.OnDismissCallback() {
            @Override
            public EnhancedListView.Undoable onDismiss(EnhancedListView enhancedListView, int i) {
                favouriteProducts.remove(i);
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
            FileManipulation.delete("favourites.txt", index, 1, getApplicationContext());

            return null;
        }
    }

    private static class ViewHolder
    {
        ImageButton imageButton;
        TextView makeText;
    }

    private class MyListAdapter extends ArrayAdapter<String>
    {
        public MyListAdapter()
        {
            super(FavouritesActivity.this, R.layout.item_view, favouriteProducts);
        }

        private View itemView;
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            itemView = convertView;
            if(itemView==null)
            {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);
                holder = new ViewHolder();
                holder.imageButton = (ImageButton) itemView.findViewById(R.id.imageButton);
                holder.makeText = (TextView) itemView.findViewById(R.id.productNameTextView);
                itemView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) itemView.getTag();
            }

            //find the string
            String currentString = favouriteProducts.get(position);

            //fill the view
            holder.imageButton.setTag(position);
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if(InternetConnectivity.checkInternet(getApplicationContext())==false)  //check for internet connection
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Oops");
                        alertDialog.setMessage("No internet connection"+"\n"+"Cannot connect to database");
                        alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }});
                        alertDialog.show();
                    }
                    else
                    {
                        Intent newIntent = new Intent(getContext(), PricesActivity.class);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(newIntent);
                    }

                    //communicate with database
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
