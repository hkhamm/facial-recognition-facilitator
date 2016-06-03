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

    private volatile boolean success = false;
    private volatile boolean fppSuccess = false;
    private volatile boolean mcsSuccess = false;
    private volatile boolean fppHasVerified = false;
    private volatile boolean mcsHasVerified = false;

    private volatile FPPCommunicator fpp;
    private volatile MCSCommunicator mcs;


    /**
     * Gets only the base64 string from a base64 string with header.
     * @param picture the original base64 string
     * @return the base64 string stripped of a header.
     */
    private String getBase64String(String picture) {
        String[] base64Array = picture.split(",");

        if (base64Array.length > 1) {
            picture = base64Array[1];
        }

        return picture;
    }

    /**
     * Makes a verify person request to the Face++ API and an identify person request to Microsoft Cognitive
     * Services Face API.
     * @param request is the JsonNode request object
     * @return a JsonNode version of LoginResponse
     */
    private JsonNode sendLogin(JsonNode request) {
        Logger.info("\n** Login request **");
        Logger.info(request.toString());

        fpp = new FPPCommunicator();
        mcs = new MCSCommunicator();

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

        String facIdFPP = loginRequest.getFacilitatorIds().get(0).getFacId();
        String facIdMCS = loginRequest.getFacilitatorIds().get(1).getFacId();
        String groupIdMCS = loginRequest.getFacilitatorIds().get(2).getFacId();
        String picture = loginRequest.getPicture();
        String base64Image = getBase64String(picture);

        Base64.Decoder decoder = Base64.getDecoder();

        mcs.createGroup(groupIdMCS);

        try {
            byte[] imageBytes = decoder.decode(base64Image);
            // TODO instead of just a boolean, if an error occurred return an error object
        
            Thread fppVerifyThread = new Thread(new Runnable() {
                public void run() {
                    fppSuccess = fpp.verifyPerson(facIdFPP, imageBytes);
                    fppHasVerified = true;
                }
            });
            fppVerifyThread.start();
        
            Thread mcsVerifyThread = new Thread(new Runnable() {
                public void run() {
                    mcsSuccess = mcs.verifyPerson(facIdMCS, imageBytes);
                    mcsHasVerified = true;
                }
            });
            mcsVerifyThread.start();

            while (!(fppHasVerified && mcsHasVerified)) {
            }

            success = fppSuccess && mcsSuccess;
             
        } catch (IllegalArgumentException e) {
            Logger.error("Unable to decode base 64 encoded image string.");
        }

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

    /**
     * Makes a login request to the facial recognition services.
     * @return a JsonNode version of LoginResponse
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> login() {
        JsonNode json = request().body().asJson();
        return CompletableFuture.supplyAsync(() -> sendLogin(json)).thenApply(Results::ok);
    }
}
