package com.example.organdonationapp;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class DonorRegistrationActivity extends AppCompatActivity {
    private TextView BackButton;

    private CircleImageView profile_image;

    private TextInputEditText RegisterFullName,registerIdNumber,registerPhoneNumber,registerEmail,registerPassword;

    private Spinner OrganSpinner;

    private Button RegisterButton;

    private Uri resultUri;
    private ProgressDialog loader;

    private FirebaseAuth mAuth;
    private DatabaseReference userDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_registration);

        BackButton = findViewById(R.id.BackButton);
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DonorRegistrationActivity.this, loginActivity.class);
                startActivity(intent);
            }
        });
        profile_image = findViewById(R.id.profile_image);
        RegisterFullName = findViewById(R.id.RegisterFullName);
        registerIdNumber = findViewById(R.id.registerIdNumber);
        registerPhoneNumber = findViewById(R.id.registerPhoneNumber);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        OrganSpinner = findViewById(R.id.OrganSpinner);
        RegisterButton = findViewById(R.id.RegisterButton);
        loader = new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);


            }
        });


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data ) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
//            resultUri = data.getData();
//            profile_image.setImageURI(resultUri);
//        }
//    }
//    }


        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = registerEmail.getText().toString().trim();
                final String password = registerPassword.getText().toString().trim();
                final String FullName = RegisterFullName.getText().toString().trim();
                final String idNumber = registerIdNumber.getText().toString().trim();
                final String phoneNumber = registerPhoneNumber.getText().toString().trim();
                final String organs = OrganSpinner.getSelectedItem().toString();

                if (TextUtils.isEmpty(email)) {
                    registerEmail.setError("email is required");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    registerPassword.setError("password is required");
                    return;
                }
                if (TextUtils.isEmpty(FullName)) {
                    RegisterFullName.setError("register FullName is required");
                    return;
                }
                if (TextUtils.isEmpty(idNumber)) {
                    registerIdNumber.setError("register IdNumber is required");
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    registerPhoneNumber.setError(" register PhoneNumber is required");
                    return;
                }
                if (organs.equals("Select organ")) {
                    Toast.makeText(DonorRegistrationActivity.this, "Select organ", Toast.LENGTH_SHORT).show();
                    return;

                } else {
                    loader.setMessage("Registering you...");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();

                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                String error = task.getException().toString();
                                Toast.makeText(DonorRegistrationActivity.this, "Error" + error, Toast.LENGTH_SHORT).show();
                            } else {
                                String currentUserId = mAuth.getCurrentUser().getUid();
                                userDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
//                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                DatabaseReference myRef = database.getReference("message");


                                //myRef.setValue("email");
                                HashMap<String, Object> userInfo = new HashMap<>();
                                userInfo.put("id", currentUserId);
                                userInfo.put("name", FullName);
                                userInfo.put("email", email);
                                userInfo.put("idNumber", idNumber);
                                userInfo.put("phoneNumber", phoneNumber);
                                userInfo.put("organs", OrganSpinner);
                                userInfo.put("type", "donor");
                                userInfo.put("search", "donor" + OrganSpinner);




                                userDatabaseRef.updateChildren(userInfo).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(DonorRegistrationActivity.this, "Data set successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                        finish();
                                        // loader.dismiss();
                                    }
                                });
//                                if (resultUri != null) {
//                                    final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile images").child(currentUserId);
//                                    Bitmap bitmap = null;
//
//                                    try {
//                                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
//                                    byte[] data = byteArrayOutputStream.toByteArray();
//                                    UploadTask uploadTask = filePath.putBytes(data);
//
//                                    uploadTask.addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(DonorRegistrationActivity.this, "image upload failed", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                        @Override
//                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                            if (taskSnapshot.getMetadata() != null && taskSnapshot.getMetadata().getReference() != null) {
//                                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
//                                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                                    @Override
//                                                    public void onSuccess(Uri uri) {
//                                                        String imageUrl = uri.toString();
//                                                        Map<String, Object> newImageMap = new HashMap<>();
//                                                        newImageMap.put("profilepictureurl", imageUrl);
//                                                        userDatabaseRef.updateChildren(newImageMap).addOnCompleteListener(new OnCompleteListener() {
//                                                            @Override
//                                                            public void onComplete(@NonNull Task task) {
//                                                                if (task.isSuccessful()) {
//                                                                    Toast.makeText(DonorRegistrationActivity.this, "Image url added to database successfully", Toast.LENGTH_SHORT).show();
//                                                                } else {
//                                                                    Toast.makeText(DonorRegistrationActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
//                                                                }
//                                                            }
//                                                    });
//                                                        finish();
//
//                                                    }
//                                                });
//                                            }
//
//                                        }
//                                    });
//
                                Intent intent = new Intent(DonorRegistrationActivity.this, MainActivity.class);

                                startActivity(intent);
                                finish();
                                loader.dismiss();
//                            }

                        }
                    }


                     });

             }

            }
        });

      }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,@Nullable Intent data ) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            resultUri = data.getData();
            profile_image.setImageURI(resultUri);
        }
    }
}