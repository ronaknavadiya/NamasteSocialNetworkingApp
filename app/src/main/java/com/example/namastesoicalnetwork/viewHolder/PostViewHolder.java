package com.example.namastesoicalnetwork.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.namastesoicalnetwork.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public CircleImageView profileImage;
    public TextView userName, date, time, description;
    public ImageView postImage;

    public PostViewHolder(@NonNull View itemView)
    {
        super(itemView);

        profileImage = itemView.findViewById(R.id.post_profile_image);
        userName = itemView.findViewById(R.id.post_user_name);
        date = itemView.findViewById(R.id.post_date);
        time = itemView.findViewById(R.id.post_time);
        description = itemView.findViewById(R.id.post_description);
        postImage = itemView.findViewById(R.id.post_image);

    }

    @Override
    public void onClick(View v) {

    }
}
