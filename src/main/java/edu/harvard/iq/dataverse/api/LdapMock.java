package edu.harvard.iq.dataverse.api;

import edu.harvard.iq.dataverse.Dataverse;
import edu.harvard.iq.dataverse.DataverseEntity;
import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.RestUsachServiceBean;
import org.json.JSONObject;

import javax.ejb.EJB;
import javax.json.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Where the secure, setup API calls live.
 * 
 * @author michael
 */

@Path("mock")
public class LdapMock extends AbstractApiBean {

	private static final Logger logger = Logger.getLogger(LdapMock.class.getName());

	@EJB
	RestUsachServiceBean restUsachServiceBean;

	@EJB
	Dataverses dataverses;

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
			return error(Response.Status.BAD_REQUEST, "User Not Found LDAP" );
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


		//dataverses.findByAffiliation(1);
		Dataverse dataverse = dataverseService.findByAffiliation(affiliation);
		System.out.println(dataverse);

		return ok(Json.createObjectBuilder().add("name", dataverse.getName()));
	}

	@GET
	@Path("/populateDataverse")
	public Response createDataverseList() throws IOException {


		Response response = restUsachServiceBean.createDataverseInitial();
		System.out.println(response);


		return ok(Json.createObjectBuilder().add("create", "creaet DATAveserse List"));
	}

	/**
	 * FACULTAD DE ADMS Y ECONOMÍA	FAE
	 * FACULTAD DE CIENCIA	FCIENCIA
	 * FACULTAD DE CIENCIAS MÉDICAS	FCM
	 * FACULTAD DE DERECHO	FDERECHO
	 * FACULTAD DE HUMANIDADES	FAHU
	 * FACULTAD DE INGENIERÍA	FING
	 * FACULTAD DE QUÍMICA Y BIOLOGÍA	FQYB
	 * FACULTAD TECNOLÓGICA	FACTEC
	 * ESCUELA DE ARQUITECTURA	ARQUITECTURA
	 * PROGRAMA DE BACHILLERATO	BACHI
	 */

}
