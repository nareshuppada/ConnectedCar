package com.ubintel.connectedcar.config;

import com.ubintel.connectedcar.obdparameters.AirIntakeTempObdCommand;
import com.ubintel.connectedcar.obdparameters.CommandEquivRatioObdCommand;
import com.ubintel.connectedcar.obdparameters.DtcNumberObdCommand;
import com.ubintel.connectedcar.obdparameters.EngineRPMObdCommand;
import com.ubintel.connectedcar.obdparameters.EngineRunTimeObdCommand;
import com.ubintel.connectedcar.obdparameters.FuelEconomyObdCommand;
import com.ubintel.connectedcar.obdparameters.FuelPressureObdCommand;
import com.ubintel.connectedcar.obdparameters.FuelTrimObdCommand;
import com.ubintel.connectedcar.obdparameters.IntakeManifoldPressureObdCommand;
import com.ubintel.connectedcar.obdparameters.MassAirFlowObdCommand;
import com.ubintel.connectedcar.obdparameters.ObdCommand;
import com.ubintel.connectedcar.obdparameters.PressureObdCommand;
import com.ubintel.connectedcar.obdparameters.SpeedObdCommand;
import com.ubintel.connectedcar.obdparameters.TempObdCommand;
import com.ubintel.connectedcar.obdparameters.ThrottleObdCommand;
import com.ubintel.connectedcar.obdparameters.TimingAdvanceObdCommand;
import com.ubintel.connectedcar.obdparameters.TroubleCodesObdCommand;

import java.util.ArrayList;


public class ObdConfig {

	public final static String COOLANT_TEMP = "Coolant Temp";
	public final static String FUEL_ECON = "Fuel Economy";
	public final static String FUEL_ECON_MAP = "Fuel Economy MAP";
	public final static String RPM = "Engine RPM";
	public final static String RUN_TIME = "Engine Runtime";
	public final static String SPEED = "Vehicle Speed";
	public final static String AIR_TEMP = "Ambient Air Temp";
	public final static String INTAKE_TEMP = "Air Intake Temp";

	public static ArrayList<ObdCommand> getCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new AirIntakeTempObdCommand());
		cmds.add(new IntakeManifoldPressureObdCommand());
		cmds.add(new PressureObdCommand("0133","Barometric Press","kPa","atm","pressure"));
		cmds.add(new TempObdCommand("0146",AIR_TEMP,"C","F","airtemp"));
		cmds.add(new SpeedObdCommand());
		cmds.add(new ThrottleObdCommand());
		cmds.add(new EngineRPMObdCommand());
		cmds.add(new FuelPressureObdCommand());
		cmds.add(new TempObdCommand("0105",COOLANT_TEMP,"C","F","ctemp"));
		cmds.add(new ThrottleObdCommand("0104","Engine Load","%","trottle"));
		cmds.add(new MassAirFlowObdCommand());
		cmds.add(new FuelEconomyObdCommand());
		cmds.add(new FuelTrimObdCommand());
		cmds.add(new FuelTrimObdCommand("0106","Short Term Fuel Trim","%","fueltrim"));
		cmds.add(new EngineRunTimeObdCommand());
		cmds.add(new CommandEquivRatioObdCommand());
		cmds.add(new TimingAdvanceObdCommand());
		cmds.add(new ObdCommand("03","Trouble Codes","","","troublecodes"));
		return cmds;
	}
	public static ArrayList<ObdCommand> getStaticCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.add(new DtcNumberObdCommand());
		cmds.add(new TroubleCodesObdCommand("03","Trouble Codes","","",""));
		cmds.add(new ObdCommand("04","Reset Codes","","",""));
		cmds.add(new ObdCommand("atz\ratz\ratz\r","Serial Reset atz","","",""));
		cmds.add(new ObdCommand("atz\ratz\ratz\rate0","Serial Echo Off ate0","","",""));
		cmds.add(new ObdCommand("ate1","Serial Echo On ate1","","",""));
		cmds.add(new ObdCommand("atsp0","Reset Protocol astp0","","",""));
		cmds.add(new ObdCommand("atspa2","Reset Protocol atspa2","","",""));
		return cmds;
	}
	public static ArrayList<ObdCommand> getAllCommands() {
		ArrayList<ObdCommand> cmds = new ArrayList<ObdCommand>();
		cmds.addAll(getStaticCommands());
		cmds.addAll(getCommands());
		return cmds;
	}
}
