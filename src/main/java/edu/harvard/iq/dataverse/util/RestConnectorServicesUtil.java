//package edu.harvard.iq.dataverse.util;
//
//import edu.harvard.iq.dataverse.api.AbstractApiBean;
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpGet;
//
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.CloseableHttpClient;
//;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.util.EntityUtils;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.logging.Logger;
//import java.util.Base64;
//
//import org.json.JSONObject;
//
//import javax.ws.rs.core.Response;
//
//import static edu.harvard.iq.dataverse.api.util.JsonResponseBuilder.error;
//import static edu.harvard.iq.dataverse.util.Constant.*;
//import static edu.harvard.iq.dataverse.util.json.JsonPrinter.json;
//
///**
// * @author rarodriguezl
// */
//
//public class RestConnectorServicesUtil extends AbstractApiBean implements java.io.Serializable {
//
//    private static final Logger logger = Logger.getLogger(RestConnectorServicesUtil.class.getCanonicalName());
//
////    public LdapUsachResponse autenticatedLdap(String user, String password){
////
////        LdapUsachResponse responseLdap = null;
////
////        HttpClient httpClient =  HttpClientBuilder.create().build();
////
////        try{
////            //Mock LDap
////            HttpPost postRequest = new HttpPost(Constant.MOCK_LDAP);
////
////            postRequest.addHeader("content-type", "application/json");
////            StringEntity userEntity = new StringEntity("{\"user\":\""+ user + "\",\"password\":\" "+ password + "\"}");
////            postRequest.setEntity(userEntity);
////
////            HttpResponse response = httpClient.execute(postRequest);
////
////            int statusCode = response.getStatusLine().getStatusCode();
////
////            if (statusCode == 200) {
////                responseLdap = parseGsonToLdapObject(EntityUtils.toString(response.getEntity()));
////                responseLdap.setSuccess(Boolean.TRUE);
////            }else{
////                responseLdap = new LdapUsachResponse();
////                responseLdap.setSuccess(Boolean.FALSE);
////            }
////
////        }catch(Exception e){
////            return responseLdap;
////        } finally {
////            httpClient.getConnectionManager().shutdown();
////        }
////
////        return responseLdap;
////    }
//
//    public  Response createUser(JSONObject jsonUser) throws IOException {
//            try {
//                String firstName = jsonUser.getString("primerApellido");
//                String lastName = jsonUser.getString("segundoApellido");
//                String userName = jsonUser.getString("nombres");
//                int codigoUnidadMayorContrato = jsonUser.getInt("codigoUnidadMayorContrato");
//                String position = jsonUser.getString("planta");
//                String email = jsonUser.getString("email");
//
//                String affiliation = "usach";
//                var affiliationEnum = Arrays.stream(EnumFacultadUsachUtil.values()).filter(p -> p.getCodigoFactultad().equals(codigoUnidadMayorContrato)).findFirst();
//                if (affiliationEnum.isPresent()) {
//                    affiliation = affiliationEnum.get().getCodigoAffiliation();
//                } else {
//                    new RuntimeException("Affiliation Not exit");
//                }
////        CloseableHttpClient client = HttpClients.createDefault();
////        HttpPost httpPost = new HttpPost("http://localhost:8080/api/builtin-users?key=builtInS3kretKey=123");
//
//                JSONObject json = new JSONObject();
//                json.put("firstName", firstName);
//                json.put("lastName", lastName);
//                json.put("userName", userName);
//                json.put("affiliation", affiliation);
//                json.put("position", position);
//                json.put("email", email);
//
////         );
//                CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//
//                // StringEntity entity = new StringEntity(json);
//                HttpPost request = new HttpPost("http://localhost:8080/api/builtin-users?key=builtInS3kretKey=123");
//                StringEntity params = new StringEntity(json.toString());
//                request.addHeader("content-type", "application/json");
//                request.setEntity(params);
//                var response = httpClient.execute(request);
//                int statusCode = response.getStatusLine().getStatusCode();
//                HttpEntity entity1 = response.getEntity();
//                if (statusCode == 200) {
//                    String retSrc = EntityUtils.toString(entity1);
//                    JSONObject result = new JSONObject(retSrc);
//                    logger.info(retSrc);
//                    return ok("User Activated ");
//                }
//                if (statusCode == 400) {
//                    String retSrc = EntityUtils.toString(entity1);
//                    return error(Response.Status.BAD_REQUEST, "Error: user already exists in dataverse");
//                }
//            }catch (Exception e){
//                logger.severe(e.getMessage());
//                throw new RuntimeException(e.getMessage());
//            }
//
//        return null;
//    }
//
//    public static JSONObject apiAcademic(String run) throws IOException {
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//
//        HttpGet getRequest = new HttpGet(API_ACADEMIC + run);
//
//        getRequest.addHeader("content-type", "application/json");
//        getRequest.addHeader("Authorization", getBasicAuthenticationHeader(USER_LDAP, PASSWORD_LDAP));
//
//        HttpResponse response = httpClient.execute(getRequest);
//
//        int statusCode = response.getStatusLine().getStatusCode();
//
//        if (statusCode == 200 && response.getEntity() != null) {
//            HttpEntity entity = response.getEntity();
//            String retSrc = EntityUtils.toString(entity);
//            JSONObject result = new JSONObject(retSrc);
//            return result;
//        }
//        httpClient.close();
//
//        return null;
//    }
//
//
//    private static String getBasicAuthenticationHeader(String username, String password) {
//        String valueToEncode = username + ":" + password;
//        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
//    }
//
////    private AcademicoUsachResponse parseGsonToAcademicoObject(String response) {
////        return new Gson().fromJson(response, AcademicoUsachResponse.class);
////    }
////    private LdapUsachResponse parseGsonToLdapObject(String response) {
////        return new Gson().fromJson(response , LdapUsachResponse.class);
////    }
////
////    private AssigmentsUsachResponse parseGsonToAssigmentsObject(String response) {
////        return new Gson().fromJson(response , AssigmentsUsachResponse.class);
////    }
//
////    public Boolean apiExistUsuarioInDataverse(Long idDataverse, String usuario){
////
////        Boolean exite = Boolean.FALSE;
////        AssigmentsUsachResponse responseAssigment = null;
////
////        String api_token = Constant.API_TOKEN;
////        String rol = Constant.ROL_CONTRIBUTOR;
////        String assigne = "@"+usuario;
////
////        HttpClient httpClient =  HttpClientBuilder.create().build();
////
////        try{
////            //HttpGet getRequest = new HttpGet("https://api.dti.usach.cl/api/docente/"+run);
////            HttpGet getRequest = new HttpGet(String.format(Constant.API_LOCAL_ASSIGNMENT, idDataverse));
////            getRequest.addHeader("content-type", "application/json");
////            getRequest.addHeader("X-Dataverse-key", api_token);
////
////            HttpResponse response = httpClient.execute(getRequest);
////
////            int statusCode = response.getStatusLine().getStatusCode();
////
////            if (statusCode == 200) {
////                responseAssigment = parseGsonToAssigmentsObject(EntityUtils.toString(response.getEntity()));
////                exite = hasUserInList(assigne, rol, responseAssigment.getData());
////            }
////
////        }catch(Exception e){
////            return exite;
////        } finally {
////            //Important: Close the connect
////            httpClient.getConnectionManager().shutdown();
////        }
////
////        return exite;
////    }
//
////    private Boolean hasUserInList(String assignee, String rol, List<AssigmentRecord> dataList){
////
////        return dataList.stream().filter(d -> d.getAssignee().equals(assignee)).findFirst().isPresent();
////
////
//////        Boolean existe = Boolean.FALSE;
//////        for(AssigmentRecord registro : dataList ){
//////            //comenatdo por cambio
//////            //if(rol.equals(registro.get_roleAlias()) && assignee.equals(registro.getAssignee())){
//////            if(assignee.equals(registro.getAssignee())){
//////                existe = Boolean.TRUE;
//////            }
//////        }
//////        return existe;
////    }
//}
