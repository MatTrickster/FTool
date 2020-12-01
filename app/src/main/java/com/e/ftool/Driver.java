package com.e.ftool;

import android.icu.util.ULocale;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class Driver {

    LatLng location;
    ArrayList<HashMap<String,String>> services;
    String name,number,key;

    Driver(LatLng location,ArrayList<HashMap<String,String>> services, String name,String number,String key){
        this.location = location;
        this.name = name;
        this.number = number;
        this.services = services;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<HashMap<String,String>> getServices() {
        return services;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setServices(ArrayList<HashMap<String,String>> services) {
        this.services = services;
    }
}
