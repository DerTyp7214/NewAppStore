/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.appstore.settings.SettingsCheckBox;
import com.dertyp7214.appstore.settings.SettingsColor;
import com.dertyp7214.appstore.settings.SettingsPlaceholder;
import com.dertyp7214.appstore.settings.SettingsSwitch;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Settings> itemList;
    private Activity context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subTitle;
        public View box;
        public ProgressBar imageRight;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            subTitle = view.findViewById(R.id.subTitle);
            box = view.findViewById(R.id.box);
            imageRight = view.findViewById(R.id.progressBar);
        }
    }

    public class ViewHolderCheckBox extends ViewHolder {
        public CheckBox title;
        public View box;

        public ViewHolderCheckBox(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
        }
    }

    public class ViewHolderSwitch extends ViewHolder {
        public Switch title;
        public View box;

        public ViewHolderSwitch(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
        }
    }

    public class ViewHolderColor extends ViewHolder {
        public TextView title;
        public View box, colorView;

        public ViewHolderColor(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
            colorView = view.findViewById(R.id.colorViewPlate);
        }
    }

    public class ViewHolderPlaceHolder extends ViewHolder {
        public TextView title;
        public View box;

        public ViewHolderPlaceHolder(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
        }
    }

    public SettingsAdapter(List<Settings> itemList, Activity context) {
        this.itemList = itemList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_normal, parent, false));
            case 1:
                return new ViewHolderCheckBox(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_checkbox, parent, false));
            case 2:
                return new ViewHolderSwitch(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_togglebutton, parent, false));
            case 3:
                return new ViewHolderColor(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_color, parent, false));
            case 4:
                return new ViewHolderPlaceHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_placeholder, parent, false));
            default:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_normal, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final ThemeStore themeManager = ThemeStore.getInstance(context);
        switch (holder.getItemViewType()){
            case 0:
                final ViewHolder viewHolder = (ViewHolder) holder;
                final Settings setting = itemList.get(position);
                if(setting!=null) {
                    viewHolder.title.setText(setting.getText());
                    viewHolder.subTitle.setText(setting.getSubTitle());
                    viewHolder.box.setOnClickListener(v -> setting.onClick(viewHolder.subTitle, viewHolder.imageRight));
                }
                break;
            case 1:
                final ViewHolderCheckBox viewHolderCheckBox = (ViewHolderCheckBox) holder;
                final SettingsCheckBox settingsCheckBox = (SettingsCheckBox) itemList.get(position);
                CheckBox checkBox = viewHolderCheckBox.title;
                checkBox.setText(settingsCheckBox.getText());
                checkBox.setChecked(settingsCheckBox.isChecked());
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> settingsCheckBox.setChecked(isChecked));
                break;
            case 2:
                final ViewHolderSwitch viewHolderSwitch = (ViewHolderSwitch) holder;
                final SettingsSwitch settingsSwitch = (SettingsSwitch) itemList.get(position);
                Switch aSwitch = viewHolderSwitch.title;
                aSwitch.setText(settingsSwitch.getText());
                aSwitch.setChecked(settingsSwitch.isChecked());
                aSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> settingsSwitch.onCheckedChanged(isChecked));
                break;
            case 3:
                final ViewHolderColor viewHolderColor = (ViewHolderColor) holder;
                final SettingsColor settingsColor = (SettingsColor) itemList.get(position);
                viewHolderColor.title.setText(settingsColor.getText());
                LayerDrawable bgDrawable = (LayerDrawable) viewHolderColor.colorView.getBackground();
                final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.plate_color);
                shape.setColor(settingsColor.getColorInt());
                viewHolderColor.box.setOnClickListener(v -> settingsColor.onClick(viewHolderColor.colorView));
                break;
            case 4:
                final ViewHolderPlaceHolder viewHolderPlaceHolder = (ViewHolderPlaceHolder) holder;
                final SettingsPlaceholder settingsPlaceholder = (SettingsPlaceholder) itemList.get(position);
                viewHolderPlaceHolder.title.setText(settingsPlaceholder.getText());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return itemList.get(position) instanceof SettingsCheckBox ?1:itemList.get(position) instanceof SettingsSwitch ?2:itemList.get(position) instanceof SettingsColor ?3:itemList.get(position) instanceof SettingsPlaceholder ?4:0;
    }

    public void saveSettings(){
        for(Settings setting : itemList){
            setting.saveSetting();
        }
    }
}