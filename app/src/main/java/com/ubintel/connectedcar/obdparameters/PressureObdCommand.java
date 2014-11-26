package com.ubintel.connectedcar.obdparameters;

public class PressureObdCommand extends IntObdCommand {

	public PressureObdCommand(String cmd, String desc, String resType, String impType,String ubintelChannel) {
		super(cmd,desc,resType,impType,ubintelChannel);
	}
	public PressureObdCommand(PressureObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		if (!isImperial() || "NODATA".equals(res)) {
			return res;
		}
		double atm = intValue * 1.0 / 101.3;
		return String.format("%.2f %s", atm, impType);
	}
}
