/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.fragments;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.dev.Logs;

public class FragmentUserCard extends Fragment {

    private RelativeLayout relativeLayout;
    private ViewGroup container;
    private LayoutInflater inflater;
    private CardView cardView;

    public FragmentUserCard() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fragment_user_card, container, false);

        this.inflater = inflater;
        this.container = container;

        cardView = view.findViewById(R.id.card);
        relativeLayout = view.findViewById(R.id.content);

        return view;
    }

    public void setContentView(@LayoutRes int layout, OnAttachListener onAttachListener) {
        try {
            View view = inflater.inflate(layout, container, false);
            onAttachListener.onAttach(view);
            relativeLayout.addView(view);
        } catch (Exception e) {
            Logs.getInstance(getActivity()).error("setContentView",  e.toString());
        }
    }

    public void setBorderRadius(float i) {
        if(cardView!=null)
            cardView.setRadius(i);
    }

    public interface OnAttachListener {
        void onAttach(View view);
    }
}
