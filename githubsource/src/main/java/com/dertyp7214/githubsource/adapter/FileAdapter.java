/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.adapter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.github.File;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Activity context;
    private List<File> fileList;

    public FileAdapter(Activity context, List<File> fileList){
        this.fileList=fileList;
        this.context=context;
    }

    @NonNull
    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.git_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter.ViewHolder holder, int position) {
        File item = fileList.get(position);

        if(item.isFile()) {
            holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_file_black_24dp));
            holder.view.setOnClickListener(v -> {
                openUrl(getString(item.getJson(),"download_url"));
            });
        } else {
            holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_folder_black_24dp));
            holder.view.setOnClickListener(v -> GitHubSource.repository.addToPath(item.getName(), context));
        }
        holder.title.setText(item.getName());
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title, subTitle;
        View view;

        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.img_icon);
            title = itemView.findViewById(R.id.txt_title);
            subTitle = itemView.findViewById(R.id.txt_subtitle);
            view = itemView.findViewById(R.id.view);

        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    private String getString(JSONObject jsonObject, String key){
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    private void openUrl(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

}
