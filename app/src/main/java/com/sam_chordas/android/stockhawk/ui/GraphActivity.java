package com.sam_chordas.android.stockhawk.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.sam_chordas.android.stockhawk.R;

public class GraphActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_graph);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_graph, menu);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String quoteSymbol = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (quoteSymbol.length() > 0) getSupportActionBar().setTitle(quoteSymbol);

        MenuItem item = menu.findItem(R.id.action_change_range);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_list_change_range, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                GraphFragment gf = (GraphFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_graph);

                if (gf != null) {
                    gf.onRangeTypeSelected(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                GraphFragment gf = (GraphFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.fragment_graph);

                if (gf != null) {
                    gf.onRangeTypeSelected(GraphFragment.RANGE_WEEK);
                }
            }
        });

        return true;
    }

}
