package com.example.namastesoicalnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickOnPostActivity extends AppCompatActivity {
    private ImageView postImage;
    private TextView postDes;
    private Button deletePostBtn, editPostBtn;
    private String postKey;
    private DatabaseReference postRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_on_post);

        postKey = getIntent().getStringExtra("postkey");

        postImage = findViewById(R.id.click_on_post_image);
        postDes = findViewById(R.id.click_on_post_des);
        editPostBtn = findViewById(R.id.click_on_post_edit_post_btn);
        deletePostBtn = findViewById(R.id.click_on_post_delete_post_btn);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        deletePostBtn.setVisibility(View.INVISIBLE);
        editPostBtn.setVisibility(View.INVISIBLE);


        postRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    final String description = dataSnapshot.child("description").getValue().toString();
                    String image = dataSnapshot.child("postimage").getValue().toString();

                    Picasso.get().load(image).placeholder(R.drawable.add_post).into(postImage);
                    postDes.setText(description);

                    String postUserId = dataSnapshot.child("userid").getValue().toString();

                    if(currentUserId.equals(postUserId))
                    {
                        deletePostBtn.setVisibility(View.VISIBLE);
                        editPostBtn.setVisibility(View.VISIBLE);
                    }

                    editPostBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            EditPost(description);

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



         deletePostBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v)
             {
                 DeletePost();
             }
         });

    }

    private void EditPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(this);
        inputField.setText(description);

        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                postRef.child(postKey).child("description").setValue(inputField.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(ClickOnPostActivity.this, "Post has been updated successfully", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ClickOnPostActivity.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    private void DeletePost()
    {
        postRef.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    Toast.makeText(ClickOnPostActivity.this, "Your post has been deleted Successfully", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }

            }
        });
    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(ClickOnPostActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}