package com.example.man_delivery_food;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.man_delivery_food.DBHelper.DBHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String OFFER_STATUS_KEY = "offer_status";
    private static final String fetchStoreDataURL = "https://www.fissadelivery.com/fissa/Man_Delivery_Food/Fetch_Data_Man_Del.php";
    private static final String updateStoreStatusURL = "https://www.fissadelivery.com/fissa/Man_Delivery_Food/Status_Livreur.php";


    private TextView Customername, NameDel, Status_tv, Allprice_main, Price_del, Order_id,
            Restaurant_location, Customer_location, Valwallet, Valmonth, Valweek, Valtoday,
            Rejected_orders, Accepted_orders, Daily_orders, Monthly_orders, Evaluation;
    private Button Detailed_order_btn, Direction_order_btn;
    private ImageView imagecustomerdel;
    private Switch offerSwitch;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your main activity layout file

        // Initialize Views
        Customername = findViewById(R.id.customer_name_main);
        NameDel = findViewById(R.id.nameDel);
        Status_tv = findViewById(R.id.status_tv);
        Allprice_main = findViewById(R.id.allprice_main);
        Price_del = findViewById(R.id.price_del_main);
        Order_id = findViewById(R.id.order_id_main);
        Restaurant_location = findViewById(R.id.Restaurant_location);
        Customer_location = findViewById(R.id.customer_location);
        Detailed_order_btn = findViewById(R.id.detailed_order_btn);
        Direction_order_btn = findViewById(R.id.direction_order_btn);
        Valwallet = findViewById(R.id.valwallet);
        Valmonth = findViewById(R.id.valmonth);
        Valweek = findViewById(R.id.valweek);
        Valtoday = findViewById(R.id.valtoday);
        Rejected_orders = findViewById(R.id.Rejected_orders);
        Accepted_orders = findViewById(R.id.Accepted_orders);
        Daily_orders = findViewById(R.id.daily_orders);
        Monthly_orders = findViewById(R.id.Monthly_orders);
        Evaluation = findViewById(R.id.item_name4);
        offerSwitch = findViewById(R.id.offer_switch_main);
        client = new OkHttpClient();
        imagecustomerdel = findViewById(R.id.imagecustomerdel);
        // Load the saved offer switch state
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean offerStatus = preferences.getBoolean(OFFER_STATUS_KEY, false);
        offerSwitch.setChecked(offerStatus);

        // Set initial state of the basket TextView
        updateBasketText(offerSwitch.isChecked());
        DBHelper dbHelper = new DBHelper(this);
        String userId = dbHelper.getUserId();
        Log.d("user id = ", userId);
        // Fetch store data from the server
        fetchDataFromServer(userId);
        showStoreStatus(userId); // Call to fetch and display current store status


