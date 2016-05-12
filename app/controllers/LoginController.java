package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import models.LoginError;
import models.LoginRequest;
import models.LoginResponse;
import models.MyPropertyNamingStrategy;

import play.Logger;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class LoginController extends Controller {

    private String getBase64String(String picture) {
        String[] base64Array = picture.split(",");

        if (base64Array.length > 1) {
            picture = base64Array[1];
        }

        return picture;
    }

    private JsonNode sendLogin(JsonNode request) {
        Logger.info("\n** Login request **");
        Logger.info(request.toString());

        FacePPAPICommunicator fpp = new FacePPAPICommunicator();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new MyPropertyNamingStrategy());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        LoginRequest loginRequest = new LoginRequest();
        LoginResponse loginResponse = new LoginResponse();

        try {
            loginRequest = mapper.readValue(request.toString(), LoginRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String facId = loginRequest.getFacilitatorIds().get(0).getFacId();
        String picture = loginRequest.getPicture();
        String base64Image = getBase64String(picture);

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] imageBytes = decoder.decode(base64Image);

        // TODO instead of just a boolean, if an error occurred return an error object
        Boolean success = fpp.verifyPerson(facId, imageBytes);
        loginResponse.setSuccess(success);

        ArrayList<LoginError> errors;
        if (!success) {
            errors = new ArrayList<>();
            LoginError loginError = new LoginError(3, "There is something unknown wrong with the image.");
            errors.add(loginError);
            loginResponse.setErrors(errors);
        }

        JsonNode jsonNode = mapper.valueToTree(loginResponse);
        Logger.info("\nLogin response");
        Logger.info(jsonNode.toString());

        return jsonNode;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> login() {
        JsonNode json = request().body().asJson();
        return CompletableFuture.supplyAsync(() -> sendLogin(json)).thenApply(Results::ok);
    }
}
