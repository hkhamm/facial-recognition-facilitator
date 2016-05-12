package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Objects;

public class MCSFaceAPICommunicator {

    private String apiKey;
    private String groupId;

    /**
     * Constructor
     */
    public MCSFaceAPICommunicator() {
        apiKey = "f78e82741d04406fa764e084a55ae59a";
        groupId = "default_group";

//        createGroup();

//        String personId = createPerson("person_0");
//
//        addFace(personId,
//                "https://www.whitehouse.gov/sites/whitehouse.gov/files/images/first-family/44_barack_obama%5B1%5D.jpg");
//
//        addFace(personId,
//                "http://www.worldnewspolitics.com/wp-content/uploads/2016/02/140718-barack-obama-2115_86aea53294a878936633ec10495866b6.jpg");
//
//        addFace(personId,
//                "http://i2.cdn.turner.com/cnnnext/dam/assets/150213095929-27-obama-0213-super-169.jpg");
//
//        addFace(personId,
//                "http://a.abcnews.go.com/images/US/AP_obama8_ml_150618_16x9_992.jpg");
//
//        addFace(personId,
//                "http://www.dailystormer.com/wp-content/uploads/2015/06/2014-10-12-obama-618x402.jpg");
//
//        addFace(personId,
//                "http://media.vocativ.com/photos/2015/10/RTS2O4I-22195838534.jpg");
//
//        addFace(personId,
//                "https://upload.wikimedia.org/wikipedia/commons/e/e9/Official_portrait_of_Barack_Obama.jpg");
//
//        addFace(personId,
//                "http://cbsnews2.cbsistatic.com/hub/i/r/2015/10/09/f27caaea-86d1-41e2-bec2-97e89e5dba03/thumbnail/770x430/0a4b24d154ee526bb6811f1888e16600/presidentobamamain.jpg");
//
//        trainGroup();
//
//        identifyPerson("http://i.huffpost.com/gen/2518262/images/n-OBAMA-628x314.jpg");
//
//        removePerson(personId);
    }

