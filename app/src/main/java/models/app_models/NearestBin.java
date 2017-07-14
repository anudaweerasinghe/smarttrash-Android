package models.app_models;


/**
 * Created by Anuda on 7/14/17.
 */

public class NearestBin {
    private NearestBinBin bin;
    private double distance;

    public NearestBinBin getBin() {
        return bin;
    }

    public void setBin(NearestBinBin bin) {
        this.bin = bin;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


}
