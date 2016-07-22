package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by yoh268 on 7/23/2016.
 */
public class ServiceUtils {

    /**
     * Returns true if the network is available or about to become available.
     *
     * @param c Context used to get the ConnectivityManager
     * @return
     */
    static public boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the stocks status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @StockTaskService.StocksStatus
    int getStocksStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_stocks_status_key), StockTaskService.STOCKS_STATUS_UNKNOWN);
    }

}
