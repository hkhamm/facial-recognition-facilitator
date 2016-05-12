package models;

public class FacilitatorId {

    private String facType;
    private String facId;

    public FacilitatorId() {}

    public FacilitatorId(String facType, String facId) {
        this.facType = facType;
        this.facId = facId;
    }

    public String getFacId() {
        return facId;
    }

    public void setFacId(String facId) {
        this.facId = facId;
    }

    public String getFacType() {
        return facType;
    }

    public void setFacType(String facType) {
        this.facType = facType;
    }
}
