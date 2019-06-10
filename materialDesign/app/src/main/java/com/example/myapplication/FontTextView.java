package com.example.myapplication;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import java.io.File;

public class FontTextView extends AppCompatTextView {

    public FontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseAttributes(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributes(context, attrs);
    }

    public FontTextView(Context context) {
        super(context);
    }

    private void parseAttributes(Context context, AttributeSet attrs) {
        setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts" + File.separator + "Roboto-Light.ttf"));
    }
}