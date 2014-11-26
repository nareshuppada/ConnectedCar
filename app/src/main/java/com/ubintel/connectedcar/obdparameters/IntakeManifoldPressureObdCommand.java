package com.ubintel.connectedcar.obdparameters;

public class IntakeManifoldPressureObdCommand extends PressureObdCommand {

	public IntakeManifoldPressureObdCommand() {
		super("010B","Intake Manifold Press","kPa","atm","intakepressure");
	}
	public IntakeManifoldPressureObdCommand(IntakeManifoldPressureObdCommand other) {
		super(other);
	}
}
