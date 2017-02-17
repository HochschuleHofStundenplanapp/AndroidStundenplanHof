package de.hof.university.app.experimental.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hof.university.app.MainActivity;
import de.hof.university.app.R;

/**
 * Created by angelina on 18.01.17.
 */

public class MapFragment extends Fragment {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 1;

    MapView myOpenMapView;
    IMapController myMapController;
    LocationManager locationManager;
    LocationListener locationListener;
    Marker marker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Speicherort für OSMDroid auf persönlichen Ordner der App setzen
        Configuration.getInstance().setOsmdroidBasePath(new File(getActivity().getFilesDir(), "OSMDroidBase"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getActivity().getFilesDir(), "OSMDroidTileCache"));

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myOpenMapView = (MapView) view.findViewById(R.id.mapView);
        myOpenMapView.setBuiltInZoomControls(true);
        myMapController = myOpenMapView.getController();
        myMapController.setZoom(40);
        marker = new Marker(myOpenMapView);

        marker.setIcon(getResources().getDrawable(R.drawable.pin));

        myOpenMapView.getOverlays().add(marker);


        CloudmadeUtil.DEBUGMODE = true;

        TilesOverlay x = this.myOpenMapView.getOverlayManager().getTilesOverlay();
        x.setOvershootTileCache(x.getOvershootTileCache() * 2);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLoc(location);
                updateLocationInfo(location);
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


        // Witch Android Version?
        // before Android 6.0 Marshmallow
        if (Build.VERSION.SDK_INT < 23) {
            // überprüfen ob NETWORK_PROVIDER vorhanden ist sonst stützt die App ab
            if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation != null) {
                updateLoc(lastLocation);
            }
        } else {
            // Android Marshmallow or higher
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Einen Dialog bringen wenn der Nutzer den Hacken bei "Nicht noch einmal anzeigen" setzt oder beim ersten Mal
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(getView().getContext())
                            .setTitle(getString(R.string.needPermissionTitle))
                            .setMessage(getString(R.string.needPermissionText))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //ask for permission
                                    requestPermission();
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //nothing to do here. Just close the message
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }
                //ask for permission
                requestPermission();
            } else {
                // we have permission
                // überprüfen ob NETWORK_PROVIDER vorhanden ist sonst stützt die App ab
                if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    updateLoc(lastLocation);
                }
            }
        }
    }

    public void requestPermission() {
        if (Build.VERSION.SDK_INT > 23) {
            this.requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                    // überprüfen ob NETWORK_PROVIDER vorhanden ist sonst stützt die App ab
                    if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        // überprüfen ob NETWORK_PROVIDER vorhanden ist sonst stützt die App ab
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.getSupportActionBar().setTitle("Map");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
    }

    public void updateLoc(Location loc) {
        GeoPoint locGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
        myMapController.setCenter(locGeoPoint);
        myOpenMapView.invalidate();
        updateLocationInfo(loc);
        marker.setPosition(locGeoPoint);
    }

    public void updateLocationInfo(Location location) {
        Log.i("LocationInfo", location.toString());
        TextView addressTextView = (TextView) getView().findViewById(R.id.addressTextView);

        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        try {
            String address = "Could not find address";

            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (listAddresses != null && listAddresses.size() > 0) {
                Log.i("PlaceInfo", listAddresses.get(0).toString());

                address = "Addresse:\n";

                if (listAddresses.get(0).getThoroughfare() != null) {
                    address += listAddresses.get(0).getThoroughfare() + " ";
                }
                if (listAddresses.get(0).getSubThoroughfare() != null) {
                    address += listAddresses.get(0).getSubThoroughfare() + "\n";
                }
                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + "\n";
                }
                if (listAddresses.get(0).getPostalCode() != null) {
                    address += listAddresses.get(0).getPostalCode() + "\n";
                }
                if (listAddresses.get(0).getCountryName() != null) {
                    address += listAddresses.get(0).getCountryName() + "\n";
                }
            }
            addressTextView.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}