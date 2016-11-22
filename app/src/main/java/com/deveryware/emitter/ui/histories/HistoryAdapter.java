/**
 *
 * Copyright (C) 2011 Deveryware S.A. All Rights Reserved.
 *  
 * @author sylvek
 *
 */
package com.deveryware.emitter.ui.histories;

import com.deveryware.emitter.Constants;
import com.deveryware.emitter.R;
import com.deveryware.emitter.provider.EmitterProvider;
import com.deveryware.library.GiftClient;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class HistoryAdapter extends SimpleCursorAdapter {

    public HistoryAdapter(Context context, int layout, Cursor c)
    {
        super(context, layout, c, new String[] {}, new int[] {});
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor)
    {
        final int time = cursor.getColumnIndex(EmitterProvider.TIME);
        final int result = cursor.getColumnIndex(EmitterProvider.RESULT);
        final int uploaded = cursor.getColumnIndex(EmitterProvider.UPLOADED);
        final int tryLater = cursor.getColumnIndex(EmitterProvider.TRYLATER);
        final int query = cursor.getColumnIndex(EmitterProvider.QUERY);
        final int elapsed = cursor.getColumnIndex(EmitterProvider.ELAPSED_TIME);

        final CheckedTextView timeView = (CheckedTextView) view.findViewById(R.id.check_item);
        timeView.setText(DateFormat.format(Constants.DATE_FORMAT, cursor.getLong(time)).toString());
        timeView.setChecked(cursor.getInt(tryLater) == 0);

        final TextView resultView = (TextView) view.findViewById(R.id.result);
        resultView.setText(GiftClient.translateResult(context, cursor.getString(result)));

        final TextView uploadedView = (TextView) view.findViewById(R.id.uploaded);
        final TextView dataSizeView = (TextView) view.findViewById(R.id.data_size);
        final TextView elapsedView = (TextView) view.findViewById(R.id.elapsed_time);
        final long uploadedTime = cursor.getLong(uploaded);
        if (uploadedTime > 0) {
            uploadedView.setVisibility(View.VISIBLE);
            uploadedView.setText(DateFormat.format(Constants.DATE_FORMAT, uploadedTime).toString());

            final int out = cursor.getString(query).length();
            final int in = cursor.getString(result).length();
            final String inOut = context.getString(R.string.in_out, in, out);
            dataSizeView.setVisibility(View.VISIBLE);
            dataSizeView.setText(inOut);
            elapsedView.setVisibility(View.VISIBLE);
            elapsedView.setText(cursor.getLong(elapsed) + " ms");
        } else {
            elapsedView.setVisibility(View.GONE);
            uploadedView.setVisibility(View.GONE);
            dataSizeView.setVisibility(View.GONE);
        }
    }
}
