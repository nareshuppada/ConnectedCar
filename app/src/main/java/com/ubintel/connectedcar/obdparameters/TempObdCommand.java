package com.ubintel.connectedcar.obdparameters;

public class TempObdCommand extends IntObdCommand{

	public TempObdCommand(String cmd, String desc, String resType, String impType,String ubintelChannel) {
		super(cmd, desc, resType, impType,ubintelChannel);
	}
	public TempObdCommand(TempObdCommand other) {
		super(other);
	}
	protected int transform(int b) {
		return b-40;
	}
	@Override
	public int getImperialInt() {
		return (intValue*9/5) + 32;
	}
}
