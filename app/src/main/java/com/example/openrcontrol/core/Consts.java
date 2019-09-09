package com.example.openrcontrol.core;

public abstract class Consts {
	public static final String BINARY = "binary";
	public static final String INTEGER = "integer";
	public static final String HEXADECIMAL = "hexadecimal";
	public static final String TEXT = "text";

	public static final String ACTION_USB_PERMISSION = "com.google.android.HID.action.USB_PERMISSION";
	public static final String RECEIVE_DATA_FORMAT = "receiveDataFormat";
	public static final String DELIMITER = "delimiter";
	public static final String DELIMITER_NONE = "none";
	public static final String DELIMITER_NEW_LINE = "newLine";
	public static final String DELIMITER_SPACE = "space";
	public static final String NEW_LINE = "\n";
	public static final String SPACE = " ";
	public static final String ASTERISK = "*";

	public static final String ACTION_USB_SHOW_DEVICES_LIST = "ACTION_USB_SHOW_DEVICES_LIST";
	public static final String ACTION_USB_DATA_TYPE = "ACTION_USB_DATA_TYPE";
	public static final int RESULT_SETTINGS = 7;
	public static final String USB_HID_TERMINAL_CLOSE_ACTION = "USB_HID_TERMINAL_EXIT";

	public static final String COMMAND_TOGGLE_RAINLIGHT = "TOGGLE_RAINLIGHT";
	public static final String COMMAND_TOGGLE_HAZARD = "TOGGLE_RAINLIGHT_HAZARD";
	public static final String COMMAND_NEXT_RAINLIGHT_COLOR = "SET_NEXT_RAINLIGHT_COLOR";
	public static final String COMMAND_SET_THROTTLE = "SET_THROTTLE";
	public static final String COMMAND_SET_STEERING = "SET_STEERING";


	private Consts() {
	}
}