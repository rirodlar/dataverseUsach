/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import static edu.harvard.iq.dataverse.util.Constant.*;

/**
 * @author gdurand
 */
@Stateless
@Named
public class RestUsachServiceBean extends AbstractApiBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(RestUsachServiceBean.class.getCanonicalName());

    public Response createDataverseInitial() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(API_LOCAL_CREATE_DATAVERSE);
        var response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (statusCode == 200) {
            String retSrc = EntityUtils.toString(entity);
            JSONArray result = new JSONArray(retSrc);
            for (int i = 0; i < result.length(); i++) {
                JSONObject objDataverse = (JSONObject) result.get(i);

                String resultCreateDataverse = createDataverse(httpClient, objDataverse);

                JSONObject JsonCreatedDataverse = new JSONObject(resultCreateDataverse);
                JSONObject mapDataverse  = (JSONObject) JsonCreatedDataverse.get("data");
                int idDataverseCreated =  mapDataverse.getInt("id");

                int statusResponseCreateDataverse = response.getStatusLine().getStatusCode();

                if (statusResponseCreateDataverse == 200) {
                    if(objDataverse.getBoolean("public")) {
                        publishDataverse(httpClient, idDataverseCreated);
                    }
                    if(objDataverse.getBoolean("group")) {

                        createGroups(httpClient, mapDataverse, idDataverseCreated);


                    }

                }
                if (statusResponseCreateDataverse == 400) {
                    System.out.println("Bad Request");
                }
                if (statusResponseCreateDataverse == 401) {
                    System.out.println("Unauthorized");
                }
                if (statusResponseCreateDataverse == 500) {
                    System.out.println("Internal Error");
                }

            }
            logger.info(result.toString());
            return ok("create Dataverse ");
        }

        httpClient.close();
        return null;
    }

    private static JSONObject createGroups(CloseableHttpClient httpClient, JSONObject mapDataverse, int idDataverseCreated) throws IOException {


        JSONObject json = new JSONObject();
        json.put("description", "Describe the group here");
        json.put("displayName", "Close Collaborators");
        json.put("aliasInOwner", mapDataverse.getString("affiliation"));

        HttpPost request = new HttpPost("http://localhost:8080/api/dataverses/"+ idDataverseCreated +"/groups");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("X-Dataverse-key", API_TOKEN);
        request.setEntity(params);

        var response = httpClient.execute(request);

        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        JSONObject result = new JSONObject(retSrc);
        return result;
    }

    private static JSONObject publishDataverse(CloseableHttpClient httpClient, int idDataverseCreated) throws IOException {
        HttpPost request = new HttpPost("http://localhost:8080/api/dataverses/"+ idDataverseCreated +"/actions/:publish");
        request.addHeader("X-Dataverse-key", API_TOKEN);
        var response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        JSONObject result = new JSONObject(retSrc);
        return result;
    }

    private static String createDataverse(CloseableHttpClient httpClient, JSONObject jsonObject) throws IOException {
        String objDataverse = jsonObject.get("dataverse").toString();

        HttpPost request = new HttpPost("http://localhost:8080/api/dataverses/usach");
        StringEntity params = new StringEntity(objDataverse);
        request.setEntity(params);
        request.addHeader("X-Dataverse-key", API_TOKEN);

        var response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();

        return EntityUtils.toString(entity);
    }

    public Response createUser(JSONObject jsonUser) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            String primerApellido = jsonUser.getString("primerApellido");
            String segundoApellido = jsonUser.getString("segundoApellido");
            String nombres = jsonUser.getString("nombres");
            int codigoUnidadMayorContrato = jsonUser.getInt("codigoUnidadMayorContrato");
            String planta = jsonUser.getString("planta");
            String email = jsonUser.getString("email");

            if(!planta.equalsIgnoreCase(ACADEMICOS)){
                return error(Response.Status.BAD_REQUEST, "Planta Invalida :"+ planta);
            }

            String affiliation = "";
            var affiliationEnum = Arrays.stream(EnumFacultadUsachUtil.values()).filter(p -> p.getCodigoFactultad().equals(codigoUnidadMayorContrato)).findFirst();
            if (affiliationEnum.isPresent()) {
                affiliation = affiliationEnum.get().getCodigoAffiliation();
            } else {
                return error(Response.Status.BAD_REQUEST, "affiliation does not exist for 'codigoUnidadMayorContrato' :"+ codigoUnidadMayorContrato);
            }

            Dataverse dataverse = findDataverseByAffiliation(affiliation);
            callApiUser(primerApellido,segundoApellido, nombres, affiliation, planta, email);
            callApiRolAssignee(nombres, dataverse);
        }catch (Exception e){
            return error(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());

        }finally {
            httpClient.close();
        }
        return null;
    }

    private static void callApiRolAssignee(String nombres, Dataverse dataverse) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        JSONObject json = new JSONObject();
        json.put("assignee", "@"+ nombres);
        json.put("role", "dsContributor");

        HttpPost request = new HttpPost("http://localhost:8080/api/dataverses/"+ dataverse.getId()+"/assignments");
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        request.addHeader("X-Dataverse-key", API_TOKEN);

        var response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        String retSrc = EntityUtils.toString(entity);
        logger.info(retSrc);
    }

    private  Response callApiUser(String primerApellido, String segundoApellido, String nombres, String affiliation, String planta, String email) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject json = new JSONObject();
        json.put("firstName", primerApellido);
        json.put("lastName", segundoApellido);
        json.put("userName", nombres);
        json.put("affiliation", affiliation);
        json.put("position", planta);
        json.put("email", email);

        HttpPost request = new HttpPost(API_LOCAL_DATAVERSE);
        StringEntity params = new StringEntity(json.toString());
        request.addHeader("content-type", "application/json");
        request.setEntity(params);

        var response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        if (statusCode == 200) {
            String retSrc = EntityUtils.toString(entity);
            JSONObject result = new JSONObject(retSrc);
            logger.info(retSrc);
            return ok("User Activated ");
        }
        if (statusCode == 400) {
            String retSrc = EntityUtils.toString(entity);
            return error(Response.Status.BAD_REQUEST, "Error: user already exists in dataverse");
        }
        return null;
    }

    public static JSONObject apiAcademic(String run) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

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
        httpClient.close();

        return null;
    }

    private static String getBasicAuthenticationHeader(String username, String password) {
        String valueToEncode = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }
}
