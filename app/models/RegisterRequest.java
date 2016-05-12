package models;

import java.util.ArrayList;

public class RegisterRequest {

    private ArrayList<Picture> pictures;

    public RegisterRequest() {}

    public RegisterRequest(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    public ArrayList<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }
}
