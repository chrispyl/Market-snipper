package com.example.christos.embeddedscanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileManipulation {

    public static void writeToFile(String writeThis, String fileName, Context context)
    {
        try
        {
            FileOutputStream fou = context.openFileOutput(fileName, context.MODE_APPEND | context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fou);
            try {
                osw.append(writeThis);
                osw.write(System.getProperty("line.separator"));
                osw.flush();
                osw.close();
                fou.close();
            }catch(IOException e)
            {

                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Oops");
                alertDialog.setMessage("Cannot write to file"+fileName);
                alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }});
                alertDialog.show();
            }
        }catch (FileNotFoundException e)
        {

        }
    }

    public static boolean checkIfIn(String s, String fileName, Context context)
    {
        try {
            FileInputStream in = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null)
                {
                    if(line.equals(s))
                    {
                        in.close();
                        inputStreamReader.close();
                        bufferedReader.close();
                        return true;
                    }
                }
            } catch (IOException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Oops");
                alertDialog.setMessage("IOException");
                alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }});
                alertDialog.show();
            }
        } catch (FileNotFoundException e) {

        }

        return false;
    }

    public static void delete(String fileName, int startLine, int numLines, Context context)
    {
        try
        {
            FileInputStream in = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer sb=new StringBuffer("");
            int linenumber=0;
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                if(linenumber<startLine||linenumber>=startLine+numLines)
                    sb.append(line+System.getProperty("line.separator"));
                linenumber++;
            }

            bufferedReader.close();
            inputStreamReader.close();
            in.close();

            FileOutputStream fou = context.openFileOutput(fileName, context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fou);
            osw.write(sb.toString());
            osw.close();
            fou.close();
        }
        catch (Exception e)
        {
            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Oops");
            alertDialog.setMessage("Fail");
            alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }});
            alertDialog.show();
        }
    }

    public static void populateListFromFile(String fileName, ArrayList list, Context context)
    {
        FileInputStream in = null;
        try {
            in = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(in);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            try {
                while ((line = bufferedReader.readLine()) != null)
                {
                    list.add(line);
                }

                bufferedReader.close();
                inputStreamReader.close();
                in.close();

            } catch (IOException e) {
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Oops");
                alertDialog.setMessage("IOException in populate");
                alertDialog.setButton(-1, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }});
                alertDialog.show();
            }
        } catch (FileNotFoundException e) {

        }
    }
}
