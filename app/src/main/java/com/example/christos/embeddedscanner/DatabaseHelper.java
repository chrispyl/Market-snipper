package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{

    Context context;
    private static final String DATABASE_NAME = "database";
    private static final int DATABASE_VERSION = 1;

    private static final String PRODUCT_TABLE = "product";
    private static final String PRODUCT_CODE = "code";
    private static final String PRODUCT_NAME = "prod_name";

    private static final String MARKET_TABLE = "market";
    private static final String MARKET_NAME = "market_name";
    private static final String MARKET_ADDRESS = "address";

    private static final String SOLD_TABLE = "sold";
    private static final String SOLD_PROD_CODE = "prod_code";
    private static final String SOLD_M_NAME = "m_name";
    private static final String SOLD_PRICE = "price";

    private static final String CREATE_PRODUCT_TABLE = "create table " + PRODUCT_TABLE + "("
            + PRODUCT_NAME + " varchar (20) not null, "
            + PRODUCT_CODE + " varchar (12) not null, "
            + "primary key (" + PRODUCT_CODE + ")" +
            ");";

    private static final String CREATE_MARKET_TABLE = "create table " + MARKET_TABLE + "("
            + MARKET_NAME + " varchar (10) not null, "
            + MARKET_ADDRESS + " varchar (20) not null, "
            + "primary key (" + MARKET_NAME + ")" +
            ");";

    private static final String CREATE_SOLD_TABLE = "create table " + SOLD_TABLE + "("
            + SOLD_PROD_CODE + " varchar (12) not null, "
            + SOLD_M_NAME + " varchar (10) not null, "
            + SOLD_PRICE + " real, "
            + "foreign key (" + SOLD_PROD_CODE + ")" + " references " + PRODUCT_TABLE + " (" + PRODUCT_CODE + "), "
            + "foreign key (" + SOLD_M_NAME + ")" + " references " + MARKET_TABLE + " (" + MARKET_NAME + "), "
            + "primary key (" + SOLD_PROD_CODE + ", " + SOLD_M_NAME + ")" +
            ");";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        context = this.context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

        try {
            db.execSQL(CREATE_PRODUCT_TABLE);
            db.execSQL(CREATE_MARKET_TABLE);
            db.execSQL(CREATE_SOLD_TABLE);
        }catch (SQLException e)
        {
            //problem creating database
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Oops");
            alertDialog.setMessage("Problem creating database");
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }});
            alertDialog.show();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
