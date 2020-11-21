package com.e.ftool;

import com.google.firebase.auth.PhoneAuthCredential;

public class User {

    String name,number,pass,vId,code;
    Double latitude,longitude;

    User(String name, String number, String pass){
        this.name = name;
        this.number = number;
        this.pass = pass;
        this.vId = vId;
        this.code = code;
    }

    User(String name,String number,Double latitude,Double longitude){
        this.name = name;
        this.number = number;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
}
