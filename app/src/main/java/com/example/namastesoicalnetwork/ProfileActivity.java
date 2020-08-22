package com.example.namastesoicalnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.namastesoicalnetwork.model_class.Posts;
import com.example.namastesoicalnetwork.viewHolder.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    private CircleImageView profilePicture;
    private TextView profileUserName, profileUserStatus;
    private RecyclerView userPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, specificUserPostsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePicture = findViewById(R.id.profile_activity_profile_image);
        profileUserName = findViewById(R.id.profile_user_name);
        profileUserStatus = findViewById(R.id.profile_user_status);

        userPostList = findViewById(R.id.profile_user_all_post);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        userPostList.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        specificUserPostsRef = FirebaseDatabase.getInstance().getReference().child("Specific Users Posts").child(currentUserId);

        getUserInformation();

        DisplayUsersAllPosts();
    }

    private void DisplayUsersAllPosts()
    {
        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>()
                .setQuery(specificUserPostsRef , Posts.class)
                .build();

        FirebaseRecyclerAdapter<Posts, PostViewHolder> adapter = new FirebaseRecyclerAdapter<Posts, PostViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostViewHolder holder, int position, @NonNull Posts model)
            {
                final String postKey = getRef(position).getKey();

                Picasso.get().load(model.profileimage).placeholder(R.drawable.profile).into(holder.profileImage);
                holder.userName.setText(model.username);
                holder.date.setText(model.date);
                holder.time.setText(model.time);
                holder.description.setText(model.description);
                Picasso.get().load(model.postimage).into(holder.postImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        SendUserToClickPostActivity(postKey);
                    }
                });

            }

            @NonNull
            @Override
            public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_layout, parent, false);
                return new PostViewHolder(view);
            }
        };

        userPostList.setAdapter(adapter);
        adapter.startListening();
    }




    private void getUserInformation()
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
                    String status = dataSnapshot.child("status").getValue().toString();
                    //String relationshipstatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    // Set the values
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(profilePicture);
                    profileUserName.setText(username);
                    profileUserStatus.setText(status);
                   // relationshipstatus.setText(relationshipstatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToClickPostActivity(String postKey)
    {
        Intent intent = new Intent(ProfileActivity.this, ClickOnPostActivity.class);
        intent.putExtra("postkey", postKey);
        startActivity(intent);
    }


}