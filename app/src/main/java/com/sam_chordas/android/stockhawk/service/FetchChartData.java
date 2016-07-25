package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.util.Log;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yoh268 on 7/23/2016.
 * An AsyncTask to download historical data for plotting
 */
public class FetchChartData extends AsyncTask<String, Void, Integer> {

    private final String LOG_TAG = FetchChartData.class.getSimpleName();

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({CHART_DATA_STATUS_OK, CHART_DATA_STATUS_NO_INTERNET, CHART_DATA_STATUS_SERVER_DOWN, CHART_DATA_STATUS_SERVER_INVALID,
            CHART_DATA_STATUS_SYMBOL_INVALID, CHART_DATA_STATUS_UNKNOWN})
    public @interface ChartDataStatus {}

    public static final int CHART_DATA_STATUS_OK = 0;
    public static final int CHART_DATA_STATUS_NO_INTERNET = 1;
    public static final int CHART_DATA_STATUS_SERVER_DOWN = 2;
    public static final int CHART_DATA_STATUS_SERVER_INVALID = 3;
    public static final int CHART_DATA_STATUS_SYMBOL_INVALID = 4;
    public static final int CHART_DATA_STATUS_UNKNOWN = 5;

    Context mContext;
    List<String> mLabels;
    List<Float> mValues;
    Callback mCallback;

    public interface Callback {
        void onDownloadSuccess(List<String> labels, List<Float> values);
        void onDownloadFailed(@ChartDataStatus int chartDataStatus);
    }

    public FetchChartData(Context context, Callback callback) {
        mContext = context;
        mCallback = callback;
    }

    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        // Will contain the raw JSON response as a string.
        String jsonStr = null;

        @ChartDataStatus int chartDataStatus = CHART_DATA_STATUS_OK;

        if(!Utility.isNetworkAvailable(mContext)) {
            return CHART_DATA_STATUS_NO_INTERNET;
        }

        if(params.length == 0) {
            return CHART_DATA_STATUS_SYMBOL_INVALID;
        }
        String quoteSymbol = params[0];
        String quoteRange = "1y";

        try {
            // Construct the URL for the chart query
            final String BASE_URL = "http://chartapi.finance.yahoo.com/instrument/1.0";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendPath(quoteSymbol)
                    .appendPath("chartdata;type=quote;range=" + quoteRange)
                    .appendPath("json")
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to the API, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return CHART_DATA_STATUS_SERVER_DOWN;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return CHART_DATA_STATUS_SERVER_DOWN;
            }

            jsonStr = buffer.toString();

            final String startStr = "finance_charts_json_callback(";
            if (jsonStr.startsWith(startStr)) {
                jsonStr = jsonStr.substring(startStr.length(), jsonStr.length() - 1). trim();
            }
        } catch(IOException e) {
            Log.e(LOG_TAG, mContext.getResources().getString(R.string.log_error)+" ", e);
            chartDataStatus = CHART_DATA_STATUS_SERVER_DOWN;
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG,
                            mContext.getResources().getString(R.string.log_error_close_stream), e);
                }
            }
        }

        if(chartDataStatus == CHART_DATA_STATUS_OK)
            return getDataFromJson(jsonStr);

        return chartDataStatus;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (result == CHART_DATA_STATUS_OK)
            mCallback.onDownloadSuccess(mLabels, mValues);
        else
            mCallback.onDownloadFailed(result);
    }

    private Integer getDataFromJson(String jsonStr) {
        final String QUOTE_SERIES = "series";
        final String QUOTE_DATE = "Date";
        final String QUOTE_CLOSE = "close";

        @ChartDataStatus int chartDataStatus = CHART_DATA_STATUS_OK;

        if (jsonStr == null) return CHART_DATA_STATUS_SERVER_INVALID;

        try {
            JSONArray jsonArr = (new JSONObject(jsonStr)).getJSONArray(QUOTE_SERIES);

            mLabels = new ArrayList();
            mValues = new ArrayList();

            for (int i=0; i<jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);

                mLabels.add(""+jsonObj.getLong(QUOTE_DATE));
                mValues.add((float) jsonObj.getDouble(QUOTE_CLOSE));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            chartDataStatus = CHART_DATA_STATUS_SERVER_INVALID;
            e.printStackTrace();
        }

        return chartDataStatus;
    }

}
