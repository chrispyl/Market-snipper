package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class PricesActivity extends ActionBarActivity {

    private String barcode;
    private DatabaseHelper dHelper;
    private ArrayList<String> markets;
    private ArrayList<String> productsInBasket;
    private ArrayList<ArrayList<String>> missingProducts;
    private ArrayList<ArrayList<String>> prodEachMarket;
    Cursor cursor;
    String from = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    private ArrayList<Float> prices=new ArrayList<Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        Intent intent= getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle!=null)
        {
            barcode =(String) bundle.get("Barcode");
            from = (String) bundle.get("From");
        }
        if(from.equals("FavouritesActivity"))
        {
            String selectedProduct = (String) bundle.get("SelectedItem");
            dHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dHelper.getWritableDatabase();
            String selectMarketsQuery = "select m_name, price " +
                                        "from product, sold " +
                                        "where code=prod_code and prod_name='"+selectedProduct+
                                        "' order by price";
            cursor = db.rawQuery(selectMarketsQuery, null);
            while (cursor.moveToNext())
            {
                prices.add(cursor.getFloat(1));
            }
        }
        else if(from.equals("MainActivity"))
        {
            dHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dHelper.getWritableDatabase();
            String selectMarketsQuery = "select m_name, price " +
                                        "from sold " +
                                        "where prod_code=" + barcode + " and (m_name='Μαρινόπουλος' or m_name='Μασούτης' or m_name='Lidl' or m_name='Βασιλόπουλος' or m_name='Κυλικείο')" +
                                        " order by price";
            cursor = db.rawQuery(selectMarketsQuery, null);
            while (cursor.moveToNext()) {
                prices.add(cursor.getFloat(1));
            }
        }
        else if(from.equals("BasketActivity"))
        {
            missingProducts = new ArrayList<ArrayList<String>>(); //0 lidl, 1 masoutis, 2 marin, 3 basil, 4 κυλικειο
            prodEachMarket = new ArrayList<ArrayList<String>>();
            productsInBasket = FileManipulation.getArrayListFromFile("basket.txt", getApplicationContext());

            ArrayList<Float> pricesSum = new ArrayList<Float>();

            for(int i=0; i<5; i++)
            {
                missingProducts.add(new ArrayList<String>());
                prodEachMarket.add(new ArrayList<String>());
                pricesSum.add(0f);
            }

            markets = new ArrayList<String>();
            markets.add("Lidl");
            markets.add("Μασούτης");
            markets.add("Μαρινόπουλος");
            markets.add("Βασιλόπουλος");
            markets.add("Κυλικείο");

            dHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dHelper.getWritableDatabase();
            String selectMarketsQuery = "select m_name, price, prod_name " +
                                        "from product, sold " +
                                        "where prod_code=code ";
            cursor = db.rawQuery(selectMarketsQuery, null);
            while (cursor.moveToNext()) {
                if(cursor.getString(0).equals("Lidl"))
                {
                    if(productsInBasket.contains(cursor.getString(2)))
                    {
                        pricesSum.set(0, pricesSum.get(0)+cursor.getFloat(1));
                        prodEachMarket.get(0).add(cursor.getString(2));
                    }
                }
                else if(cursor.getString(0).equals("Μασούτης"))
                {
                    if(productsInBasket.contains(cursor.getString(2)))
                    {
                        pricesSum.set(1, pricesSum.get(1)+cursor.getFloat(1));
                        prodEachMarket.get(1).add(cursor.getString(2));
                    }
                }
                else if(cursor.getString(0).equals("Μαρινόπουλος"))
                {
                    if(productsInBasket.contains(cursor.getString(2)))
                    {
                        pricesSum.set(2, pricesSum.get(2)+cursor.getFloat(1));
                        prodEachMarket.get(2).add(cursor.getString(2));
                    }
                }
                else if(cursor.getString(0).equals("Βασιλόπουλος"))
                {
                    if(productsInBasket.contains(cursor.getString(2)))
                    {
                        pricesSum.set(3, pricesSum.get(3) + cursor.getFloat(1));
                        prodEachMarket.get(3).add(cursor.getString(2));
                    }
                }
                else if(cursor.getString(0).equals("Κυλικείο"))
                {
                    if(productsInBasket.contains(cursor.getString(2)))
                    {
                        pricesSum.set(4, pricesSum.get(4) + cursor.getFloat(1));
                        prodEachMarket.get(4).add(cursor.getString(2));
                    }
                }
            }

            for(int i=0; i<5; i++)
            {
                for(int j=0; j<productsInBasket.size(); j++)
                {
                    if(!prodEachMarket.get(i).contains(productsInBasket.get(j)))
                    {
                        missingProducts.get(i).add(productsInBasket.get(j));
                    }
                }
            }

            for(int i=pricesSum.size()-1; i>=0; i--)
            {
                if(pricesSum.get(i)==0)
                {
                    pricesSum.remove(i);
                    markets.remove(i);
                }
            }

            OptimizedBubblesort.sortPricesAndMarkets(pricesSum, markets);
            for(int i=0; i<pricesSum.size(); i++)
            {
                prices.add(pricesSum.get(i));
            }

        }
        //
        //prices.add(2.49f);
        //prices.add(2.99f);
        //prices.add(3.99f);
        //prices.add(4.99f);
        populateListView();
    }

    ListView list;
    private void populateListView()
    {
        list = (ListView) findViewById(R.id.pricesListView);
        ArrayAdapter<Float> adapter = new MyListAdapter();
        list.setAdapter(adapter);
        if(from.equals("BasketActivity"))
        {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    list.setItemChecked(position, true);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(PricesActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View convertView = (View) inflater.inflate(R.layout.custom, null);
                    alertDialog.setView(convertView);
                    alertDialog.setTitle("Products missing");
                    ListView lv = (ListView) convertView.findViewById(R.id.missingProdListView);
                    ArrayAdapter<String> adapter=null;
                    boolean show=false;
                    if(markets.get(position).equals("Lidl"))
                    {
                        adapter = new ArrayAdapter<String>(PricesActivity.this, android.R.layout.simple_list_item_1, missingProducts.get(0));
                        if(missingProducts.get(0).size()>0) show=true;
                    }
                    else if(markets.get(position).equals("Μασούτης"))
                    {
                        adapter = new ArrayAdapter<String>(PricesActivity.this, android.R.layout.simple_list_item_1, missingProducts.get(1));
                        if(missingProducts.get(1).size()>0) show=true;
                    }
                    else if(markets.get(position).equals("Μαρινόπουλος"))
                    {
                        adapter = new ArrayAdapter<String>(PricesActivity.this, android.R.layout.simple_list_item_1, missingProducts.get(2));
                        if(missingProducts.get(2).size()>0) show=true;
                    }
                    else if(markets.get(position).equals("Βασιλόπουλος"))
                    {
                        adapter = new ArrayAdapter<String>(PricesActivity.this, android.R.layout.simple_list_item_1, missingProducts.get(3));
                        if(missingProducts.get(3).size()>0) show=true;
                    }
                    else if(markets.get(position).equals("Κυλικείο"))
                    {
                        adapter = new ArrayAdapter<String>(PricesActivity.this, android.R.layout.simple_list_item_1, missingProducts.get(4));
                        if(missingProducts.get(4).size()>0) show=true;
                    }
                    lv.setAdapter(adapter);
                    if(show) alertDialog.show();
                }
            });
        }
    }

    private static class ViewHolder
    {
        ImageView imageView;
        TextView textView;
    }

    private class MyListAdapter extends ArrayAdapter<Float>
    {
        public MyListAdapter()
        {
            super(PricesActivity.this, R.layout.item_view_3, prices);
        }

        private View itemView;
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            itemView = convertView;
            if(itemView==null)
            {
                itemView = getLayoutInflater().inflate(R.layout.item_view_3, parent, false);
                holder = new ViewHolder();
                holder.imageView = (ImageView) itemView.findViewById(R.id.imageView);
                holder.textView = (TextView) itemView.findViewById(R.id.priceTextView);
                itemView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) itemView.getTag();
            }

            //find the string
            Float currentPrice = prices.get(position);

            if(from.equals("BasketActivity"))
            {
                if(markets.get(position).equals("Lidl"))
                {
                    holder.imageView.setImageResource(R.drawable.lidl);
                    if(missingProducts.get(0).size()>0) itemView.setBackgroundColor(Color.rgb(255, 115, 115));
                }
                else if(markets.get(position).equals("Μασούτης"))
                {
                    holder.imageView.setImageResource(R.drawable.masoutis);
                    if(missingProducts.get(1).size()>0) itemView.setBackgroundColor(Color.rgb(255, 115, 115));
                }
                else if(markets.get(position).equals("Μαρινόπουλος"))
                {
                    holder.imageView.setImageResource(R.drawable.carrefour);
                    if(missingProducts.get(2).size()>0) itemView.setBackgroundColor(Color.rgb(255, 115, 115));
                }
                else if(markets.get(position).equals("Βασιλόπουλος"))
                {
                    holder.imageView.setImageResource(R.drawable.basilopoulos);
                    if(missingProducts.get(3).size()>0) itemView.setBackgroundColor(Color.rgb(255, 115, 115));
                }
                else if(markets.get(position).equals("Κυλικείο"))
                {
                    holder.imageView.setImageResource(R.drawable.kulikeio);
                    if(missingProducts.get(4).size()>0) itemView.setBackgroundColor(Color.rgb(255, 115, 115));
                }

            }
            else
            {
                if (cursor.isAfterLast()) cursor.moveToFirst();
                if (cursor.getString(0).equals("Lidl")) {
                    holder.imageView.setImageResource(R.drawable.lidl);
                } else if (cursor.getString(0).equals("Μασούτης")) {
                    holder.imageView.setImageResource(R.drawable.masoutis);
                } else if (cursor.getString(0).equals("Μαρινόπουλος")) {
                    holder.imageView.setImageResource(R.drawable.carrefour);
                } else if (cursor.getString(0).equals("Βασιλόπουλος")) {
                    holder.imageView.setImageResource(R.drawable.basilopoulos);
                } else if (cursor.getString(0).equals("Κυλικείο")) {
                    holder.imageView.setImageResource(R.drawable.kulikeio);
                }
                cursor.moveToNext();
            }

            holder.imageView.setTag(position);

            holder.textView.setText(Float.toString(currentPrice));

            return itemView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_prices, menu);
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
