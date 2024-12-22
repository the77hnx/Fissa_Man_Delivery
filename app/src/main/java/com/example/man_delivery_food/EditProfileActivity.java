package com.example.man_delivery_food;
import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.man_delivery_food.DBHelper.DBHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextView displayTextView, descriptionTextView, addressTextView, gpsTextView, telrestv, passwordrestv, repasswordrestv;
    private EditText etNumber, etName, addressEditText, etDescription, etpasswordedit, etrepasswordedit;
    private Button editBtn1, editBtn2, editBtn3,editBtn4, editBtn5, editBtn6, editProfileImageButton, btnPhoneLogin;
    private boolean isEditingName = false, isEditingDescription = false, isEditingAddress = false, isEditingpassword = false, isEditingrepassword = false, isEditingtel = false;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String currentLocation;
    private OkHttpClient client;

    private ImageView profileImageView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize views
        displayTextView = findViewById(R.id.displayTextView);
        descriptionTextView = findViewById(R.id.descriptiontv);
        addressTextView = findViewById(R.id.addresstvwritten);
        telrestv = findViewById(R.id.telrestv);
        passwordrestv = findViewById(R.id.etpasswordedittv);
        repasswordrestv = findViewById(R.id.repasswordrestv);
        etNumber = findViewById(R.id.etNumber);
        etName = findViewById(R.id.etNameeditpage);
        addressEditText = findViewById(R.id.addressEditText);
        etDescription = findViewById(R.id.etDescription);
        etpasswordedit = findViewById(R.id.etpasswordedit);
        etrepasswordedit = findViewById(R.id.etrepasswordedit);
        editBtn1 = findViewById(R.id.editbtn1);
        editBtn2 = findViewById(R.id.editbtn2);
        editBtn3 = findViewById(R.id.editbtn3);
        editBtn4 = findViewById(R.id.editbtn4);
        editBtn5 = findViewById(R.id.editbtn5);
        editBtn6 = findViewById(R.id.editbtn6);
        btnPhoneLogin = findViewById(R.id.saveeditbtn);
        gpsTextView = findViewById(R.id.editGPStext);
        client = new OkHttpClient();
        profileImageView = findViewById(R.id.profileImageView);
        editProfileImageButton = findViewById(R.id.editProfileImageButton);
        editProfileImageButton.setOnClickListener(v -> showImageSourceDialog());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView_ep);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile); // Change this based on the activity

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    // Navigate to ShopsActivity
                    startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                    return true;
                }else if (itemId == R.id.navigation_basket) {
                    // Navigate to OrderSummaryActivity
                    startActivity(new Intent(EditProfileActivity.this, OrderInDeliveryActivity.class));
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to ProfileActivity
                    startActivity(new Intent(EditProfileActivity.this, EditProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Set click listeners for the buttons
        editBtn1.setOnClickListener(v -> {
            toggleEditText(etNumber, displayTextView, editBtn1, isEditingName);
            isEditingName = !isEditingName;
        });
        editBtn2.setOnClickListener(v -> {
            toggleEditText(etName, descriptionTextView, editBtn2, isEditingDescription);
            isEditingDescription = !isEditingDescription;
        });
        editBtn3.setOnClickListener(v -> {
            toggleEditText(addressEditText, addressTextView, editBtn3, isEditingAddress);
            isEditingAddress = !isEditingAddress;
        });
        editBtn4.setOnClickListener(v -> {
            toggleEditText(etDescription, telrestv, editBtn4, isEditingtel);
            isEditingAddress = !isEditingAddress;
        });
        editBtn5.setOnClickListener(v -> {
            toggleEditText(etpasswordedit, passwordrestv, editBtn5, isEditingpassword);
            isEditingAddress = !isEditingAddress;
        });
        editBtn6.setOnClickListener(v -> {
            toggleEditText(etrepasswordedit, repasswordrestv, editBtn6, isEditingrepassword);
            isEditingAddress = !isEditingAddress;
        });

        DBHelper dbHelper = new DBHelper(this);
        String userId = dbHelper.getUserId();
        Log.d("user id = ", userId) ;

        // Set click listener for the save button
        btnPhoneLogin.setOnClickListener(v -> {
            String numberuser = etNumber.getText().toString();
            String nameuser = etName.getText().toString();
            String numbervehicule = addressEditText.getText().toString();
            String namevehicule = etDescription.getText().toString();
            String password = etpasswordedit.getText().toString();
            String rePassword = etrepasswordedit.getText().toString();
            String address = gpsTextView.getText().toString();
            Bitmap profileBitmap = ((BitmapDrawable) profileImageView.getDrawable()).getBitmap();


            if (!password.equals(rePassword)) {
                Toast.makeText(EditProfileActivity.this, "Passwords do not match. Please re-enter.", Toast.LENGTH_SHORT).show();
            } else {
                // Save all edits if passwords match
                saveAllEdits();
                updateUserData(nameuser, numbervehicule, numberuser, password, namevehicule, userId, address, profileBitmap);
                // Navigate to the menu page

            }
        });

        // Fetch and display user data
        fetchUserData(userId);
    }

    private void toggleEditText(EditText editText, TextView textView, Button button, boolean isEditing) {
        if (isEditing) {
            // Save changes and switch to TextView
            String newText = editText.getText().toString();
            if (!TextUtils.isEmpty(newText)) {
                textView.setText(newText);
            }
            textView.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            button.setBackgroundResource(R.drawable.ic_edit);
        } else {
            // Switch to EditText
            editText.setText(textView.getText());
            textView.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            button.setBackgroundResource(R.drawable.ic_done);
        }
    }

    private void saveAllEdits() {
        if (etNumber.getVisibility() == View.VISIBLE) {
            String newNumber = etNumber.getText().toString();
            if (!TextUtils.isEmpty(newNumber)) {
                displayTextView.setText(newNumber);
            }
            etNumber.setVisibility(View.GONE);
            displayTextView.setVisibility(View.VISIBLE);
        }

        if (etName.getVisibility() == View.VISIBLE) {
            String newName = etName.getText().toString();
            if (!TextUtils.isEmpty(newName)) {
                descriptionTextView.setText(newName);
            }
            etName.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.VISIBLE);
        }

        if (addressEditText.getVisibility() == View.VISIBLE) {
            String newAddress = addressEditText.getText().toString();
            if (!TextUtils.isEmpty(newAddress)) {
                addressTextView.setText(newAddress);
            }
            addressEditText.setVisibility(View.GONE);
            addressTextView.setVisibility(View.VISIBLE);
        }

        if (etpasswordedit.getVisibility() == View.VISIBLE) {
            String newPassword = etpasswordedit.getText().toString();
            if (!TextUtils.isEmpty(newPassword)) {
                passwordrestv.setText(newPassword);  // Avoid showing passwords in plain text in real apps
            }
            etpasswordedit.setVisibility(View.GONE);
            passwordrestv.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserData(String nameuser, String numbervehicule, String numberuser, String password, String namevehicule, String userId, String address, Bitmap profileBitmap) {
        String url = "https://www.fissadelivery.com/fissa/Man_Delivery_Food/Update_Profile.php"; // Replace with your server's URL

        // Convert the Bitmap to a byte array (optional)
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        profileBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Create a RequestBody for the image
        RequestBody imageBody = RequestBody.create(imageBytes, okhttp3.MediaType.parse("image/jpeg"));

        // Create the multipart request body
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("user_id", userId)
                .addFormDataPart("Nom_Livreur", nameuser)
                .addFormDataPart("Nom_Vehicule", namevehicule)
                .addFormDataPart("Tel_Livreur", numberuser)
                .addFormDataPart("Password", password)
                .addFormDataPart("N_Vehicule", numbervehicule)
                .addFormDataPart("Coordonnes", address)
                .addFormDataPart("profile_image", "profile.jpg", imageBody) // Send the image with the name "profile.jpg"
                .build();

        // Build the POST request
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                Log.e("ProfileActivity", "Error: " + e.getMessage(), e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(EditProfileActivity.this, "Failed to update profile: " + response.message(), Toast.LENGTH_SHORT).show());
                    Log.e("ProfileActivity", "Response Error: " + response.message());
                }
            }
        });
    }

    private void fetchUserData(String userId) {
        String url = "https://www.fissadelivery.com/fissa/Man_Delivery_Food/Get_Profile.php";

        RequestBody PostuserID = new FormBody.Builder()
                .add("user_id", userId) // استخدام 'user_id' بدلاً من 'userId'
                .build();

        okhttp3.Request request = new Request.Builder()
                .url(url)
                .post(PostuserID)
                .build();

        Log.d("request", String.valueOf(request));

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(EditProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_LONG).show();
                });
                Log.e("ProfileActivity", "Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d("ProfileActivity", "Raw Response Data: " + responseBody); // Log raw response

                    try {
                        // Check if the response is valid JSON
                        JSONObject jsonObject = new JSONObject(responseBody);
                        if (jsonObject.has("error")) {
                            // Handle error message
                            runOnUiThread(() -> {
                                Toast.makeText(EditProfileActivity.this, jsonObject.optString("error"), Toast.LENGTH_LONG).show();
                            });
                        } else {
                            // Process user data
                            String fullName = jsonObject.optString("Nom_Livreur", "N/A");
                            String nameVehicule = jsonObject.optString("Nom_Vehicule", "N/A");
                            String phone = jsonObject.optString("Tel_Livreur", "N/A");
                            String address = jsonObject.optString("Coordonnes", "N/A");
                            String numberVehicule = jsonObject.optString("N_Vehicule", "N/A");
                            String password = jsonObject.optString("Password", "N/A");
                            String imagePath = jsonObject.optString("imagePath", null);

                            runOnUiThread(() -> {
                                displayTextView.setText(fullName);
                                descriptionTextView.setText(nameVehicule);
                                addressTextView.setText(numberVehicule);
                                telrestv.setText(phone);
                                passwordrestv.setText(password);
                                repasswordrestv.setText(password);
                                gpsTextView.setText(address);
                                // Load image from imagePath using Glide or Picasso
                                String baseUrl = "https://www.fissadelivery.com/fissa/";
                                String fullImagePath = baseUrl + imagePath.replace("../", "");
                                Glide.with(EditProfileActivity.this)
                                        .load(fullImagePath)
                                        .into(profileImageView);

                                Log.d("ProfileActivity", "Final Image URL: " + fullImagePath);

                            });
                        }
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditProfileActivity.this, "Error parsing user data", Toast.LENGTH_LONG).show();
                        });
                        Log.e("ProfileActivity", "JSON parsing error: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(EditProfileActivity.this, "Error fetching user data", Toast.LENGTH_LONG).show();
                    });
                    Log.e("ProfileActivity", "Response error: " + response.message());
                }
            }
        });
    }
    private void showImageSourceDialog() {
        String[] options = {"Choose from Gallery", "Take a Photo"};
        new AlertDialog.Builder(this)
                .setTitle("Select Profile Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Choose from Gallery
                        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            openGallery();
                        } else {
                            requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 101);
                        }
                    } else if (which == 1) {
                        // Take a Photo
                        if (checkPermission(Manifest.permission.CAMERA)) {
                            openCamera();
                        } else {
                            requestPermission(Manifest.permission.CAMERA, 102);
                        }
                    }
                })
                .show();
    }

    private boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, 201);
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, 202);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 101) {
                openGallery();
            } else if (requestCode == 102) {
                openCamera();
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 201 && data != null) {
                // Handle image from gallery
                Uri selectedImageUri = data.getData();
                profileImageView.setImageURI(selectedImageUri); // Display image
            } else if (requestCode == 202 && data != null) {
                // Handle image from camera
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                profileImageView.setImageBitmap(photo); // Display image
            }
        }
    }
}