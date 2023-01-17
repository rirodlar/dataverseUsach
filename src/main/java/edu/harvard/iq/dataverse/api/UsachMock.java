package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.RestUsachServiceBean;
import org.json.JSONObject;

import javax.ejb.EJB;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

import static edu.harvard.iq.dataverse.util.Constant.USER_NOT_FOUND_LDAP;

/**
 * Where the secure, setup API calls live.
 * 
 * @author rarodriguezl
 */

@Path("mock")
public class UsachMock extends AbstractApiBean {

	private static final Logger logger = Logger.getLogger(UsachMock.class.getName());


	@EJB
	RestUsachServiceBean restUsachServiceBean;

	@EJB
	DataverseServiceBean dataverseService;

	@POST
	@Path("/ldap")
	public String ldapMock(JsonObject jsonObject) {
		String user = jsonObject.getString("user");
		if(user.equals("NOMBRE1")) return "11111111";
		if(user.equals("NOMBRE2")) return "22222222";
		if(user.equals("NOMBRE3")) return "33333333";
		if(user.equals("ESTEFANIA")) return "17504958";
		return null;
	}

	@POST
	@Path("/activate")
	public Response activate(JsonObject jsonObject) throws IOException {

		String rut = this.ldapMock(jsonObject);
		if(rut == null){
			return error(Response.Status.BAD_REQUEST, USER_NOT_FOUND_LDAP);
		}

		JSONObject jsonApiAcademic = restUsachServiceBean.apiAcademic(rut);

		try {
			return restUsachServiceBean.createUser(jsonApiAcademic);
		}catch (Exception e){
			return error(Response.Status.BAD_REQUEST, "Error: " + e.getMessage() );
		}
	}

	@GET
	@Path("/find")
	public Response find(@QueryParam("affiliation") String affiliation) throws IOException {


		Dataverse dataverse = dataverseService.findByAffiliation(affiliation);

		return ok(Json.createObjectBuilder().add("name", dataverse.getName()));
	}

	@POST
	@Path("/populateDataverse")
	public Response createDataverseList(JsonArray jsonArray) throws IOException {

		Response response = restUsachServiceBean.createDataverseInitial(jsonArray);
		return response;
	}

}
