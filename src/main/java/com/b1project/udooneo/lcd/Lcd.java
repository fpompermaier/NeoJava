package com.b1project.udooneo.lcd;
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

@SuppressWarnings({"unused", "FieldCanBeLocal", "PointlessBitwiseExpression", "WeakerAccess"})
public class Lcd{

    public final static int NO_RW = 0x00;

    //# commands
    private final static char LCD_CLEARDISPLAY        = 0x01;
    private final static char LCD_RETURNHOME          = 0x02;
    private final static char LCD_ENTRYMODESET        = 0x04;
    private final static char LCD_DISPLAYCONTROL      = 0x08;
    private final static char LCD_CURSORSHIFT         = 0x10;
    private final static char LCD_FUNCTIONSET         = 0x20;
    private final static char LCD_SETCGRAMADDR        = 0x40;
    private final static char LCD_SETDDRAMADDR        = 0x80;

    //# flags for display entry mode
    private final static char LCD_ENTRYRIGHT          = 0x00;
    private final static char LCD_ENTRYLEFT           = 0x02;
    private final static char LCD_ENTRYSHIFTINCREMENT = 0x01;
    private final static char LCD_ENTRYSHIFTDECREMENT = 0x00;

    //# flags for display on/off control
    private final static char LCD_DISPLAYON           = 0x04;
    private final static char LCD_DISPLAYOFF          = 0x00;
    private final static char LCD_CURSORON            = 0x02;
    private final static char LCD_CURSOROFF           = 0x00;
    private final static char LCD_BLINKON             = 0x01;
    private final static char LCD_BLINKOFF            = 0x00;

    //# flags for display/cursor shift
    private final static char LCD_DISPLAYMOVE         = 0x08;
    private final static char LCD_CURSORMOVE          = 0x00;
    private final static char LCD_MOVERIGHT           = 0x04;
    private final static char LCD_MOVELEFT            = 0x00;

    //# flags for function set
    private final static char LCD_8BITMODE            = 0x10;
    private final static char LCD_4BITMODE            = 0x00;
    private final static char LCD_2LINE               = 0x08;
    private final static char LCD_1LINE               = 0x00;
    private final static char LCD_5x10DOTS            = 0x04;
    private final static char LCD_5x8DOTS             = 0x00;


    /*#  BL: gpio16
      #  D7: gpio15
      #  D6: gpio14
      #  D5: gpio22
      #  D4: gpio25
      #  EN: gpio20
      #  RS: gpio21*/
      
    private Gpio lcd_en;
    private Gpio lcd_rs;
    private Gpio lcd_rw;

    private Gpio lcd_bl;

    private Gpio lcd_d4;
    private Gpio lcd_d5;
    private Gpio lcd_d6;
    private Gpio lcd_d7;
    
    private char lcd_mode = LCD_4BITMODE;

    private static char DEFAULT_LCD_STATE = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;

    public Lcd(int en, int rs, int d4, int d5, int d6, int d7, int bl, int rw) throws Exception{
        super();
        lcd_mode = LCD_4BITMODE;
        lcd_en = Gpio.getInstance(en);
        lcd_en.setMode(Gpio.PinMode.OUTPUT);
        lcd_rs = Gpio.getInstance(rs);
        lcd_rs.setMode(Gpio.PinMode.OUTPUT);
        if(rw != NO_RW){
            lcd_rw = Gpio.getInstance(rw);
        }

        lcd_bl = Gpio.getInstance(bl);
        lcd_bl.setMode(Gpio.PinMode.OUTPUT);

        lcd_d4 = Gpio.getInstance(d4);
        lcd_d4.setMode(Gpio.PinMode.OUTPUT);
        lcd_d5 = Gpio.getInstance(d5);
        lcd_d5.setMode(Gpio.PinMode.OUTPUT);
        lcd_d6 = Gpio.getInstance(d6);
        lcd_d6.setMode(Gpio.PinMode.OUTPUT);
        lcd_d7 = Gpio.getInstance(d7);
        lcd_d7.setMode(Gpio.PinMode.OUTPUT);

        lcd_rs.low();
        lcd_en.low();
        if(lcd_rw != null) {
            lcd_rw.low();
        }
        lcd_d7.low();
        lcd_d6.low();
        lcd_d5.low();
        lcd_d4.low();
        lcd_rs.high();
        lcd_rs.low();

        this.set((char)0x33);
        this.set((char)0x32);

        this.set((char)(LCD_SETDDRAMADDR | 0x40));

        this.set((char)(LCD_FUNCTIONSET | lcd_mode | LCD_2LINE | LCD_5x8DOTS));
        setBacklightState(true);
        setLcdDisplayState(true);
        this.set((char)(LCD_ENTRYMODESET | LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT));
        System.out.println("\rClear display");
        System.out.print("#:");
        this.clear();
        Thread.sleep(2000);
        System.out.println("\rLCD init complete");
        System.out.print("#:");

    }

