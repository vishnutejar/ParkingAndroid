package com.parking.app;

import java.util.Map;

public class ParkingSlot {
    private String name;
    private String status;
    private double latitude;
    private double longitude;
    private Map<String, Integer> prices;

    public ParkingSlot(String name, String status, double latitude, double longitude, Map<String, Integer> prices) {
        this.name = name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.prices = prices;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<String, Integer> getPrices() {
        return prices;
    }
}
