/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.adapter.FileAdapter;
import com.dertyp7214.githubsource.github.File;
import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.dertyp7214.githubsource.helpers.DefaultColorStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainScreen extends AppCompatActivity {

    private int PERMISSIONS = 7214;
    private Repository repository;
    private List<File> files = new ArrayList<>();
    private FileAdapter fileAdapter;
    private static List<Activity> activities = new ArrayList<>();
    private Toolbar toolbar;
    private ColorStyle colorStyle;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        colorStyle = new DefaultColorStyle();

        if (GitHubSource.colorStyle != null)
            colorStyle = GitHubSource.colorStyle;

        getWindow().setStatusBarColor(colorStyle.getPrimaryColorDark());
        getWindow().setNavigationBarColor(colorStyle.getPrimaryColor());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        setToolbarIconColor(colorStyle.getPrimaryColor(), toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::presentActivity);
        fab.setBackgroundTintList(ColorStateList.valueOf(colorStyle.getAccentColor()));
        fab.setColorFilter(isColorBright(colorStyle.getAccentColor())?Color.BLACK:Color.WHITE);

        repository = GitHubSource.repository;

        if(!repository.hasCalls()){
            new MaterialDialog.Builder(this)
                    .title("Error")
                    .content(repository.getMessage())
                    .positiveText(android.R.string.ok)
                    .onPositive((dialog, which) -> finish())
                    .show();
        } else {

            activities.add(this);

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
    }

    public void setToolbarIconColor(@ColorInt int toolbarColor, Toolbar toolbar){
        int tintColor = isColorBright(toolbarColor) ? Color.BLACK : Color.WHITE;
        toolbar.setBackgroundColor(toolbarColor);
        for(ImageView imageButton : findChildrenByClass(ImageView.class, toolbar)) {
            Drawable drawable = imageButton.getDrawable();
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
            imageButton.setImageDrawable(drawable);
        }
        for (TextView textView : findChildrenByClass(TextView.class, toolbar)) {
            textView.setTextColor(tintColor);
            textView.setHintTextColor(tintColor);
        }
    }

    public static boolean isColorBright(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness < 0.5;
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
        repository.goBack();
        activities.remove(this);
        super.onBackPressed();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        MenuItem close_all = menu.findItem(R.id.action_close_all);
        MenuItem share = menu.findItem(R.id.action_share);

        if (isColorBright(colorStyle.getPrimaryColor())) {
            close_all.getIcon().setTint(Color.BLACK);
            share.getIcon().setTint(Color.BLACK);
        } else {
            close_all.getIcon().setTint(Color.WHITE);
            share.getIcon().setTint(Color.WHITE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_close_all) {
            Collections.reverse(activities);
            for(Activity activity : activities)
                activity.finish();
            return true;
        } else if (id == R.id.action_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            new MaterialDialog.Builder(this)
                    .title(getString(R.string.share))
                    .content(getString(R.string.share_repo_current))
                    .positiveText(R.string.repo)
                    .onPositive((dialog, which) -> {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, repository.getRepoUrl());
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.repo)));
                    })
                    .negativeText(R.string.current_path)
                    .onNegative((dialog, which) -> {
                        sendIntent.putExtra(Intent.EXTRA_TEXT, repository.getCurrentUrl());
                        startActivity(Intent.createChooser(sendIntent, getString(R.string.current_path)));
                    })
                    .show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void presentActivity(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(this, view, "transition");
        int revealX = (int) (view.getX() + view.getWidth() / 2);
        int revealY = (int) (view.getY() + view.getHeight() / 2);

        Intent intent = new Intent(this, Info.class);
        intent.putExtra(Info.EXTRA_CIRCULAR_REVEAL_X, revealX);
        intent.putExtra(Info.EXTRA_CIRCULAR_REVEAL_Y, revealY);

        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    private <V extends View> Collection<V> findChildrenByClass(Class<V> clazz, ViewGroup... viewGroups) {
        Collection<V> collection = new ArrayList<>();
        for(ViewGroup viewGroup : viewGroups)
            collection.addAll(gatherChildrenByClass(viewGroup, clazz, new ArrayList<>()));
        return collection;
    }

    private <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {

        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V)child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }
}
