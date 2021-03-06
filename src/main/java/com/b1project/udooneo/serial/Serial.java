package com.b1project.udooneo.serial;
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

import com.b1project.udooneo.NeoJava;
import com.b1project.udooneo.listeners.SerialOutputListener;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.InputStream;
import java.io.OutputStream;

public class Serial {
    private String mDeviceUri = NeoJava.DEFAULT_BINDING_TTY;
    private int mBaudRate = NeoJava.SERIAL_PORT_BAUD_RATE;
    private static OutputStream mOutputStream;
    private static InputStream mInputStream;
    private SerialOutputListener mListener;
    private SerialPort mSerialPort;
    private SerialReader mReaderTask;

    public Serial(String deviceUri, SerialOutputListener listener){
        super();
        if(deviceUri != null) {
            this.mDeviceUri = deviceUri;
        }
        this.mListener = listener;
    }

    public void connect() throws Exception {
        System.out.println("\rConnecting to serial port...");
        System.out.print("#:");
        CommPortIdentifier mCommPortIdentifier = CommPortIdentifier.getPortIdentifier(mDeviceUri);
        if (mCommPortIdentifier.isCurrentlyOwned()) {
            System.err.println("\rError: Port currently in use");
            System.out.print("#:");
        } else {
            CommPort mCommPort = mCommPortIdentifier.open(this.getClass().getName(), 2000);
            if(mCommPort instanceof SerialPort) {
                mSerialPort = (SerialPort) mCommPort;
                mSerialPort.setSerialPortParams(mBaudRate,
                                                SerialPort.DATABITS_8,
                                                SerialPort.STOPBITS_1,
                                                SerialPort.PARITY_NONE);
                mSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                mOutputStream = mSerialPort.getOutputStream();
                mInputStream  = mSerialPort.getInputStream();
                mReaderTask = new SerialReader(mInputStream, mListener);
                (new Thread(mReaderTask)).start();

                Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }));
                System.out.println("\rSerial port connected");
                System.out.print("#:");
            } else {
                System.err.println("\rError: Only serial ports are handled");
                System.out.print("#:");
            }
        }
    }

    public void disconnect() throws Exception{
        if(mSerialPort != null){
            System.out.println("\rDisconnecting from serial port...");
            System.out.print("#:");
            mReaderTask.cancel();
            mOutputStream.close();
            mInputStream.close();
            new Thread(){
                @Override
                public void run(){
                    mSerialPort.removeEventListener();
                    mSerialPort.close();
                    System.out.println("\rSerial port disconnected");
                    System.out.print("#:");
                }
            }.start();
        }
    }

    public void write(String message) throws Exception{
        if(mOutputStream != null){
            mOutputStream.write(message.getBytes());
        }
    }

}
