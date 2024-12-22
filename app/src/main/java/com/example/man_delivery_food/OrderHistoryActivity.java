package com.example.man_delivery_food;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.man_delivery_food.Adapter.OrderAdapter;
import com.example.man_delivery_food.DBHelper.DBHelper;
import com.example.man_delivery_food.Model.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private OkHttpClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order_history);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView_oh);
        bottomNavigationView.setSelectedItemId(R.id.navigation_basket); // Change this based on the activity

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // Navigate to ShopsActivity
                    startActivity(new Intent(OrderHistoryActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_basket) {
                    // Navigate to OrderSummaryActivity
                    startActivity(new Intent(OrderHistoryActivity.this, OrderHistoryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to ProfileActivity
                    startActivity(new Intent(OrderHistoryActivity.this, EditProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycler_view_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        client = new OkHttpClient();

        // Initialize the orderList to avoid NullPointerException
        orderList = new ArrayList<>();

        // Set adapter with a click listener
        orderAdapter = new OrderAdapter(this, orderList, order -> {
            // Intent to navigate to OrderDetailsActivity
            Intent intent = new Intent(OrderHistoryActivity.this, OrderDetailsActivity.class);
            intent.putExtra("Id_Demandes", order.getOrderId());
            startActivity(intent);
        });
        recyclerView.setAdapter(orderAdapter);

        DBHelper dbHelper = new DBHelper(this);
        String userId = dbHelper.getUserId();
        Log.d("user id = ", userId) ;

        // Fetch orders from the server
        fetchOrders(userId);



        // Handle system insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchOrders(String userId) {
        OkHttpClient client = new OkHttpClient();

        // The URL for your PHP script
        String url = "https://www.fissadelivery.com/fissa/Man_Delivery_Food/Order_History.php";

        RequestBody postUserID = new FormBody.Builder()
                .add("user_id", userId) // Use 'user_id' instead of 'userId'
                .build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(postUserID)
                .build();

        Log.d("request", String.valueOf(request));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(OrderHistoryActivity.this, "Failed to fetch data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    Log.d("Server Response", "Response: " + jsonData);

                    if (jsonData.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(OrderHistoryActivity.this, "Empty response from server", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    // Parse JSON and update RecyclerView
                    parseJsonData(jsonData);
                } else {
                    runOnUiThread(() -> Toast.makeText(OrderHistoryActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void parseJsonData(String jsonData) {
        try {
            // Parse the JSON data as a JSONObject
            JSONObject jsonObject = new JSONObject(jsonData);

            // Get the "orders" JSONArray from the JSONObject
            JSONArray jsonArray = jsonObject.getJSONArray("orders");

            // Clear the list to avoid duplication
            orderList.clear();

            // Loop through each item in the "orders" array
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject orderObject = jsonArray.getJSONObject(i);
                String orderId = orderObject.getString("Id_Demandes");
                int numberOfItems = orderObject.getInt("num_items");
                String orderDate = orderObject.getString("Date_commande");
                String orderTime = orderObject.getString("Heure_commande");

                // Create an Order object and add it to the list
                Order order = new Order(orderId, numberOfItems, orderDate, orderTime);
                orderList.add(order);
            }

            // Update RecyclerView on the main thread
            runOnUiThread(() -> {
                recyclerView.setAdapter(orderAdapter);
                orderAdapter.notifyDataSetChanged(); // Notify adapter of data change
            });
        } catch (JSONException e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(OrderHistoryActivity.this, "Failed to parse data", Toast.LENGTH_SHORT).show()
            );
        }
    }




}
