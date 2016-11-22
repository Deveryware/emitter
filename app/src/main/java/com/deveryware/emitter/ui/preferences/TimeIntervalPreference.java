/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.ui.preferences;

import android.content.Context;
import android.preference.DialogPreference;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeIntervalPreference extends DialogPreference {
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    protected TextView mSplashText;

    protected EditText mValueText;

    protected Button plus, minus;

    protected Context mContext;

    protected String mDialogMessage, mSuffix;

    protected long mDefault, mMax, mMin = 5;

    public TimeIntervalPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;

        mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns, "text");
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
        mMax = attrs.getAttributeIntValue(androidns, "max", 100);
    }

    @Override
    protected View onCreateDialogView()
    {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(6, 6, 6, 6);

        mSplashText = new TextView(mContext);
        mSplashText.setGravity(Gravity.CENTER_HORIZONTAL);
        if (mDialogMessage != null) {
            mSplashText.setText(mDialogMessage);
        } else {
            mSplashText.setText(getSummary());
        }
        layout.addView(mSplashText);

        plus = new Button(mContext);
        plus.setText("+");
        plus.setGravity(Gravity.CENTER_HORIZONTAL);
        plus.setTextSize(20);
        plus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                final String value = mValueText.getText().toString();
                long currentValue = Long.valueOf(value);
                if (currentValue < mMax) {
                    currentValue += 1;
                } else {
                    currentValue = mMax;
                }
                mValueText.setText(Long.toString(currentValue));
            }
        });
        layout.addView(plus, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        mValueText = new EditText(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(32);
        mValueText.setKeyListener(new DigitsKeyListener(false, false));
        layout.addView(mValueText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        minus = new Button(mContext);
        minus.setText("-");
        minus.setGravity(Gravity.CENTER_HORIZONTAL);
        minus.setTextSize(20);
        minus.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v)
            {
                final String value = mValueText.getText().toString();
                long currentValue = Long.valueOf(value);
                if (currentValue > mMin) {
                    currentValue -= 1;
                } else {
                    currentValue = mMin;
                }
                mValueText.setText(Long.toString(currentValue));
            }
        });
        layout.addView(minus, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist()) {
            final long value = getPersistedLong(mDefault);
            mValueText.setText(Long.toString(value));
        }

        return layout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            persistLong(Long.valueOf(mValueText.getText().toString()));
        }
    }
}
