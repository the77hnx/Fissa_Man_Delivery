package com.example.man_delivery_food;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignNewManActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CAMERA = 2;
    private static final int REQUEST_PERMISSION_STORAGE = 3;

    private EditText etNumber, etNameMag, etName, etEmail, etPassword, etConfirmPassword, etPlacesRes, etIdNational, etEnregistrement;
    private Button selectImageResButton, btnPhoneLogin;
    private ImageView selectedImageRes;
    private OkHttpClient client = new OkHttpClient();
    private Uri photoUri;

    private final ActivityResultLauncher<String> selectImageResLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        displayImage(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_new_man);

        initializeViews();

        selectImageResButton.setOnClickListener(v -> showImagePickerDialog());
        btnPhoneLogin.setOnClickListener(v -> validateAndSubmit());
    }

    private void initializeViews() {
        etNumber = findViewById(R.id.etnumber);
        etName = findViewById(R.id.etName);
        etNameMag = findViewById(R.id.etNameMag);
        etEmail = findViewById(R.id.etemailsign);
        etPassword = findViewById(R.id.etPasswordsign);
        etConfirmPassword = findViewById(R.id.etconfirmPassword);
        etPlacesRes = findViewById(R.id.etplacesres);
        etIdNational = findViewById(R.id.etIdNational);
        etEnregistrement = findViewById(R.id.etN0Enrg);
        selectImageResButton = findViewById(R.id.selectImageresButton);
        btnPhoneLogin = findViewById(R.id.btnsendinfores);
        selectedImageRes = findViewById(R.id.selectedImageRes);
        selectedImageRes.setVisibility(View.GONE);
    }

    private void showImagePickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_picker, null);
        bottomSheetDialog.setContentView(dialogView);

        Button chooseFromGallery = dialogView.findViewById(R.id.btnPickGallery);
        Button capturePhoto = dialogView.findViewById(R.id.btnCaptureCamera);

        chooseFromGallery.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                selectImageResLauncher.launch("image/*");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_STORAGE);
            }
            bottomSheetDialog.dismiss();
        });

        capturePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this,
                            "com.example.manager_food.fileprovider",
                            photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException ex) {
                Log.e("SignNewShopActivity", "Error occurred while creating the File", ex);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            if (photoUri != null) {
                displayImage(photoUri);
            } else {
                Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayImage(Uri uri) {
        if (uri != null) {
            selectedImageRes.setImageURI(uri);
            selectImageResButton.setVisibility(View.GONE);
            selectedImageRes.setVisibility(View.VISIBLE);
        }
    }

    private void validateAndSubmit() {
        String number = etNumber.getText().toString().trim();
        String namemag = etNameMag.getText().toString().trim();
        String nameveh = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String places = etPlacesRes.getText().toString().trim();
//        Bitmap imageBitmap = ((BitmapDrawable) selectedImageRes.getDrawable()).getBitmap();
        String enregistrement = etEnregistrement.getText().toString().trim();
        String idNational = etIdNational.getText().toString().trim();

        // Validate inputs
        if (number.isEmpty() || namemag.isEmpty() || nameveh.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || places.isEmpty() ||
//                imageBitmap == null ||
                enregistrement.isEmpty() || idNational.isEmpty()) {
            Toast.makeText(this, "All fields and images must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert image to file and upload
        uploadImageAndSubmitData(
//                imageBitmap,
                 namemag, nameveh, email, password, number, places, enregistrement, idNational
        );

        // Optionally, redirect to another activity (like HomeActivity)
        Intent intent = new Intent(SignNewManActivity.this, VehicleInfoActivity.class);
        startActivity(intent);
        finish();

    }

    private void uploadImageAndSubmitData(
//            Bitmap imageBitmap,
            String nameliv, String nameveh, String email, String password, String number, String address,
            String numberveh, String idNational) {

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] imageData = baos.toByteArray();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
//                .addFormDataPart("image.png", "image.png", RequestBody.create(imageData, MultipartBody.FORM))
                .addFormDataPart("etNameLivreur", nameliv)
                .addFormDataPart("etemail", email)
                .addFormDataPart("etPassword", password)
                .addFormDataPart("etnumber", number)
                .addFormDataPart("etNameveh", nameveh)
                .addFormDataPart("etplacesres", address)
                .addFormDataPart("etN0Enrgveh", numberveh)
                .addFormDataPart("etIdNational", idNational);

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url("https://www.fissadelivery.com/fissa/Man_Delivery_Food/Signup_Man_Del.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Log.e("Error",e.getMessage());
                    Toast.makeText(SignNewManActivity.this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignNewManActivity.this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();

                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(SignNewManActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
