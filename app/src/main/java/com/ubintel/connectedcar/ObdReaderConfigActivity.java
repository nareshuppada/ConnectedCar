package com.ubintel.connectedcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.ubintel.connectedcar.config.ObdConfig;
import com.ubintel.connectedcar.obdparameters.ObdCommand;

import java.util.ArrayList;
import java.util.Set;

public class ObdReaderConfigActivity extends PreferenceActivity implements OnPreferenceChangeListener {

	public static final String BLUETOOTH_LIST_KEY = "bluetooth_list_preference";
	public static final String UPLOAD_URL_KEY = "upload_url_preference";
	public static final String UPLOAD_DATA_KEY = "upload_data_preference";
	public static final String UPDATE_PERIOD_KEY = "update_period_preference";
	public static final String VEHICLE_ID_KEY = "vehicle_id_preference";
	/*public static final String ENGINE_DISPLACEMENT_KEY = "engine_displacement_preference";
	public static final String VOLUMETRIC_EFFICIENCY_KEY = "volumetric_efficiency_preference";*/
	public static final String IMPERIAL_UNITS_KEY = "imperial_units_preference";
	public static final String COMMANDS_SCREEN_KEY = "obd_commands_screen";
	public static final String ENABLE_GPS_KEY = "enable_gps_preference";
	public static final String MAX_FUEL_ECON_KEY = "max_fuel_econ_preference";
	public static final String CONFIG_READER_KEY = "reader_config_preference";
    public static final String UPLOAD_DOMAIN_LIST_KEY = "upload_domain_preference";
    public static final String DEVICE_ID_KEY ="device_id_preference";
    public static final String APP_TEST_MODE_KEY ="test_mode_preference";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ArrayList<CharSequence> pairedDeviceStrings = new ArrayList<CharSequence>();
        ArrayList<CharSequence> vals = new ArrayList<CharSequence>();
		ListPreference listPref = (ListPreference) getPreferenceScreen().findPreference(BLUETOOTH_LIST_KEY);
		//String[] prefKeys = new String[]{ENGINE_DISPLACEMENT_KEY,VOLUMETRIC_EFFICIENCY_KEY,UPDATE_PERIOD_KEY,MAX_FUEL_ECON_KEY};
        String[] prefKeys = new String[]{UPDATE_PERIOD_KEY};
		for (String prefKey:prefKeys) {
			EditTextPreference txtPref = (EditTextPreference) getPreferenceScreen().findPreference(prefKey);
			txtPref.setOnPreferenceChangeListener(this);
		}
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
        	listPref.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
            listPref.setEntryValues(vals.toArray(new CharSequence[0]));
        	return;
        }
        final Activity thisAct = this;
        listPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				if (mBluetoothAdapter == null) {
					Toast.makeText(thisAct, "This device does not support bluetooth", Toast.LENGTH_LONG);
					return false;
				}
				return true;
			}
		});

        ListPreference domainListPref = (ListPreference) getPreferenceScreen().findPreference(UPLOAD_DOMAIN_LIST_KEY);

        ArrayList<CharSequence> domainsArray = new ArrayList<CharSequence>();
        domainsArray.add("UNARTO");
        domainsArray.add("UBINTEL");
        domainListPref.setEntries(domainsArray.toArray(new CharSequence[0]));
        ArrayList<CharSequence> domainValues = new ArrayList<CharSequence>();
        domainValues.add("1");
        domainValues.add("2");
        domainListPref.setEntryValues(domainValues.toArray(new CharSequence[0]));





        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceStrings.add(device.getName() + "\n" + device.getAddress());
                vals.add(device.getAddress());
            }
        }
        listPref.setEntries(pairedDeviceStrings.toArray(new CharSequence[0]));
        listPref.setEntryValues(vals.toArray(new CharSequence[0]));
        ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
        PreferenceScreen cmdScr = (PreferenceScreen) getPreferenceScreen().findPreference(COMMANDS_SCREEN_KEY);
        for (int i = 0; i < cmds.size(); i++) {
        	ObdCommand cmd = cmds.get(i);
        	CheckBoxPreference cpref = new CheckBoxPreference(this);
        	cpref.setTitle(cmd.getDesc());
        	cpref.setKey(cmd.getDesc());
        	cpref.setChecked(true);
        	cmdScr.addPreference(cpref);
        }
	}
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (UPDATE_PERIOD_KEY.equals(preference.getKey()) || 
				/*VOLUMETRIC_EFFICIENCY_KEY.equals(preference.getKey()) ||
				ENGINE_DISPLACEMENT_KEY.equals(preference.getKey()) ||*/
				MAX_FUEL_ECON_KEY.equals(preference.getKey())) {
			try {
				Double.parseDouble(newValue.toString());
				return true;
			} catch (Exception e) {
				Toast.makeText(this, "Couldn't parse '" + newValue.toString() + "' as a number.", Toast.LENGTH_LONG).show();
			}
		}
		return false;
	}
    public static int getUpdatePeriod(SharedPreferences prefs) {
        String periodString = prefs.getString(ObdReaderConfigActivity.UPDATE_PERIOD_KEY, "4");
        int period = 4000;
        try {
			period = Integer.parseInt(periodString) * 1000;
		} catch (Exception e) {
		}
    	if (period <= 0) {
    		period = 250;
    	}
    	return period;
    }
/*    public static double getVolumetricEfficieny(SharedPreferences prefs) {
    	String veString = prefs.getString(ObdReaderConfigActivity.VOLUMETRIC_EFFICIENCY_KEY, ".85");
    	double ve = 0.85;
    	try {
			ve = Double.parseDouble(veString);
		} catch (Exception e) {
		}
		return ve;
    }
    public static double getEngineDisplacement(SharedPreferences prefs) {
    	String edString = prefs.getString(ObdReaderConfigActivity.ENGINE_DISPLACEMENT_KEY, "1.6");
    	double ed = 1.6;
    	try {
			ed = Double.parseDouble(edString);
		} catch (Exception e) {
		}
		return ed;
    }*/
    public static ArrayList<ObdCommand> getObdCommands(SharedPreferences prefs) {
    	ArrayList<ObdCommand> cmds = ObdConfig.getCommands();
    	ArrayList<ObdCommand> ucmds = new ArrayList<ObdCommand>();
    	for (int i = 0; i < cmds.size(); i++) {
    		ObdCommand cmd = cmds.get(i);
    		boolean selected = prefs.getBoolean(cmd.getDesc(), true);
    		if (selected) {
    			ucmds.add(cmd);
    		}
    	}
    	return ucmds;
    }
 /*   public static double getMaxFuelEconomy(SharedPreferences prefs) {
    	String maxStr = prefs.getString(ObdReaderConfigActivity.MAX_FUEL_ECON_KEY, "70");
    	double max = 70;f
    	try {
			max = Double.parseDouble(maxStr);
		} catch (Exception e) {
		}
		return max;
    }*/
    public static String[] getReaderConfigCommands(SharedPreferences prefs) {
    	String cmdsStr = prefs.getString(CONFIG_READER_KEY, "atsp0\natz");
    	String[] cmds = cmdsStr.split("\n");
    	return cmds;
    }
}
