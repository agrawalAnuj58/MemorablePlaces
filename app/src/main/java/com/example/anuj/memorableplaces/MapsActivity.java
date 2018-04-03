package com.example.anuj.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.anuj.memorableplaces.MainActivity.arrayAdapter;
import static com.example.anuj.memorableplaces.MainActivity.latitude;
import static com.example.anuj.memorableplaces.MainActivity.longitude;
import static com.example.anuj.memorableplaces.MainActivity.places;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    String title;
    int userChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mMap.setMyLocationEnabled(true);
                onMyLocationButtonClick();
                mMap.setOnMyLocationButtonClickListener(this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1,locationListener);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent = getIntent();
        userChoice = intent.getIntExtra("listPosition",0);

        if (userChoice == 0){

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                mMap.setMyLocationEnabled(true);
                onMyLocationButtonClick();
                mMap.setOnMyLocationButtonClickListener(this);
                mMap.setOnMapLongClickListener(this);

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1,locationListener);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

        }
        else{
            LatLng location = new LatLng(Double.parseDouble(latitude.get(userChoice)), Double.parseDouble(longitude.get(userChoice)));

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15));
            mMap.setOnMarkerClickListener(this);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        mMap.clear();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addresses != null && addresses.size() > 0)
                title = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (title == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy");
            title = sdf.format(new Date());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(title));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
        Toast.makeText(this, "Place Saved", Toast.LENGTH_SHORT).show();

        places.add(title);
        latitude.add(Double.toString(latLng.latitude));
        longitude.add(Double.toString(latLng.longitude));
        arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences = this.getSharedPreferences("com.example.anuj.memorableplaces",MODE_PRIVATE);
        try {
            sharedPreferences.edit().putString("permanentPlaces",ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("permanentLatitude", ObjectSerializer.serialize(latitude)).apply();
            sharedPreferences.edit().putString("permanentLongitude", ObjectSerializer.serialize(longitude)).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setTitle(places.get(userChoice));
        return false;
    }
}
