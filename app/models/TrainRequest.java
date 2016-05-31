package models;

import java.util.ArrayList;

public class TrainRequest {

    private ArrayList<FacilitatorId> facilitatorIds;
    private ArrayList<Picture> pictures;

    public TrainRequest() {}

    public ArrayList<FacilitatorId> getFacilitatorIds() {
        return facilitatorIds;
    }

    public void setFacilitatorIds(ArrayList<FacilitatorId> facilitatorIds) {
        this.facilitatorIds = facilitatorIds;
    }

    public TrainRequest(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }

    public ArrayList<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(ArrayList<Picture> pictures) {
        this.pictures = pictures;
    }
}
