package com.example.christos.embeddedscanner;

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

import java.util.ArrayList;


public class PricesActivity extends ActionBarActivity {

    private ArrayList<Float> prices=new ArrayList<Float>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prices);

        prices.add(2.49f);
        prices.add(2.99f);
        prices.add(3.99f);
        prices.add(4.99f);
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

            //fill the view
            if(position==0)
            {
                holder.imageView.setImageResource(R.drawable.lidl);
            }
            else if(position==1)
            {
                holder.imageView.setImageResource(R.drawable.carrefour);
            }
            else if(position==2)
            {
                holder.imageView.setImageResource(R.drawable.masoutis);
            }
            else
            {
                holder.imageView.setImageResource(R.drawable.basilopoulos);
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
