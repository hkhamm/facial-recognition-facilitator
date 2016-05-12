package models;

import java.util.ArrayList;

public class LoginResponse {

    private Boolean success;
    private ArrayList<LoginError> errors;

    public LoginResponse() {}

    public LoginResponse(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ArrayList<LoginError> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<LoginError> errors) {
        this.errors = errors;
    }
}
