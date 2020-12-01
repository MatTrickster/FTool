package com.e.ftool;

import com.google.android.gms.maps.model.LatLng;

public class Customer {

    String name,contact;
    LatLng latLng;

    Customer(String name,String contact,LatLng latLng){
        this.name = name;
        this.contact = contact;
        this.latLng = latLng;
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
