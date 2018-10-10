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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TranslatorAdapter extends RecyclerView.Adapter<TranslatorAdapter.ViewHolder> {

    private Context context;
    private List<Translator> translators;

    public TranslatorAdapter(Context context, List<Translator> translators) {
        this.translators = translators;
        this.context = context;
    }

    @NonNull
    @Override
    public TranslatorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.translator_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TranslatorAdapter.ViewHolder holder, int position) {
        Translator item = translators.get(position);

        holder.userName.setText(item.getName());
        holder.userPicture.setImageDrawable(item.getProfilePicture());

        holder.view.setOnClickListener(v -> openGitHubProfile(item.getUserName()));
    }

    private void openGitHubProfile(String userName) {
        openUrl("https://github.com/" + userName);
    }

    private void openUrl(String url) {
        Intent gitIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(gitIntent);
    }

    @Override
    public int getItemCount() {
        return translators.size();
    }

    public static class Translator {
        private String userName, name;
        private Drawable profilePicture;

        public Translator(String userName, String name, Drawable profilePicture) {
            this.userName = userName;
            this.name = name;
            this.profilePicture = profilePicture;
        }

        public String getUserName() {
            return userName;
        }

        public String getName() {
            return name;
        }

        public Drawable getProfilePicture() {
            return profilePicture;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView userPicture;
        TextView userName;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            userPicture = itemView.findViewById(R.id.profilePicture);
            userName = itemView.findViewById(R.id.userName);
            view = itemView.findViewById(R.id.view);
        }
    }
}
