package models.api_models;

/**
 * Created by anuda on 1/3/18.
 */

public class RedeemRequest {

    private String authCode;
    private String phone;
    private Double redeemLat;
    private Double redeemLng;

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getRedeemLat() {
        return redeemLat;
    }

    public void setRedeemLat(Double redeemLat) {
        this.redeemLat = redeemLat;
    }

    public Double getRedeemLng() {
        return redeemLng;
    }

    public void setRedeemLng(Double redeemLng) {
        this.redeemLng = redeemLng;
    }
}
