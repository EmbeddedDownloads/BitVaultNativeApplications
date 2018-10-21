package net.sourceforge.opencamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

public class LocationSupplier {
	private static final String TAG = "LocationSupplier";

	private Context context = null;
	private LocationManager locationManager = null;
	private MyLocationListener [] locationListeners = null;

	LocationSupplier(Context context) {
		this.context = context;
		locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
	}

	public Location getLocation() {
		// returns null if not available
		if( locationListeners == null )
			return null;
		// location listeners should be stored in order best to worst
		for(int i=0;i<locationListeners.length;i++) {
			Location location = locationListeners[i].getLocation();
			if( location != null )
				return location;
		}
		return null;
	}
	
	private class MyLocationListener implements LocationListener {
		private Location location = null;
		public boolean test_has_received_location = false;
		
		Location getLocation() {
			return location;
		}
		
	    public void onLocationChanged(Location location) {
			if( MyDebug.LOG )
				Log.d(TAG, "onLocationChanged");
			this.test_has_received_location = true;
    		// Android camera source claims we need to check lat/long != 0.0d
    		if( location.getLatitude() != 0.0d || location.getLongitude() != 0.0d ) {
	    		if( MyDebug.LOG ) {
	    			Log.d(TAG, "received location:");
	    			Log.d(TAG, "lat " + location.getLatitude() + " long " + location.getLongitude() + " accuracy " + location.getAccuracy());
	    		}
				this.location = location;
    		}
	    }

	    public void onStatusChanged(String provider, int status, Bundle extras) {
	         switch( status ) {
	         	case LocationProvider.OUT_OF_SERVICE:
	         	case LocationProvider.TEMPORARILY_UNAVAILABLE:
	         	{
					if( MyDebug.LOG ) {
						if( status == LocationProvider.OUT_OF_SERVICE )
							Log.d(TAG, "location provider out of service");
						else if( status == LocationProvider.TEMPORARILY_UNAVAILABLE )
							Log.d(TAG, "location provider temporarily unavailable");
					}
					this.location = null;
					this.test_has_received_location = false;
	         		break;
	         	}
	         }
	    }

	    public void onProviderEnabled(String provider) {
	    }

	    public void onProviderDisabled(String provider) {
			if( MyDebug.LOG )
				Log.d(TAG, "onProviderDisabled");
			this.location = null;
			this.test_has_received_location = false;
	    }
	}
	
	void setupLocationListener() {
		if( MyDebug.LOG )
			Log.d(TAG, "setupLocationListener");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		// Define a listener that responds to location updates
		// we only set it up if store_location is true, to avoid unnecessarily wasting battery
		boolean store_location = sharedPreferences.getBoolean(PreferenceKeys.getLocationPreferenceKey(), false);
		if( store_location && locationListeners == null ) {
			locationListeners = new MyLocationListener[2];
			locationListeners[0] = new MyLocationListener();
			locationListeners[1] = new MyLocationListener();
			
			// location listeners should be stored in order best to worst
			// also see https://sourceforge.net/p/opencamera/tickets/1/ - need to check provider is available
			if( locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) ) {
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListeners[1]);
			}
			if( locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER) ) {
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListeners[0]);
			}
		}
		else if( !store_location ) {
			freeLocationListeners();
		}
	}
	
	void freeLocationListeners() {
		if( MyDebug.LOG )
			Log.d(TAG, "freeLocationListeners");
		if( locationListeners != null ) {
			for(int i=0;i<locationListeners.length;i++) {
				locationManager.removeUpdates(locationListeners[i]);
	            locationListeners[i] = null;
			}
            locationListeners = null;
		}
	}
	
	// for testing:

	public boolean testHasReceivedLocation() {
		if( locationListeners == null )
			return false;
		for(int i=0;i<locationListeners.length;i++) {
			if( locationListeners[i].test_has_received_location )
				return true;
		}
		return false;
	}

	public boolean hasLocationListeners() {
		if( this.locationListeners == null )
			return false;
		if( this.locationListeners.length != 2 )
			return false;
		for(int i=0;i<this.locationListeners.length;i++) {
			if( this.locationListeners[i] == null )
				return false;
		}
		return true;
	}

	public static String locationToDMS(double coord) {
		String sign = (coord < 0.0) ? "-" : "";
		boolean is_zero = true;
		coord = Math.abs(coord);
	    int intPart = (int)coord;
	    is_zero = is_zero && (intPart==0);
	    String degrees = String.valueOf(intPart);
	    double mod = coord - intPart;

	    coord = mod * 60;
	    intPart = (int)coord;
	    is_zero = is_zero && (intPart==0);
	    mod = coord - intPart;
	    String minutes = String.valueOf(intPart);

	    coord = mod * 60;
	    intPart = (int)coord;
	    is_zero = is_zero && (intPart==0);
	    String seconds = String.valueOf(intPart);

	    if( is_zero ) {
	    	// so we don't show -ve for coord that is -ve but smaller than 1"
	    	sign = "";
	    }
	    
	    return sign + degrees + " " + minutes + "'" + seconds + "\"";
	}
}
