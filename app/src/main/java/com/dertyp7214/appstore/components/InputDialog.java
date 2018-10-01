/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;

import com.dertyp7214.appstore.R;

public class InputDialog {

    private Context context;
    private Listener listener;
    private AlertDialog.Builder builder;

    public InputDialog(String title, String text, String hint, Context context){
        this.context=context;
        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText input = new EditText(context);
        input.setHint(hint);
        input.setText(text);
        input.setMaxLines(1);
        builder.setView(input);

        builder.setPositiveButton(context.getString(R.string.popup_ok), (dialog, which) -> {
            listener.onSubmit(input.getText().toString());
            dialog.cancel();
        });
        builder.setNegativeButton(context.getString(R.string.popup_close),
                (dialog, which) -> dialog.cancel());
    }

    public void setListener(Listener listener){
        this.listener=listener;
    }

    public void show(){
        builder.show();
    }

    public interface Listener{
        void onSubmit(String text);
        void onCancel();
    }
}