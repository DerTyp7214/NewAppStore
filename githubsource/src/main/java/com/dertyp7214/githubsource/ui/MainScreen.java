/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.adapter.FileAdapter;
import com.dertyp7214.githubsource.github.File;
import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.dertyp7214.githubsource.helpers.DefaultColorStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainScreen extends AppCompatActivity {

    private int PERMISSIONS = 7214;
    private Repository repository;
    private List<File> files = new ArrayList<>();
    private FileAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        ColorStyle colorStyle = new DefaultColorStyle();
        repository = GitHubSource.repository;

        if(GitHubSource.colorStyle!=null)
            colorStyle=GitHubSource.colorStyle;

        getWindow().setStatusBarColor(colorStyle.getPrimaryColorDark());
        getWindow().setNavigationBarColor(colorStyle.getPrimaryColor());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(colorStyle.getPrimaryColor()));
        setTitle(repository.getTitle());

        fileAdapter = new FileAdapter(this, files);

        RecyclerView recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);

        new Thread(() -> {
            files.clear();
            files.addAll(repository.getContentList());
            runOnUiThread(() -> fileAdapter.notifyDataSetChanged());
        }).start();

        checkPermissions();
    }

    private void checkPermissions() {
        ActivityCompat.requestPermissions(this,
                permissons().toArray(new String[0]),
                PERMISSIONS);
    }

    @NonNull
    private List<String> permissons() {
        return new ArrayList<>(Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        GitHubSource.repository.goBack();
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        GitHubSource.repository.goBack();
        onBackPressed();
        return true;
    }
}
