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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.github.File;
import com.dertyp7214.githubsource.ui.ViewFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    private Activity context;
    private List<File> fileList;
    public static ProgressBar progressBar;

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
            holder.subTitle.setText(getSize(item.getSize()));
            getFileIcon(item.getName(), holder.icon);
            holder.view.setOnClickListener(v -> {
                holder.progressBar.setIndeterminate(true);
                holder.progressBar.setVisibility(View.VISIBLE);
                progressBar = holder.progressBar;
                Intent intent = new Intent(context, ViewFile.class);
                intent.putExtra("filename", item.getName());
                intent.putExtra("url", getString(item.getJson(),"download_url"));
                context.startActivity(intent);
            });
        } else {
            holder.subTitle.setText("");
            holder.icon.setImageDrawable(context.getDrawable(R.drawable.ic_folder_black_24dp));
            holder.icon.setColorFilter(GitHubSource.colorStyle.getAccentColor());
            holder.view.setOnClickListener(v -> GitHubSource.repository.addToPath(item.getName(), context));
        }
        holder.title.setText(item.getName());
    }

    private String getSize(long size){
        if(size>1000000000000L) return String.valueOf((double) (size/1000000000000L))+"TB";
        if(size>1000000000) return String.valueOf((double) (size/1000000000))+"GB";
        if(size>1000000) return String.valueOf((double) (size/1000000))+"MB";
        if(size>1000) return String.valueOf((double) (size/1000))+"KB";
        return String.valueOf(size)+"Bytes";
    }

    private void getFileIcon(String filename, ImageView imageView) {
        String ending = filename.split("\\.")[filename.split("\\.").length - 1];
        switch (ending) {
            case "cmd":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_command));
                imageView.setColorFilter(GitHubSource.colorStyle.getAccentColor());
                break;
            case "bat":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_command));
                imageView.setColorFilter(GitHubSource.colorStyle.getAccentColor());
                break;
            case "sh":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_command));
                imageView.setColorFilter(GitHubSource.colorStyle.getAccentColor());
                break;
            case "md":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_markdown));
                imageView.setColorFilter(GitHubSource.colorStyle.getAccentColor());
                break;
            case "gradle":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_gradle_file));
                break;
            case "gradlew":
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_gradle_file));
                break;
            case "properties":
                if(filename.startsWith("gradle"))
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_gradle_file));
                else
                    imageView.setImageDrawable(context.getDrawable(R.drawable.ic_properties));
                break;
            default:
                imageView.setImageDrawable(context.getDrawable(R.drawable.ic_xml));
                imageView.setColorFilter(GitHubSource.colorStyle.getAccentColor());
                break;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title, subTitle;
        View view;
        ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.img_icon);
            title = itemView.findViewById(R.id.txt_title);
            subTitle = itemView.findViewById(R.id.txt_subtitle);
            view = itemView.findViewById(R.id.view);
            progressBar = itemView.findViewById(R.id.progressBar);

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
