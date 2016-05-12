package models;

public class AddFaceResponse {

    private String faceId;
    private Boolean success;
    private PictureError pictureError;

    public AddFaceResponse() {}

    public AddFaceResponse(String faceId, Boolean success) {
        this.faceId = faceId;
        this.success = success;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public PictureError getPictureError() {
        return pictureError;
    }

    public void setPictureError(PictureError pictureError) {
        this.pictureError = pictureError;
    }
}
