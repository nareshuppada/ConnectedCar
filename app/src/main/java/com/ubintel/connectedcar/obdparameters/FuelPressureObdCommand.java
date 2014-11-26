package com.ubintel.connectedcar.obdparameters;

public class FuelPressureObdCommand extends PressureObdCommand{

	public FuelPressureObdCommand() {
		super("010A","Fuel Press","kPa","atm","fuelpressure");
	}
	public FuelPressureObdCommand(FuelPressureObdCommand other) {
		super(other);
	}
	public int transform(int b) {
		return b*3;
	}
}
