/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dertyp7214.appstore.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Activity context;
    private List<User> users;
    private OnClick click;

    public UserAdapter(Activity context, List<User> users, OnClick click) {
        this.users = users;
        this.context = context;
        this.click = click;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User item = users.get(position);

        holder.img.setImageDrawable(item.getProfileImage());
        holder.img.setOnClickListener(click.onClick(item.getUid()));

    }

    public interface OnClick {
        View.OnClickListener onClick(String uid);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        ViewHolder(View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.user_img);

        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class User {
        private final String uid, name, email, created_at;
        private final Drawable profileImage;

        public User(String uid, String name, String email, String created_at, Drawable profileImage) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.created_at = created_at;
            this.profileImage = profileImage;
        }

        public String getName() {
            return name;
        }

        public String getUid() {
            return uid;
        }

        public String getEmail() {
            return email;
        }

        public String getCreatedAt() {
            return created_at;
        }

        public Drawable getProfileImage() {
            return profileImage;
        }
    }
}
