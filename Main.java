package salesforce_rest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import aj.org.objectweb.asm.TypeReference;

import org.json.JSONArray;
import org.json.JSONException;

public class Main {

	static final String USERNAME = "ritikamathur1997@outlook.com";
	static final String PASSWORD = "Admin12345YObRiUTVtkF8uS1npdatDZxv3";
	static final String LOGINURL = "https://login.salesforce.com";
	static final String GRANTSERVICE = "/services/oauth2/token?grant_type=password";
	static final String CLIENTID = "3MVG9fe4g9fhX0E41Kb0yYzJsImU5j2GMXIZ8PTsay8KVULSw9MB8nZYqilhxwpQboRDGwwmePa8GYKcl2hNg";
	static final String CLIENTSECRET = "78F2DE48C8F1C73D1034AC9D004F456BFB8EC5C72226FAFFEE9E20CF1151135B";
	private static final String REST_ENDPOINT = "/services/data";
	private static final String API_VERSION = "/v55.0";
	private static String baseUri;
	private static Header oauthHeader;
	private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
	private static String LeadFirstName;
	private static String LeadCompany;
	private static String LeadId;

	public static void main(String[] args) throws IOException, Exception {
		HttpClient httpclient = HttpClientBuilder.create().build();

		String loginURL = LOGINURL + GRANTSERVICE + "&client_id=" + CLIENTID + "&client_secret=" + CLIENTSECRET
				+ "&username=" + USERNAME + "&password=" + PASSWORD;

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

		oauthHeader = new BasicHeader("Authorization", "Bearer " + loginAccessToken);
		System.out.println("oauthHeader1 : " + oauthHeader);
		System.out.println("\n" + response.getStatusLine());
		System.out.println(response.getStatusLine());
		System.out.println(" Successful login ");
		System.out.println(" instance URL : " + loginInstanceUrl);
		System.out.println(" access token / session ID : " + loginAccessToken);

		// release connection
		httpPost.releaseConnection();
		createLeads();
	}

	
	public static void queryLeads() {
		System.out.println("\n_Lead QUERY_\n");
		try {
			// Set up the HTTP objects needed to make the request .
			HttpClient httpClient = HttpClientBuilder.create().build();
			String uri = baseUri + "/query?q=Select+Company+From+Lead+Limit+3";
			System.out.println(" Query URL : " + uri);
			HttpGet httpGet = new HttpGet(uri);
			System.out.println("oautnHeader2 : " + oauthHeader);
			httpGet.addHeader(oauthHeader);
			httpGet.addHeader(prettyPrintHeader);
			HttpResponse response = httpClient.execute(httpGet);

			// Process the result
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 200) {
				String response_string = EntityUtils.toString(response.getEntity());
				try {
					JSONObject json = new JSONObject(response_string);
					System.out.println(" JSON result of Query : \n " + json.toString(1));
					JSONArray j = json.getJSONArray("records");
					for (int i = 0; i < j.length(); i++) {
						LeadCompany = json.getJSONArray("records").getJSONObject(i).getString("Company");
					}
				} catch (JSONException je) {
					// TODO: handle exception
					je.printStackTrace();
				}
			} else {
				System.out.println("Query was unsuccessful . Status code returned is " + statusCode);
				System.out.println(" An error has occured . Http status : " + response.getStatusLine().getStatusCode());
				//System.out.println ( getBody( response.getEntity ( ) . getContent ( ) ) ) ;
				System.exit(-1);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

	public static void createLeads() throws Exception, IOException {
		System.out.println("\n_ Lead INSERT__");
		
		String uri = baseUri + "/sobjects/Lead/";
		try {
			// create the JS0N object containing the new lead details .
			
			//Reading JSON from file systes
			BufferedReader br = new BufferedReader(new FileReader("C:/Users/ritik/git/SPRINGREST/SalesforceRESTAPI/src/salesforce_rest/users.json"));
			String line;
			/*
			 * JSONObject lead = new JSONObject(); lead.put("FirstName", "REST API");
			 * lead.put("LastName", "Lead"); lead.put("Company", "bispsolutions.com");
			 * System.out.println("JSON for lead record to be inserted : \n" +
			 * lead.toString(1));
			 */
			StringBuilder sBuilderObj = new StringBuilder();
			
			while ((line = br.readLine()) != null) {
				sBuilderObj.append(line);
			}
			
			System.out.println("Json " +sBuilderObj.toString());
			
			JSONObject lead = new JSONObject();
			JSONObject jsonObject = new JSONObject(sBuilderObj.toString());
			
			System.out.println("Json Obj " +jsonObject);
			
			JSONArray j = jsonObject.getJSONArray("leads");
			
			for (int i = 0; i < j.length(); i++) {
				System.out.println("Array[i] " + j.getJSONObject(i).getString("Company"));
				lead.put("Company", j.getJSONObject(i).getString("Company"));
				lead.put("FirstName", j.getJSONObject(i).getString("FirstName"));
				lead.put("LastName", j.getJSONObject(i).getString("LastName"));
			}
			
			// Construct the objects needed for the request
			HttpClient httpClient = HttpClientBuilder.create().build();

			System.out.println("URL " + uri);

			HttpPost httpPost = new HttpPost(uri);
			System.out.println("oauthHeaderrr " + oauthHeader);
			httpPost.addHeader(oauthHeader);
			httpPost.addHeader(prettyPrintHeader);

			StringEntity body = new StringEntity(lead.toString());
			body.setContentType("application/json");
			httpPost.setEntity(body);

			System.out.println("HEADER " + httpPost.getAllHeaders().toString());

			System.out.println("httpPost" + httpPost);

			// Make the request
			HttpResponse response = httpClient.execute(httpPost);

			System.out.println("RESPONSE " + response);

			// Process the results
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == 201) {
				String response_string = EntityUtils.toString(response.getEntity());
				JSONObject json = new JSONObject(response_string);

				// Store the retrieved lead id to use when we update the lead .
				//LeadCompany = json.getString("id");
				System.out.println("New Leads inserted successfully");

			} else {
				System.out.println(" Insertion unsuccessful . Status code returned is " + statusCode);
				// The message we are going to post
			}
			
			br.close();
		} catch (JSONException e) {
			System.out.println(" Issue creating JSON or processing results ");
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
	}

}
