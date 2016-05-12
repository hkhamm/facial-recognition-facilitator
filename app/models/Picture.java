package models;

public class Picture {

    private String base64;
    private int pictureId;

    public Picture() {}

    public Picture(String base64, int pictureId) {
        this.base64 = base64;
        this.pictureId = pictureId;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public int getPictureId() {
        return pictureId;
    }

    public void setPictureId(int pictureId) {
        this.pictureId = pictureId;
    }
}
