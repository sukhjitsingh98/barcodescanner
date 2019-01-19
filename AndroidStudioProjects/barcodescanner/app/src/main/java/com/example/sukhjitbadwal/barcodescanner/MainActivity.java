package com.example.sukhjitbadwal.barcodescanner;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import android.view.View;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    EditText quantity; //EditText representing the quantity of item input by user
    EditText names; //EditText representing the item name input by user

    String name; //Value of 'names' converted into string
    String quant; //Value of 'quantity' converted into string
    String scanned; //Represents the string value of the scanned barcode
    String check1bool = "INPUT"; //Default type of item
    String check3bool = "OLD"; //Default state of the item

    Button scanBtn; //The 'SCAN' button
    Button viewBtn; //The 'VIEW LIST' button

    CheckBox checkBox1; //The 'OUTPUT' checkbox
    CheckBox checkBox3; //The 'NEW' checkbox

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Default settings
        setContentView(R.layout.activity_main); //Default settings
        final Activity activity = this; //Default settings

        scanBtn = (Button) findViewById(R.id.scan_btn); //Assigns the xml value of 'SCAN' button
        quantity   = (EditText)findViewById(R.id.editText); //Assigns the xml value of 'ITEM QUANTITY'
        names = (EditText)findViewById(R.id.editText2); //Assigns the xml value of 'ITEM NAME'


        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            //Implementation of the 'SCAN' button using zxing library to scan barcode and retrieve data

                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.PRODUCT_CODE_TYPES); //Looks for BARCODE TYPE of data
                integrator.setPrompt("Scan");
                integrator.setBeepEnabled(false);
                integrator.setCameraId(0);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();
            }
        });

        //Checks to see if 'OUTPUT' checkbox is checked and assigns the value "OUTPUT" to the check1bool string
        checkBox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check1bool="OUTPUT";
            }
        });

        //Checks to see if 'NEW' checkbox is checked and assigns the value "NEW" to the check3bool string
        checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
        checkBox3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check3bool="NEW";
            }
        });

        //Implementation of 'VIEW LIST' button. Changes the view from 'activity_main' to 'activity_list'
        //From here the code in 'ListActivity.java' is implemented
        viewBtn = (Button) findViewById(R.id.viewList_btn);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openActivity();
            }
        });
    }

    //Method called during the implementation of 'VIEW LIST' button
    public void openActivity(){
        //Changes the view from 'activity_main' to 'activity_list'
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    //Method that is executed after an item is scanned. 
    //The scanned data is sent to an external database by calling the 'addItemToSheet' method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            scanned = result.getContents(); //Assigns the scanned data to 'scanned'
            quant = quantity.getText().toString(); //converts the value of 'ITEM QUANTITY' to string and assigns it to 'quant'
            name = names.getText().toString(); //converts the value of 'ITEM NAME' to string and assigns it to 'name'

            if (scanned != null) {
                addItemToSheet(); //If there is a scanned value, then execute the method for the next step
            } else { //Else do nothing
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Method that is executed to send scanned data and user inputs to the external database (In this case Google Sheets)
    private void addItemToSheet() {

        //Progress dialog that communicates that the user input is being processed
        final ProgressDialog loading = ProgressDialog.show(this,"Adding Item","Please wait");

        //Sends data to 'appscript' webapp backend where the data is processed, formatted, and input into the external database
        StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, "https://script.google.com/macros/s/AKfycbz-ANa14E4dvdJc_0ATwy07oBOSqRaZLSaIJPa7qx6AjZ5NkV8q/exec",
                //Listens for data to be input into the system
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                    }
                },
                //Sends an error response if something is wrong
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }
        ) {
            @Override
            //Hash map that organizes the data that is to be sent to the 'appscript' webapp
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("sdata",scanned); //Scanned data
                params.put("quant",quant); //Quantity of the item
                params.put("name", name); //Name of the item
                params.put("check1", check1bool); //Value of 'OUTPUT' checkbox string
                params.put("check3", check3bool); //Value of 'NEW' checkbox string
                return params; //Return the hash map
            }
        };

        int socketTimeOut = 50000; //Processing timeout

        //If the data does not process than retry after the socketTimeOut has reached its value
        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }
}