package com.example.yusuf.smartsmsbox;

/**
 * Created by Yusuf on 26.05.2018.
 */

public class Contact {
    private String name,imageURI,phone;
    public Contact(String name, String phone,String imageURI){
        this.name=name; this.phone=phone; this.imageURI=imageURI;
    }

    public String getName() {
        return name;
    }

    public String getImageURI() {
        return imageURI;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }



}
