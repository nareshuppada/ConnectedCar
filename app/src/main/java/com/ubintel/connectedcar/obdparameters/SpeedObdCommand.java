package com.ubintel.connectedcar.obdparameters;

import com.ubintel.connectedcar.config.ObdConfig;

public class SpeedObdCommand extends IntObdCommand {

	public SpeedObdCommand() {
		super("010D",ObdConfig.SPEED,"km/h","mph","speed");
	}
	public SpeedObdCommand(SpeedObdCommand other) {
		super(other);
	}
	@Override
	public int getImperialInt() {
		if (intValue <= 0) {
			return 0;
		}
		return (int)(intValue * .625);
	}
}
