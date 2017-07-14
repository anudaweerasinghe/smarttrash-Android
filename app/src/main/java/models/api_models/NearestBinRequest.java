package models.api_models;

/**
 * Created by Anuda on 7/14/17.
 */

public class NearestBinRequest {
    private double currentLocationLat;
    private double currentLocationLng;

    public double getCurrentLocationLat() {
        return currentLocationLat;
    }

    public void setCurrentLocationLat(double currentLocationLat) {
        this.currentLocationLat = currentLocationLat;
    }

    public double getCurrentLocationLng() {
        return currentLocationLng;
    }

    public void setCurrentLocationLng(double currentLocationLng) {
        this.currentLocationLng = currentLocationLng;
    }
}
