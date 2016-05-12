package controllers;

import models.AddFaceResponse;
import models.PictureError;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import play.Logger;

import java.io.File;
import java.util.Objects;

/**
 * Facilitates communication between the authentication server and the FacePlusPlus API.
 * Makes HTTP requests to the API to create groups of people. Each person has a set of face
 * images. These are used to determine if a provided image of a person's face matches any
 * of the people in the group.
 */
public class FacePPAPICommunicator {

    private HttpRequests httpRequests;
    private String groupName;

    /**
     * Constructor
     */
    FacePPAPICommunicator() {
        String apiKey = "c3de3abff9815f7bdf3b35347a519ccd";
        String apiSecret = "bLdQbvPADlkjoUgXsoqzu4hcFIaEnN9o";
        Boolean useChineseServer = false;
        Boolean useHttp = true;
        groupName = "group_0";

        httpRequests = new HttpRequests(apiKey, apiSecret, useChineseServer, useHttp);
    }

    /**
     * Attempts to detect a face in the image at the given URL.
     * @param url the image URL
     * @return the FacePP FaceId string
     */
    String detectFace(String url) {
        Logger.info("\nDetecting face from URL");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setUrl(url));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return getFaceId(response);
    }

    /**
     * Attempts to detect a face in the image file.
     * @param file the image file
     * @return the result string
     */
    String detectFace(File file) {
        Logger.info("\nDetecting face from file");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setImg(file));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Attempts to detect a face in the image byte array.
     * @param data the image byte array
     * @return the result string
     */
    String detectFace(byte[] data) {
        Logger.info("\nDetecting face from byte[]");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.detectionDetect(new PostParameters().setImg(data));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        // {"face":[{"face_id":"6cf26ff5d022104e05ef7d51f7a4e70b"

        String face_id = "";
        try {
            face_id = response.getJSONArray("face").getJSONObject(0).getString("face_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return face_id;
    }

    String getPersonId(JSONObject response) {
        String personId = "";
        try {
            personId = response.getString("person_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return personId;
    }

    /**
     * Creates a person with the given name.
     * @return the response string
     */
    String createPerson() {
        Logger.info("\nCreating person");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.personCreate(new PostParameters());
        } catch (FaceppParseException e) {
            try {
                response = httpRequests.personGetInfo(new PostParameters());
            } catch (FaceppParseException e1) {
                e1.printStackTrace();
            }
//            Integer errorCode = e.getErrorCode();
//            String errorMessage = e.getErrorMessage();
//
//            // Error: NAME_EXIST
//            if (errorCode == 1503) {
//                try {
//                    response = httpRequests.personGetInfo(new PostParameters().setPersonName(personName));
//                } catch (FaceppParseException e1) {
//                    e1.printStackTrace();
//                }
//            }
//            e.printStackTrace();
        }

        Logger.info(response.toString());

        String personId = "";
        try {
            personId = response.getString("person_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        addPerson(personId);

        return personId;
    }

    /**
     * Removes a person using its name.
     * @param personId the person's ID
     * @return the response string
     */
    String removePerson(String personId) {
        Logger.info("\nRemoving person");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.personDelete(new PostParameters().setPersonId(personId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Gets the face ID from the given result.
     * @param result a JSONObject result returned from a detect face API call
     * @return the face ID string
     */
    String getFaceId(JSONObject result) {
        String faceId = "";

        try {
            faceId = result.getJSONArray("face").getJSONObject(0).getString("face_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return faceId;
    }

    /**
     * Adds a face to a person using the person's ID and the face ID.
     * @param personId the person's ID
     * @param image the image byte[]
     * @return the response string
     */
    AddFaceResponse addFace(String personId, byte[] image) {
        JSONObject response = new JSONObject();

        // TODO get error code and message on bad image
        String faceId = detectFace(image);

        Logger.info("\nAdding face to person from byte[]");

        try {
            response = httpRequests.personAddFace(
                    new PostParameters().setPersonId(personId).setFaceId(faceId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());

        // {"response_code":200,"added":1,"success":true}

        Boolean success = false;
        try {
            success = response.getBoolean("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        AddFaceResponse addFaceResponse = new AddFaceResponse();
        addFaceResponse.setSuccess(success);
        addFaceResponse.setFaceId(faceId);

        return addFaceResponse;
    }

    /**
     * Adds a face to a person using the person's ID and the face ID.
     * @param personId the person's ID
     * @param url the image URL
     * @return the response string
     */
    String addFace(String personId, String url) {
        JSONObject response = new JSONObject();

        String faceId = detectFace(url);

        Logger.info("\nAdding face to person");

        try {
            response = httpRequests.personAddFace(
                    new PostParameters().setPersonId(personId).setFaceId(faceId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Removes a face from a person using the person's ID and the face ID.
     * @param personId the person's ID
     * @param faceId the face ID string
     * @return the response string
     */
    String removeFace(String personId, String faceId) {
        Logger.info("\nRemoving face");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.personRemoveFace(
                    new PostParameters().setPersonId(personId).setFaceId(faceId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Creates a person group.
     * @return the response string
     */
    String createGroup() {
        Logger.info("\nAdding group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupCreate(new PostParameters().setGroupName(groupName));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Removes an existing person group.
     * @return the response string
     */
    String removeGroup() {
        Logger.info("\nRemoving group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupDelete(new PostParameters().setGroupName(groupName));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Adds a person to a group with the person's ID.
     * @param personId the person's ID
     * @return the response string
     */
    String addPerson(String personId) {
        Logger.info("\nAdding person to group");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.groupAddPerson(
                    new PostParameters().setGroupName(groupName).setPersonId(personId));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.toString();
    }

    /**
     * Trains a person group in preparation for identification.
     * @return the response string
     */
    Boolean trainGroup() {
        Logger.info("\nTraining group");
        JSONObject trainResponse;
        JSONObject response = new JSONObject();

        try {
            trainResponse = httpRequests.trainIdentify(new PostParameters().setGroupName(groupName));
            response = httpRequests.getSessionSync(trainResponse.getString("session_id"));
        } catch (FaceppParseException | JSONException e) {
            e.printStackTrace();
        }

        // {"result":{"success":true},"response_code":200,"create_time":1461588035,"session_id":"95d27d5794264ef8843a2cab565d1b2f","finish_time":1461587773,"status":"SUCC"}
        Boolean result = false;
        try {
            result = response.getJSONObject("result").getBoolean("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Logger.info(response.toString());
//        return response.toString();

        return result;
    }

    /**
     * Attempts to identify the person in the image at the given URL.
     * @param url the image URL
     * @return the response JSONObject
     */
    JSONObject identifyPerson(String url) {
        Logger.info("\nIdentifying person from a url");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setUrl(url));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response;
    }

    /**
     * Attempts to identify the person in the given image file.
     * @param file the image file
     * @return the response JSONObject
     */
    JSONObject identifyPerson(File file) {
        Logger.info("\nIdentifying person from a file");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setImg(file));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }


        Logger.info(response.toString());
        return response;
    }

    /**
     * Attempts to identify the person in the given image byte array.
     * @param data the image byte array
     * @return the response JSONObject
     */
    Boolean identifyPerson(byte[] data, String userId) {
        Logger.info("\nIdentifying person from a byte[]");
        JSONObject response = new JSONObject();

        try {
            response = httpRequests.recognitionIdentify(new PostParameters().setGroupName(groupName).setImg(data));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        JSONObject candidate =
                response.getJSONArray("face").getJSONObject(0).getJSONArray("candidate").getJSONObject(0);
        String person_id = candidate.getString("person_id");
        String person_name = candidate.getString("person_name");
        Double confidence = candidate.getDouble("confidence");

        // TODO handle UserId as person_id and person_name, if one fails try the other
        Boolean isSamePerson = false;
        if (confidence >= 90 && (Objects.equals(person_id, userId) || Objects.equals(person_name, userId))) {
            isSamePerson = true;
        }

        Logger.info(response.toString());

        return isSamePerson;
    }

    Boolean trainPerson(String personId) {
        Logger.info("\nTraining person");
        JSONObject trainResponse;
        JSONObject response = new JSONObject();

        try {
            trainResponse = httpRequests.trainVerify(new PostParameters().setPersonId(personId));
            response = httpRequests.getSessionSync(trainResponse.getString("session_id"));
        } catch (FaceppParseException | JSONException e) {
            e.printStackTrace();
        }

        Boolean result = false;
        try {
            result = response.getJSONObject("result").getBoolean("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return result;
    }

    Boolean verifyPerson(String personId, byte[] image) {
        JSONObject response = new JSONObject();

        String faceId = detectFace(image);

        Logger.info("\n[FPP] Verifying person from a byte[]");

        try {
            response = httpRequests.recognitionVerify(
                    new PostParameters().setPersonId(personId).setFaceId(faceId).setImg(image));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.getBoolean("is_same_person");
    }

    Boolean verifyPerson(String personId, String image) {
        JSONObject response = new JSONObject();

        String faceId = detectFace(image);

        Logger.info("\n[FPP] Verifying person from a url");

        try {
            response = httpRequests.recognitionVerify(
                    new PostParameters().setPersonId(personId).setFaceId(faceId).setUrl(image));
        } catch (FaceppParseException e) {
            e.printStackTrace();
        }

        Logger.info(response.toString());
        return response.getBoolean("is_same_person");
    }

    public static void main(String[] args) {
//        FacePPAPICommunicator fpp = new FacePPAPICommunicator();
//
////        fpp.createGroup();
//
//        String personId = fpp.createPerson();
//
//        fpp.addFace(personId,
//                "https://www.whitehouse.gov/sites/whitehouse.gov/files/images/first-family/44_barack_obama%5B1%5D.jpg");
//
//        fpp.addFace(personId,
//                "http://www.worldnewspolitics.com/wp-content/uploads/2016/02/140718-barack-obama-2115_86aea53294a878936633ec10495866b6.jpg");
//
//        fpp.addFace(personId,
//                "http://i2.cdn.turner.com/cnnnext/dam/assets/150213095929-27-obama-0213-super-169.jpg");
//
//        fpp.addFace(personId,
//                "http://a.abcnews.go.com/images/US/AP_obama8_ml_150618_16x9_992.jpg");
//
//        fpp.addFace(personId,
//                "http://www.dailystormer.com/wp-content/uploads/2015/06/2014-10-12-obama-618x402.jpg");
//
//        fpp.addFace(personId,
//                "http://media.vocativ.com/photos/2015/10/RTS2O4I-22195838534.jpg");
//
//        fpp.addFace(personId,
//                "http://i.huffpost.com/gen/2518262/images/n-OBAMA-628x314.jpg");
//
//        fpp.addFace(personId,
//                "http://cbsnews2.cbsistatic.com/hub/i/r/2015/10/09/f27caaea-86d1-41e2-bec2-97e89e5dba03/thumbnail/770x430/0a4b24d154ee526bb6811f1888e16600/presidentobamamain.jpg");
//
//        fpp.trainPerson(personId);
//
//        fpp.verifyPerson(personId, "https://upload.wikimedia.org/wikipedia/commons/e/e9/Official_portrait_of_Barack_Obama.jpg");
//
////        fpp.trainGroup();
////
////        fpp.identifyPerson("https://upload.wikimedia.org/wikipedia/commons/e/e9/Official_portrait_of_Barack_Obama.jpg");
//
//        fpp.removePerson(personId);
    }
}
