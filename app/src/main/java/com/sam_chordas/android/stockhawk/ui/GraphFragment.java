package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.graph_helper.DateXAxisValueFormatter;
import com.sam_chordas.android.stockhawk.graph_helper.StocksMarkerView;
import com.sam_chordas.android.stockhawk.service.FetchChartData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class GraphFragment extends Fragment implements FetchChartData.Callback {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RANGE_WEEK, RANGE_MONTH, RANGE_YEAR})
    public @interface RangeType {}

    public static final int RANGE_WEEK = 0;
    public static final int RANGE_MONTH = 1;
    public static final int RANGE_YEAR = 2;

    LineChart mLineChart;

    List<String> mLabels;
    List<Float> mValues;

    Context mContext;

    public GraphFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        mContext = getActivity();

        int textColor = ContextCompat.getColor(mContext, android.R.color.primary_text_dark);

        mLineChart = (LineChart) rootView.findViewById(R.id.linechart);
        mLineChart.setDescription(null);
        mLineChart.setMarkerView(new StocksMarkerView(mContext, R.layout.marker_view_layout));

        mLineChart.getAxisRight().setEnabled(false);
        mLineChart.getAxisLeft().setTextColor(textColor);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setTextColor(textColor);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new DateXAxisValueFormatter());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String symbol = getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT);
        FetchChartData fetchChartData = new FetchChartData(mContext, this);
        fetchChartData.execute(symbol);
    }

    @Override
    public void onDownloadSuccess(List<String> labels, List<Float> values) {
        mLabels = labels;
        mValues = values;

        onRangeTypeSelected(RANGE_WEEK);
    }

    @Override
    public void onDownloadFailed(@FetchChartData.ChartDataStatus int chartDataStatus) {
        int message;
        switch (chartDataStatus) {
            case FetchChartData.CHART_DATA_STATUS_NO_INTERNET:
                message = R.string.no_chart_data_no_network;
                break;
            case FetchChartData.CHART_DATA_STATUS_SERVER_DOWN:
                message = R.string.no_chart_data_server_down;
                break;
            case FetchChartData.CHART_DATA_STATUS_SERVER_INVALID:
                message = R.string.no_chart_data_server_error;
                break;
            default:
                message = R.string.no_chart_data;
        }
        mLineChart.setNoDataText(mContext.getString(message));
    }

    public void onRangeTypeSelected(@RangeType int rangeType) {
        int startIdx;
        List<String> labels;
        List<Float> values;

        ArrayList<Entry> entries = new ArrayList<>();

        if (mValues != null) {
            switch(rangeType) {
                case RANGE_WEEK:
                    startIdx = mLabels.size() - 6;
                    break;
                case RANGE_MONTH:
                    String dateEndStr = mLabels.get(mLabels.size() - 1);
                    String dateStartStr;
                    int yearEnd = Integer.parseInt(dateEndStr.substring(0, 4));
                    int monthEnd = Integer.parseInt(dateEndStr.substring(4, 6));
                    int day = Integer.parseInt(dateEndStr.substring(6, 8));
                    int monthStart = monthEnd - 1;
                    int yearStart = yearEnd;
                    if (monthStart == 0) {
                        monthStart = 12;
                        yearStart -= 1;
                    }
                    dateStartStr = ""+yearStart;
                    dateStartStr += String.format("%02d", monthStart);
                    dateStartStr += String.format("%02d", day);
                    startIdx = mLabels.indexOf(dateStartStr);
                    break;
                case RANGE_YEAR:
                    startIdx = 0;
                    break;
                default:
                    startIdx = mValues.size() - 8;
            }

            labels = mLabels.subList(startIdx, mLabels.size());
            values = mValues.subList(startIdx, mValues.size());

            for (int i=0; i<values.size(); i++) {
                entries.add(new Entry(values.get(i), i));
            }

            LineDataSet lineDataSet = new LineDataSet(entries, null);
            int colorTeal = ContextCompat.getColor(mContext, R.color.material_deep_teal_200);
            lineDataSet.setCircleColor(colorTeal);
            lineDataSet.setCircleColorHole(colorTeal);
            lineDataSet.setColor(colorTeal);
            lineDataSet.setDrawValues(false);
            LineData lineData = new LineData(labels, lineDataSet);

            mLineChart.clear();
            mLineChart.setData(lineData);
            mLineChart.getLegend().setEnabled(false);
        }
    }

}