    /**
     * Creates a person group.
     * @return the response string
     */
    protected String createGroup() {
        System.out.println("\n[MCS] Creating group");
        HttpClient httpclient = HttpClients.createDefault();
        String result = "";

        try {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/face/v1.0/persongroups/" + groupId);

            URI uri = builder.build();
            HttpPut request = new HttpPut(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            // RegisterRequest body
            //
            // application/json
            //
            // {
            //   "name":"group1",
            //   "userData":"user-provided data attached to the person group"
            // }
            //
            String body = String.format("{\"name\":\"%1$s\", \"userData\":\"none\"}", groupId);
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            } else {
                System.out.println("Group created");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    /**
     * Removes an existing person group.
     * @return the result string
     */
    protected String removeGroup() {
        System.out.println("\nRemoving group");
        HttpClient httpclient = HttpClients.createDefault();
        String result = "";

        try {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/face/v1.0/persongroups/" + groupId);

            URI uri = builder.build();
            HttpDelete request = new HttpDelete(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    /**
     * Creates a person with the given name.
     * @param personName the person's name
     * @return the Face API personId
     */
    protected String createPerson(String personName) {
        System.out.println("\n[MCS] Creating person in group");
        HttpClient httpclient = HttpClients.createDefault();
        String personId = "";

        try {
            String address = String.format("https://api.projectoxford.ai/face/v1.0/persongroups/%1$s/persons",
                    groupId);
            URIBuilder builder = new URIBuilder(address);

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            // RegisterRequest body
            //
            // application/json
            //
            // {
            //   "name":"Person1",
            //   "userData":"User-provided data attached to the person"
            // }
            //
            String body = String.format("{\"name\":\"%1$s\", \"userData\":\"none\"}", personName);
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(entity.getContent());

                personId = json.get("personId").asText();
                System.out.println(json);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return personId;
    }

    /**
     * Removes a person using its personId.
     * @param personId the Face API personId
     * @return the response string
     */
    protected String removePerson(String personId) {
        System.out.println("\n[MCS] Removing person");
        HttpClient httpclient = HttpClients.createDefault();
        String result = "";

        try {
            String address = String.format(
                    "https://api.projectoxford.ai/face/v1.0/persongroups/%1$s/persons/%2$s",
                    groupId, personId);
            URIBuilder builder = new URIBuilder(address);

            URI uri = builder.build();
            HttpDelete request = new HttpDelete(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    /**
     * Adds a face to a person using the personId and an image URL.
     * @param personId the Face API personId
     * @param url the image URL
     * @return the response string
     */
    protected String addFace(String personId, String url) {
        System.out.println("\n[MCS] Adding face to person");
        HttpClient httpclient = HttpClients.createDefault();
        String result = "";

        try {
            String address = String.format(
                    "https://api.projectoxford.ai/face/v1.0/persongroups/%1$s/persons/%2$s/persistedFaces",
                    groupId, personId);
            URIBuilder builder = new URIBuilder(address);

//            builder.setParameter("userData", "{string}");
//            builder.setParameter("targetFace", "{string}");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            // RegisterRequest body
            //
            // application/json
            // {
            //   "url":"http://example.com/1.jpg"
            // }
            //
            // or
            //
            // application/octet-stream
            //
            // {
            //   [binary data]
            // }
            String body = String.format("{\"url\":\"%1$s\"}", url);
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    /**
     * Trains a person group in preparation for identification.
     * @return the response string
     */
    protected String trainGroup() {
        System.out.println("\n[MCS] Training group");
        HttpClient httpclient = HttpClients.createDefault();
        String result;

        try {
            String address = String.format("https://api.projectoxford.ai/face/v1.0/persongroups/%1$s/train",
                    groupId);
            URIBuilder builder = new URIBuilder(address);

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            // RegisterRequest body
            //
            // application/json
            //
            // body is empty
            //
            StringEntity reqEntity = new StringEntity("");
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
//                System.out.println(EntityUtils.toString(entity));
                System.out.println("Started training...");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        while (true) {
            if (getTrainingStatus()) {
                result = "{\"result\":\"success\"}";
                break;
            } else {
                try {
                    Thread.sleep(1000);  // 1000 milliseconds
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        return result;
    }

    /**
     * Gets the training status after a train group request.
     * @return true when the training is successful
     */
    protected Boolean getTrainingStatus() {
        System.out.println("\n[MCS] Getting training status");
        HttpClient httpclient = HttpClients.createDefault();
        Boolean result = false;

        try {
            String address = String.format(
                    "https://api.projectoxford.ai/face/v1.0/persongroups/%1$s/training",
                    groupId);
            URIBuilder builder = new URIBuilder(address);

            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(entity.getContent());
                System.out.println(json);

                if (Objects.equals(json.get("status").asText(), "succeeded")) {
                    result = true;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    /**
     * Attempts to detect a face in the image at the given URL.
     * @param imageUrl the image URL
     * @return the response string
     */
    protected String detectFace(String imageUrl) {
        System.out.println("\n[MCS] Detecting face");
        HttpClient httpclient = HttpClients.createDefault();
        String faceId = "";

        try {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/face/v1.0/detect");

            builder.setParameter("returnFaceId", "true");
            builder.setParameter("returnFaceLandmarks", "false");
//            builder.setParameter("returnFaceAttributes", "{string}");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            // RegisterRequest body
            //
            // application/json
            // {
            //   "url":"http://example.com/1.jpg"
            // }
            //
            // or
            //
            // application/octet-stream
            //
            // [binary data]
            //
            String body = String.format("{\"url\":\"%1$s\"}", imageUrl);
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode json = mapper.readTree(entity.getContent());
                System.out.println(json);

                faceId = json.get(0).get("faceId").asText();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return faceId;
    }

    /**
     * Attempts to identify the person in the given image URL.
     * @param imageUrl the image URL.
     * @return the response string
     */
    protected String identifyPerson(String imageUrl) {
        HttpClient httpclient = HttpClients.createDefault();
        String result = "";

        try {
            URIBuilder builder = new URIBuilder("https://api.projectoxford.ai/face/v1.0/identify");

            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Ocp-Apim-Subscription-Key", apiKey);

            String faceId = detectFace(imageUrl);

            System.out.println("\n[MCS] Identifying face");

            // RegisterRequest body
            //
            // application/json
            //
            // {
            //   "personGroupId":"sample_group",
            //   "faceIds":[
            //     "c5c24a82-6845-4031-9d5d-978df9175426",
            //     "65d083d4-9447-47d1-af30-b626144bf0fb"
            //   ],
            //   "maxNumOfCandidatesReturned":1
            // }
            //
            String body = String.format(
                    "{\"personGroupId\":\"%1$s\", \"faceIds\":[\"%2$s\"], \"maxNumOfCandidatesReturned\":1}",
                    groupId, faceId);
            StringEntity reqEntity = new StringEntity(body);
            request.setEntity(reqEntity);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                result = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println(result);
        return result;
    }

    public static void main(String[] args) {
        new MCSFaceAPICommunicator();
    }
}
