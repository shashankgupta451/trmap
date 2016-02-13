package com.map.tr.trmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Raviiii on 08-01-2016.
 */
public class GooglePlace {
    private String icon;
    private String name;
    private String category;
    private String rating;
    private String open;
    private String vicinity;
    private Bitmap bitmap;

    public GooglePlace() {
        this.icon = "";
        this.name = "";
        this.rating = "";
        this.open = "";
        this.setCategory("");
        this.vicinity = "";
        this.bitmap = null;

    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getRating() {
        return rating;
    }

    public void setOpenNow(String open) {
        this.open = open;
    }

    public String getOpenNow() {
        return open;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getVicinity() {
        return vicinity;
    }

    public Bitmap getBitmapFromURL(String src){
        try{
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap mybitmap = BitmapFactory.decodeStream(input);
            return mybitmap;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
