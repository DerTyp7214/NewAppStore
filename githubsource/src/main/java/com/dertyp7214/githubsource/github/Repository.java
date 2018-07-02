/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.github;

import android.content.Context;
import android.content.Intent;

import com.dertyp7214.githubsource.ui.MainScreen;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Repository {

    private final static String BASE_URL = "https://api.github.com/repos/{user}/{repo}";
    private final static String USER = "{user}";
    private final static String REPO = "{repo}";

    private final String user, repo;
    public static String api_key;
    private String path = "";
    private JSONObject rootObject;

    public Repository(String user, String repo, String apikey){
        this.user=user;
        this.repo=repo;
        api_key=apikey;
        try {
            rootObject = new JSONObject(getJSONObject(getBaseUrl()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentUrl(){
        return getString("html_url") + "/tree/" + getString("default_branch") + "/" + path;
    }

    public String getRepoUrl(){
        return getString("clone_url");
    }

    private String getJSONObject(String url){
        try {
            URL web = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) web.openConnection();
            connection.setRequestProperty("Authorization", "token "+api_key);
            BufferedReader in;

            if(api_key==null || api_key.equals(""))
                in = new BufferedReader(new InputStreamReader(web.openStream()));
            else
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder ret = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                ret.append(inputLine);

            in.close();
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"message\": \"Something went wrong.\"}";
        }
    }

    public boolean hasCalls(){
        return rootObject != null && !rootObject.has("message");
    }

    public JSONObject getRootObject(){
        return rootObject;
    }

    public String getMessage(){
        return getString("message");
    }

    private String getString(String key){
        try {
            return rootObject.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    private String getBaseUrl(){
        return BASE_URL.replace(USER, user).replace(REPO, repo);
    }

    public String getTitle(){
        if(path.length()<1)
            return getString("name");
        return path.split("/")[path.split("/").length-1];
    }

    public String getRepoName(){
        return getString("name");
    }

    public void setPath(String path){
        this.path=path;
    }

    public void addToPath(String path){
        this.path+=path+"/";
    }

    public void addToPath(String path, Context context){
        addToPath(path);
        context.startActivity(new Intent(context, MainScreen.class));
    }

    public void goBack(){
        String[] folders = path.split("/");
        StringBuilder ret = new StringBuilder();
        for(int i=0;i<folders.length-1;i++)
            ret.append(folders[i]).append("/");
        if(ret.toString().equals("/"))
            path = "";
        else
            path = ret.toString();
    }

    public void goBack(Context context){
        goBack();
        context.startActivity(new Intent(context, MainScreen.class));
    }

    public List<File> getContentList(){
        try{
            List<File> files = new ArrayList<>();
            List<File> dirs = new ArrayList<>();

            JSONArray content = new JSONArray(getJSONObject(getString("contents_url").replace("{+path}", path)));

            for(int i=0;i<content.length();i++){
                JSONObject file = content.getJSONObject(i);
                switch (file.getString("type")){
                    case "file":
                        files.add(new File(file.getString("name"), file.getLong("size"), file));
                        break;
                    case "dir":
                        dirs.add(new Folder(file.getString("name"), file));
                        break;
                }
            }

            dirs.addAll(files);

            return dirs;
        }catch (Exception ignored){
            return new ArrayList<>();
        }
    }
}
