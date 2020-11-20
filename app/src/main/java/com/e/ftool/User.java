package com.e.ftool;

public class User {

    String name,number,pass;

    User(String name,String number,String pass){
        this.name = name;
        this.number = number;
        this.pass = pass;
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
