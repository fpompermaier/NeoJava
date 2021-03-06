#!/bin/bash

[[ "$(pidof -x $(basename $0))" != $$ ]] && echo "Program already running. Exit." && exit

if [ ! -h /dev/ttyS0 ]; then
	#sudo ln -s /dev/ttymxc3 /dev/ttyS0
	sudo ln -s /dev/ttyMCC /dev/ttyS0
fi
if [ ! -f /sys/class/i2c-dev/i2c-1/device/1-0048/temp1_input ]; then
	sudo sh -c 'echo lm75 0x48 >/sys/class/i2c-dev/i2c-1/device/new_device'
	sudo rmmod lm75
	sudo modprobe lm75
fi

sudo java -jar target/NeoJava.jar
