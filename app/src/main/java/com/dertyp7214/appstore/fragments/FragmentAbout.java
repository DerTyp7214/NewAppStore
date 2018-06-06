/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.content.Context;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.dertyp7214.appstore.Utils;

public class FragmentAbout extends MaterialAboutFragment {

    public FragmentAbout() {

    }

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        MaterialAboutCard card = new MaterialAboutCard.Builder()
                .title("Authors")
                .addItem(new MaterialAboutActionItem.Builder()
                        .text("Main Author")
                        .subText("Josua Lengwenath")
                        .icon(Utils.drawableFromUrl(context, "https://avatars0.githubusercontent.com/u/37804065"))
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .build())
                .build();

        return new MaterialAboutList.Builder()
                .addCard(card)
                .build();
    }
}
