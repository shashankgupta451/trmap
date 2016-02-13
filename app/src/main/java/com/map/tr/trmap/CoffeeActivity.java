package com.map.tr.trmap;

/**
 * Created by Raviiii on 31-01-2016.
 */

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import com.map.tr.trmap.log.L;
import com.map.tr.trmap.model.CoffeeShops;
import com.map.tr.trmap.model.Results;
import com.map.tr.trmap.rest.GooglePlacesService;

public class CoffeeActivity  extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private RecyclerView mRecyclerCoffeeShops;
    private CoffeeShopsAdapter mAdapter;

    private void loadNearbyCoffeeShops(double latitude, double longitude) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://maps.googleapis.com")
                .setLogLevel(RestAdapter.LogLevel.BASIC)
                .setLog(new RestAdapter.Log() {

                    @Override
                    public void log(String message) {
                        Log.d("TR", message);
                    }
                }).build();

        GooglePlacesService service = restAdapter.create(GooglePlacesService.class);
        service.getCafes(getHashMapWithQueryParameters(latitude, longitude), new CoffeeShopsCallback());
    }

    private Map<String, String> getHashMapWithQueryParameters(double latitude, double longitude) {
        Map<String, String> map = new HashMap<>(5);
        map.put("location", getCsvLatLng(latitude, longitude));
        map.put("radius", "1500");
        map.put("types", "mosque||museum||night_club||park||place_of_worship");
        map.put("sensor", true + "");
        map.put("key", getString(R.string.google_places_api_key));
        return map;
    }

    private String getCsvLatLng(double latitude, double longitude) {
        return latitude + "," + longitude;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMap();
        initRecycler();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_main2;
    }

    @Override
    public void onLocationUpdate(LatLng latLng) {
        loadNearbyCoffeeShops(latLng.latitude, latLng.longitude);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initRecycler() {
        mAdapter = new CoffeeShopsAdapter(this);
        mRecyclerCoffeeShops = (RecyclerView) findViewById(R.id.recycler_coffee_shops);
        mRecyclerCoffeeShops.setLayoutManager(new GridLayoutManager(this, 1, GridLayoutManager.HORIZONTAL, false));
        mRecyclerCoffeeShops.setAdapter(mAdapter);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 16.0F));
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        int position = mAdapter.getPosition(marker.getTitle());
        if (position != -1) {
            mRecyclerCoffeeShops.smoothScrollToPosition(position);
        }
        return false;
    }


    public class CoffeeShopsCallback implements Callback<CoffeeShops> {

        @Override
        public void success(CoffeeShops coffeeShops, Response response) {
            Log.d("TR", coffeeShops.toString());
            String status = coffeeShops.getStatus();

            if (status.equals(getString(R.string.status_ok))) {

                ArrayList<Results> listCoffeeShops = new ArrayList<>(50);
                //Normal flow of events
                for (Results current : coffeeShops.getResults()) {
                    double latitude = Double.valueOf(current.getGeometry().getLocation().getLatitude());
                    double longitude = Double.valueOf(current.getGeometry().getLocation().getLongitude());
                    LatLng position = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(position)
                            .title(current.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_coficon));
                    mMap.addMarker(markerOptions);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                    listCoffeeShops.add(current);
                }

                mAdapter.setDataSource(listCoffeeShops);
            } else if (status.equals(getString(R.string.status_over_query_limit))) {
                //Do actions to indicate the developer that the tier for this application must be increased
            } else if (status.equals(getString(R.string.status_request_denied))) {
                //The key is invalid in this case
                Toast.makeText(CoffeeActivity.this, "Request Denied", Toast.LENGTH_SHORT).show();
            } else if (status.equals(getString(R.string.status_invalid_request))) {
                //Some parameters are missing
            } else {
                L.s(CoffeeActivity.this, "Are you standed on an island? Because we didnt find any coffee shops near you");
            }
        }

        @Override
        public void failure(RetrofitError error) {
            L.s(CoffeeActivity.this, error.toString());
        }
    }
}
