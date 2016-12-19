package com.byteshaft.laundry.laundry;

import android.net.Uri;

/**
 * Created by s9iper1 on 12/15/16.
 */

public class LaundryItem {

    private int id;
    private String name;
    private String price;
    private Uri imageUri;

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = Uri.parse(imageUri);
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
