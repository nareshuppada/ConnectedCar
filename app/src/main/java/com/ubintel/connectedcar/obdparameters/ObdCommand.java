package com.ubintel.connectedcar.obdparameters;

import com.ubintel.connectedcar.services.ObdConnectThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class ObdCommand extends Thread {

	protected InputStream in = null;
	protected OutputStream out = null;
	protected ArrayList<Byte> buff = null;
	protected String cmd = null;
	protected String desc = null;
	protected String resType = null;



    protected String ubintelChannel = null;
	protected Exception error;
	protected Object rawValue = null;
	protected HashMap<String,Object> data = null;
	protected ObdConnectThread connectThread = null;
	protected String impType = null;

	public ObdCommand(String cmd, String desc, String resType, String impType, String ubintelChannel) {
		this.cmd = cmd;
		this.desc = desc;
		this.resType = resType;
		this.buff = new ArrayList<Byte>();
		this.impType = impType;
        this.ubintelChannel = ubintelChannel;
	}
	public void setConnectThread(ObdConnectThread thread) {
		this.connectThread = thread;
	}
	public boolean isImperial() {
		if (connectThread != null && connectThread.getImperialUnits()) {
			return true;
		}
		return false;
	}
	public ObdCommand(ObdCommand other) {
		this(other.cmd, other.desc, other.resType, other.impType, other.ubintelChannel);
	}
	public void setInputStream(InputStream in) {
		this.in = in;
	}
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	public void run() {
		sendCmd(cmd);
		readResult();
	}
	public void setDataMap(HashMap<String,Object> data) {
		this.data = data;
	}
	protected void sendCmd(String cmd) {
		try {
			cmd += "\r\n";
			out.write(cmd.getBytes());
			out.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	protected void readResult() {
		byte c = 0;
		this.buff.clear();
		try {
			while ((char)(c = (byte)in.read()) != '>') {
				buff.add(c);
			}
		} catch (IOException e) {
		}
	}
	public String getResult() {
		return new String(getByteArray());
	}
	public byte[] getByteArray() {
		byte[] data = new byte[this.buff.size()];
		for (int i = 0; i < this.buff.size(); i++) {
			data[i] = this.buff.get(i);
		}
		return data;
	}
	public String formatResult() {
		String res = getResult();
		String[] ress = res.split("\r");
		res = ress[0].replace(" ","");
		return res;
	}
	public InputStream getIn() {
		return in;
	}
	public OutputStream getOut() {
		return out;
	}
	public ArrayList<Byte> getBuff() {
		return buff;
	}
	public String getCmd() {
		return cmd;
	}
	public String getDesc() {
		return desc;
	}
	public String getResType() {
		return resType;
	}
	public void setError(Exception e) {
		error = e;
	}
	public Exception getError() {
		return error;
	}
	public Object getRawValue() {
		return rawValue;
	}
    public String getUbintelChannel() {
        return ubintelChannel;
    }

    public void setUbintelChannel(String ubintelChannel) {
        this.ubintelChannel = ubintelChannel;
    }
}
