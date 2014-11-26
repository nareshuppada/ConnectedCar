package com.ubintel.connectedcar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.ubintel.connectedcar.config.ObdConfig;
import com.ubintel.connectedcar.drawable.CoolantGaugeView;
import com.ubintel.connectedcar.obdparameters.ObdCommand;
import com.ubintel.connectedcar.services.ObdReaderService;
import com.ubintel.connectedcar.services.ObdReaderServiceConnection;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;

public class ObdReaderMainActivity extends Activity {
	static final int NO_BLUETOOTH_ID = 0;
	static final int NO_GPS_ID = 1;
	static final int START_LIVE_DATA = 1;
	static final int STOP_LIVE_DATA = 2;
	static final int SETTINGS = 3;
	static final int COMMAND_ACTIVITY = 5;
	static final int TABLE_ROW_MARGIN = 6;

	private Handler handler = null;
	private Intent serviceIntent = null;
	private ObdReaderServiceConnection serviceConn = null;
	private UpdateThread updater = null;
	private SensorManager sensorManager = null;
	private Sensor orientSensor = null;
	private double fuelEconAvg = 0;
	private int fuelEconi = 0;
	private double speedAvg = 0;
	private int speedi = 0;
	private SharedPreferences prefs = null;
	private double maxFuelEcon = 70.0;
	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakeLock = null;
    int serviceRunning =0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	int count=0;

    BufferedWriter writer;
    File traceFile;
    String filename = "";

