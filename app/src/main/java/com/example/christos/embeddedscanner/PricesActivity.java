package com.example.christos.embeddedscanner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class PricesActivity extends ActionBarActivity {

    private String barcode;
    private DatabaseHelper dHelper;
    private ArrayList<String> markets;
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
                                        "where prod_code=" + barcode + " and (m_name='Μαρινόπουλος' or m_name='Μασούτης' or m_name='Lidl' or m_name='Βασιλόπουλος')" +
                                        " order by price";
            cursor = db.rawQuery(selectMarketsQuery, null);
            while (cursor.moveToNext()) {
                prices.add(cursor.getFloat(1));
            }
        }
        else
        {
            ArrayList<Float> pricesSum = new ArrayList<Float>();
            markets = new ArrayList<String>();
            markets.add("Lidl");
            markets.add("Μασούτης");
            markets.add("Μαρινόπουλος");
            markets.add("Βασιλόπουλος");
            for(int i=0; i<4; i++) pricesSum.add(0f); //to 4 apo ta tessera market

            dHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dHelper.getWritableDatabase();
            String selectMarketsQuery = "select m_name, price, prod_name " +
                                        "from product, sold " +
                                        "where prod_code=code " +
                                        "group by m_name";
            cursor = db.rawQuery(selectMarketsQuery, null);
            while (cursor.moveToNext()) {
                if(cursor.getString(0).equals("Lidl"))
                {
                    pricesSum.set(0, pricesSum.get(0)+cursor.getFloat(1));
                }
                else if(cursor.getString(0).equals("Μασούτης"))
                {
                    pricesSum.set(1, pricesSum.get(1)+cursor.getFloat(1));
                }
                else if(cursor.getString(0).equals("Μαρινόπουλος"))
                {
                    pricesSum.set(2, pricesSum.get(2)+cursor.getFloat(1));
                }
                else if(cursor.getString(0).equals("Βασιλόπουλος"))
                {
                    pricesSum.set(3, pricesSum.get(3)+cursor.getFloat(1));
                }
            }

            for(int i=pricesSum.size()-1; i>=0; i--)
            {
                if(pricesSum.get(i)==0f)
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

    private void populateListView()
    {
        ArrayAdapter<Float> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.pricesListView);
        list.setAdapter(adapter);
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
                }
                else if(markets.get(position).equals("Μαρινόπουλος"))
                {
                    holder.imageView.setImageResource(R.drawable.carrefour);
                }
                else if(markets.get(position).equals("Μασούτης"))
                {
                    holder.imageView.setImageResource(R.drawable.masoutis);
                }
                else if(markets.get(position).equals("Βασιλόπουλος"))
                {
                    holder.imageView.setImageResource(R.drawable.basilopoulos);
                }
            }
            else
            {
                if (cursor.isAfterLast()) cursor.moveToFirst();
                if (cursor.getString(0).equals("Lidl")) {
                    holder.imageView.setImageResource(R.drawable.lidl);
                } else if (cursor.getString(0).equals("Μαρινόπουλος")) {
                    holder.imageView.setImageResource(R.drawable.carrefour);
                } else if (cursor.getString(0).equals("Μασούτης")) {
                    holder.imageView.setImageResource(R.drawable.masoutis);
                } else if (cursor.getString(0).equals("Βασιλόπουλος")) {
                    holder.imageView.setImageResource(R.drawable.basilopoulos);
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
