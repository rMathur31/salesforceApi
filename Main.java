package salesforce_rest;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONTokener;
import org.json.JSONArray;
import org.json.JSONException;

public class Main {

	static final String USERNAME = "math31rit-ewkz@force.com";
	static final String PASSWORD = "Admin12345CKIsgSMjJ3KglEjce5BXJr34";
	static final String LOGINURL = "https://login.salesforce.com";
	static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	static final String CLIENTID = "3MVG9fe4g9fhX0E4mYLq6KYA1Kai1.7Hlg_SaOM3NeLIvNTG3p24r9683SgUsKEzuu2hmH_DzwROz3e.GxO9s";
	static final String CLIENTSECRET = "96568DF0C3E08164079194DD07892A1CBE1B151B944C3D7BE73EC5BB26BF9317";
	private static final String REST_ENDPOINT = "/services/data";
	private static final String API_VERSION = "/v56.0";

	private static String baseUri;
	private static Header oauthHeader;
	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	private static String LeadFirstName;
	private static String LeadCompany;

	public static void main(String[] args) {
		HttpClient httpclient = HttpClientBuilder.create().build();

		String loginURL = LOGINURL + GRANTSERVICE + "&client_id=" + CLIENTID + "&client_secret=" + CLIENTSECRET
				+ "&username=" + USERNAME + "&password=" + PASSWORD;

		// String loginURL =
		// "https://login.salesforce.com/services/oauth2/token?grant_type=password&client_id=3MVG9fe4g9fhX0E4mYLq6KYA1Kai1.7Hlg_SaOM3NeLIvNTG3p24r9683SgUsKEzuu2hmH_DzwROz3e.GxO9s&client_secret=96568DF0C3E08164079194DD07892A1CBE1B151B944C3D7BE73EC5BB26BF9317&username=math31rit-ewkz@force.com&password=Admin12345CKIsgSMjJ3KglEjce5BXJr34";
		System.out.println(loginURL);
		HttpPost httpPost = new HttpPost(loginURL);
		HttpResponse response = null;

		try {
			// Execute the login POST request
			response = httpclient.execute(httpPost);
		} catch (ClientProtocolException cpException) {
			cpException.printStackTrace();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}

		final int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) {
			System.out.println(" Error authenticating to Force.com : " + statusCode);
			// Error is in EntityUtils.toString ( response.getEntity ( ) )
			return;
		}
		String getResult = null;

		try {
			getResult = EntityUtils.toString(response.getEntity());
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		JSONObject jsonObject = null;
		String loginAccessToken = null;
		String loginInstanceUrl = null;

		try {
			jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
			System.out.println(jsonObject);
			loginAccessToken = jsonObject.getString("access_token");
			loginInstanceUrl = jsonObject.getString("instance_url");
		} catch (JSONException jsonException) {
			jsonException.printStackTrace();
		}

		baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION;
		System.out.println("Base URI" + baseUri);

		oauthHeader = new BasicHeader("Authorization", "OAuth2" + loginAccessToken);
		System.out.println("oauthHeader1 : " + oauthHeader);
		System.out.println("\n" + response.getStatusLine());
		System.out.println(response.getStatusLine());
		System.out.println(" Successful login ");
		System.out.println(" instance URL : " + loginInstanceUrl);
		System.out.println(" access token / session ID : " + loginAccessToken);

		// release connection
		httpPost.releaseConnection();
		queryLeads();
		//createLeads();
	}

	public static void queryLeads() {
	       System.out.println ( "\n_Lead QUERY_\n" ) ;
	       try {
	           // Set up the HTTP objects needed to make the request .
	           HttpClient httpClient = HttpClientBuilder.create ().build() ;
	           String uri = baseUri + "/query?q=Select+Company+,+FirstName+,+Company+From+Account+Limit+5" ;
	           System.out.println ( " Query URL : " + uri ) ;
	           HttpGet httpGet = new HttpGet ( uri ) ;
	           System.out.println ("oautnHeader2 : " + oauthHeader ) ;
	           httpGet.addHeader (oauthHeader) ;
	           httpGet.addHeader (prettyPrintHeader) ;
	           HttpResponse response = httpClient.execute (httpGet);
	           
	        // Process the result
	           int statusCode = response.getStatusLine ( ) . getStatusCode ( ) ;
	           if ( statusCode == 200 ) {
	               String response_string = EntityUtils.toString ( response.getEntity ( ) ) ;
	               try {
	            	    JSONObject json = new JSONObject ( response_string ) ;
	            	   System.out.println ( " JSON result of Query : \n " + json.toString ( 1 ) ) ;
	            	    JSONArray j = json.getJSONArray ( " records " ) ;
	            	    for ( int i = 0 ; i < j.length ( ) ; i ++ ) {
	            	    	LeadCompany = json.getJSONArray ( "records" ).getJSONObject(i).getString ( "Company" ) ;
	            	        LeadFirstName = json.getJSONArray ( "records" ) . getJSONObject ( i ) .getString ( "Name" ) ;
	            	                  
	            	    }
	               }
	               catch (JSONException je) {
					// TODO: handle exception
	            	   je.printStackTrace();
				}} else {
				    System.out.println ( "Query was unsuccessful . Status code returned is "+ statusCode ) ;
				    	    System.out.println ( " An error has occured . Http status : "+ response.getStatusLine ( ) . getStatusCode ( ) ) ;
				    	    //System.out.println ( getBody( response.getEntity ( ) . getContent ( ) ) ) ;
				    	    System.exit ( -1 ) ;
	     	       }
	       }catch ( IOException ioe ) {
	        	    ioe.printStackTrace ( ) ;}
	        	    catch( NullPointerException npe ) {
	        	    npe.printStackTrace ( ) ;
	        	} 
	        	}

	public static void createLeads() throws Exception, IOException {
		System.out.println("\n_ Lead INSERT__");

		String uri = baseUri + "/sobjects/Lead/";

		// create the 350N object containing the new lead details .
		JSONObject lead = new JSONObject();
		lead.put("FirstName", "REST API");
		lead.put("LastName", "Lead");
		lead.put("Company", "hispsolutions.com");
		System.out.println("JSON for lead record to be inserted : \n" + lead.toString(1));
		// Construct the objects needed for the request
		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost httpPost = new HttpPost(uri);
		httpPost.addHeader(oauthHeader);
		httpPost.addHeader(prettyPrintHeader);
		System.out.println(httpPost);

		// Make the request
		HttpResponse response = httpClient.execute(httpPost);

		// Process the results
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode == 200) {
			try {
				String response_string = EntityUtils.toString(response.getEntity());
				JSONObject json = new JSONObject(response_string);

				// Store the retrieved lead id to use when we update the lead .
				LeadCompany = json.getString("Company");
				System.out.println("New Lead id from response : " + LeadCompany);

			} catch (JSONException e) {
				System.out.println(" Issue creating JSON or processing results ");
				e.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
		} else {
			System.out.println(" Insertion unsuccessful . Status code returned is " + statusCode);
			// The message we are going to post
		}

	}
}
