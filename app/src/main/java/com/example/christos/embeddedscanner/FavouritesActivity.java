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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import de.timroes.android.listview.EnhancedListView;


public class FavouritesActivity extends ActionBarActivity {

    private ArrayList<String> favouriteProducts = new ArrayList<String>();
    private ArrayList<Boolean> existInBasket = new ArrayList<Boolean>();
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        FileManipulation.populateListFromFile("favourites.txt", favouriteProducts, getApplicationContext());
        for(int i=0, j=favouriteProducts.size(); i<j; i++) existInBasket.add(false); //den exei shmasia to false tha allaksei mesa sthn populate listview analoga me thn eikona
        populateListView();
    }

    private de.timroes.android.listview.EnhancedListView list;
    private void populateListView()
    {
        adapter = new MyListAdapter();
        list = (de.timroes.android.listview.EnhancedListView) findViewById(R.id.favouritesListView);
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

        final Context context = this;
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.setItemChecked(position, true);
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
                    Intent newIntent = new Intent(context, PricesActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(newIntent);
                }
            }
        });

        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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

            String currentString = favouriteProducts.get(position);

            if(FileManipulation.checkIfIn(FileManipulation.getStringFromPosition("favourites.txt", position, getContext()), "basket.txt", getApplicationContext())==false)
            {
                holder.imageButton.setImageResource(R.drawable.basket);
                existInBasket.set(position, false);
            }
            else
            {
                holder.imageButton.setImageResource(R.drawable.basket_tick);
                existInBasket.set(position, true);
            }

            holder.imageButton.setTag(position);
            final int stringPosition=position;
            final int viewPosition=position;
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    String productName = FileManipulation.getStringFromPosition("favourites.txt", stringPosition, getContext());
                    if(existInBasket.get(viewPosition)==false)
                    {
                        if (FileManipulation.checkIfIn(productName, "basket.txt", getApplicationContext()) == false) {
                            FileManipulation.writeToFile(productName, "basket.txt", getApplicationContext());
                            ArrayList<String> sortedList = new ArrayList<String>(FileManipulation.getArrayListFromFile("basket.txt", getApplicationContext()));
                            Collections.sort(sortedList);
                            FileManipulation.writeMany(sortedList, "basket.txt", getApplicationContext());
                            Toast.makeText(getBaseContext(), "Saved to basket", Toast.LENGTH_SHORT).show();
                            adapter.notifyDataSetChanged(); //to notifyAdapterChanged ginetai sto telos autou giati edw prepei na ginetai elegxos afou teliwsei auth h diadikasia gia na mpainei to tick
                        } else {
                            Toast.makeText(getBaseContext(), "Product already in basket", Toast.LENGTH_SHORT).show();
                        }
                        existInBasket.set(viewPosition, true);
                    }
                    else
                    {
                        FileManipulation.deleteByName("basket.txt", productName, getContext());
                        existInBasket.set(viewPosition, false);
                        Toast.makeText(getBaseContext(), "Product removed from basket", Toast.LENGTH_SHORT).show();
                        notifyDataSetChanged();
                    }
                }
            });

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
