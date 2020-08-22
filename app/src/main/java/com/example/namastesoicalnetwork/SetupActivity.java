package com.example.namastesoicalnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
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
import com.theartofdev.edmodo.cropper.CropOverlayView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{
    private EditText userName, userFullName, userCountry;
    private Button saveInfoBtn;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private String currentUserId;
    private StorageReference userProfileImgRef;
    private ProgressDialog loadingBar;
    final static int galleryPic = 1;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        currentUserId =  mAuth.getCurrentUser().getUid();

        userName = findViewById(R.id.setup_user_name);
        userFullName = findViewById(R.id.setup_full_name);
        userCountry = findViewById(R.id.setup_user_country);
        saveInfoBtn = findViewById(R.id.setup_account_btn);
        profileImage = findViewById(R.id.setup_profile_image);

        saveInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String uName = userName.getText().toString();
                String fName = userFullName.getText().toString();
                String uCountry = userCountry.getText().toString();

                if(uName.isEmpty())
                {
                    Toast.makeText(SetupActivity.this, "Please enter the valid User Name", Toast.LENGTH_SHORT).show();
                }
                else if(fName.isEmpty())
                {
                    Toast.makeText(SetupActivity.this, "Please enter the valid full Name", Toast.LENGTH_SHORT).show();
                }
                else if(uCountry.isEmpty())
                {
                    Toast.makeText(SetupActivity.this, "Please enter your country name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Checking credentials..");
                    loadingBar.setMessage("Please wait, while your account is creating... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    SaveInfoToDatabase(uName,fName,uCountry);
                }

            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // To pick the image from the gallery

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, galleryPic);
            }
        });

        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
     //  For crop the image
        if(requestCode == galleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

            //////////////
            profileImage.setImageURI(imageUri);
            ///////////////
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
                profileImage.setImageURI(resultUri);

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

    private void SaveInfoToDatabase(String uName, String fName, String uCountry)
    {
        String currentUserId = mAuth.getCurrentUser().getUid();

        HashMap userInfoMap = new HashMap();

        userInfoMap.put("username", uName);
        userInfoMap.put("fullname", fName);
        userInfoMap.put("country", uCountry);
        userInfoMap.put("userid", currentUserId);
        userInfoMap.put("status", "Namaste, I am available... ");
        userInfoMap.put("gender", "none");
        userInfoMap.put("dob", "none");
        userInfoMap.put("relationshipstatus", "none");
        userInfoMap.put("profileimage", downloadUrl);

        usersRef.child(currentUserId).updateChildren(userInfoMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(SetupActivity.this, "Your account has been created successfully", Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                    SendUserToMainActivity();
                }
                else
                {
                    Toast.makeText(SetupActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    loadingBar.dismiss();
                }


            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(SetupActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}