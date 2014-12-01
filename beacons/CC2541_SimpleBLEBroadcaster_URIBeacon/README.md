#This is uribeacon code for Texas Instrument CC2541/CC2540 platform. 

Here is a working code and hex file for Texas Instrument CC2541 platform. 
So UriBeacon can be played for TI platform users. 
Since no peripheral is used. This code should work on any CC2541 board.

##What you would need to try this without compiler

CC Debugger

A CC2541 board

Smart RF Flash Programmer


##How to compile this project.

You will need:

IAR Embedded Workbench for 8051

CC Debugger

A CC2541/2540 board

BLE-STACK 1.4.0

1.Duplicate "SimpleBLEBroadcaster" so you don't mess up orignal code

![dup proj](01_duplicate_project.png)

2.Compile SimpleBLEBroadcaster example and make sure it works well on your board.

If you see. "Segment ISTACK is too long" error in IAR 8.30, you can change "number of virtual registers" to 8

![reg](02_compile_ERR_8_30.png)