    @Override
    protected void finalize() throws Throwable{
        lcd_en.release();
        lcd_en = null;
        lcd_rs.release();
        lcd_rs = null;

        lcd_bl.release();
        lcd_bl = null;

        lcd_d4.release();
        lcd_d4 = null;
        lcd_d5.release();
        lcd_d5 = null;
        lcd_d6.release();
        lcd_d6 = null;
        lcd_d7.release();
        lcd_d7 = null;
        super.finalize();
    }

    /**
     * Clear lcd screen
     * @throws Exception
     */
    public void clear() throws Exception{
        this.set(LCD_CLEARDISPLAY);
    }

    /**
     * print String on screen
     * @param message String
     * @throws Exception
     */
    public void print(String message) throws Exception{
        if(message == null){
            throw new NullPointerException("Try to print a null String on LCD");
        }
        this.print(message.toCharArray());
    }

    /**
     * print char[] on screen
     * @param message char[]
     * @throws Exception
     */
    public void print(char[] message) throws Exception{
        for (char aMessage : message) {
            if (aMessage == '\n' || aMessage == '\r') {
                this.set((char) 0xC0);
            } else {
                write(aMessage);
            }
        }
    }

    /**
     * toggle display ON/OFF
     * @param state boolean
     * @throws Exception
     */
    public void setLcdDisplayState(boolean state) throws Exception{
        if (state) {
            DEFAULT_LCD_STATE &= LCD_DISPLAYON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_DISPLAYON;
        }
        this.set((char)(LCD_DISPLAYCONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * toggle cursor ON/OFF
     * @param state boolean
     * @throws Exception
     */
    public void setCursorState(boolean state) throws Exception{
        if(state){
            DEFAULT_LCD_STATE &= LCD_CURSORON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_CURSORON;
        }
        this.set((char)(LCD_DISPLAYCONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * Mode cursor to position
     * @param col int from 0 to n
     * @param row int from 0 to n
     * @throws Exception
     */
    public void setCursorPosition(int col, int row) throws Exception{
        int[] row_offsets = { 0x00, 0x40, 0x14, 0x54 };
        this.set((char)(LCD_SETDDRAMADDR | col + row_offsets[row]));
    }

    /**
     * toggle cursor blinking ON/OFF
     * @param state boolean
     * @throws Exception
     */
    public void setCursorBlinkingState(boolean state) throws Exception{
        if(state){
            DEFAULT_LCD_STATE &= LCD_BLINKON;
        }
        else{
            DEFAULT_LCD_STATE &= ~LCD_BLINKON;
        }
        this.set((char)(LCD_DISPLAYCONTROL | DEFAULT_LCD_STATE));
    }

    /**
     * toggle backlight ON/OFF
     * @param state boolean
     * @throws Exception
     */
    public void setBacklightState(boolean state) throws Exception{
        if (state) {
            lcd_bl.high();
        } else {
            lcd_bl.low();
        }
        Thread.sleep(2);
    }

    /**
     * create custom char at location (from 0 to 7) with charmap
     * @param location int
     * @param charMap char[]
     * @throws Exception
     */
    public void createChar(int location, char[] charMap) throws Exception{
        location &= 0x7;
        this.set((char)(LCD_SETCGRAMADDR | (location << 3)));
        for(int i = 0; i < 8; i++){
            this.write(charMap[i]);
        }
    }

    /****** Low level part */

    public void pulseEn() throws Exception{
        //System.out.println("Pulse En");
        Thread.sleep(2);
        lcd_en.high();
        Thread.sleep(2);
        lcd_en.low();
   }
   
    public void set(char value) throws Exception{
        this.writeNibbles(value, 0);
        Thread.sleep(2);
    }

    public void write(char value) throws Exception{
        this.writeNibbles(value, 1);
        Thread.sleep(2);
    }
    
    public void writeNibbles(char value, int mode) throws Exception{
        if(mode == 1){
            lcd_rs.high();
        }
        else{
            lcd_rs.low();
        }
        if(lcd_rw != null) {
            lcd_rw.low();
        }
        int nib = ((int)value >> 4);

        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        Thread.sleep(1);
        nib = ((int)value & 0x0f);

        if((nib & 0x8) != 0){lcd_d7.high();}else{lcd_d7.low();}
        if((nib & 0x4) != 0){lcd_d6.high();}else{lcd_d6.low();}
        if((nib & 0x2) != 0){lcd_d5.high();}else{lcd_d5.low();}
        if((nib & 0x1) != 0){lcd_d4.high();}else{lcd_d4.low();}
        this.pulseEn();
        Thread.sleep(1);
    }
}
