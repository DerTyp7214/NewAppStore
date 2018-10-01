/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.items.SearchItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentAppInfo extends Fragment {

    private TextView version, size;
    private Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_info, container, false);

        activity = getActivity();

        version = view.findViewById(R.id.txt_verison);
        size = view.findViewById(R.id.txt_size);

        return view;
    }

    public void getAppInfo(SearchItem searchItem){
        new Thread(() -> {
            String ver = Utils.getWebContent(Config.API_URL+"/apps/list.php?version="+searchItem.getId());
            String si = Utils.getWebContent(Config.API_URL+"/apps/list.php?size="+searchItem.getId());
            activity.runOnUiThread(() -> setText(new TextView[] {version, size}, new String[]{ver, si}));
        }).start();
    }

    private void setText(TextView[] textView, String[] text){
        if(textView.length != text.length)
            return;
        for (int i=0;i<textView.length;i++)
            textView[i].setText(text[i]);
    }

}
