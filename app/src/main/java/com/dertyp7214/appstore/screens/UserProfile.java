/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.dev.Logs;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import static com.dertyp7214.appstore.Config.API_URL;

public class UserProfile extends Utils {

    private static HashMap<String, User> userHashMap = new HashMap<>();
    private User user = new User("Error", "Error", "Error", "Error");
    private Drawable userImage;
    private ImageView profileImageView;
    private TextView txt_name, txt_mail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        logs = Logs.getInstance(this);

        themeStore = ThemeStore.getInstance(this);
        profileImageView = findViewById(R.id.user_image);
        txt_name = findViewById(R.id.txt_name);
        txt_mail = findViewById(R.id.txt_email);

        Bundle extras = getIntent().getExtras();

        setUser((String) checkExtraKey(extras, "uid"));
    }

    private void setUser(String uid) {
        new Thread(() -> {
            if (! userHashMap.containsKey(uid)) {
                try {
                    JSONObject jsonObject = new JSONObject(
                            getWebContent(Config.API_URL + "/apps/user.php?type=json&uid=" + uid));
                    user = new User(jsonObject.getString("name"), jsonObject.getString("email"),
                            jsonObject.getString("created_at"), jsonObject.getString("uid"));
                    userImage = getUserImage(uid);
                    user.setUserImage(userImage);
                    userHashMap.put(uid, user);
                } catch (Exception e) {
                    e.printStackTrace();
                    logs.error("setUser", e.toString());
                    userImage = getResources().getDrawable(R.mipmap.ic_launcher, null);
                }
            } else
                user = userHashMap.get(uid);
            runOnUiThread(() -> {
                SpannableString email = new SpannableString(user.getEmail());
                email.setSpan(new UnderlineSpan(), 0, email.length(), 0);
                profileImageView.setImageDrawable(user.getUserImage());
                txt_name.setText(user.getName());
                txt_mail.setText(email);
                txt_mail.setOnClickListener(v -> startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:" + user.getEmail()))));
                setTitle(user.getName());
                setColors();
                setBackButton();
            });
        }).start();
    }

    private Drawable getUserImage(String uid) throws Exception {
        Drawable profilePic;
        String url = API_URL + "/apps/pic/" + URLEncoder.encode(uid, "UTF-8")
                .replace("+", "_") + ".png";
        File imgFile = new File(getFilesDir(), uid + ".png");
        if (! imgFile.exists()) {
            if (Config.SERVER_ONLINE) {
                profilePic = Utils.drawableFromUrl(this, url);
                FileOutputStream fileOutputStream = new FileOutputStream(imgFile);
                drawableToBitmap(profilePic)
                        .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);

            } else
                profilePic = getResources().getDrawable(R.mipmap.ic_launcher, null);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            profilePic = new BitmapDrawable(getResources(),
                    BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options));
        }
        return profilePic;
    }

    private class User {
        private String name, email, createdAt, uid;
        private Drawable userImage;

        private User(String name, String email, String createdAt, String uid) {
            this.name = name;
            this.email = email;
            this.createdAt = createdAt;
            this.uid = uid;
        }

        private void setUserImage(Drawable userImage) {
            this.userImage = userImage;
        }

        private Drawable getUserImage() {
            return this.userImage != null ? userImage : getResources()
                    .getDrawable(R.mipmap.ic_launcher, null);
        }

        private String getCreatedAt() {
            return createdAt;
        }

        private String getEmail() {
            return email;
        }

        private String getName() {
            return name;
        }

        private String getUid() {
            return uid;
        }
    }
}
