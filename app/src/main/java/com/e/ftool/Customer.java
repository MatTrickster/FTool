package com.e.ftool;

import com.google.android.gms.maps.model.LatLng;

public class Customer {

    String name,contact,service,rating;
    LatLng latLng;

    Customer(String name,String contact,LatLng latLng,String service,String rating){
        this.name = name;
        this.contact = contact;
        this.latLng = latLng;
        this.service = service;
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getContact() {
        return contact;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
