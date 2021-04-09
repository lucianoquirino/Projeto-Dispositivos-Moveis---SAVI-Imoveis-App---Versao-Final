package com.lucianoquirino.saviimoveisapp.activity;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lucianoquirino.saviimoveisapp.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocalizacaoImovelActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_localizacao_imovel);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        //Tipo de mapa exibido
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Processo de Geocoding (Transformar um endereço em latitude/longitude
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault() );

        Bundle dados = getIntent().getExtras();
        String localizacaoSelecionada = dados.getString("localizacao");

        try {
            //List<Address> listaEndereco = geocoder.getFromLocation(latitude, longitude,1);
            String stringEndereco = localizacaoSelecionada;
            List<Address> listaEndereco = geocoder.getFromLocationName(stringEndereco,1);
            if( listaEndereco != null && listaEndereco.size() > 0 ){
                Address endereco = listaEndereco.get(0);

                Double lat = endereco.getLatitude();
                Double lon = endereco.getLongitude();

                mMap.clear();
                LatLng localUsuario = new LatLng(lat, lon);
                mMap.addMarker(new MarkerOptions().position(localUsuario).title("Meu local"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localUsuario,17));

                Log.d("local", "onLocationChanged: " + endereco.getAddressLine(0) );

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}