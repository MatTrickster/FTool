package com.e.ftool;

import android.icu.util.ULocale;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashMap;

public class Driver {

    LatLng location;
    String name,number,key,service,rating,imgUrl,serviceCharge;
    long rCount;
    Float dist;

    Driver(String name,String number,String service,String rating,String imgUrl,long rCount){
        this.name = name;
        this.number = number;
        this.service = service;
        this.rating = rating;
        this.imgUrl = imgUrl;
        this.rCount = rCount;
    }

    Driver(LatLng location,String serviceCharge, String name,String number,String key,String imgUrl,String rating,
           float dist){
        this.location = location;
        this.name = name;
        this.number = number;
        this.serviceCharge = serviceCharge;
        this.key = key;
        this.imgUrl = imgUrl;
        this.rating = rating;
        this.dist = dist;
    }

    public Float getDist() {
        return dist;
    }

    public void setDist(Float dist) {
        this.dist = dist;
    }

    public long getrCount() {
        return rCount;
    }

    public void setrCount(long rCount) {
        this.rCount = rCount;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
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

    public String getServiceCharge() {
        return serviceCharge;
    }

    public void setServiceCharge(String serviceCharge) {
        this.serviceCharge = serviceCharge;
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

}
