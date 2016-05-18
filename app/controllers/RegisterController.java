package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import models.*;

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

public class RegisterController extends Controller {

    private String getBase64String(Picture picture) {
        String base64Image = picture.getBase64();
        String[] base64Array = base64Image.split(",");

        if (base64Array.length > 1) {
            base64Image = base64Array[1];
        }

        return base64Image;
    }

    private JsonNode sendRegister(JsonNode request) {
        Logger.info("\n** Register request **");
        Logger.info(request.toString());

        FacePPAPICommunicator fpp = new FacePPAPICommunicator();
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new MyPropertyNamingStrategy());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        RegisterResponse registerResponse = new RegisterResponse();

        RegisterRequest registerRequest = new RegisterRequest();
        try {
            registerRequest = mapper.readValue(request.toString(), RegisterRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Picture> picturesIn = registerRequest.getPictures();

        String fppPersonId = fpp.createPerson();
        // TODO if string == null, return success = false

        ArrayList<PictureError> pictureErrors = new ArrayList<>();
        ArrayList<AddFaceResponse> addFaceResponses = new ArrayList<>();
        int successCount = 0;

        for (Picture picture : picturesIn) {
            String base64Image = getBase64String(picture);

            try {
                byte[] imageBytes = decoder.decode(base64Image);

                AddFaceResponse addFaceResponse = fpp.addFace(fppPersonId, imageBytes);
                addFaceResponses.add(addFaceResponse);

                if (addFaceResponse.getSuccess()) {
                    successCount++;
                } else {
                    PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                            "There is something unknown wrong with the image.");
                    pictureErrors.add(pictureError);
                }
            } catch (Exception e) {
                successCount -= 1;
                PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                        "There is something unknown wrong with the image.");
                pictureErrors.add(pictureError);
            }
        }

        Boolean success = fpp.trainPerson(fppPersonId);

        Boolean isTrained = false;
        if (successCount >= 7 && success) {
            for (int i = addFaceResponses.size() - 1; i >= 0; i--) {
                if (addFaceResponses.get(i).getSuccess()) {
                    fpp.removeFace(fppPersonId, addFaceResponses.get(i).getFaceId());
                    byte[] imageBytes = decoder.decode(picturesIn.get(i).getBase64());
                    fpp.verifyPerson(fppPersonId, imageBytes);
                    fpp.addFace(fppPersonId, imageBytes);
                    isTrained = true;
                    break;
                }
            }
        }

        if (successCount < 7) {
            PictureError pictureError = new PictureError(999, 11,
                    "Not enough pictures to train the user. Training requires 7 - 8 pictures.");
            pictureErrors.add(pictureError);
        }

        if (!success) {
            PictureError pictureError = new PictureError(999, 12,
                    "Training the new user failed for an unknown reason.");
            pictureErrors.add(pictureError);
        }

        registerResponse.setSuccess(isTrained);

        if (pictureErrors.size() > 0 || !isTrained) {
            registerResponse.setErrors(pictureErrors);
        } else {
            ArrayList<FacilitatorId> facilitatorIds = new ArrayList<>();
            FacilitatorId facilitatorId = new FacilitatorId("fpp", fppPersonId);
            facilitatorIds.add(facilitatorId);
            registerResponse.setFacilitatorIds(facilitatorIds);
        }

        JsonNode jsonNode = mapper.valueToTree(registerResponse);
        Logger.info("\nRegister response");
        Logger.info(jsonNode.toString());

        return jsonNode;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> register() {
        JsonNode json = request().body().asJson();
        return CompletableFuture.supplyAsync(() -> sendRegister(json)).thenApply(Results::ok);
    }
}
