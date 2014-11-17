#Setup Windows environment for Nordic Smart Beacons
1. Setup the Keil mVision IDE - current version 5.12.
2. Setup the nRF51822 Bluetooth Smart Beacon environment.
	* Use the SD S110-SD-v6 (version 6.0.0)- S110 SoftDevice.
3. Necessary Equipment
 	* Tag from nRF51822 Bluetooth Smart Beacon Kit
	* J-Link Lite CortexM-9 JTAG/SWD Emulator from nRF51822 Development Kit
	* TC2030-CTX-NL 6-Pin "No Legs" Cable with 10-pin micro-connector for Cortex processors


4. Reset the Tag
	* Connect tag to J-Link emulator using the "No Legs" Cable.
	* Reset tag using nRFgo Studio to erase the tag and reload the softdevice and bootloader, if desired.
		* Use C:\s110_nrf51822_6.0.0\s110_nrf51822_6.0.0_softdevice.hex.
	* (Optional) Enabling over the air direct firmware update (DFU)
		* Program the bootloader: C:\Nordic Semiconductor\nRF51822 Beacon v1.0.1\Precompiled hex\bootloader.hex.
		* Make sure you program the softdevice first, then the bootloader, then the app can be loaded.


#Setup ble_uri_beacon Project in Keil mVision

1. Set up working directory

	* Copy ble_uri_beacon into folder into
C:\Nordic Semiconductor\nRF51822 Beacon v1.0.1\Source Code\Nordic\nrf51822\Board\nrf51_beacon\pca20006.
	* Copy C:\Nordic Semiconductor\nRF51822 Beacon v1.0.1\Source Code\Nordic\nrf51822\Board\nrf51_beacon\pca20006\ble_app_beacon_bcs\pstorage_platfrom.h to the new ble_uri_beacon directory.

2. Open ble_uri_beacon.uvprojx with Keil mVision. 
	* Build the app, and then Load the app to the connected tag.

#Using the nRF51822 Smart Beacons 

* After being programmed, the tags are left in a debug mode, which consumes significantly more power. Pop the battery in and out and it will go into normal mode with low energy consumption.

* The J-Link emulator draws power from the tag.  If the battery level of the tag’s battery is below a certain threshold, you cannot upload programs to the tag, but the tag will still beacon. Get a new battery. The best option for debugging is to use a tag that is connected to and powered by a power source.

* Before running the Uribeacon app on the tag, it is best to erase all data and reset the tag.

* Since persistent memory determines how the tags are configured, it is also best to erase all data and reset the tag if you make changes to the code.


