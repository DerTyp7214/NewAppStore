/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.SearchAdapter;
import com.dertyp7214.appstore.adapter.UserAdapter;
import com.dertyp7214.appstore.components.MaterialSearchBar;
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.fragments.FragmentAbout;
import com.dertyp7214.appstore.fragments.FragmentAppGroups;
import com.dertyp7214.appstore.fragments.FragmentMyApps;
import com.dertyp7214.appstore.fragments.TabFragment;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.helpers.SessionManager;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static com.dertyp7214.appstore.Config.API_URL;

public class MainActivity extends Utils
        implements NavigationView.OnNavigationItemSelectedListener,
        MaterialSearchBar.OnSearchActionListener {

    private ViewPagerAdapter adapter;
    private SearchAdapter searchAdapter;
    private int id = 0;
    private Random random;
    private TabLayout tabLayout;
    private NavigationView navView;
    private Drawable profilePic;
    private MaterialSearchBar searchBar;
    private DrawerLayout drawer;
    private View app_bar;
    private FragmentAbout fragmentAbout;

    private List<SearchItem> appItems = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        themeStore = ThemeStore.resetInstance(this);
        logs = new Logs(this);

        checkAppDir();
        checkForOldAppStore();
        setNavigationBarColor(this, getWindow().getDecorView(), themeStore.getPrimaryColor(),
                300);

        drawer = findViewById(R.id.drawer_layout);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
        searchBar.setRoundedSearchBarEnabled(true);
        searchBar.setupRoundedSearchBarEnabled(getSettings(this).getInt("search_bar_radius", 20));
        searchBar.setCardViewElevation(10);
        searchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        app_bar = findViewById(R.id.app_bar);
        app_bar.setBackgroundColor(ThemeStore.getInstance(this).getPrimaryColor());
        app_bar.setPadding(0, getStatusBarHeight(), 0, 0);

        int margin = getMargin();
        setMargins(searchBar, margin, margin, margin, margin);

        navView = findViewById(R.id.nav_view);
        navView.setCheckedItem(R.id.nav_home);
        navView.setPadding(0, 0, 0, getNavigationBarHeight());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        ViewPager viewPager = findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        addFragment(new FragmentAppGroups());
        addFragment(new FragmentMyApps());
        fragmentAbout = new FragmentAbout(this);
        adapter.addFragment(fragmentAbout, getString(R.string.mal_title_about));

        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabBar);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(themeStore.getPrimaryTextColor());
        tabLayout.setTabTextColors(themeStore.getPrimaryTextColor(),
                themeStore.getPrimaryTextColor());

        searchAdapter = new SearchAdapter(this, appItems);

        RecyclerView searchRecView = findViewById(R.id.search_view);
        searchRecView.setLayoutManager(new LinearLayoutManager(this));
        searchRecView.setPadding(0, 0, 0, getNavigationBarHeight());
        searchRecView.setAdapter(searchAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                app_bar.setBackgroundColor(themeStore
                        .getPrimaryHue((int) (((float) position + positionOffset) * 36)));
                getWindow().setNavigationBarColor(themeStore
                        .getPrimaryHue((int) (((float) position + positionOffset) * 36)));
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        navView.setCheckedItem(R.id.nav_home);
                        break;
                    case 1:
                        navView.setCheckedItem(R.id.nav_myapps);
                        break;
                    case 2:
                        navView.setCheckedItem(R.id.nav_about);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setUpNavView();
    }

    private int getMargin() {
        return dpToPx(6) + dpToPx(2);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onResume() {
        themeStore = ThemeStore.resetInstance(this);
        super.onResume();
    }

    @Override
    protected void onRestart() {
        themeStore = ThemeStore.resetInstance(this);
        super.onRestart();
    }

    @Override
    protected void onStart() {
        themeStore = ThemeStore.resetInstance(this);
        super.onStart();
    }

    private void setUpNavView() {
        new Thread(() -> {
            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
            HashMap<String, String> user = db.getUserDetails();
            String userID = user.get("uid");
            String userName = user.get("name");
            String userEmail = user.get("email");
            View bg = findViewById(R.id.nav_bg);
            ImageView img = findViewById(R.id.user_image);
            TextView name = findViewById(R.id.txt_name);
            TextView email = findViewById(R.id.txt_email);
            profilePic = getDrawable(R.mipmap.ic_launcher_round);
            runOnUiThread(() -> {
                try {
                    name.setText(userName);
                    email.setText(userEmail);
                    setAccounts();
                } catch (Exception e) {
                    e.printStackTrace();
                    logs.error("setUpNavView - runOnUiThread",
                            e.toString() + "\n" + e.getMessage());
                    sleep(100);
                    setUpNavView();
                }
            });
            try {
                File file = new File(getFilesDir(), userID + ".png");
                if (file.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    profilePic = new BitmapDrawable(getResources(),
                            BitmapFactory.decodeFile(file.getAbsolutePath(), options));
                } else {
                    String url = API_URL + "/apps/pic/" + URLEncoder.encode(userID, "UTF-8")
                            .replace("+", "_") + ".png";
                    profilePic = Utils.drawableFromUrl(this, url);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    drawableToBitmap(profilePic)
                            .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logs.error("setUpNavView", e.toString() + "\n" + e.getMessage());
            }
            runOnUiThread(() -> {
                try {
                    int color = Palette.from(
                            Utils.drawableToBitmap(Utils.userImageHashMap.get(userID + "_bg")))
                            .generate()
                            .getDominantColor(ThemeStore.getInstance(this).getPrimaryColor());
                    if (Utils.isColorBright(color)) {
                        name.setTextColor(Color.BLACK);
                        email.setTextColor(Color.BLACK);
                    } else {
                        name.setTextColor(Color.WHITE);
                        email.setTextColor(Color.WHITE);
                    }
                    bg.setBackground(Utils.userImageHashMap.get(userID + "_bg"));
                    img.setImageDrawable(profilePic);
                    img.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, UserProfile.class);
                        intent.putExtra("uid", userID);
                        Pair<View, String> icon = Pair.create(img, "profilePic");
                        Pair<View, String> nameTransition = Pair.create(name, "name");
                        Pair<View, String> emailTransition = Pair.create(email, "email");
                        ActivityOptions options =
                                ActivityOptions
                                        .makeSceneTransitionAnimation(this, icon, nameTransition,
                                                emailTransition);
                        startActivity(intent, options.toBundle());
                        drawer.closeDrawers();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    logs.error("setUpNavView - runOnUiThread",
                            e.toString() + "\n" + e.getMessage());
                    sleep(100);
                    setUpNavView();
                }
            });
        }).start();
    }

    private void setAccounts() {
        SQLiteHandler db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();
        String userID = user.get("uid");
        List<UserAdapter.User> users = new ArrayList<>();
        AccountManager am = AccountManager.get(this);
        UserAdapter userAdapter = new UserAdapter(this, users, uid -> v -> {
            UserAdapter.User u = null;
            for (UserAdapter.User us : users)
                if (us.getUid().equals(uid))
                    u = us;
            if (u != null) {
                SessionManager session = new SessionManager(getApplicationContext());
                session.setLogin(true);
                db.deleteUsers();
                db.addUser(u.getName(), u.getEmail(), uid, u.getCreatedAt());
                startActivity(this, Splashscreen.class);
                drawer.closeDrawers();
                finish();
            }
        });
        new Thread(() -> {
            for (Account account : am.getAccounts()) {
                try {
                    String uid = am.getUserData(account, "uid");
                    if (! uid.equals(userID))
                        users.add(new UserAdapter.User(uid, am.getUserData(account, "name"),
                                account.name,
                                am.getUserData(account, "created_at"),
                                getUserImage(uid)));
                } catch (Exception e) {
                    logs.error("setAccounts", e.toString());
                }
            }
            runOnUiThread(userAdapter::notifyDataSetChanged);
        }).start();
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        RecyclerView userRecyclerView = findViewById(R.id.rv_users);
        userRecyclerView.setLayoutManager(layoutManager);
        userRecyclerView.setAdapter(userAdapter);
    }

    private Drawable getUserImage(String uid) {
        Drawable profilePic;
        try {
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
        } catch (Exception e) {
            profilePic = getResources().getDrawable(R.mipmap.ic_launcher, null);
        }
        return profilePic;
    }

    private void search(String query) {
        id = random.nextInt();
        Thread thread = new Thread(() -> {
            try {

                int localId = id;
                appItems.clear();

                if (new JSONObject(LocalJSON.getJSON(MainActivity.this)).getBoolean("error"))
                    LocalJSON.setJSON(MainActivity.this, Utils.getWebContent(
                            API_URL + "/apps/list.php?user=" + Config.UID(this)));

                JSONObject jsonObject = new JSONObject(LocalJSON.getJSON(MainActivity.this));

                JSONArray array = jsonObject.getJSONArray("apps");

                List<SearchItem> appItemList = new ArrayList<>();

                for (int i = 0; i < array.length() - 1; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj.getString("title").toLowerCase().contains(query.toLowerCase())
                            && ! query.equals(""))
                        appItemList.add(new SearchItem(obj.getString("title"), obj.getString("ID"),
                                Utils.drawableFromUrl(MainActivity.this, obj.getString("image"))));
                }

                if (id == localId)
                    appItems.addAll(appItemList);

                MainActivity.this.runOnUiThread(() -> searchAdapter.notifyDataSetChanged());

            } catch (Exception ignored) {
            }
        });
        if (thread.isAlive())
            thread.interrupt();
        thread.start();
    }

    private void addFragment(TabFragment fragment) {
        adapter.addFragment(fragment, fragment.getName(this));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        View searchView = findViewById(R.id.searchLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.getVisibility() == View.VISIBLE) {
            View content = findViewById(R.id.content);
            content.setVisibility(View.VISIBLE);
            changeOpacity(searchView, true);
            tabLayout.setVisibility(View.VISIBLE);
        } else if (fragmentAbout.bottomSheetBehavior.getState() == STATE_EXPANDED) {
            fragmentAbout.bottomSheetBehavior.setState(STATE_HIDDEN);
        } else {
            super.onBackPressed();
        }
    }

    private void changeOpacity(View view, boolean hide) {
        float from = hide ? 1F : 0F;
        float to = hide ? 0F : 1F;
        AlphaAnimation animation1 = new AlphaAnimation(from, to);
        animation1.setDuration(300);
        animation1.setFillAfter(true);
        view.startAnimation(animation1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        } else if (id == R.id.nav_myapps) {
            Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        } else if (id == R.id.nav_about) {
            Objects.requireNonNull(tabLayout.getTabAt(2)).select();
        } else if (id == R.id.nav_modules) {
            startActivity(this, ModulesScreen.class);
        } else if (id == R.id.nav_settings) {
            new startActivityAsync(this, SettingsScreen.class).setTime(250)
                    .start(this::startActivity);
        } else if (id == R.id.nav_logout) {
            startActivity(this, LogOut.class);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if (enabled && ! Config.SERVER_ONLINE) searchBar.disableSearch();
        if (! enabled) {
            View searchLayout = findViewById(R.id.searchLayout);
            View content = findViewById(R.id.content);
            changeOpacity(searchLayout, true);
            tabLayout.setVisibility(View.VISIBLE);
            content.setVisibility(View.VISIBLE);
            searchBar.clearSuggestions();
        }
    }

    @Override
    public void onSearchConfirmed(CharSequence text) {
        submit(text);
    }

    private void submit(CharSequence text) {
        search(text.toString());
        View searchLayout = findViewById(R.id.searchLayout);
        View content = findViewById(R.id.content);
        changeOpacity(searchLayout, false);
        tabLayout.setVisibility(View.GONE);
        content.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode) {
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawer.openDrawer(Gravity.START);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                openVoiceRecognizer();
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
    }

    private void openVoiceRecognizer() {
        if (! searchBar.isFocused())
            searchBar.enableSearch();
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, getString(R.string.language_model));
        startActivityForResult(intent, 99);
        searchBar.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 99: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchBar.setText(text.get(0));
                    submit(text.get(0));
                }
                break;
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
