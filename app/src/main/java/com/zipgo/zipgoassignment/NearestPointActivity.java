package com.zipgo.zipgoassignment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Activity to show two nearest place to each other
 */
public class NearestPointActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearest_point);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Get Intent
        Intent intent = getIntent();
        Place place1 = intent.getParcelableExtra("first_place");
        Place place2 = intent.getParcelableExtra("second_place");
        // Get views
        TextView placeNameView1 = findViewById(R.id.place_name1);
        TextView latLongView1 = findViewById(R.id.lat_lon_1);
        TextView placeNameView2 = findViewById(R.id.place_name2);
        TextView latLongView2 = findViewById(R.id.lat_lon_2);
        // Set Data
        placeNameView1.setText(place1.getPlaceName());
        String latLon1 = String.valueOf(place1.getLatitude()) + ", " + String.valueOf(place1.getLongitude());
        latLongView1.setText(latLon1);
        placeNameView2.setText(place2.getPlaceName());
        String latLon2 = String.valueOf(place2.getLatitude()) + ", " + String.valueOf(place2.getLongitude());
        latLongView2.setText(latLon2);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
