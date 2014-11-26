package com.ubintel.connectedcar.obdparameters;

public class FuelTrimObdCommand extends IntObdCommand {

	public FuelTrimObdCommand(String cmd, String desc, String resType, String ubintelChannel) {
		super(cmd,desc,resType,resType,ubintelChannel);
	}
	public FuelTrimObdCommand(FuelTrimObdCommand other) {
		super(other);
	}

	public FuelTrimObdCommand() {
		super("0107","Long Term Fuel Trim","%","%","fueltrim");
	}

	@Override
	public int transform(int b) {
		double perc = (b-128)*(100.0/128);
		return (int)perc;
	}
}
