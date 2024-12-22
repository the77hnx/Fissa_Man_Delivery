package com.example.man_delivery_food;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.man_delivery_food.Adapter.ItemDetailsAdpter;
import com.example.man_delivery_food.Model.FoodItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    // View declarations
    private TextView additionalInfoMagView;
    private TextView restaurantNameView, restaurantLocationView, restaurantStatusView, restaurantRatingView, number_items;
    private Button delivery_order_done;
    private TextView CustomerNameView;
    private RecyclerView itemRecyclerView;
    private ItemDetailsAdpter itemAdapter;
    private List<FoodItem> foodItemsList;
    private OkHttpClient client;
    private ImageView restaurantImageView , userImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar and make the activity full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_order_details);



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView_odet);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // Navigate to ShopsActivity
                    startActivity(new Intent(OrderDetailsActivity.this, MainActivity.class));
                    return true;
                }else if (itemId == R.id.navigation_basket) {
                    // Navigate to OrderSummaryActivity
                    startActivity(new Intent(OrderDetailsActivity.this, OrderInDeliveryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to ProfileActivity
                    startActivity(new Intent(OrderDetailsActivity.this, EditProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
        // Initialize views
        initializeViews();

        // Initialize OkHttpClient
        client = new OkHttpClient();

        // Initialize RecyclerView
        foodItemsList = new ArrayList<>();
        itemRecyclerView = findViewById(R.id.itemRecyclerView);
        itemRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemDetailsAdpter(this, foodItemsList);
        itemRecyclerView.setAdapter(itemAdapter);

        // Get the Id_Demandes from the intent
        String orderIdStr = getIntent().getStringExtra("Id_Demandes");
        int orderId;

        // Ensure the order ID is valid
        try {
            orderId = Integer.parseInt(orderIdStr);
        } catch (NumberFormatException e) {
            showError("Invalid order ID.");
            return;
        }

        fetchOrderDetails(orderId);

        if(orderId == 6){
            delivery_order_done.setVisibility(View.GONE);
        }
        // Set onClick listener for the button
        delivery_order_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrderStatus(orderId);
            }
        });
    }

    private void initializeViews() {
        additionalInfoMagView = findViewById(R.id.additional_information_text_mandel);
        restaurantNameView = findViewById(R.id.resnameinfodet);
        restaurantLocationView = findViewById(R.id.placeresinfodet);
        restaurantStatusView = findViewById(R.id.statusinfodet);
        restaurantRatingView = findViewById(R.id.valtvinfodet);
        restaurantImageView = findViewById(R.id.imageresinfodet);
        CustomerNameView = findViewById(R.id.name_user_det);
        number_items = findViewById(R.id.number_items);
        delivery_order_done = findViewById(R.id.delivery_order_done); // Initialize here
        userImageView = findViewById(R.id.user_image);
    }

    private void fetchOrderDetails(int orderId) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Id_Demandes", orderId);
        } catch (JSONException e) {
            e.printStackTrace();
            showError("Failed to create request parameters.");
            return;
        }

        Request request = new Request.Builder()
                .url("https://www.fissadelivery.com/fissa/Man_Delivery_Food/Details_order.php")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
                runOnUiThread(() -> showError("Failed to fetch order details."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonResponse = response.body().string();
                    Log.d(TAG, "Response from server: " + jsonResponse);
                    runOnUiThread(() -> handleResponse(jsonResponse));
                } else {
                    Log.e(TAG, "Server error: " + response.message());
                    runOnUiThread(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    private void handleResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if (!jsonObject.has("error")) {
                JSONObject order = jsonObject.getJSONObject("order");
                updateOrderDetails(order);
            } else {
                String errorMessage = jsonObject.optString("error", "Unknown error occurred.");
                showError("Error fetching order data: " + errorMessage);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse response: " + e.getMessage());
            showError("Failed to parse server response.");
        }
    }

    private void updateOrderDetails(JSONObject order) throws JSONException {
        // Update TextViews with the fetched order and restaurant details
        additionalInfoMagView.setText(order.optString("restaurantMessage"));
        restaurantNameView.setText(order.optString("restaurant_name"));
        restaurantLocationView.setText(order.optString("restaurant_address"));
        restaurantStatusView.setText(order.optString("restaurant_status"));
        restaurantRatingView.setText(order.optString("restaurant_eval"));
        CustomerNameView.setText(order.optString("customerName"));

        // Load restaurant image
        String baseUrl = "https://www.fissadelivery.com/fissa/";
        String restaurantImageUrl = order.optString("restaurantImage");
        String fullImageResPath = baseUrl + restaurantImageUrl.replace("../", "");
        Glide.with(this)
                .load(fullImageResPath)
                .into(restaurantImageView);

        // Load user image
        String userImageUrl = order.optString("customerImage");
        String fullUserImagePath = baseUrl + userImageUrl.replace("../", "");
        Glide.with(this)
                .load(fullUserImagePath)
                .into(userImageView);

        // If the order contains items, display them in a list
        if (order.has("items")) {
            JSONArray items = order.getJSONArray("items");
            if (items.length() > 0) { // Check if items array is not empty
                foodItemsList.clear(); // Clear existing items
                int totalItemCount = 0;
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String itemName = item.optString("itemName");
                    double itemPrice = item.optDouble("itemPrice");
                    int itemQuantity = item.optInt("itemQuantity");
                    String itemImage = item.optString("itemImage"); // Get the image path

                    totalItemCount += itemQuantity; // Update the total item count
                    foodItemsList.add(new FoodItem(itemName, itemPrice, itemQuantity, itemImage)); // Pass the image path to the FoodItem constructor
                }
                number_items.setText("عدد العناصر : " + totalItemCount);
                itemAdapter.notifyDataSetChanged(); // Refresh the adapter to display updated items
            } else {
                number_items.setText("عدد العناصر : 0");
            }
        }
    }

    private void showError(String message) {
        Log.e(TAG, message);
        // Implement error display logic (e.g., Toast or Snackbar)
        Toast.makeText(this, "حدث خطأ: " + message, Toast.LENGTH_LONG).show();
    }

    // Method to update order status
    private void updateOrderStatus(int orderId) {
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("Id_Demandes", orderId);
            jsonParams.put("Id_Statut_Commande", 6); // Set new status
        } catch (JSONException e) {
            e.printStackTrace();
            showError("فشل في إنشاء معلمات الطلب.");
            return;
        }

        Request request = new Request.Builder()
                .url("https://www.fissadelivery.com/fissa/Man_Delivery_Food/Update_Status_cmd.php")
                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams.toString()))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to update order status: " + e.getMessage());
                runOnUiThread(() -> showError("فشل في تحديث حالة الطلب."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String jsonResponse = response.body().string();
                    Log.d(TAG, "Response from server: " + jsonResponse);
                    runOnUiThread(() -> handleUpdateResponse(jsonResponse));
                } else {
                    Log.e(TAG, "Server error: " + response.message());
                    runOnUiThread(() -> showError("Error: " + response.message()));
                }
            }
        });
    }

    private void handleUpdateResponse(String jsonResponse) {
        try {
            JSONObject jsonObject = new JSONObject(jsonResponse);
            if (!jsonObject.has("error")) {
                Toast.makeText(this, "تم تحديث حالة الطلب بنجاح.", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity after successful update
            } else {
                String errorMessage = jsonObject.optString("error", "Unknown error occurred.");
                showError("خطأ في تحديث حالة الطلب: " + errorMessage);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse update response: " + e.getMessage());
            showError("فشل في تحليل الاستجابة من الخادم.");
        }
    }
}
