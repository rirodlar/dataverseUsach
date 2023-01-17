/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.api.AbstractApiBean;
import edu.harvard.iq.dataverse.util.EnumFacultadUsachUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import static edu.harvard.iq.dataverse.util.Constant.*;

/**
 * @author rarodriguezl
 */
@Stateless
@Named
public class RestUsachServiceBean extends AbstractApiBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(RestUsachServiceBean.class.getCanonicalName());
    public static final String X_DATAVERSE_KEY = "X-Dataverse-key";
    public static final String APPLICATION_JSON = "application/json";


    public Response createDataverseInitial(JsonArray jsonArray) throws IOException {
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                JsonObject objDataverse =  jsonArray.getJsonObject(i);

                JSONObject resultCreateDataverse = createDataverse(objDataverse);
                 if(resultCreateDataverse.get("status").equals("ERROR")){
                     logger.severe(resultCreateDataverse.get("message").toString());
                     errorList.add(resultCreateDataverse.get("message").toString());
                     continue;
                 }
                JSONObject dataverseJson = (JSONObject) resultCreateDataverse.get("data");
                int dataverseCreatedId = dataverseJson.getInt("id");
                    if (objDataverse.getBoolean("public")) {
                        publishDataverse(dataverseCreatedId);
                    }
            } catch (Exception e) {
                logger.severe("Error :" + e.getMessage());
            }


        }
        if(errorList.size() > 0) {
            return error(Response.Status.BAD_REQUEST, errorList.toString());
        }
        return ok("OK");

    }

    private static JSONObject createGroups(CloseableHttpClient httpClient, JSONObject mapDataverse, int idDataverseCreated) throws IOException {


        JSONObject json = new JSONObject();
        json.put("description", "groups the group here");
        json.put("displayName", "Close Collaborators");
        json.put("aliasInOwner", mapDataverse.getString("affiliation"));

        HttpPost request = new HttpPost(URI_PATH + "/api/dataverses/" + idDataverseCreated + "/groups");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("X-Dataverse-key", API_TOKEN);
        request.setEntity(params);

        var response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        JSONObject result = new JSONObject(retSrc);
        return result;
    }

    private static JSONObject publishDataverse(int idDataverseCreated) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(URI_PATH + "/api/dataverses/" + idDataverseCreated + "/actions/:publish");
        request.addHeader("X-Dataverse-key", API_TOKEN);
        var response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        JSONObject result = new JSONObject(retSrc);
        return result;
    }

    private JSONObject createDataverse(JsonObject jsonObject) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {

            String objDataverse = jsonObject.get("dataverse").toString();

            HttpPost request = new HttpPost(API_LOCAL_USACH);
            StringEntity params = new StringEntity(objDataverse);
            request.setEntity(params);
            request.addHeader(X_DATAVERSE_KEY, API_TOKEN);

            var response = httpClient.execute(request);
//            HttpEntity entity = response.getEntity();
//            String retSrc = EntityUtils.toString(entity);
//            return ok("Assignee Ok ");
            HttpEntity entity = response.getEntity();
            String retSrc = EntityUtils.toString(entity);
            JSONObject result = new JSONObject(retSrc);
            return result;
        } catch (Exception e) {
            return null;//error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            httpClient.close();
        }
    }

    public Response createUser(JSONObject jsonUser) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {

            String nombres = jsonUser.getString("nombres");
            int codigoUnidadMayorContrato = jsonUser.getInt("codigoUnidadMayorContrato");
            String planta = jsonUser.getString("planta");

            if (!planta.equalsIgnoreCase(ACADEMICOS)) {
                return error(Response.Status.BAD_REQUEST, "Planta Invalida :" + planta);
            }

            String affiliation = getAffiliation(codigoUnidadMayorContrato);
            if (affiliation == null) {
                return error(Response.Status.BAD_REQUEST, "affiliation does not exist for 'codigoUnidadMayorContrato' :" + codigoUnidadMayorContrato);
            }

            Dataverse dataverse = findDataverseByAffiliation(affiliation);

            JSONObject response = callApiUser(jsonUser, affiliation);
          //  logger.info(String.valueOf(response.getStatus()));
            if(response.get("status").equals("OK")){
                return callApiRolAssignee(nombres, dataverse);
            }
            return error(Response.Status.BAD_REQUEST, response.get("message").toString());

//            Response r1 = callApiRolAssignee(nombres, dataverse);
//            logger.info(String.valueOf(r1.getStatus()));
        } catch (Exception e) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            httpClient.close();
        }
    }

    private String getAffiliation(int codigoUnidadMayorContrato) {

        var affiliationEnum = Arrays.stream(EnumFacultadUsachUtil.values())
                .filter(p -> p.getCodigoFactultad()
                        .equals(codigoUnidadMayorContrato))
                .findFirst();
        if (affiliationEnum.isPresent()) {
            return affiliationEnum.get().getCodigoAffiliation();
        } else {
            return null;
        }
    }

    private Response callApiRolAssignee(String nombres, Dataverse dataverse) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        JSONObject json = new JSONObject();
        json.put("assignee", "@" + nombres);
        json.put("role", ROL_DS_CONTRIBUTOR);

        HttpPost request = new HttpPost(URI_PATH + "/api/dataverses/" + dataverse.getId() + "/assignments");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        request.addHeader(X_DATAVERSE_KEY, API_TOKEN);

        var response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        logger.info(retSrc);
        if (statusCode == 200 && entity != null) {
            return ok("User Active, Assignee Ok :"+ dataverse.getAffiliation());
        }

        return error(Response.Status.BAD_REQUEST, "Error: Assignee rol");
    }

    private JSONObject callApiUser(JSONObject jsonUser, String affiliation) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {

            JSONObject json = new JSONObject();
            json.put("firstName", jsonUser.getString("primerApellido"));
            json.put("lastName", jsonUser.getString("segundoApellido"));
            json.put("userName", jsonUser.getString("nombres"));
            json.put("affiliation", affiliation);
            json.put("position", jsonUser.getString("planta"));
            json.put("email", jsonUser.getString("email"));

            HttpPost request = new HttpPost(CREATE_BUILD_USERS);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", APPLICATION_JSON);
            request.setEntity(params);

            var response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (statusCode == 200 && entity != null) {
               // HttpEntity entity = response.getEntity();
                String retSrc = EntityUtils.toString(entity);
                JSONObject result = new JSONObject(retSrc);
                return result;
            }
            if (statusCode == 400) {
                String retSrc = EntityUtils.toString(entity);
                JSONObject result = new JSONObject(retSrc);
                //return error(Response.Status.BAD_REQUEST, "Error: user already exists in dataverse");
                return result;
            }
            return null;
        } finally {
            httpClient.close();
        }
    }

    public static JSONObject apiAcademic(String run) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {

            HttpGet getRequest = new HttpGet(API_ACADEMIC + run);
            getRequest.addHeader("content-type", "application/json");
            getRequest.addHeader("Authorization", getBasicAuthenticationHeader(USER_LDAP, PASSWORD_LDAP));

            HttpResponse response = httpClient.execute(getRequest);

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200 && response.getEntity() != null) {
                HttpEntity entity = response.getEntity();
                String retSrc = EntityUtils.toString(entity);
                JSONObject result = new JSONObject(retSrc);
                return result;
            }
        } finally {
            httpClient.close();
        }

        return null;
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
