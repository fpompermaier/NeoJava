package com.b1project.udooneo.net;

import java.net.Socket;
import java.util.List;

import com.b1project.udooneo.board.BoardInfo;

/**
 *  Copyright (C) 2015 Cyril Bosselut <bossone0013@gmail.com>
 *
 *  This file is part of NeoJava Tools for UDOO Neo
 *
 *  NeoJava Tools for UDOO Neo is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This libraries are distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import com.b1project.udooneo.gpio.Gpio;
import com.b1project.udooneo.gpio.Gpio.PinState;
import com.b1project.udooneo.listeners.NeoJavaProtocolListener;
import com.b1project.udooneo.messages.Message;
import com.b1project.udooneo.messages.RequestMessage;
import com.b1project.udooneo.messages.ResponseMessage;
import com.b1project.udooneo.messages.response.ResponseAccelerometerData;
import com.b1project.udooneo.messages.response.ResponseExportGpios;
import com.b1project.udooneo.messages.response.ResponseGyroscopeData;
import com.b1project.udooneo.messages.response.ResponseMagnetometer;
import com.b1project.udooneo.messages.response.ResponseSetPinMode;
import com.b1project.udooneo.messages.response.ResponseSetPinState;
import com.b1project.udooneo.messages.response.ResponseTemperature;
import com.b1project.udooneo.model.Pin;
import com.b1project.udooneo.model.SensorData;
import com.b1project.udooneo.model.Temperature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

public class NeoJavaProtocol {

	public final static String REQ_HELP = "help";
	public final static String REQ_VERSION = "version";
	public final static String REQ_QUIT = "quit";
	public static final String REQ_SERVER_ACTION = "message/server";
	public final static String REQ_SENSORS_TEMPERATURE = "sensors/temperature";
	public final static String REQ_SENSORS_MAGNETOMETER = "sensors/magnetometer";
	public final static String REQ_SENSORS_ACCELEROMETER = "sensors/accelerometer";
	public final static String REQ_SENSORS_GYROSCOPE = "sensors/gyroscope";
	public final static String REQ_GPIOS_EXPORT = "gpios/exported";
	public final static String REQ_GPIO_SET_MODE = "gpios/mode";
	public final static String REQ_GPIO_SET_STATE = "gpios/state";
	public final static String REQ_GPIO_RELEASE = "gpios/release";
	public final static String REQ_LCD_CLEAR = "lcd/clear";
	public final static String REQ_LCD_PRINT = "lcd/print";
	public static final String REQ_BOARD_ID = "board/id";
	public static final String REQ_BOARD_MODEL = "board/model";
	public static final String REQ_BOARD_NAME = "board/name";
	

	public static final String RESP_HELP = "resp/"+REQ_HELP;
	public static final String RESP_VERSION = "resp/"+REQ_VERSION;
	public final static String RESP_QUIT = "resp/"+REQ_QUIT;
	public static final String RESP_GPIOS_EXPORT = "resp/"+REQ_GPIOS_EXPORT;
	public static final String RESP_SET_PIN_MODE = "resp/"+REQ_GPIO_SET_MODE;
	public static final String RESP_SET_PIN_STATE = "resp/"+REQ_GPIO_SET_STATE;
	public static final String RESP_TEMPERATURE = "resp/"+REQ_SENSORS_TEMPERATURE;
	public static final String RESP_GYROSCOPE = "resp/"+REQ_SENSORS_GYROSCOPE;
	public static final String RESP_ACCELEROMETER = "resp/"+REQ_SENSORS_ACCELEROMETER;
	public static final String RESP_MAGNETOMETER = "resp/"+REQ_SENSORS_MAGNETOMETER;
	public static final String RESP_LCD_CLEAR = "resp/"+REQ_LCD_CLEAR;
	public static final String RESP_LCD_PRINT = "resp/"+REQ_LCD_PRINT;


	public final static String OUTPUT_STATE_CHANGED = "state-changed";
	public final static String OUTPUT_MODE_CHANGED = "mode-changed";
	public static final String ERROR = "error";

	private NeoJavaProtocolListener listener;
	private Socket clientSocket;

	private static final RuntimeTypeAdapterFactory<Message> msgAdapter = RuntimeTypeAdapterFactory.of(Message.class)
			.registerSubtype(RequestMessage.class)
			.registerSubtype(ResponseMessage.class);
	
	private static final RuntimeTypeAdapterFactory<ResponseMessage> responseAdapter = RuntimeTypeAdapterFactory.of(ResponseMessage.class)
			.registerSubtype(ResponseExportGpios.class)
			.registerSubtype(ResponseGyroscopeData.class)
			.registerSubtype(ResponseSetPinMode.class)
			.registerSubtype(ResponseSetPinState.class)
			.registerSubtype(ResponseTemperature.class);

	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapterFactory(msgAdapter)
			.registerTypeAdapterFactory(responseAdapter)
			.create();
	
	public NeoJavaProtocol(Socket clientSocket, NeoJavaProtocolListener listener) {
		this.listener = listener;
		this.clientSocket = clientSocket;
	}

	public static String toJson(Object value) {
		return gson.toJson(value);
	}

	public static <T> T fromJson(String value, Class<T> clazz) {
		return gson.fromJson(value, clazz);
	}

	public ResponseMessage processInput(String input) {
		System.out.println("--------------------\n" + input);
		try {
			RequestMessage m = fromJson(input, RequestMessage.class);
			String output = null;
			String responseMethod = m.method;
			if (!m.method.isEmpty()) {
				// System.out.println("\nRequest: " + message.method);
				// System.out.print("Content: " + content + "\n#:");
				switch (m.method) {
				case REQ_HELP:
					output = REQ_HELP + " - this help\\n";
					output += REQ_VERSION + " - show version\\n";
					output += REQ_QUIT + " - quit\\n";
					output += REQ_GPIOS_EXPORT + " - list exported gpios\\n";
					output += REQ_GPIO_SET_MODE  + " - set gpio mode\\n";
					output += REQ_GPIO_SET_STATE + " - set gpio state\\n";
					output += REQ_GPIO_RELEASE + " - release gpio\\n";
					output += REQ_LCD_CLEAR + " - clear LCD\\n";
					output += REQ_LCD_PRINT + " - print message on 16x2 LCD screen\\n";
					output += REQ_SENSORS_TEMPERATURE + " - request temperature and pressure\\n";
					output += REQ_SENSORS_ACCELEROMETER + " - request accelerometer data\\n";
					output += REQ_SENSORS_MAGNETOMETER + " - request magnetometer data\\n";
					output += REQ_SENSORS_GYROSCOPE + " - request gyroscope data";
					break;
				case REQ_VERSION:
					if (listener != null) {
						output = listener.getVersionString();
					}
					break;
				case REQ_QUIT:
					if (listener != null) {
						listener.onQuitRequest(clientSocket);
					}
					output = "Goodbye !";
					break;
				case REQ_BOARD_ID:
					output = BoardInfo.getBoardID();
					break;
				case REQ_BOARD_MODEL:
					output = BoardInfo.getBoardModel();
					break;
				case REQ_BOARD_NAME:
					output = BoardInfo.getBoardName();
					break;
				case REQ_GPIOS_EXPORT:
					List<Pin> gpios = null;
					if (listener != null) {
						gpios = listener.onExportedGpiosRequest();
					}
					return new ResponseExportGpios("OK", gpios);
				case REQ_GPIO_SET_MODE:
					try {
						Gpio gpio = Gpio.getInstance(m.pinId);
						gpio.setMode(m.mode);
						output = "OK";
					} catch (Exception e) {
						output = "Invalid set GPIO mode: " + input;
						responseMethod = ERROR;
					}
					break;
				case REQ_GPIO_SET_STATE:
					try {
						Gpio gpio = Gpio.getInstance(m.pinId);
						if (gpio.getMode() == Gpio.PinMode.OUTPUT) {
							gpio.write(m.state);
							output = "OK";
						} else {
							output = "PIN is in INPUT mode, can't write value";
							responseMethod = ERROR;
						}
					} catch (Exception e) {
						output = "invalid set GPIO state: " + input;
						responseMethod = ERROR;
					}
					break;
				case REQ_GPIO_RELEASE:
					try {
						Gpio gpio = Gpio.getInstance(m.pinId);
						gpio.release();
						output = "OK";
					} catch (Exception e) {
						output = "error during PIN release: " + e.getMessage();
						responseMethod = ERROR;
					}
					break;
				case REQ_SENSORS_TEMPERATURE:
					if (listener != null) {
						listener.onTemperatureRequest();
					}
					output = "Reading temperature";
					break;
				case REQ_SENSORS_ACCELEROMETER:
					if (listener != null) {
						listener.onAccelerometerRequest();
					}
					output = "Reading accelerometer data";
					break;
				case REQ_SENSORS_MAGNETOMETER:
					if (listener != null) {
						listener.onMagnetometerRequest();
					}
					output = "Reading magnetometer data";
					break;
				case REQ_SENSORS_GYROSCOPE:
					if (listener != null) {
						listener.onGyroscopeRequest();
					}
					output = "Reading gyroscope data";
					break;
				case REQ_LCD_CLEAR:
					if (listener != null) {
						listener.onClearLCDRequest();
						output = "OK";
					}
					break;
				case REQ_LCD_PRINT:
					if (listener != null) {
						listener.onLCDPrintRequest(m.textToDisplay);
					}
					output = "OK";
					break;
				default:
					output = "Unknown method: " + m.method;
				}
				return new ResponseMessage(responseMethod, output);
			}
			return new ResponseMessage(ERROR, "Empty method: " + input.replace("\"", "\\\""));
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return new ResponseMessage(ERROR, "Invalid request: " + input);
		}
	}

	public static ResponseExportGpios makeExportMessage(List<Pin> gpios) {
		return new ResponseExportGpios("OK", gpios);
	}

	public static ResponseSetPinMode makePinModeMessage(int pinId, Gpio.PinMode mode) {
		return new ResponseSetPinMode("OK", new Pin(pinId, mode));
	}

	public static ResponseSetPinState makePinStateMessage(int pinId, PinState state) {
		return new ResponseSetPinState("OK", new Pin(pinId,state));
	}

	public static ResponseMagnetometer makeMagnetometerMessage(String sensorDataStr) {
		return new ResponseMagnetometer("OK", new SensorData(sensorDataStr));
	}

	public static ResponseAccelerometerData makeAccelerometerMessage(String sensorDataStr) {
		return new ResponseAccelerometerData("OK", new SensorData(sensorDataStr));
	}

	public static ResponseGyroscopeData makeGyroscopeMessage(String sensorDataStr) {
		return new ResponseGyroscopeData("OK", new SensorData(sensorDataStr));
	}

	public static ResponseTemperature makeTemperatureMessage(Float temp, Float pressure) {
		return new ResponseTemperature("OK", new Temperature(temp, pressure));
	}

	public static ResponseMessage makeShutdownMessage() {
		return new ResponseMessage(REQ_SERVER_ACTION, "shutdown");
	}

}
