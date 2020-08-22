package com.example.namastesoicalnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private CircleImageView profilePicture;
    private EditText userName, userFullName, userDOB, userStatus, userCountry, userRelationStatus, userGender;
    private Button updateInfoBtn;
    private FirebaseAuth mAuth;
    private String currentUserId, downloadUrl;
    private DatabaseReference usersRef;
    private int galleryPic = 1;
    private ProgressDialog loadingBar;
    private StorageReference userProfileImgRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mToolbar = findViewById(R.id.settings_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        profilePicture = findViewById(R.id.settings_profile_photo);
        userName = findViewById(R.id.settings_user_name);
        userFullName = findViewById(R.id.settings_user_full_name);
        userStatus = findViewById(R.id.settings_user_status);
        userRelationStatus = findViewById(R.id.settings_user_relationship_status);
        userDOB = findViewById(R.id.settings_user_birth_date);
        userCountry = findViewById(R.id.settings_user_country);
        userGender = findViewById(R.id.settings_user_gender);
        updateInfoBtn = findViewById(R.id.settings_update_info_btn);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        loadingBar = new ProgressDialog(this);

        getInformation();

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, galleryPic);

            }
        });

        updateInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loadingBar.setTitle("Updating.... ");
                loadingBar.setMessage("Please wait, while we update your information... ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                HashMap userInfoMap = new HashMap();

                userInfoMap.put("username", userName.getText().toString());
                userInfoMap.put("fullname", userFullName.getText().toString());
                userInfoMap.put("country", userCountry.getText().toString());
                userInfoMap.put("userid", currentUserId);
                userInfoMap.put("status", userStatus.getText().toString());
                userInfoMap.put("gender", userGender.getText().toString());
                userInfoMap.put("dob", userDOB.getText().toString());
                userInfoMap.put("relationshipstatus", userRelationStatus.getText().toString());
                userInfoMap.put("profileimage", downloadUrl);

                usersRef.child(currentUserId).updateChildren(userInfoMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Your Information has been updated successfully", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            SendUserToMainActivity();
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }

                    }
                });
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        // For get the image

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            //  to store image inti database
            if(resultCode==RESULT_OK)
            {
                loadingBar.setTitle("Profile image ");
                loadingBar.setMessage("Please wait, while we uploading profile image... ");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                profilePicture.setImageURI(resultUri);

                final StorageReference filePath = userProfileImgRef.child(currentUserId+".jpg");
                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                downloadUrl = uri.toString();
                                loadingBar.dismiss();
                                Log.d("ronak", downloadUrl);
                            }
                        });
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Error: Image can be cropped try again", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void getInformation()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    // get the values
                    String image = dataSnapshot.child("profileimage").getValue().toString();
                    String username = dataSnapshot.child("username").getValue().toString();
                    String fullname = dataSnapshot.child("fullname").getValue().toString();
                    String country = dataSnapshot.child("country").getValue().toString();
                    String dob = dataSnapshot.child("dob").getValue().toString();
                    String gender = dataSnapshot.child("gender").getValue().toString();
                    String status = dataSnapshot.child("status").getValue().toString();
                    String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    // Set the values
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePicture);
                    userName.setText(username);
                    userFullName.setText(fullname);
                    userCountry.setText(country);
                    userDOB.setText(dob);
                    userGender.setText(gender);
                    userStatus.setText(status);
                    userRelationStatus.setText(relationshipstatus);

                    downloadUrl = image;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}