package com.embedded.contacts.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Font file used for displaying the icons as a text.
 */

public class BitVaultFont extends android.support.v7.widget.AppCompatTextView {

    public BitVaultFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BitVaultFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BitVaultFont(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/" + "bitcontact.ttf");
        setTypeface(tf);
    }

}