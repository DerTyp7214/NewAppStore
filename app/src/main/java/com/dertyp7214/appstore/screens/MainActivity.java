/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.animation.LayoutTransition;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.CustomSnackbar;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.SearchAdapter;
import com.dertyp7214.appstore.fragments.FragmentAbout;
import com.dertyp7214.appstore.fragments.FragmentAppGroups;
import com.dertyp7214.appstore.fragments.TabFragment;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Utils
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPagerAdapter adapter;
    private FloatingActionButton fab;
    private SearchAdapter searchAdapter;
    private Thread thread;
    private int id = 0;
    private Random random;
    private ThemeStore themeStore;
    private TabLayout tabLayout;

    private List<SearchItem> appItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        overridePendingTransition(R.anim.fast_out_extra_slow_in, R.anim.fast_out_extra_slow_in);

        random = new Random();
        themeStore = ThemeStore.getInstance(this);

        checkAppDir();
        checkPermissions();
        checkForOldAppStore();

        toolbar.setBackgroundColor(ThemeStore.getInstance(this).getPrimaryColor());
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        findViewById(R.id.nav_view).setPadding(0, 0, 0, getNavigationBarHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(this, toolbar, ThemeStore.getInstance(this).getPrimaryColor(), 300);
        }

        fab = findViewById(R.id.fab);
        int fabMargin = (int) getResources().getDimension(R.dimen.fab_margin);
        fab.setColorFilter(Utils.isColorBright(themeStore.getAccentColor()) ? Color.BLACK : Color.WHITE);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeStore.getAccentColor()));
        fab.setOnClickListener(view -> new CustomSnackbar(MainActivity.this, getWindow().getNavigationBarColor()).make(view, "Replace with your own action", CustomSnackbar.LENGTH_LONG)
                .setAction("Action", null).setCallBack(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        fab.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onShown(Snackbar snackbar) {
                        fab.setVisibility(View.INVISIBLE);
                    }
                }).show());

        setMargins(fab, fabMargin, fabMargin, fabMargin, fabMargin+getNavigationBarHeight());

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ViewPager viewPager = findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        addFragment(new FragmentAppGroups());
        adapter.addFragment(new FragmentAbout(), getString(R.string.mal_title_about));

        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabBar);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setBackgroundColor(themeStore.getPrimaryColor());
        tabLayout.setSelectedTabIndicatorColor(themeStore.getInvertedPrimaryColor());
        tabLayout.setTabTextColors(themeStore.getPrimaryTextColor(), themeStore.getPrimaryTextColor());

        searchAdapter = new SearchAdapter(this, appItems);

        RecyclerView searchRecView = findViewById(R.id.search_view);
        searchRecView.setLayoutManager(new LinearLayoutManager(this));
        searchRecView.setPadding(0, 0, 0, getNavigationBarHeight());
        searchRecView.setAdapter(searchAdapter);

    }

    private void search(String query){
        id = random.nextInt();
        thread = new Thread(() -> {
            try{

                int localId = id;
                appItems.clear();

                if(new JSONObject(LocalJSON.getJSON(MainActivity.this)).getBoolean("error"))
                    LocalJSON.setJSON(MainActivity.this, Utils.getWebContent(Config.API_URL + "/apps/list.php"));

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
            searchView.setVisibility(View.INVISIBLE);
            content.setVisibility(View.VISIBLE);
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);

        int iconTint = Utils.isColorBright(themeStore.getPrimaryColor()) ? Color.BLACK : Color.WHITE;

        MenuItem searchMenu = menu.findItem(R.id.action_search);
        searchMenu.getIcon().setTint(iconTint);

        SearchView searchView = (SearchView) searchMenu.getActionView();
        ((ImageView) searchView.findViewById(R.id.search_close_btn)).setImageTintList(ColorStateList.valueOf(iconTint));
        searchView.setQueryHint(getString(R.string.search));
        searchView.setIconifiedByDefault(true);

        for (TextView textView : findChildrenByClass(TextView.class, searchView)) {
            textView.setTextColor(iconTint);
            textView.setHintTextColor(iconTint);
        }

        for(ImageView imageButton : findChildrenByClass(ImageView.class, searchView))
            imageButton.setColorFilter(iconTint);

        LinearLayout searchBar = searchView.findViewById(R.id.search_bar);
        searchBar.setLayoutTransition(new LayoutTransition());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });

        searchView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewDetachedFromWindow(View arg0) {
                View searchLayout = findViewById(R.id.searchLayout);
                View content = findViewById(R.id.content);
                content.setVisibility(View.VISIBLE);
                tabLayout.setVisibility(View.VISIBLE);
                searchLayout.setVisibility(View.INVISIBLE);
                setTimeOut(100, () -> fab.setVisibility(View.VISIBLE));
            }

            @Override
            public void onViewAttachedToWindow(View arg0) {
                View searchLayout = findViewById(R.id.searchLayout);
                View content = findViewById(R.id.content);
                content.setVisibility(View.INVISIBLE);
                tabLayout.setVisibility(View.GONE);
                searchLayout.setVisibility(View.VISIBLE);
                fab.setVisibility(View.INVISIBLE);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        switch(id) {
            case R.id.action_search:
                TransitionManager.beginDelayedTransition(findViewById(R.id.toolbar));
                MenuItemCompat.expandActionView(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {
            startActivity(this, ModulesScreen.class);
        } else if (id == R.id.nav_manage) {
            startActivity(this, SettingsScreen.class);
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
