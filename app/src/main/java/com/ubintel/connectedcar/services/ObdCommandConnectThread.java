package com.ubintel.connectedcar.services;

import android.bluetooth.BluetoothDevice;

import com.ubintel.connectedcar.ObdReaderCommandActivity;
import com.ubintel.connectedcar.config.ObdConfig;
import com.ubintel.connectedcar.obdparameters.ObdCommand;

public class ObdCommandConnectThread extends ObdConnectThread {

	private ObdCommand cmd = null;
	private ObdReaderCommandActivity activity = null;

	public ObdCommandConnectThread(BluetoothDevice dev, ObdReaderCommandActivity activity, ObdCommand cmd,boolean imperialUnits) {
		super(dev, null, null, null, 0,imperialUnits, false, ObdConfig.getAllCommands(),false);
		this.cmd = cmd;
		this.activity = activity;
	}

	public void run() {
		try {
			activity.logMsg("Starting device...");
			startDevice();
			activity.logMsg("Device started, running " + cmd.getCmd() + "...");
			cmd.setConnectThread(this);
			String res = runCommand(cmd);
			String rawRes = cmd.getResult().replace("\r","\\r").replace("\n","\\n");
			activity.logMsg("Raw result is '" + rawRes + "'");
			results.put(cmd.getDesc(), res);
			if (cmd.getError() != null) {
				activity.logMsg(cmd.getError().getMessage());
				activity.logMsg(getStackTrace(cmd.getError()));
			}
		} catch (Exception e) {
			activity.logMsg("Error running command: " + e.getMessage() + ", result was: '" + cmd.getResult() + "'");
			activity.logMsg("Error was: " + getStackTrace(e));
		} finally {
			close();
		}
	}
}
