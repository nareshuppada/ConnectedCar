package com.ubintel.connectedcar.network;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class DataUploader {

    //Ubintel
    MqttClient mqttClient;
    MqttConnectOptions connectOptions ;


	public String uploadRecord(String urlStr, Map<String,String> data) throws IOException, URISyntaxException {
        System.out.print("in uploadRecord "+urlStr);
        try {
            mqttClient = new MqttClient(urlStr, "connected_car", null);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectOptions.setUserName("545649a82d95f5f051e66d13");
        //connectOptions.setConnectionTimeout(10);

        connectOptions.setPassword("765e8174-68b4-472d-8c28-056a3a0472d6".toCharArray());
        if(!mqttClient.isConnected()){
            try {

                System.out.println("Attempting Connection");

                mqttClient.connect(connectOptions);


            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            Iterator<String> keys = data.keySet().iterator();
            while (keys.hasNext()) {
                String k = keys.next();
                String value = data.get(k);
                String channel = k.replaceAll(" ","");
                if(value.equals("")|| value.equals("--"))
                    continue;
                MqttMessage message = new MqttMessage(new String(value).getBytes());
                message.setQos(0);
                System.out.println("Channel Name:" + channel + "is Connected: "+mqttClient.isConnected() );
                try {
                    if(mqttClient.isConnected()){
                        mqttClient.publish("device/545649a82d95f5f051e66d13/"+channel, message);

                        System.out.println("Message published :"+channel+":"+value);
                    }
                } catch (MqttPersistenceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (MqttException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }






        }


		//String encData = getEncodedData(data);
		/*JSONArray jsonArray = getUbintelJsonData(data);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        HttpClient client = new DefaultHttpClient(params);
        urlStr ="http://api.unarto.com/v1/feeds/54061a1a8ccbd3a038d63d9c";
        //Ubintel Post Setup
        for(int i=0;i<jsonArray.length();i++){
        HttpPut request = new HttpPut();
        request.setURI(new URI(urlStr));
        request.setHeader("Content-Type", "application/json");
        request.setHeader("X-Api-Key","c1092fc9-91ef-4bf6-9ae8-d06d33cd8276");
        try {
			request.setEntity(new StringEntity(jsonArray.getString(i)));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ResponseHandler<String> resHandle = new BasicResponseHandler();
        String response = client.execute(request,resHandle);
        }*/



        return "";
	}
	public String getEncodedData(Map<String,String> data) throws UnsupportedEncodingException {
		StringBuffer buff = new StringBuffer();
		Iterator<String> keys = data.keySet().iterator();
		while (keys.hasNext()) {
			String k = keys.next();
			buff.append(URLEncoder.encode(k, "UTF-8"));
			buff.append("=");
			buff.append(URLEncoder.encode(data.get(k), "UTF-8"));
			buff.append("&");
		}
		return buff.toString();
	}
	
	public JSONArray getUbintelJsonData(Map<String,String> mapdata){
		//{ "version" : "1.0.0", "datapoint" : { "id" : "rpm", "value" : "40" }  }
		
         JSONArray dataArray = new JSONArray();
		try {
			Iterator<String> keys = mapdata.keySet().iterator();
			while (keys.hasNext()) {
				String k = keys.next();
				 JSONObject data = new JSONObject();
		         JSONObject datapoint = new JSONObject();
		        	data.put("version", "1.0.0");
		            datapoint.put("id", k);
		            datapoint.put("value", mapdata.get(k));
		            data.put("datapoint", datapoint);
		            dataArray.put(data);
			}
           // Log.d("output", parent.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
		return dataArray;
		
	}
}
