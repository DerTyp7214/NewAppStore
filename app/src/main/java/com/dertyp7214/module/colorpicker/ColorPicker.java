package com.dertyp7214.module.colorpicker;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dertyp7214.appstore.R;

/**
 * Created by lengw on 20.09.2017.
 */

public class ColorPicker extends Dialog {

    private TextView redTxt, greenTxt, blueTxt;
    private ColorSeekBar redBar, greenBar, blueBar;
    private float red = Color.red(Color.GRAY), green = Color.green(Color.GRAY), blue =
            Color.blue(Color.GRAY);
    private View colorView;
    private Context c;
    private GradientDrawable shape;
    private EditText hexCode;
    private Listener listener;
    private int time = 1100;
    private float minBrighness = 0;
    private float maxBrighness = 1;

    public ColorPicker(Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_picker);

        hexCode = findViewById(R.id.hexTxt);

        redTxt = findViewById(R.id.txtRed);
        greenTxt = findViewById(R.id.txtGreen);
        blueTxt = findViewById(R.id.txtBlue);

        redBar = findViewById(R.id.red);
        greenBar = findViewById(R.id.green);
        blueBar = findViewById(R.id.blue);
        colorView = findViewById(R.id.colorView);
        LayerDrawable bgDrawable = (LayerDrawable) colorView.getBackground();
        shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.color_plate);

        redBar.setColor(Color.RED);
        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                red = i;
                setAllColors(red, green, blue);
                if (b) setHex(getIntFromColor(red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        greenBar.setColor(Color.GREEN);
        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                green = i;
                setAllColors(red, green, blue);
                if (b) setHex(getIntFromColor(red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        blueBar.setColor(Color.BLUE);
        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blue = i;
                setAllColors(red, green, blue);
                if (b) setHex(getIntFromColor(red, green, blue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        Setup();

        Button btn_ok = findViewById(R.id.btn_ok);
        Button btn_cancel = findViewById(R.id.btn_cancel);

        btn_ok.setOnClickListener(view -> listener.color(getIntFromColor(red, green, blue)));
        btn_cancel.setOnClickListener(view -> listener.cancel());
        hexCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Math.abs(i1 - i2) == 1 && hexCode.getText().length() == 6) {
                    String hex = hexCode.getText().toString();
                    int color = Color.parseColor("#" + hex);
                    int red = Color.red(color);
                    int green = Color.green(color);
                    int blue = Color.blue(color);
                    setAllColors(red, green, blue, false);
                    hexCode.setSelection(hex.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (hexCode.getText().length() == 6) {
                    int color = Color.parseColor("#" + hexCode.getText().toString());
                    setAllColors(Color.red(color), Color.green(color), Color.blue(color), true);
                }
            }
        });
    }

    public void setMinMaxBrighness(float min, float max) {
        this.minBrighness = min;
        this.maxBrighness = max;
    }

    public void setAnimationTime(int time) {
        this.time = time;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void color(int color);

        void cancel();
    }

    private void Setup() {
        setAllColors((int) this.red, (int) this.green, (int) this.blue, false);
    }

    public void setColor(int color) {
        intColor(color);
    }

    public void setColor(String color) {
        stringColor(color);
    }

    private void stringColor(String color) {
        int tmp = Color.parseColor(color);
        setAllColors(Color.red(tmp), Color.green(tmp), Color.blue(tmp), false);
    }

    private void intColor(int color) {
        setAllColors(Color.red(color), Color.green(color), Color.blue(color), false);
    }

    private void setAllColors(float r, float g, float b) {
        setAllColors((int) r, (int) g, (int) b, true);
    }

    private void setAllColors(int r, int g, int b, boolean self) {
        int color = getIntFromColor(r, g, b);
        int rc = Color.red(color), gc = Color.green(color), bc = Color.blue(color);

        this.red = rc;
        this.green = gc;
        this.blue = bc;

        try {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            Button btn = findViewById(R.id.btn_ok);
            if (hsv[2] < minBrighness || hsv[2] > maxBrighness) {
                btn.setEnabled(false);
                btn.setTextColor(Color.LTGRAY);
            } else {
                btn.setEnabled(true);
                btn.setTextColor(Color.BLACK);
            }
        } catch (Exception ignored) {
        }

        try {
            if (! self) {
                animateSeek(redBar, rc, time);
                animateSeek(greenBar, gc, time);
                animateSeek(blueBar, bc, time);
                setHex(color);
            }

            redTxt.setText(String.valueOf(rc));
            greenTxt.setText(String.valueOf(gc));
            blueTxt.setText(String.valueOf(bc));
            shape.setColor(color);
        } catch (Exception ignored) {
        }
    }

    private void animateSeek(final SeekBar seekBar, final int toVal, final int time) {
        ValueAnimator anim = ValueAnimator.ofInt(seekBar.getProgress(), toVal);
        anim.setDuration(time);
        anim.addUpdateListener(animation -> {
            int animProgress = (Integer) animation.getAnimatedValue();
            try {
                seekBar.setProgress(animProgress);
            } catch (Exception ignored) {
            }
        });
        anim.start();
    }

    private void setHex(int color) {
        String hex = String.format("#%06X", (0xFFFFFF & color));
        hexCode.setText(hex.replace("#", ""));
    }

    private int getIntFromColor(float Red, float Green, float Blue) {
        int R = Math.round(255 * (256 - Red));
        int G = Math.round(255 * (256 - Green));
        int B = Math.round(255 * (256 - Blue));

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }

    public int getColorInt() {
        return getIntFromColor(red, green, blue);
    }

    public String getColorString(int red, int green, int blue) {
        return String.format("#%06X", (0xFFFFFF & getIntFromColor(red, green, blue)));
    }
}
