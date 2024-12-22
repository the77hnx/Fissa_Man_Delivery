package com.example.man_delivery_food;

import static android.accounts.AccountManager.KEY_PASSWORD;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.man_delivery_food.DBHelper.DBHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPasslogin;
    private CheckBox checkboxRememberMe;
    private Button btnPhoneLogin, btnOpenAccount;
    private OkHttpClient client;


    private static final String PREF_NAME = "login_preferences";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etemaillogin);
        etPasslogin = findViewById(R.id.etPasslogin);
        checkboxRememberMe = findViewById(R.id.checkboxRememberMe);
        btnPhoneLogin = findViewById(R.id.btnPhoneLoginpage);
        btnOpenAccount = findViewById(R.id.btnopenaccount);
        client = new OkHttpClient();

        btnPhoneLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPasslogin.getText().toString().trim();

            if (validateInput(email, password)) {
                // Create a POST request to send email and password
                RequestBody formBody = new FormBody.Builder()
                        .add("email", email)
                        .add("password", password)
                        .build();

                Request request = new Request.Builder()
                        .url("https://www.fissadelivery.com/fissa/Man_Delivery_Food/Login.php")
                        .post(formBody)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Failed to connect to server", Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String jsonData = response.body().string();
                            try {
                                // Try to parse JSON
                                JSONObject jsonObject = new JSONObject(jsonData);
                                boolean success = jsonObject.getBoolean("success");
                                String message = jsonObject.getString("message");

                                Log.d("success : ", String.valueOf(success));
                                Log.d("message : ", message);

                                runOnUiThread(() -> {
                                    if (success) {
                                        String activite = jsonObject.optString("activite");
                                        if ("مقبول".equals(activite)) {
                                            // تحقق من وجود "userId" في الرد
                                            String userId = null;
                                            if (jsonObject.has("userId")) {
                                                // حفظ معرف المستخدم في قاعدة البيانات المحلية
                                                try {
                                                    userId = jsonObject.getString("userId");
                                                } catch (JSONException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                DBHelper dbHelper = new DBHelper(LoginActivity.this);
                                                dbHelper.insertUserId(userId);
                                            } else {
                                                // إذا لم يكن معرف المستخدم موجودًا في الرد
                                                Toast.makeText(LoginActivity.this, "User ID not found in server response", Toast.LENGTH_LONG).show();
                                            }
                                            // If login is successful, navigate to the LocationActivity
                                            Log.d("use id = ", userId);
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if ("قيد المراجعة".equals(activite)) {
                                            Intent intent = new Intent(LoginActivity.this, VehicleInfoActivity.class);
                                            intent.putExtra("nom_livreur", jsonObject.optString("nom_livreur"));
                                            intent.putExtra("n_vehicule", jsonObject.optString("n_vehicule"));
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            } catch (JSONException e) {
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Error parsing JSON", Toast.LENGTH_LONG).show());
                                Log.e("Error parsing JSON", jsonData);  // Log the raw response for debugging
                            }
                        } else {
                            runOnUiThread(() -> {
                                try {
                                    String responseBody = response.body().string();
                                    Log.e("Server error", responseBody);  // Log server response if not successful
                                    Toast.makeText(LoginActivity.this, "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                                } catch (IOException e) {
                                    Log.e("IOException", e.getMessage());
                                }
                            });
                        }
                    }

                });
            }
        });

        // Load saved login info if "Remember Me" was previously checked
        if (loadLoginPreferences()) {
            String savedPhoneNumber = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_PHONE_NUMBER, "");
            String savedPassword = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getString(KEY_PASSWORD, "");

            etEmail.setText(savedPhoneNumber);
            etPasslogin.setText(savedPassword);
            checkboxRememberMe.setChecked(true);
        }

        btnOpenAccount.setOnClickListener(v -> {
            // Handle open account request (replace with your actual logic)

            Intent intent = new Intent(LoginActivity.this, SignNewManActivity.class);
            startActivity(intent);
            finish();
        });
    }






    private boolean loadLoginPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
    }


    // Validate email and password input
    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "الرجاء إدخال البريد الالكتروني", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "الرجاء إدخال كلمة المرور", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Here you can add more checks (e.g., email format, password strength, etc.)
        return true;
    }


}
