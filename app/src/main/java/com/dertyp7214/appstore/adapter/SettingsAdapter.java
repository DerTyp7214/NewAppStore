/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.appstore.settings.SettingsCheckBox;
import com.dertyp7214.appstore.settings.SettingsColor;
import com.dertyp7214.appstore.settings.SettingsPlaceholder;
import com.dertyp7214.appstore.settings.SettingsSlider;
import com.dertyp7214.appstore.settings.SettingsSwitch;
import com.hacker.lib.colorlib.Color;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Settings> itemList;
    private Activity context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, subTitle;
        public View box;
        ProgressBar imageRight;

        ViewHolder(View view) {
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

        ViewHolderCheckBox(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);

            ThemeStore store = ThemeStore.getInstance(context);

            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{- android.R.attr.state_checked},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.GRAY,
                            store.getAccentColor(),
                            Color.LTGRAY
                    }
            );

            title.setButtonTintList(colorStateList);
        }
    }

    public class ViewHolderSwitch extends ViewHolder {
        public Switch title;
        public View box;

        ViewHolderSwitch(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);

            setSwitchColor(title);
        }
    }

    public class ViewHolderColor extends ViewHolder {
        public TextView title;
        public View box, colorView;

        ViewHolderColor(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
            colorView = view.findViewById(R.id.colorViewPlate);
        }
    }

    public class ViewHolderPlaceHolder extends ViewHolder {
        public TextView title;
        public View box;

        ViewHolderPlaceHolder(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
        }
    }

    public class ViewHolderSlider extends ViewHolder {
        TextView title, progress;
        public View box;
        public SeekBar seekBar;

        ViewHolderSlider(View view) {
            super(view);
            title = view.findViewById(R.id.text);
            box = view.findViewById(R.id.box);
            seekBar = view.findViewById(R.id.seekBar);
            progress = view.findViewById(R.id.txt_prog);
        }
    }

    public SettingsAdapter(List<Settings> itemList, Activity context) {
        this.itemList = itemList;
        this.context = context;
    }

    private void setSwitchColor(Switch s) {
        ThemeStore store = ThemeStore.getInstance(context);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
            s.getThumbDrawable().setColorFilter(store.getAccentColor(), PorterDuff.Mode.MULTIPLY);
            s.getTrackDrawable().setColorFilter(store.getAccentColor(), PorterDuff.Mode.MULTIPLY);
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            ColorStateList buttonStates = new ColorStateList(
                    new int[][]{
                            new int[]{- android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.LTGRAY,
                            store.getAccentColor(),
                            Color.GRAY
                    }
            );
            s.setButtonTintList(buttonStates);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ColorStateList thumbStates = new ColorStateList(
                    new int[][]{
                            new int[]{- android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_checked},
                            new int[]{}
                    },
                    new int[]{
                            Color.LTGRAY,
                            store.getAccentColor(),
                            Color.GRAY
                    }
            );
            s.setThumbTintList(thumbStates);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ColorStateList trackStates = new ColorStateList(
                        new int[][]{
                                new int[]{- android.R.attr.state_enabled},
                                new int[]{}
                        },
                        new int[]{
                                Color.GRAY,
                                Color.LTGRAY
                        }
                );
                s.setTrackTintList(trackStates);
                s.setTrackTintMode(PorterDuff.Mode.OVERLAY);
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_normal, parent, false));
            case 1:
                return new ViewHolderCheckBox(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_checkbox, parent, false));
            case 2:
                return new ViewHolderSwitch(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_togglebutton, parent, false));
            case 3:
                return new ViewHolderColor(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_color, parent, false));
            case 4:
                return new ViewHolderPlaceHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_placeholder, parent, false));
            case 5:
                return new ViewHolderSlider(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_slider, parent, false));
            default:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_normal, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 0:
                final ViewHolder viewHolder = (ViewHolder) holder;
                final Settings setting = itemList.get(position);
                if (setting != null) {
                    viewHolder.title.setText(setting.getText());
                    viewHolder.subTitle.setText(setting.getSubTitle());
                    viewHolder.box.setOnClickListener(
                            v -> setting.onClick(viewHolder.subTitle, viewHolder.imageRight));
                }
                break;
            case 1:
                final ViewHolderCheckBox viewHolderCheckBox = (ViewHolderCheckBox) holder;
                final SettingsCheckBox settingsCheckBox = (SettingsCheckBox) itemList.get(position);
                CheckBox checkBox = viewHolderCheckBox.title;
                checkBox.setText(settingsCheckBox.getText());
                checkBox.setChecked(settingsCheckBox.isChecked());
                checkBox.setOnCheckedChangeListener(
                        (buttonView, isChecked) -> settingsCheckBox.setChecked(isChecked));
                break;
            case 2:
                final ViewHolderSwitch viewHolderSwitch = (ViewHolderSwitch) holder;
                final SettingsSwitch settingsSwitch = (SettingsSwitch) itemList.get(position);
                Switch aSwitch = viewHolderSwitch.title;
                aSwitch.setText(settingsSwitch.getText());
                aSwitch.setChecked(settingsSwitch.isChecked());
                aSwitch.setOnCheckedChangeListener(
                        (buttonView, isChecked) -> settingsSwitch.onCheckedChanged(isChecked));
                break;
            case 3:
                final ViewHolderColor viewHolderColor = (ViewHolderColor) holder;
                final SettingsColor settingsColor = (SettingsColor) itemList.get(position);
                viewHolderColor.title.setText(settingsColor.getText());
                LayerDrawable bgDrawable =
                        (LayerDrawable) viewHolderColor.colorView.getBackground();
                final GradientDrawable shape =
                        (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.plate_color);
                shape.setColor(settingsColor.getColorInt());
                viewHolderColor.box
                        .setOnClickListener(v -> settingsColor.onClick(viewHolderColor.colorView));
                break;
            case 4:
                final ViewHolderPlaceHolder viewHolderPlaceHolder = (ViewHolderPlaceHolder) holder;
                final SettingsPlaceholder settingsPlaceholder =
                        (SettingsPlaceholder) itemList.get(position);
                viewHolderPlaceHolder.title.setText(settingsPlaceholder.getText());
                break;
            case 5:
                final ViewHolderSlider viewHolderSlider = (ViewHolderSlider) holder;
                final SettingsSlider settingsSlider =
                        (SettingsSlider) itemList.get(position);
                viewHolderSlider.title.setText(settingsSlider.getText());
                viewHolderSlider.seekBar.setProgress(settingsSlider.getProgress());
                viewHolderSlider.progress
                        .setText(String.valueOf(viewHolderSlider.seekBar.getProgress()));
                viewHolderSlider.seekBar.setOnSeekBarChangeListener(
                        new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                settingsSlider.onUpdate(progress);
                                viewHolderSlider.progress.setText(String.valueOf(progress));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                settingsSlider.saveSetting();
                            }
                        });
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
        return itemList.get(position) instanceof SettingsCheckBox ? 1 : itemList
                .get(position) instanceof SettingsSwitch ? 2 : itemList
                .get(position) instanceof SettingsColor ? 3 : itemList
                .get(position) instanceof SettingsPlaceholder ? 4 : itemList
                .get(position) instanceof SettingsSlider ? 5 : 0;
    }

    public void saveSettings() {
        for (Settings setting : itemList) {
            setting.saveSetting();
        }
    }
}