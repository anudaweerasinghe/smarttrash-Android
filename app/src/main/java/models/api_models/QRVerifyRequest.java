package models.api_models;

/**
 * Created by anuda on 1/3/18.
 */

public class QRVerifyRequest {

    private String phone;

    private String authCode;

    private Double redeemLat;
    private Double redeemLng;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
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
