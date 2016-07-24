package com.sam_chordas.android.stockhawk.graph_helper;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.sam_chordas.android.stockhawk.R;

/**
 * Created by yoh268 on 7/24/2016.
 */
public class StocksMarkerView extends MarkerView {

    private TextView mValueView;

    public StocksMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        mValueView = (TextView) findViewById(R.id.marker_value);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        mValueView.setText("" + e.getVal());
    }

    @Override
    public int getYOffset(float ypos) {
        return -getHeight();
    }

    @Override
    public int getXOffset(float xpos) {
        return -(getWidth() / 2);
    }
}
