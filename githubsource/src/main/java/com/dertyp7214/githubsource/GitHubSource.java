/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;

import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.dertyp7214.githubsource.ui.MainScreen;

public class GitHubSource {

    public static ColorStyle colorStyle;
    public static Repository repository;

    @SuppressLint("StaticFieldLeak")
    private static GitHubSource instance;

    private Activity activity;

    private GitHubSource(Activity activity, Repository repo){
        this.activity=activity;
        repository = repo;
        instance=this;
    }

    public static GitHubSource getInstance(Activity activity, Repository repo){
        if(instance==null)
            new GitHubSource(activity, repo);
        repository = repo;
        return instance;
    }

    public GitHubSource setColorStyle(ColorStyle cStyle){
        colorStyle = cStyle;
        return this;
    }

    public void open(){
        activity.startActivity(new Intent(activity, MainScreen.class));
    }
}
