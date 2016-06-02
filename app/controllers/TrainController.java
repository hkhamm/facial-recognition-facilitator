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
import java.util.UUID;
                     
public class TrainController extends Controller {
        private volatile boolean fppHasCreatedPerson = false;
        private volatile boolean mcsHasCreatedPerson = false;
        private volatile boolean fppTrained = false;
        
        private volatile boolean fppHasAddedFaces = false;
        private volatile ArrayList<Picture> picturesIn;
        private volatile ArrayList<PictureError> pictureErrors;
        private volatile ArrayList<AddFaceResponse> addFaceResponsesFPP;
        private volatile ArrayList<AddFaceResponse> addFaceResponsesMCS;
        private volatile FPPCommunicator fpp;
        private volatile MCSCommunicator mcs;
        private volatile boolean mcsTrained = false;
        private volatile boolean mcsHasAddedFaces = false;
        private volatile String fppPersonId = "";
        private volatile String mcsPersonId = "";
        private volatile String mcsGroupId = "";
        
                                                                        
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
        MCSCommunicator mcs = new MCSCommunicator();
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
        
        picturesIn = trainRequest.getPictures();

        ArrayList<FacilitatorId> facilitatorIds = trainRequest.getFacilitatorIds();

        if (facilitatorIds != null) {
            for (FacilitatorId facilitatorId : facilitatorIds) {
                if (Objects.equals(facilitatorId.getFacType(), "fpp")) {
                    fppPersonId = facilitatorId.getFacId();
                }
                if (Objects.equals(facilitatorId.getFacType(), "mcs")) {
                    mcsPersonId = facilitatorId.getFacId();
                }
                if (Objects.equals(facilitatorId.getFacType(), "mcsGroup")) {
                    mcsGroupId = facilitatorId.getFacId();
                    mcs.createGroup(mcsGroupId);
                }
                
            }
        } else {
            mcsGroupId = UUID.randomUUID().toString();
            mcs.createGroup(mcsGroupId);
            Thread fppCreatedPersonThread = new Thread(new Runnable() {
            public void run() {
            fppPersonId = fpp.createPerson();
            fppHasCreatedPerson = true;
            }
            });
            fppCreatedPersonThread.start();
        
            Thread mcsCreatedPersonThread = new Thread(new Runnable() {
            public void run() {
            mcsPersonId = mcs.createPerson();
            mcsHasCreatedPerson = true;
            }
            });
            mcsCreatedPersonThread.start();

            while(!(fppHasCreatedPerson && mcsHasCreatedPerson))
            {
            }

            // TODO if string == null, return success = false

            facilitatorIds = new ArrayList<>();
            FacilitatorId facilitatorId = new FacilitatorId("fpp", fppPersonId);
            facilitatorIds.add(facilitatorId);
            facilitatorId = new FacilitatorId("mcs", mcsPersonId);
            facilitatorIds.add(facilitatorId);
            facilitatorId = new FacilitatorId("mcsGroup", mcsGroupId);
            facilitatorIds.add(facilitatorId);            
        }

        trainResponse.setFacilitatorIds(facilitatorIds);

        pictureErrors = new ArrayList<>();
        addFaceResponsesFPP = new ArrayList<>();
        addFaceResponsesMCS = new ArrayList<>();
        
        Thread fppAddedFacesThread = new Thread(new Runnable() {
            public void run() {
                for (Picture picture : picturesIn) {
                    String base64Image = getBase64String(picture);

                    try {
                        byte[] imageBytes = decoder.decode(base64Image);

                        AddFaceResponse addFaceResponseFPP = fpp.addFace(fppPersonId, imageBytes);
                        addFaceResponsesFPP.add(addFaceResponseFPP);

                        if (addFaceResponseFPP.getSuccess()) {
                        } else {
                            PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                                                                         "There is something unknown wrong with the image.");
                            pictureErrors.add(pictureError);
                        }
                    } catch (Exception e) {
                        PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                                                                     "There is something unknown wrong with the image.");
                        pictureErrors.add(pictureError);
                    }
                }
                fppTrained = fpp.trainPerson(fppPersonId);
                fppHasAddedFaces = true;
            }
            });
        fppAddedFacesThread.start();

        Thread mcsAddedFacesThread = new Thread(new Runnable() {
            public void run() {
                for (Picture picture : picturesIn) {
                    String base64Image = getBase64String(picture);

                    try {
                        byte[] imageBytes = decoder.decode(base64Image);

                        AddFaceResponse addFaceResponseMCS = mcs.addFace(mcsPersonId, imageBytes);
                        addFaceResponsesMCS.add(addFaceResponseMCS);

                        if (addFaceResponseMCS.getSuccess()) {
                        } else {
                            PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                                                                         "There is something unknown wrong with the image.");
                            pictureErrors.add(pictureError);
                        }
                    } catch (Exception e) {
                        PictureError pictureError = new PictureError(picture.getPictureId(), 3,
                                                                     "There is something unknown wrong with the image.");
                        pictureErrors.add(pictureError);
                    }
                }
                mcsTrained = mcs.trainGroup();
                mcsHasAddedFaces = true;
            }
            });
        mcsAddedFacesThread.start();
        
        while(!(fppHasAddedFaces && mcsHasAddedFaces))
        {
        }

        Boolean success = fppTrained && mcsTrained;

        if (!success) {
            PictureError pictureError = new PictureError(999, 12,
                    "Training the new user failed for an unknown reason.");
            pictureErrors.add(pictureError);
        }

        boolean isTrained = true;

        trainResponse.setSuccess(isTrained);

        if (pictureErrors.size() > 0 || !isTrained) {
            trainResponse.setErrors(pictureErrors);
        }

        JsonNode jsonNode = mapper.valueToTree(trainResponse);
        Logger.info("\nTrain response");
        Logger.info(jsonNode.toString());

        return jsonNode;
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> train() {
        JsonNode json = request().body().asJson();
        return CompletableFuture.supplyAsync(() -> sendRegister(json)).thenApply(Results::ok);
    }
}
