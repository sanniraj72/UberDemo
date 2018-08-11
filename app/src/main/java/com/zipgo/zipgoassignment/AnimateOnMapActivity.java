package com.zipgo.zipgoassignment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * This activity will help to show the all location with a connected line
 */
public class AnimateOnMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Log TAG
    private static final String TAG = "zipgo_app";
    // GoogleMap Object
    private GoogleMap googleMap;
    // list of LatLng
    private List<LatLng> listLatLng = new ArrayList<>();
    // Polyline with black color
    private Polyline blackPolyLine;
    // Polyline with grey color
    private Polyline greyPolyLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animate_on_map);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.animate_on_map);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Get the SupportMapFragment and request notification when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMaxZoomPreference(20);
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style);
        googleMap.setMapStyle(style);

        RealmResults<Place> places = Realm.getDefaultInstance().where(com.zipgo.zipgoassignment.Place.class).findAll();
        if (places.size() >= 2) {
            // if list will have 2 or more than 2 location then only proceed to draw
            List<LatLng> latLngList = new ArrayList<>();
            for (Place place : places) {
                latLngList.add(new LatLng(place.getLatitude(), place.getLongitude()));
            }
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(new LatLng(latLngList.get(0).latitude, latLngList.get(0).longitude));
            builder.include(new LatLng(latLngList.get(latLngList.size() - 1).latitude, latLngList.get(latLngList.size() - 1).longitude));
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            googleMap.animateCamera(cu, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {

                }

                @Override
                public void onCancel() {

                }
            });
            setUpPolyLine(latLngList);
        } else {
            Toast.makeText(AnimateOnMapActivity.this, "At least 2 locations should be added in list.", Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    /**
     * Set up Polyline
     *
     * @param latLngList latLngList
     */
    protected void setUpPolyLine(List<LatLng> latLngList) {
        String source = latLngList.get(0).latitude + "," + latLngList.get(0).longitude;
        String destination = latLngList.get(latLngList.size() - 1).latitude + "," + latLngList.get(latLngList.size() - 1).longitude;
        String waypoints = "";
        if (latLngList.size() > 2) {
            for (int i = 1; i < latLngList.size() - 1; i++) {
                waypoints = waypoints.concat(latLngList.get(i).latitude + "," + latLngList.get(i).longitude + "|");
            }
            waypoints = waypoints.substring(0, waypoints.length() - 1);
        }
        // Create retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/directions/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GetPolyline polyline = retrofit.create(GetPolyline.class);
        // Call to Get Polyline Data
        polyline.getPolylineData(source, destination, waypoints, "AIzaSyA7IBmfRAZa6jVGlNjVHbDcHFbPS8BdNGU")
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        JsonObject gson = new JsonParser().parse(response.body().toString()).getAsJsonObject();
                        try {
                            Single.just(parse(new JSONObject(gson.toString())))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<List<List<HashMap<String, String>>>>() {
                                        @Override
                                        public void accept(List<List<HashMap<String, String>>> lists) throws Exception {
                                            drawPolyline(lists);
                                        }
                                    });
                        } catch (JSONException e) {
                            Toast.makeText(AnimateOnMapActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        Toast.makeText(AnimateOnMapActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Parse Json response
     *
     * @param jObject jObject
     * @return List
     */
    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

        List<List<HashMap<String, String>>> routes = new ArrayList<>();
        JSONArray jLegs;
        JSONArray jSteps;
        try {
            JSONArray jRoutes = jObject.getJSONArray("routes");
            // Traversing all routes
            for (int i = 0; i < jRoutes.length(); i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                List<HashMap<String, String>> path = new ArrayList<>();
                // Traversing all legs
                for (int j = 0; j < jLegs.length(); j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                    // Traversing all steps
                    for (int k = 0; k < jSteps.length(); k++) {
                        String polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                        List<LatLng> list = decodePoly(polyline);
                        // Traversing all points
                        for (int l = 0; l < list.size(); l++) {
                            HashMap<String, String> hm = new HashMap<>();
                            hm.put("lat", Double.toString(list.get(l).latitude));
                            hm.put("lng", Double.toString((list.get(l)).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }
        } catch (Exception ignored) {
            Log.w(TAG, ignored.getMessage());
        }
        return routes;
    }

    /**
     * Draw Polyline
     *
     * @param result result
     */
    private void drawPolyline(List<List<HashMap<String, String>>> result) {
        ArrayList<LatLng> points;
        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++) {
            points = new ArrayList<>();
            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            this.listLatLng.addAll(points);
        }
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.width(10);
        lineOptions.color(Color.BLACK);
        lineOptions.startCap(new SquareCap());
        lineOptions.endCap(new SquareCap());
        lineOptions.jointType(JointType.ROUND);
        blackPolyLine = this.googleMap.addPolyline(lineOptions);

        PolylineOptions greyOptions = new PolylineOptions();
        greyOptions.width(10);
        greyOptions.color(Color.GRAY);
        greyOptions.startCap(new SquareCap());
        greyOptions.endCap(new SquareCap());
        greyOptions.jointType(JointType.ROUND);
        greyPolyLine = this.googleMap.addPolyline(greyOptions);

        animatePolyLine();
    }

    /**
     * Animate Polyline on Map
     */
    private void animatePolyLine() {
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(3000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                List<LatLng> latLngList = blackPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * listLatLng.size()) / 100;
                if (initialPointSize < newPoints) {
                    latLngList.addAll(listLatLng.subList(initialPointSize, newPoints));
                    blackPolyLine.setPoints(latLngList);
                }
            }
        });
        animator.addListener(polyLineAnimationListener);
        animator.start();
    }

    /**
     * Decode Poly points
     *
     * @param encoded encoded
     * @return List
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    /**
     * Listener for Animation
     */
    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {
            if (listLatLng.size() != 0) {
                addMarker(listLatLng.get(listLatLng.size() - 1));
            }
        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> blackLatLng = blackPolyLine.getPoints();
            List<LatLng> greyLatLng = greyPolyLine.getPoints();

            greyLatLng.clear();
            greyLatLng.addAll(blackLatLng);
            blackLatLng.clear();

            blackPolyLine.setPoints(blackLatLng);
            greyPolyLine.setPoints(greyLatLng);

            blackPolyLine.setZIndex(2);

            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };

    /**
     * Add Marker at destination
     *
     * @param destination destination
     */
    private void addMarker(LatLng destination) {
        MarkerOptions options = new MarkerOptions();
        options.position(destination);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        this.googleMap.addMarker(options);
    }
}
