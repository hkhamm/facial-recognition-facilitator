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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TrainController extends Controller {

    private String getBase64String(Picture picture) {
        String base64Image = picture.getBase64();
        String[] base64Array = base64Image.split(",");

        if (base64Array.length > 1) {
            base64Image = base64Array[1];
        }

        return base64Image;
    }

    private JsonNode sendRegister(JsonNode request) {
        Logger.info("\n** Train request **");
        Logger.info(request.toString());

        FPPCommunicator fpp = new FPPCommunicator();
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(new MyPropertyNamingStrategy());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        TrainResponse trainResponse = new TrainResponse();

        TrainRequest trainRequest = new TrainRequest();
        try {
            trainRequest = mapper.readValue(request.toString(), TrainRequest.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ArrayList<Picture> picturesIn = trainRequest.getPictures();

        ArrayList<FacilitatorId> facilitatorIds = trainRequest.getFacilitatorIds();

        String fppPersonId = "";
        if (facilitatorIds != null) {
            for (FacilitatorId facilitatorId : facilitatorIds) {
                if (Objects.equals(facilitatorId.getFacType(), "fpp")) {
                    fppPersonId = facilitatorId.getFacId();
                }
            }
        } else {
            fppPersonId = fpp.createPerson();
            // TODO if string == null, return success = false

            facilitatorIds = new ArrayList<>();
            FacilitatorId facilitatorId = new FacilitatorId("fpp", fppPersonId);
            facilitatorIds.add(facilitatorId);
        }

        trainResponse.setFacilitatorIds(facilitatorIds);

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

        trainResponse.setSuccess(isTrained);

        if (pictureErrors.size() > 0 || !isTrained) {
            trainResponse.setErrors(pictureErrors);
        }

        JsonNode jsonNode = mapper.valueToTree(trainResponse);
        Logger.info("\nRegister response");
        Logger.info(jsonNode.toString());

        return jsonNode;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> train() {
        JsonNode json = request().body().asJson();
        return CompletableFuture.supplyAsync(() -> sendRegister(json)).thenApply(Results::ok);
    }
}
