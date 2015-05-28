package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.timroes.android.listview.EnhancedListView;


public class BasketActivity extends ActionBarActivity {

    private ArrayList<String> basketProducts = new ArrayList<String>();
    private ArrayList<Boolean> checked = new ArrayList<Boolean>();  //ta ticks diathrountai sto orientation change mesa ap to manifest file opou to activity den ginetai destroy
    //private ArrayList<Boolean> thumbs = new ArrayList<Boolean>();
    ArrayAdapter<String> adapter;
    Button bttnBestPrice;
    Button bttnDelete;
    CheckBox chSelectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        bttnBestPrice = (Button) findViewById(R.id.bestPriceButton);
        bttnDelete = (Button) findViewById(R.id.deleteButton);
        chSelectAll = (CheckBox) findViewById(R.id.selectAllCheckBox);

        bttnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean atLeastOneChecked=false;
                for(int i=0, j=checked.size(); i<j; i++)
                {
                    if(checked.get(i)==true)
                    {
                        atLeastOneChecked = true;
                        break;
                    }
                }

                if(atLeastOneChecked==true)
                {
                    ArrayList<Boolean> checkedCopy = new ArrayList<Boolean>(checked); //logw parallhlou thread mporei na prokupsei provlima an peirazoun tin idia lista me to epomeno loop
                    FileDeleteMultAsync async = new FileDeleteMultAsync();            //opote dhmiourw antigrafo kai peirazw auto sto allo thread
                    async.execute(checkedCopy);

                    for (int i = checked.size() - 1; i >= 0; i--)
                    {
                        if (checked.get(i) == true)
                        {
                            checked.remove(i);
                            basketProducts.remove(i);
                        }
                    }

                    adapter.notifyDataSetChanged();
                }
            }
        });

        chSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(chSelectAll.isChecked())
                {
                    for (int i = 0, j = checked.size(); i < j; i++) checked.set(i, true);
                }
                else
                {
                    for (int i = 0, j = checked.size(); i < j; i++) checked.set(i, false);
                }
                adapter.notifyDataSetChanged();
            }
        });

        final Context context = this;
        bttnBestPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*for(int i=0; i<thumbs.size(); i++)
                {
                    thumbs.set(i, false);
                    adapter.notifyDataSetChanged();
                }*/
                    if(basketProducts.size()>0)
                    {
                        Intent newIntent = new Intent(getApplicationContext(), PricesActivity.class);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        newIntent.putExtra("From", "BasketActivity");
                        startActivity(newIntent);
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Basket is empty", Toast.LENGTH_SHORT).show();
                    }
                }
        });


        FileManipulation.populateListFromFile("basket.txt", basketProducts, getApplicationContext());
        for(int i=0, j=basketProducts.size(); i<j; i++) checked.add(false);                                 //SOS
        //for(int i=0, j=basketProducts.size(); i<j; i++) thumbs.add(false);
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
                checked.remove(i);
                //thumbs.remove(i);
                FileDeleteAsync async = new FileDeleteAsync();
                async.execute(i);
                adapter.notifyDataSetChanged();
                return null;
            }
        });
        list.enableSwipeToDismiss();
        list.setAdapter(adapter);
    }

    private class FileDeleteMultAsync extends AsyncTask<ArrayList<Boolean>, String, Void>
    {
        @Override
        protected Void doInBackground(ArrayList<Boolean>... args) {
            FileManipulation.deleteMultiple(args[0], "basket.txt", getApplicationContext());

            return null;
        }
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
        //ImageButton thumbImageButton;
        TextView makeText;
        CheckBox checkBox;
    }

    private class MyListAdapter extends ArrayAdapter<String>
    {
        public MyListAdapter()
        {
            super(BasketActivity.this, R.layout.item_view_2, basketProducts);
        }

        private View itemView;

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            ViewHolder holder;
            itemView = convertView;

            if(itemView==null)
            {
                itemView = getLayoutInflater().inflate(R.layout.item_view_2, parent, false);
                holder = new ViewHolder();
                //holder.thumbImageButton = (ImageButton) itemView.findViewById(R.id.thumbImageButton);
                holder.makeText = (TextView) itemView.findViewById(R.id.productNameTextView);
                holder.checkBox = (CheckBox) itemView.findViewById(R.id.checkBox);
                itemView.setTag(holder);
                //itemView.setBackgroundColor(Color.rgb(245, 245, 245));
            }
            else
            {
                holder = (ViewHolder) itemView.getTag();
            }

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checked.set(position, !checked.get(position));
                    notifyDataSetChanged();
                }
            });

            holder.checkBox.setChecked(checked.get(position));
            holder.checkBox.setTag(position);

            //find the string
            String currentString = basketProducts.get(position);
            /*Boolean currentThumb = thumbs.get(position);

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
            });*/

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
