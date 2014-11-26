package com.ubintel.connectedcar.obdparameters;

public class MassAirFlowObdCommand extends ObdCommand {

	private double maf = -9999.0;
	public MassAirFlowObdCommand() {
		super("0110","Mass Air Flow","g/s","g/s","massairflow");
	}
	public MassAirFlowObdCommand(MassAirFlowObdCommand other) {
		super(other);
	}
	public String formatResult() {
		String res = super.formatResult();
		if ("NODATA".equals(res)) {
			return "NODATA";
		}
		String A = res.substring(4,6);
		String B = res.substring(6,8);
		int a = Integer.parseInt(A, 16);
		int b = Integer.parseInt(B, 16);
		maf = ((256.0*a)+b) / 100.0;
		return Double.toString(maf);
	}
	double getMAF() {
		return maf;
	}
}
