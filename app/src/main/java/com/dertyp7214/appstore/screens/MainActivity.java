/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
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
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
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
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.fragments.FragmentAbout;
import com.dertyp7214.appstore.fragments.FragmentAppGroups;
import com.dertyp7214.appstore.fragments.FragmentMyApps;
import com.dertyp7214.appstore.fragments.TabFragment;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.items.SearchItem;
import com.mancj.materialsearchbar.MaterialSearchBar;

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

import static com.dertyp7214.appstore.Config.API_URL;

public class MainActivity extends Utils
        implements NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener {

    private ViewPagerAdapter adapter;
    private SearchAdapter searchAdapter;
    private Thread thread;
    private int id = 0;
    private Random random;
    private ThemeStore themeStore;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private NavigationView navView;
    private Drawable profilePic;
    private MaterialSearchBar searchBar;
    private DrawerLayout drawer;
    private View app_bar;

    private List<SearchItem> appItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = new Random();
        themeStore = ThemeStore.resetInstance(this);
        logs = new Logs(this);

        checkAppDir();
        checkForOldAppStore();

        drawer = findViewById(R.id.drawer_layout);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnSearchActionListener(this);
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

        int margin = dpToPx(8);
        setMargins(searchBar, margin, margin, margin, margin);

        navView = findViewById(R.id.nav_view);
        navView.setPadding(0, 0, 0, getNavigationBarHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(this, toolbar, ThemeStore.getInstance(this).getPrimaryColor(), 300);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        addFragment(new FragmentAppGroups());
        addFragment(new FragmentMyApps());
        adapter.addFragment(new FragmentAbout(), getString(R.string.mal_title_about));

        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabBar);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setSelectedTabIndicatorColor(themeStore.getPrimaryTextColor());
        tabLayout.setTabTextColors(themeStore.getPrimaryTextColor(), themeStore.getPrimaryTextColor());

        searchAdapter = new SearchAdapter(this, appItems);

        RecyclerView searchRecView = findViewById(R.id.search_view);
        searchRecView.setLayoutManager(new LinearLayoutManager(this));
        searchRecView.setPadding(0, 0, 0, getNavigationBarHeight());
        searchRecView.setAdapter(searchAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                app_bar.setBackgroundColor(themeStore.getPrimaryHue((int) (((float) position + positionOffset) * 100)));
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
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

    private int dpToPx(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
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
            String userName = user.get("name");
            String userEmail = user.get("email");
            View bg = findViewById(R.id.nav_bg);
            ImageView img = findViewById(R.id.nav_img);
            TextView name = findViewById(R.id.txt_name);
            TextView email = findViewById(R.id.txt_email);
            profilePic = getDrawable(R.mipmap.ic_launcher_round);
            runOnUiThread(() -> {
                try {
                    name.setText(userName);
                    email.setText(userEmail);
                } catch (Exception e) {
                    e.printStackTrace();
                    logs.info("setUpNavView - runOnUiThread", e.toString() + "\n" + e.getMessage());
                    sleep(100);
                    setUpNavView();
                }
            });
            try {
                File file = new File(getFilesDir(), userName + ".png");
                if (file.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    profilePic = new BitmapDrawable(getResources(), BitmapFactory.decodeFile(file.getAbsolutePath(), options));
                } else {
                    String url = API_URL + "/apps/pic/" + URLEncoder.encode(userName, "UTF-8").replace("+", "_") + ".png";
                    profilePic = Utils.drawableFromUrl(this, url);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    drawableToBitmap(profilePic).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logs.info("setUpNavView", e.toString() + "\n" + e.getMessage());
            }
            int color = Palette.from(Utils.drawableToBitmap(profilePic))
                    .generate()
                    .getDominantColor(ThemeStore.getInstance(this).getPrimaryColor());
            runOnUiThread(() -> {
                try {
                    if (Utils.isColorBright(color)) {
                        name.setTextColor(Color.BLACK);
                        email.setTextColor(Color.BLACK);
                    } else {
                        name.setTextColor(Color.WHITE);
                        email.setTextColor(Color.WHITE);
                    }
                    bg.setBackgroundColor(color);
                    img.setImageDrawable(profilePic);
                } catch (Exception e) {
                    e.printStackTrace();
                    logs.info("setUpNavView - runOnUiThread", e.toString() + "\n" + e.getMessage());
                    sleep(100);
                    setUpNavView();
                }
            });
        }).start();
    }

    private ColorStateList getColorStateList(int disabled, int enabled, int unchecked, int pressed){
        int[][] state = new int[][] {
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed

        };
        int[] color = new int[] {
                disabled,
                enabled,
                unchecked,
                pressed
        };
        return new ColorStateList(state, color);
    }

    private void search(String query){
        id = random.nextInt();
        thread = new Thread(() -> {
            try{

                int localId = id;
                appItems.clear();

                if(new JSONObject(LocalJSON.getJSON(MainActivity.this)).getBoolean("error"))
                    LocalJSON.setJSON(MainActivity.this, Utils.getWebContent(API_URL + "/apps/list.php?user="+Config.UID(this)));

                JSONObject jsonObject = new JSONObject(LocalJSON.getJSON(MainActivity.this));

                JSONArray array = jsonObject.getJSONArray("apps");

                List<SearchItem> appItemList = new ArrayList<>();

                for(int i=0;i<array.length()-1;i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj.getString("title").toLowerCase().contains(query.toLowerCase()) && !query.equals(""))
                        appItemList.add(new SearchItem(obj.getString("title"), obj.getString("ID"), Utils.drawableFromUrl(MainActivity.this, obj.getString("image"))));
                }

                if(id==localId)
                    appItems.addAll(appItemList);

                MainActivity.this.runOnUiThread(() -> searchAdapter.notifyDataSetChanged());

            }catch (Exception ignored){
            }
        });
        if(thread.isAlive())
            thread.interrupt();
        thread.start();
    }

    private void addFragment(TabFragment fragment){
        adapter.addFragment(fragment, fragment.getName(this));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        View searchView = findViewById(R.id.searchLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (searchView.getVisibility() == View.VISIBLE){
            View content = findViewById(R.id.content);
            content.setVisibility(View.VISIBLE);
            changeOpacity(searchView, true);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    private void changeOpacity(View view, boolean hide){
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

        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void setTextColorForMenuItem(MenuItem menuItem, @ColorInt int color) {
        SpannableString spanString = new SpannableString(menuItem.getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(color), 0, spanString.length(), 0);
        menuItem.setTitle(spanString);
    }

    private void resetAllMenuItemsTextColor(NavigationView navigationView) {
        for (int i = 0; i < navigationView.getMenu().size(); i++)
            setTextColorForMenuItem(navigationView.getMenu().getItem(i), Color.DKGRAY);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
            new startActivityAsync(this, SettingsScreen.class).setTime(250).start(this::startActivity);
        } else if (id == R.id.nav_logout) {
            startActivity(this, LogOut.class);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSearchStateChanged(boolean enabled) {
        if(enabled&&!Config.SERVER_ONLINE) searchBar.disableSearch();
        if(!enabled) {
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
        search(text.toString());
        View searchLayout = findViewById(R.id.searchLayout);
        View content = findViewById(R.id.content);
        changeOpacity(searchLayout, false);
        tabLayout.setVisibility(View.GONE);
        content.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onButtonClicked(int buttonCode) {
        switch (buttonCode){
            case MaterialSearchBar.BUTTON_NAVIGATION:
                drawer.openDrawer(Gravity.START);
                break;
            case MaterialSearchBar.BUTTON_SPEECH:
                break;
            case MaterialSearchBar.BUTTON_BACK:
                searchBar.disableSearch();
                break;
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
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

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
