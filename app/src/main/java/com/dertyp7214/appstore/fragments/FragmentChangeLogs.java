/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.ColorUtils;
import android.text.Html;
import android.text.Spanned;
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

import static com.dertyp7214.appstore.Utils.manipulateColor;

public class FragmentChangeLogs extends Fragment {

    private TextView changes;
    private ThemeStore themeStore;
    private Activity activity;
    private String changeLog, version;
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_change_logs, container, false);

        activity=getActivity();

        themeStore = ThemeStore.getInstance(getActivity());

        changes = view.findViewById(R.id.txt_change);

        setColor(themeStore.getPrimaryColor());

        return view;
    }

    public void setColor(@ColorInt int color) {
        if (changes.getVisibility() == View.VISIBLE) {
            changes.setTextColor(manipulateColor(color, 0.6F));
            view.setBackgroundColor(ColorUtils.setAlphaComponent(color, 0x37));
        }
    }

    public void getChangeLogs(SearchItem searchItem, Callback callback) {
        if (changeLog == null)
            changeLog = Utils.getWebContent(Config.API_URL + "/apps/list.php?changes=" + searchItem.getId());
        if (version == null)
            version = Utils.getWebContent(Config.API_URL + "/apps/list.php?version=" + searchItem.getId());
        callback.run(changes, setText(changeLog, version, false));
    }

    private Spanned setText(String text, String version, boolean big){
        if(text == null || text.length() == 0 || Objects.requireNonNull(text).startsWith("{")) {
            return null;
        }
        if(!big)
            text = text.split("</new>")[0]+"</body></html>";
        text = "<br/>"+text.replace("%ver%", version);
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(text);
        }
        return result;
    }

    public interface Callback{
        void run(TextView textView, Spanned text);
    }
}
