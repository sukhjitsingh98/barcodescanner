package com.example.sukhjitbadwal.barcodescanner;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sukhjitbadwal.barcodescanner.adapter.MyArrayAdapter;
import com.example.sukhjitbadwal.barcodescanner.model.MyDataModel;
import com.example.sukhjitbadwal.barcodescanner.parser.JSONParser;
import com.example.sukhjitbadwal.barcodescanner.util.InternetConnection;
import com.example.sukhjitbadwal.barcodescanner.util.Keys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private ArrayList<MyDataModel> list; //ArrayList to hold the data received from the external database (Google Sheets)
    private ListView listView; //ListView to display the ArrayList
    private MyArrayAdapter adapter; //A MyArrayAdapter object

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Default settings
        setContentView(R.layout.activity_list); //Default settings

        list = new ArrayList<>(); //ArrayList list is initialized
        adapter = new MyArrayAdapter(this, list); //Binds the 'list' to the 'adapter'

        listView = (ListView) findViewById(R.id.listView); //Assigns the xml value of listView
        listView.setAdapter(adapter); //Sets the listView to the MyArrayAdapter object

        //Implementation of the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(findViewById(R.id.parentLayout), list.get(position).getName() + " => " + list.get(position).getQuantity(), Snackbar.LENGTH_LONG).show();
            }
        });

        //Implementation of the 'fab' (Download) button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View view) {
                //Checks to see if there is internet connection
                if (InternetConnection.checkConnection(getApplicationContext())) {
                    //Calls the GetDataTask() method to retrieve the list of items
                    new GetDataTask().execute();
                } else {
                    //Else displays an error message
                    Snackbar.make(view, "Internet Connection Not Available", Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }

    //Gets data from the database through an appscript webapp which processes and formats the data before sending it to the user
    class GetDataTask extends AsyncTask<Void, Void, Void> {

        ProgressDialog dialog;
        int jIndex;
        int x;

        @Override
        //Progress dialogue
        protected void onPreExecute() {
            super.onPreExecute();

            x = list.size();

            if (x == 0)
                jIndex = 0;
            else
                jIndex = x;

            dialog = new ProgressDialog(ListActivity.this);
            dialog.setTitle("Hey Wait Please..." + x);
            dialog.setMessage("I am getting your data"); //Change to getting your data
            dialog.show();
        }

        @Nullable
        @Override
        protected Void doInBackground(Void... params) {

            //Uses okHttp library to obtain the data
            JSONObject jsonObject = JSONParser.getDataFromWeb();

            try {
               //Check if the data is null
                if (jsonObject != null) {
                    //Check the data length
                    if (jsonObject.length() > 0) {

                        //Getting an array named contacts (Sheet name in Google Sheets, in this case "Sheet 1")
                        JSONArray array = jsonObject.getJSONArray(Keys.KEY_CONTACTS);

                        //Check the array length
                        int lenArray = array.length();
                        if (lenArray > 0) {
                            for (; jIndex < lenArray; jIndex++) {

                                MyDataModel model = new MyDataModel();

                                //Assigns the data to variables
                                JSONObject innerObject = array.getJSONObject(jIndex); //Row being evaluated
                                String name = innerObject.getString(Keys.KEY_NAME); //Name of item
                                String quantity = innerObject.getString(Keys.KEY_QUANTITY); //Quantity of item

                                model.setName(name); //Sends name to 'MyDataModel' class
                                model.setQuantity(quantity); //Sends quantity to 'MyDataModel' class

                                list.add(model); //Formats and adds the data to the final list
                            }
                        }
                    }
                } else {
                    //Else do nothing
                }
            } catch (JSONException je) {
                Log.i(JSONParser.TAG, "" + je.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
            //If size of the array is greater than zero, then update the list
            if(list.size() > 0) {
                adapter.notifyDataSetChanged();
            } else {
                //Else send error message
                Snackbar.make(findViewById(R.id.parentLayout), "No Data Found", Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
