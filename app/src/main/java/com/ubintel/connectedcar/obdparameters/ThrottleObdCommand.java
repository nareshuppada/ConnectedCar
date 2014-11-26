package com.ubintel.connectedcar.obdparameters;

public class ThrottleObdCommand extends IntObdCommand {

	public ThrottleObdCommand(String cmd, String desc, String resType,String ubintelChannel) {
		super(cmd,desc,resType,resType,ubintelChannel);
	}
	public ThrottleObdCommand() {
		super("0111","Throttle Position","%","%","throttle");
	}
	public ThrottleObdCommand(ThrottleObdCommand other) {
		super(other);
	}
	protected int transform(int b) {
		return (int)((double)(b*100)/255.0);
	}
}
