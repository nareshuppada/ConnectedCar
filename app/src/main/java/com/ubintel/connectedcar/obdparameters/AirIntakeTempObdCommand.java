package com.ubintel.connectedcar.obdparameters;

public class AirIntakeTempObdCommand extends TempObdCommand{

	public AirIntakeTempObdCommand() {
		super("010F","Air Intake Temp","C","F","airintaketemp");
	}
	public AirIntakeTempObdCommand(AirIntakeTempObdCommand other) {
		super(other);
	}
}
