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
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.SimpleTimeZone;

public class AddNewPostActivity extends AppCompatActivity
{
    private ImageButton selectNewPostBtn;
    private EditText postDescription;
    private Button updatePostBtn;
    final static int galleryPic = 1;
    private Toolbar mToolbar;
    private  Uri imageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference postsRef, usersRef,specificUserPostsRef;
    private StorageReference postImageRef;
    private String currentUserId;
    private String saveCurrentDate, saveCurrentTime,postRandomName, downloadImageUri;
    private  String description;
    private ProgressDialog loadingBar;
    private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_post);

        mToolbar = findViewById(R.id.update_post_page_appbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add New Post");

        selectNewPostBtn = findViewById(R.id.select_new_post);
        postDescription = findViewById(R.id.new_post_description);
        updatePostBtn = findViewById(R.id.update_post_btn);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postImageRef = FirebaseStorage.getInstance().getReference().child("Post Images");
        specificUserPostsRef = FirebaseDatabase.getInstance().getReference().child("Specific Users Posts");

        selectNewPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SelectImageFromGallery();
            }
        });

        updatePostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                description = postDescription.getText().toString();

                if(imageUri == null)
                {
                    Toast.makeText(AddNewPostActivity.this, "Please select the image", Toast.LENGTH_SHORT).show();
                }
                else if(description.isEmpty())
                {
                    Toast.makeText(AddNewPostActivity.this, "Please write post description..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Post Uploading");
                    loadingBar.setMessage("Please wait, while your post is uploading... ");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    StoreImageToStorage(imageUri);
                }
            }
        });


    }

    private void StoreInformationToDatabase()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userName = dataSnapshot.child("username").getValue().toString();
                    String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                    final String postId = currentUserId + postRandomName;

                    final HashMap postInfo = new HashMap();

                    postInfo.put("username", userName);
                    postInfo.put("profileimage", profileImage);
                    postInfo.put("description", description);
                    postInfo.put("userid", currentUserId);
                    postInfo.put("postimage", downloadImageUri);
                    postInfo.put("date", saveCurrentDate);
                    postInfo.put("time", saveCurrentTime);
                    postInfo.put("postid", postId);

                    postsRef.child(postId).updateChildren(postInfo).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            specificUserPostsRef.child(currentUserId).child(postId).updateChildren(postInfo).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(AddNewPostActivity.this, "Your Post has been Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                        SendUserToMainActivity();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(AddNewPostActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void StoreImageToStorage(Uri imageUri)
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMMM yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());
        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filepath = postImageRef.child(imageUri.getLastPathSegment()+ postRandomName + ".jpg");

        filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        downloadImageUri = uri.toString();
                        Toast.makeText(AddNewPostActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                        StoreInformationToDatabase();
                    }
                });
            }
        });

    }


    private void SelectImageFromGallery()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, galleryPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {
            imageUri = data.getData();

            selectNewPostBtn.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);

    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(AddNewPostActivity.this, MainActivity.class);
        startActivity(intent);
    }
}