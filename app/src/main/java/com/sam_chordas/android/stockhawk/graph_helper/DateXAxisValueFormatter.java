package com.sam_chordas.android.stockhawk.graph_helper;

import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yoh268 on 7/24/2016.
 */
public class DateXAxisValueFormatter implements XAxisValueFormatter {
    private final String DATE_FORMAT_IN = "yyyyMMdd";
    private final String DATE_FORMAT_OUT = "ddMMMyyyy";

    @Override
    public String getXValue(String original, int index, ViewPortHandler viewPortHandler) {
        SimpleDateFormat dateFormatIn = new SimpleDateFormat(DATE_FORMAT_IN);
        SimpleDateFormat dateFormatOut = new SimpleDateFormat(DATE_FORMAT_OUT);
        try {
            Date date = dateFormatIn.parse(original);
            return dateFormatOut.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return original;
    }

}
