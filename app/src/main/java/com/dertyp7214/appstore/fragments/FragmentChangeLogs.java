/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.items.SearchItem;

import java.util.Objects;

public class FragmentChangeLogs extends Fragment {

    private TextView changes;
    private ThemeStore themeStore;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_logs, container, false);

        activity=getActivity();

        themeStore = ThemeStore.getInstance(getActivity());

        changes = view.findViewById(R.id.txt_change);

        changes.setTextColor(themeStore.getPrimaryDarkColor());
        view.setBackgroundColor(ColorUtils.setAlphaComponent(themeStore.getPrimaryColor(), 0x37));

        return view;
    }

    public void getChangeLogs(SearchItem searchItem){
        new Thread(() -> {
            String changeLog = Utils.getWebContent(Config.API_URL+"/apps/list.php?changes="+searchItem.getId());
            String version = Utils.getWebContent(Config.API_URL+"/apps/list.php?version="+searchItem.getId());
            activity.runOnUiThread(() -> setText(changes, changeLog, version));
        }).start();
    }

    private void setText(TextView textView, String text, String version){
        if(text == null || text.length() == 0 || Objects.requireNonNull(text).startsWith("{")) {
            textView.setVisibility(View.GONE);
            return;
        }
        text = text.replace("%ver%", version);
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(text);
        }
        textView.setText(result);
        textView. setMovementMethod(LinkMovementMethod.getInstance());
        textView.setVisibility(View.VISIBLE);
    }
}
