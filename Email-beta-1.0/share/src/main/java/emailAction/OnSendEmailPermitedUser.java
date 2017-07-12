package emailAction;



import javax.servlet.http.HttpSession;

import org.alfresco.web.evaluator.BaseEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Response;

//import com.eisenvault.versiondelete.evaluator.DeleteVersionsActionEvaluator;



public class OnSendEmailPermitedUser extends BaseEvaluator{
	

	@Override
	public boolean evaluate(JSONObject arg0) {

		try {
			
			//debug("Inside evaluate()");
			
			//System.out.println("*******Printing JSONObject " + arg0);
			
			final RequestContext rc = ThreadLocalRequestContext
					.getRequestContext();
			HttpSession session = ServletUtil.getSession();
			CredentialVault cv = rc.getCredentialVault();
			
			if (cv != null) {
				String userName = (String) session
						.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
				Connector connector = rc.getServiceRegistry()
						.getConnectorService()
						.getConnector("alfresco", userName, session);
				
				String membershipUri = "/api/sites/" + getSiteId(arg0)
						+ "/memberships/" + rc.getUserId();
				JSONObject membership = (JSONObject) getResponse(connector ,membershipUri);
				//System.out.println("Printing membership object " + membership);
				
				// Adding check for the pages like repository and Audit Trail that doesn't contain info abt the user membership
				if(membership == null){
					return false;
				}
				
				String versionUri = "/api/version?nodeRef="+arg0.get("nodeRef");
				JSONArray version = (JSONArray) getResponse(connector ,versionUri);
				//System.out.println("Printing version object " + version);
				
				if(("SiteManager".equals((String) membership.get("role"))) || ("SiteCollaborator".equals((String) membership.get("role")))){
					return true;
				}
			}
		} catch (ConnectorServiceException | ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Object getResponse(Connector connector, String uri) throws ParseException {
		
		Response res = connector.call(uri);
		JSONObject jsonRes = null;
		JSONArray jsonArray = null;
		if (res.getStatus().getCode() == Status.STATUS_OK) {
			String response = res.getResponse();
			JSONParser p = new JSONParser();
			Object obj = p.parse(response);
			//System.out.println(">> Object obj value :: " + obj);
			if (obj instanceof JSONObject) {
				jsonRes = (JSONObject) obj;
				return jsonRes;
			}
			if (obj instanceof JSONArray) {
				jsonArray = (JSONArray) obj;
				return jsonArray;
			}
		}
		return null;
	}

	
	
}
