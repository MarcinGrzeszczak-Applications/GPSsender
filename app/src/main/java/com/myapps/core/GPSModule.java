package com.myapps.core;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;



public class GPSModule {

    public interface Listener {
        void getActualPosition(double lat, double lng);
    }

    private GoogleApiClient googleApi;
    private Listener listener;
    private LocationRequest locR;
    private Activity activity;

    public GPSModule(Listener listener, Activity activity) {
        this.activity = activity;
        this.listener = listener;

        // Stworzenie nowego clienta google oraz nadanie callback
        googleConnectionCallback callback = new googleConnectionCallback();
        googleApi = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .enableAutoManage((FragmentActivity) activity, 0, callback)
                .addConnectionCallbacks(callback)
                .build();
        googleApi.connect(); // łączenie

    }

    // ustawianie opcji gps
    public void check() {
        locR = new LocationRequest();
        locR.setInterval(1000); // interwały
        locR.setFastestInterval(500); // interwały
        locR.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // ustawienie wysokiej dokładności

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locR);   // dodanie ustawień

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi
                .checkLocationSettings(googleApi, builder.build());  // dodanie ustawień

        result.setResultCallback(resultLocationCallback); // sprawdzenie czy gps włączony
    }



    // Ciało listenera. Pobiera lokalizację
    LocationListener locationPositionListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            listener.getActualPosition(location.getLatitude(),location.getLongitude()); // wywołanie listenera i przekazania lokalizacji do klasy SendingScreen
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApi, this); // Zatrzymanie sprawdania lokalizacji tylko raz wystarczy

        }
    };

    //ciało callbacka sprawdzającego czy gps włączony
    ResultCallback<LocationSettingsResult> resultLocationCallback = new ResultCallback<LocationSettingsResult>() {

        @Override
        public void onResult(@NonNull LocationSettingsResult result) {

            final Status status = result.getStatus();

            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    actualPosition();  // jesli tak , przejdź do funkcji
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        status.startResolutionForResult(activity, 0x1);  // jeśli jest wyłąćzony przejdź do osługi zdarzeń w SendingScreen
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    break;
            }

        }
    };


    public void actualPosition() {

        //Sprawdzenie pozwoleń aplikacji do lokalizacji tylko do API 23 (Marshmellow)
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    0x2);  // Jeśli nie ma pozwolenia przejdź do obsługi zdarzeń w SendingScreen
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApi, locR, locationPositionListener); // Włącz nasłuchiwanie lokalizacji
        }

    }

    // Callback od google , można wpisać w odopowiednie metody jakieś komunikaty dla użytkownika
    class googleConnectionCallback implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    }

}
