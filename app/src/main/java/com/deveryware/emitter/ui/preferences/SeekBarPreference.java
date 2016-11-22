/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.ui.preferences;

import com.deveryware.emitter.R;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, OnCheckedChangeListener {
    private static final String androidns = "http://schemas.android.com/apk/res/android";

    protected SeekBar mSeekBar;

    protected CheckBox mCheckBox;

    protected TextView mSplashText, mValueText;

    protected Context mContext;

    protected String mDialogMessage, mSuffix;

    protected int mDefault, mMax;

    protected boolean addCheckBox;

    public SeekBarPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;

        mDialogMessage = attrs.getAttributeValue(androidns, "dialogMessage");
        mSuffix = attrs.getAttributeValue(androidns, "text");
        mDefault = attrs.getAttributeIntValue(androidns, "defaultValue", -1);
        mMax = attrs.getAttributeIntValue(androidns, "max", 100);
        addCheckBox = attrs.getAttributeBooleanValue(null, "checkbox", true);
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

        mValueText = new TextView(mContext);
        mValueText.setGravity(Gravity.CENTER_HORIZONTAL);
        mValueText.setTextSize(32);
        layout.addView(mValueText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        mSeekBar = new SeekBar(mContext);
        mSeekBar.setOnSeekBarChangeListener(this);
        layout.addView(mSeekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        mCheckBox = new CheckBox(mContext);
        mCheckBox.setOnCheckedChangeListener(this);
        mCheckBox.setText(R.string.infinite);

        if (addCheckBox) {
            layout.addView(mCheckBox, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        mSeekBar.setMax(mMax);

        final int mValue = getPersistedInt(mDefault);
        if (mValue <= 0 && addCheckBox) {
            mSeekBar.setEnabled(false);
            mCheckBox.setChecked(true);
        } else {
            mSeekBar.setProgress(mValue);
        }

        return layout;
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch)
    {
        String t = String.valueOf(value);
        mValueText.setText(mSuffix == null ? t : t.concat(mSuffix));
        callChangeListener(new Integer(value));
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1)
    {
        mSeekBar.setEnabled(!arg1);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            if (mCheckBox.isChecked()) {
                persistInt(-1);
            } else {
                persistInt(mSeekBar.getProgress());
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0)
    {
    }
}
