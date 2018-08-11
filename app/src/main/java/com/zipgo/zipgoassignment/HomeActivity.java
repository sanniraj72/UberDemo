package com.zipgo.zipgoassignment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Launcher Activity
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    // Log TAG
    private static final String TAG = "zipgo_app";

    // List of Place
    private List<com.zipgo.zipgoassignment.Place> places;

    // List of Place coming from realm
    private RealmResults<com.zipgo.zipgoassignment.Place> placeRealmList;

    // Button to find Nearest Location
    private Button nearestButton;
    // Button for animation on map
    private Button animateButton;

    // Realm Object
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        // Set Toolbar Title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.home_screen);
        }
        RecyclerView placeList = findViewById(R.id.place_list);
        nearestButton = findViewById(R.id.nearest_point_button);
        animateButton = findViewById(R.id.animate_button);
        // Initialize Realm
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
        realm = Realm.getDefaultInstance();
        // Get All Place
        places = new ArrayList<>();
        placeRealmList = realm.where(com.zipgo.zipgoassignment.Place.class).findAll();
        places.addAll(placeRealmList);
        // Set Adapter
        final PlaceAdapter placeAdapter = new PlaceAdapter(this, places);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        placeList.setLayoutManager(layoutManager);
        placeList.setAdapter(placeAdapter);
        // Search for to find the place
        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteFragment.setHint("Type a landmark name");
        // Set Place Selection Listener
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                placeRealmList = realm.where(com.zipgo.zipgoassignment.Place.class).findAll();
                if (placeRealmList.size() < 10) {
                    // Get Data
                    String id = place.getId();
                    String name = place.getName().toString();
                    LatLng latLng = place.getLatLng();
                    // Create Place object with new data
                    com.zipgo.zipgoassignment.Place place1 = new com.zipgo.zipgoassignment.Place(id, name, latLng.latitude, latLng.longitude);
                    // Save to Realm
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(place1);
                    realm.commitTransaction();
                    // Save Locally
                    places.clear();
                    places.addAll(placeRealmList);
                    // Refresh List
                    placeAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(HomeActivity.this, "Max 10 locations is allowed", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "Error: " + status);
                Toast.makeText(HomeActivity.this, status.getStatusMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        nearestButton.setOnClickListener(this);
        animateButton.setOnClickListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nearestButton.setOnClickListener(null);
        animateButton.setOnClickListener(null);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nearest_point_button:
                List<com.zipgo.zipgoassignment.Place> placeList = findNearestPlaces();
                if (placeList.size() == 2) {
                    Intent nearestIntent = new Intent(HomeActivity.this, NearestPointActivity.class);
                    nearestIntent.putExtra("first_place", placeList.get(0));
                    nearestIntent.putExtra("second_place", placeList.get(1));
                    startActivity(nearestIntent);
                } else {
                    Toast.makeText(this, "To find nearest point 2 places are required", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.animate_button:
                Intent intent = new Intent(HomeActivity.this, AnimateOnMapActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * Find Two nearest point
     *
     * @return List of nearest place
     */
    private List<com.zipgo.zipgoassignment.Place> findNearestPlaces() {
        RealmResults<com.zipgo.zipgoassignment.Place> places = realm.where(com.zipgo.zipgoassignment.Place.class).findAll().sort("latitude");
        double diff = Double.MAX_VALUE;
        com.zipgo.zipgoassignment.Place firstPlace = null;
        com.zipgo.zipgoassignment.Place secondPlace = null;
        List<com.zipgo.zipgoassignment.Place> placeList = new ArrayList<>();
        if (places.size() >= 2) {
            for (int i = 0; i < places.size() - 1; i++) {
                com.zipgo.zipgoassignment.Place place1 = places.get(i);
                com.zipgo.zipgoassignment.Place place2 = places.get(i + 1);
                if (place1 != null && place2 != null) {
                    LatLng latLng1 = new LatLng(place1.getLatitude(), place1.getLongitude());
                    LatLng latLng2 = new LatLng(place2.getLatitude(), place2.getLongitude());
                    double dist = SphericalUtil.computeDistanceBetween(latLng2, latLng1);
                    if (dist < diff) {
                        diff = dist;
                        firstPlace = place1;
                        secondPlace = place2;
                    }
                }
            }
            placeList.add(firstPlace);
            placeList.add(secondPlace);
        }
        return placeList;
    }
}
