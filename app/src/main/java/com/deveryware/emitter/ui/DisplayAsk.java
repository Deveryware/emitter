/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 * Created on 7 mai 2011
 *
 */
package com.deveryware.emitter.ui;

import com.deveryware.emitter.R;
import com.deveryware.emitter.broadcast.Answer;
import com.deveryware.library.GiftClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author sylvek
 * 
 */
public class DisplayAsk extends Activity {

    private static final int ASK = 0;

    public static final String ACK = "ACK";

    public boolean isAck;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        isAck = getIntent().getBooleanExtra(ACK, true);
        showDialog(ASK);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id) {
        default:
        case ASK:
            final Intent intent = getIntent();
            final String[] params = intent.getStringArrayExtra(GiftClient.CMD_PARAMS);

            final Builder builder = new AlertDialog.Builder(this).setTitle(R.string.app_name);
            if (params != null && params.length > 0) {
                builder.setMessage(params[0]);
            }

            if (params != null && params.length > 1) {
                builder.setPositiveButton(params[1], new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final Intent answer = new Intent(Answer.MESSAGE);
                        final String id = getIntent().getStringExtra(GiftClient.CMD_ID);
                        answer.putExtra(GiftClient.CMD_ID, id);
                        answer.putExtra(GiftClient.CMD_ACK, 0);
                        answer.putExtra(GiftClient.CMD_TEXT, params[1]);
                        sendBroadcast(answer);

                        finish();
                    }
                });
            }

            if (params != null && params.length > 2) {
                builder.setNegativeButton(params[2], new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final Intent answer = new Intent(Answer.MESSAGE);
                        final String id = getIntent().getStringExtra(GiftClient.CMD_ID);
                        answer.putExtra(GiftClient.CMD_ID, id);
                        answer.putExtra(GiftClient.CMD_ACK, 0);
                        answer.putExtra(GiftClient.CMD_TEXT, params[2]);
                        sendBroadcast(answer);

                        finish();
                    }
                });
            }

            if (params != null && params.length > 3) {
                builder.setNeutralButton(params[3], new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        final Intent answer = new Intent(Answer.MESSAGE);
                        final String id = getIntent().getStringExtra(GiftClient.CMD_ID);
                        answer.putExtra(GiftClient.CMD_ID, id);
                        answer.putExtra(GiftClient.CMD_ACK, 0);
                        answer.putExtra(GiftClient.CMD_TEXT, params[3]);
                        sendBroadcast(answer);

                        finish();
                    }
                });
            }

            builder.setCancelable(params == null || params.length <= 1);

            return builder.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog)
                {
                    if (isAck) {
                        final Intent answer = new Intent(Answer.MESSAGE);
                        final String id = getIntent().getStringExtra(GiftClient.CMD_ID);
                        answer.putExtra(GiftClient.CMD_ID, id);
                        answer.putExtra(GiftClient.CMD_ACK, 0);
                        sendBroadcast(answer);
                    }

                    finish();
                }
            }).create();
        }
    }
}