	private final SensorEventListener orientListener = new SensorEventListener() {
		/*public void onSensorChanged(SensorEvent event) {
			float x = event.values[0];
			String dir = "";
			if (x >= 337.5 || x < 22.5) {
				dir = "N";
			} else if (x >= 22.5 && x < 67.5) {
				dir = "NE";
			} else if (x >= 67.5 && x < 112.5) {
				dir = "E";
			} else if (x >= 112.5 && x < 157.5) {
				dir = "SE";
			} else if (x >= 157.5 && x < 202.5) {
				dir = "S";
			} else if (x >= 202.5 && x < 247.5) {
				dir = "SW";
			} else if (x >= 247.5 && x < 292.5) {
				dir = "W";
			} else if (x >= 292.5 && x < 337.5) {
				dir = "NW";
			}
			TextView compass = (TextView)findViewById(R.id.compass_text);
			updateTextView(compass,dir);
		}*/

        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}
	};



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        handler = new Handler();
        serviceIntent = new Intent(this, ObdReaderService.class);

        serviceConn = new ObdReaderServiceConnection();
        if (savedInstanceState != null) {
            int serviceState = savedInstanceState.getInt("ServiceCreated");
            if (serviceState != 1) {

                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
                serviceRunning = 1;
            }
        }
        else{
            bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
            serviceRunning = 1;
        }


      LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
        	showDialog(NO_GPS_ID);
        	return;
        }
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	showDialog(NO_BLUETOOTH_ID);
        	return;
        }
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sens = sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        if ( sens.size() <= 0) {
        	Toast.makeText(this, "No orientation available.", Toast.LENGTH_LONG).show();
        } else {
        	orientSensor = sens.get(0);
        }

        try
        {
            // Creates a trace file in the primary external storage space of the
            // current application.
            // If the file does not exists, it is created.
            Date date = new Date();
            filename = "ObdData"+date.getTime()+".txt";
            traceFile = new File(((Context)this).getExternalFilesDir(null), filename);
            if (!traceFile.exists())
                traceFile.createNewFile();
            // Adds a line to the trace file
             writer = new BufferedWriter(new FileWriter(traceFile, true /*append*/));
            writer.write("Start of OBD Data" + date.getDate() + "");
            writer.flush();
            writer.close();
          //  writer.close();
            // Refresh the data so it can seen when the device is plugged in a
            // computer. You may have to unplug and replug the device to see the
            // latest changes. This is not necessary if the user should not modify
            // the files.
            MediaScannerConnection.scanFile((Context) (this),
                    new String[]{traceFile.toString()},
                    null,
                    null);

        }
        catch (IOException e)
        {
           System.out.println("com.ubintel.connectedcar. FileTest Unable to write to the TraceFile.txt file.");
        }




    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            int serviceState = savedInstanceState.getInt("ServiceCreated");
            if (serviceState != 1) {

                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE);
                serviceRunning = 1;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        if(serviceRunning==1) {
            savedInstanceState.putInt("ServiceCreated", 1);
        }
        else{
            savedInstanceState.putInt("ServiceCreated", 0);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();

    	if (wakeLock.isHeld()) {
    		wakeLock.release();
    	}
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	if (wakeLock.isHeld()) {
    		wakeLock.release();
    	}
    }
    protected void onResume() {
    	super.onResume();
    	sensorManager.registerListener(orientListener, orientSensor, SensorManager.SENSOR_DELAY_UI);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//maxFuelEcon = ObdReaderConfigActivity.getMaxFuelEconomy(prefs);
		powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "ObdReader");
    }
    private void updateConfig() {
    	Intent configIntent = new Intent(this,ObdReaderConfigActivity.class);
    	startActivity(configIntent);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, START_LIVE_DATA, 0, "Start Live Data");
        menu.add(0, SETTINGS, 0, "Settings");
        menu.add(0, COMMAND_ACTIVITY, 0, "Run Command");
        menu.add(0, STOP_LIVE_DATA, 0, "Stop");
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case START_LIVE_DATA:
        	liveData();
            return true;
        case STOP_LIVE_DATA:
        	cancel();
        	return true;
        case SETTINGS:
        	updateConfig();
        	return true;
        case COMMAND_ACTIVITY:
        	staticCommand();
        	return true;
        }
        return false;
    }
    private void staticCommand() {
    	Intent commandIntent = new Intent(this, ObdReaderCommandActivity.class);
    	startActivity(commandIntent);
    }
    private void liveData() {
      /*  Map<String,String> mapData = new HashMap<String,String>();
        mapData.put("rpm","2000");
        mapData.put("eheat","100");
        mapData.put("rpm1","2000");
        mapData.put("eheat1","100");
        mapData.put("rpm2","2000");
        mapData.put("eheat3","100");mapData.put("rpm3","2000");
        mapData.put("eheat3","100");mapData.put("rpm4","2000");
        mapData.put("eheat4","100");mapData.put("rpm5","2000");
        mapData.put("eheat5","100");




        setDataTableText(mapData);*/


        if (!serviceConn.isRunning()) {
    		serviceConn.getService().startService();
    	}
    	updater = new UpdateThread();
    	updater.start();
    	wakeLock.acquire();
    }
    private void cancel() {
    	stopService(serviceIntent);
    	if (serviceConn.isRunning()) {
    		serviceConn.getService().stopService();
    	}
    	if (updater != null) {
    		updater.stop = true;
    	}
    	wakeLock.release();
    }
    protected Dialog onCreateDialog(int id) {
    	AlertDialog.Builder build = new AlertDialog.Builder(this);
    	switch(id) {
    	case NO_BLUETOOTH_ID:
            build.setMessage("Sorry, your device doesn't support bluetooth");
            return build.create();
    	case NO_GPS_ID:
            build.setMessage("Sorry, your device doesn't support gps");
            return build.create();
    	}
    	return null;
    }
    public boolean onPrepareOptionsMenu(Menu menu) {
    	MenuItem startItem = menu.findItem(START_LIVE_DATA);
    	MenuItem stopItem = menu.findItem(STOP_LIVE_DATA);
    	MenuItem settingsItem = menu.findItem(SETTINGS);
    	MenuItem commandItem = menu.findItem(COMMAND_ACTIVITY);
    	if (serviceConn.isRunning()) {
    		startItem.setEnabled(false);
    		stopItem.setEnabled(true);
    		settingsItem.setEnabled(false);
    		commandItem.setEnabled(false);
    	} else {
    		stopItem.setEnabled(false);
    		startItem.setEnabled(true);
    		settingsItem.setEnabled(true);
    		commandItem.setEnabled(true);
    	}
    	return true;
    }
    public void updateDataTable(final Map<String,String> dataMap) {
    	handler.post(new Runnable() {
            public void run() {
                setDataTableText(dataMap);
            }
        });
    }
    public void updateTextView(final TextView view, final String txt) {
    	handler.post(new Runnable() {
            public void run() {
                view.setText(txt);
            }
        });
    }

    public JSONObject getUbintelJsonData(Map<String,String> mapdata){
        //{ "version" : "1.0.0", "datapoint" : { "id" : "rpm", "value" : "40" }  }

        JSONObject data = null;
        try {
        Date now = new Date();
        data = new JSONObject();
        data.put("time", sdf.format(now));

            Iterator<String> keys = mapdata.keySet().iterator();
            while (keys.hasNext()) {
                String k = keys.next();
                data.put(k, mapdata.get(k));
             }

         // Log.d("output", parent.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return data;


    }
    public void setDataTableText(Map<String,String> dataMap) {
    	TableLayout tl = (TableLayout) findViewById(R.id.data_table);
    	tl.removeAllViews();
    	Set<String> keySet = dataMap.keySet();
    	String[] keys = keySet.toArray(new String[0]);
    	Arrays.sort(keys);
        String currentData = "{";
        Date now = new Date();
        currentData+="time:"+now.getTime()+",";
        for (String k:keys) {
    		addTableRow(tl,k,dataMap.get(k));
            currentData+=k+":"+dataMap.get(k)+",";

    	}
        currentData = currentData+"},";
           JSONObject jsonData = getUbintelJsonData(dataMap);
           currentData = jsonData!=null?jsonData.toString():currentData;
            try {

                writer = new BufferedWriter(new FileWriter(traceFile, true /*append*/));
                writer.write(currentData);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        String coolant = dataMap.get(ObdConfig.COOLANT_TEMP);
    	String runTime = dataMap.get(ObdConfig.RUN_TIME);
    	String rpm = dataMap.get(ObdConfig.RPM);
    	String fuelEcon = dataMap.get(ObdConfig.FUEL_ECON);
    	String fuelEconMap = dataMap.get(ObdConfig.FUEL_ECON_MAP);
    	String speed = dataMap.get(ObdConfig.SPEED);
    	String temp = dataMap.get(ObdConfig.INTAKE_TEMP);
    	/*if (coolant != null) {
    		setCoolantTemp(coolant);
    	}*/
    	/*if (isFill(fuelEcon)) {
    		if (!isFill(fuelEconMap)) {
    			setFuelEconomy(fuelEconMap);
    		}
    	} else {
    		setFuelEconomy(fuelEcon);
    	}*/
    	setRunTime(runTime);
    	setRpm(rpm);

    	setAvgSpeed(speed);
    	setAirTemp(temp);
    }
    private void setAirTemp(String temp) {
    	if (isFill(temp)) {
    		return;
    	}
    	/*TextView tempView = (TextView)findViewById(R.id.air_temp_text);
    	tempView.setText(temp);*/
    }
    private void setAvgSpeed(String spd) {
    	try {
			String[] spds = spd.split(" ");
			int spdv = Integer.parseInt(spds[0]);
			if ("km/h".equals(spds[1])) {
				spdv = (int)((double)spdv / .625);
			}
			if (spdv > 0) {
				speedAvg = (spdv + (speedi*speedAvg)) / (speedi + 1);
				speedi += 1;
			}
			TextView avgSpeed = (TextView)findViewById(R.id.avg_spd_text);
			spdv = (int)speedAvg;
			if ("km/h".equals(spds[1])) {
				spdv = (int)(speedAvg*.625);
			}
			avgSpeed.setText(String.format("%d %s", spdv, spds[1]));
		} catch (Exception e) {
		}
    }
    private boolean isFill(String data) {
    	if ("--".equals(data) || "NODATA".equals(data)) {
    		return true;
    	}
    	return false;
    }
    private void setRpm(String rpm) {
    	if (isFill(rpm)) {
    		return;
    	}
    	TextView rpmView = (TextView)findViewById(R.id.avg_rpm_text);
    	rpmView.setText(rpm);
    }
    private void setRunTime(String runTime) {
    	if (isFill(runTime)) {
    		return;
    	}
    	TextView runTimeView = (TextView)findViewById(R.id.run_time_text);
    	runTimeView.setText(runTime);
    }
   /* private void setFuelEconomy(String fuelEcon) {
    	try {
			String[] econs = fuelEcon.split(" ");
			double econ = Double.parseDouble(econs[0]);
			if ("kml".equals(econs[1])) {
				econ = econ / 0.354013;
			}
			if (econ > 0 && econ <= maxFuelEcon) {
				fuelEconAvg = (econ + (fuelEconi*fuelEconAvg)) / (fuelEconi + 1);
				fuelEconi += 1;
			}
			TextView instEcon = (TextView)findViewById(R.id.inst_fuel_econ_text);
			TextView avgEcon = (TextView)findViewById(R.id.avg_fuel_econ_text);
			TextView avgEconlbl = (TextView)findViewById(R.id.avg_fuel_econ_lbl);
			instEcon.setText(fuelEcon);
			String lbl = "mpg";
			if ("kml".equals(econs[1])) {
				econ = fuelEconAvg * 0.354013;
				lbl = "kml";
			}
			avgEcon.setText(String.format("%d", Math.round(fuelEconAvg)));
			avgEconlbl.setText(lbl);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }*/
   /* private void setCoolantTemp(String coolant) {
    	try {
    		String[] cools = coolant.split(" ");
    		int temp = Integer.parseInt(cools[0]);
    		if ("F".equals(cools[1])) {
    			temp = (temp - 32) * 5 / 9;
    		}
    		CoolantGaugeView gauge = (CoolantGaugeView)findViewById(R.id.coolant_gauge);
    		gauge.setTemp(temp);
    	} catch (Exception e) {
    	}
    }*/
    private void addTableRow(TableLayout tl, String key, String val) {
    	TableRow tr = new TableRow(this);
		MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
				LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
		params.setMargins(TABLE_ROW_MARGIN,TABLE_ROW_MARGIN,TABLE_ROW_MARGIN,TABLE_ROW_MARGIN);
		tr.setLayoutParams(params);
		tr.setBackgroundColor(Color.BLACK);
		TextView name = new TextView(this);
		name.setGravity(Gravity.RIGHT);
		name.setText(key + ": ");
		TextView value = new TextView(this);
		value.setGravity(Gravity.LEFT);
		value.setText(val);
		tr.addView(name);
		tr.addView(value);
		tl.addView(tr,new TableLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
    }
    private class UpdateThread extends Thread {
    	boolean stop = false;
    	public void run() {
    		String vehicleId = prefs.getString(ObdReaderConfigActivity.VEHICLE_ID_KEY,"");
    		while (!stop && serviceConn.isRunning()) {
    			ObdReaderService svc = serviceConn.getService();
    			Map<String,String> dataMap = null;
    			if (svc == null || svc.getDataMap() == null) {
    				dataMap = new HashMap<String, String>();
    				for (ObdCommand cmd:ObdConfig.getCommands()) {
    					dataMap.put(cmd.getDesc(),"--");
    				}
    			} else {
    				dataMap = svc.getDataMap();
    			}
    			if (vehicleId != null && !"".equals(vehicleId.trim())) {
        			dataMap.put("Vehicle ID", vehicleId);
        		}
    			updateDataTable(dataMap);
    			try {
					Thread.sleep(ObdReaderConfigActivity.getUpdatePeriod(prefs));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
}