// Set up listener for the Switch
        offerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateBasketText(isChecked);
            saveOfferSwitchState(isChecked); // Save state when it changes
            updateStoreStatus(isChecked, userId);    // Update status on the server
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView_ma);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home); // Change this based on the activity

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // Navigate to ShopsActivity
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_basket) {
                    // Navigate to OrderSummaryActivity
                    startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to ProfileActivity
                    startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Set up click listeners for buttons
        Detailed_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start OrderDetailsActivity
                Intent intent = new Intent(MainActivity.this, OrderDetailsActivity.class);
                // Get the Order ID from the TextView
                String orderId = Order_id.getText().toString().replace("رقم الطلب : ", "").trim();
                // Add the Order ID to the Intent
                intent.putExtra("Id_Demandes", orderId);
                // Start the OrderDetailsActivity
                startActivity(intent);
            }
        });

        Direction_order_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start OrderInDeliveryActivity
                Intent intent = new Intent(MainActivity.this, OrderInDeliveryActivity.class);
                // Get the Order ID from the TextView
                String orderId = Order_id.getText().toString().replace("رقم الطلب : ", "").trim();
                // Add the Order ID to the Intent
                intent.putExtra("Id_Demandes", orderId);
                // Start the OrderInDeliveryActivity
                startActivity(intent);
            }
        });

    }

    public void fetchDataFromServer(String userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // URL for your PHP script
                URL url = new URL(fetchStoreDataURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Prepare the POST data
                String postData = "user_id=" + URLEncoder.encode(userId, "UTF-8");

                // Send the POST data
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(postData);
                writer.flush();
                writer.close();

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                String response = result.toString();

                handler.post(() -> {

                    if (response != null) {
                        Log.d("Server Response", response);
                        if (response.equals("false")) {
                            Toast.makeText(MainActivity.this, "No data found or error occurred", Toast.LENGTH_SHORT).show();
                        } else {
                        try {
                            JSONObject jsonObject = new JSONObject(response);


                            // Set data to TextViews if available
                            Customername.setText(jsonObject.optString("Customer_Name", "N/A"));
                            NameDel.setText(jsonObject.optString("Livreur_name", "N/A"));
                            Status_tv.setText(jsonObject.optString("Statut_Livreur", "N/A"));
                            Allprice_main.setText("سعر الطلبية : " + jsonObject.optString("Order_Price", "0") + " دج");
                            Price_del.setText("سعر التوصيل : " + jsonObject.optString("Delivery_Price", "0") + " دج");
                            Order_id.setText("رقم الطلب : " + jsonObject.optString("Order_ID", "N/A"));
                            Restaurant_location.setText(jsonObject.optString("Restaurant_Location", "N/A"));
                            Customer_location.setText(jsonObject.optString("Customer_Location", "N/A"));
                            Valwallet.setText(jsonObject.optString("wallet_value", "0"));
                            Valmonth.setText(jsonObject.optString("wallet_monthly_value", "0"));
                            Valweek.setText(jsonObject.optString("wallet_weekly_value", "0"));
                            Valtoday.setText(jsonObject.optString("wallet_daily_value", "0"));
                            Rejected_orders.setText(jsonObject.optString("cancelled_orders", "0"));
                            Accepted_orders.setText(jsonObject.optString("accepted_orders", "0"));
                            Daily_orders.setText(jsonObject.optString("todays_orders", "0"));
                            Monthly_orders.setText(jsonObject.optString("monthly_orders", "0"));
                            Evaluation.setText(jsonObject.optString("Evaluation", "N/A"));
                            String imagePath = jsonObject.optString("Customer_Image", "");

                            String baseUrl = "https://www.fissadelivery.com/fissa/";
                            String fullImagePath = baseUrl + imagePath.replace("../", "");
                            Glide.with(MainActivity.this)
                                        .load(fullImagePath) // Image URL from server
                                        .into(imagecustomerdel); // Your ImageView
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("Error parsing data", e.getMessage());
                            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }}

                    } else {
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void saveOfferSwitchState(boolean isChecked) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(OFFER_STATUS_KEY, isChecked);
        editor.apply();
    }

    private void showStoreStatus(String userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                String urlWithParams = fetchStoreDataURL + "?user_id=" + URLEncoder.encode(userId, "UTF-8");
                URL url = new URL(urlWithParams);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);

                // Prepare the POST data
                String postData = "user_id=" + URLEncoder.encode(userId, "UTF-8");

                // Send the POST data
                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(postData);
                writer.flush();
                writer.close();

                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
                String response = result.toString();

                handler.post(() -> {
                    if (response != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.optString("Statut_magasin");
                            updateStoreStatusUI(status);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(MainActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateStoreStatus(boolean isOpen, String userId) {
        new AsyncTask<Boolean, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Boolean... params) {
                try {
                    URL url = new URL(updateStoreStatusURL); // URL to update the store status
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String statutLivreur = params[0] ? "متاح" : "غير متاح";
                    String postData = "user_id=" + URLEncoder.encode(userId, "UTF-8") +
                            "&statut_magasin=" + URLEncoder.encode(statutLivreur, "UTF-8");

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write(postData);
                    writer.flush();
                    writer.close();

                    int responseCode = connection.getResponseCode();

                    Log.d("Update Store Status", "Sending status: " + (params[0] ? "متاح" : "غير متاح"));
                    return responseCode == HttpURLConnection.HTTP_OK;

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Error message", e.getMessage());
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, "Store status updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(isOpen);
    }


    private void updateStoreStatusUI(String status) {
        // Update the UI based on the store status
        if ("متاح".equals(status)) {
            offerSwitch.setChecked(true);
            updateBasketText(true); // Update basket text to "متاح"
        } else {
            offerSwitch.setChecked(false);
            updateBasketText(false); // Update basket text to "غير متاح"
        }
    }

    // Method to update the basket TextView based on the Switch state
    private void updateBasketText(boolean isChecked) {
        if (isChecked) {
            Status_tv.setText("متاح");
            Status_tv.setTextColor(getResources().getColor(R.color.green));
        } else {
            Status_tv.setText("غير متاح");
            Status_tv.setTextColor(getResources().getColor(R.color.red));
        }
    }

}
