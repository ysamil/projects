package com.example.yusuf.smartsmsbox;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Sms> smsArrayList;
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
    public void onMapReady(GoogleMap googleMap) {
        Bundle bundle =getIntent().getExtras();
        smsArrayList=(ArrayList<Sms>)bundle.getSerializable("allSms");
        mMap = googleMap;
        googleMap.setMinZoomPreference(14.0f);
        double latitude;
        double longitude;
        LatLng location =new LatLng(0,0) ;
        for(Sms s:smsArrayList) {
            if(s.getType().equals("sending")) {
                if(s.getLatitude()!=0.0&&s.getLongitude()!=0.0) {
                    latitude=s.getLatitude();
                    longitude=s.getLongitude();
                    location = new LatLng(latitude,longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title("Your sms location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                }
            }
            else if(s.getType().equals("receiving")){
                if(s.getLatitude()!=0.0&&s.getLongitude()!=0.0) {
                    latitude=s.getLatitude();
                    longitude=s.getLongitude();
                    location = new LatLng(latitude,longitude);
                    mMap.addMarker(new MarkerOptions().position(location).title("Your sms location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        }

    }
}
