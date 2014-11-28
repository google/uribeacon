/**************************************************************************************************
  Filename:       hal_timer.h
  Revised:        $Date: 2007-07-06 10:42:24 -0700 (Fri, 06 Jul 2007) $
  Revision:       $Revision: 13579 $

  Description:    This file contains the interface to the Timer Service.


  Copyright 2005-2007 Texas Instruments Incorporated. All rights reserved.

  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License").  You may not use this
  Software unless you agree to abide by the terms of the License. The License
  limits your use, and you acknowledge, that the Software may not be modified,
  copied or distributed unless embedded on a Texas Instruments microcontroller
  or used solely and exclusively in conjunction with a Texas Instruments radio
  frequency transceiver, which is integrated into your product.  Other than for
  the foregoing purpose, you may not use, reproduce, copy, prepare derivative
  works of, modify, distribute, perform, display or sell this Software and/or
  its documentation for any purpose.

  YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
  PROVIDED “AS IS” WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
  INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE, 
  NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
  TEXAS INSTRUMENTS OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT,
  NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER
  LEGAL EQUITABLE THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES
  INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE
  OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT
  OF SUBSTITUTE GOODS, TECHNOLOGY, SERVICES, OR ANY CLAIMS BY THIRD PARTIES
  (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.

  Should you have any questions regarding your right to use this Software,
  contact Texas Instruments Incorporated at www.TI.com. 
**************************************************************************************************/

#ifndef HAL_TIMER_H
#define HAL_TIMER_H

#ifdef __cplusplus
extern "C"
{
#endif

/***************************************************************************************************
 *                                            INCLUDES
 ***************************************************************************************************/
#include "hal_board.h"

/***************************************************************************************************
 *                                             MACROS
 ***************************************************************************************************/



/***************************************************************************************************
 *                                            CONSTANTS
 ***************************************************************************************************/
/* Timer ID definitions */
#define HAL_TIMER_0                0x00    // 8bit timer
#define HAL_TIMER_1                0x01    // 16bit Mac timer
#define HAL_TIMER_2                0x02    // 8bit timer
#define HAL_TIMER_3                0x03    // 16bit timer
#define HAL_TIMER_MAX              4       // Max number of timer

/* Operation Modes for timer */
#define HAL_TIMER_MODE_NORMAL     0x01    // Normal Mode
#define HAL_TIMER_MODE_CTC        0x02    // Clear Timer On Compare
#define HAL_TIMER_MODE_MASK       (HAL_TIMER_MODE_NORMAL | HAL_TIMER_MODE_CTC)

/* Timer1 channels */
#define HAL_T1_CH0 0  
#define HAL_T1_CH1 1  
#define HAL_T1_CH2 2  
#define HAL_T1_CH3 3  
#define HAL_T1_CH4 4  
  
/* Channel definitions */
#define HAL_TIMER_CHANNEL_SINGLE   0x01    // Single Channel - default
#define HAL_TIMER_CHANNEL_A        0x02    // Channel A
#define HAL_TIMER_CHANNEL_B        0x04    // Channel B
#define HAL_TIMER_CHANNEL_C        0x08    // Channel C
#define HAL_TIMER_CHANNEL_D        0x10    // Channel D
#define HAL_TIMER_CHANNEL_E        0x20    // Channel E
#define HAL_TIMER_CHANNEL_MASK    (HAL_TIMER_CHANNEL_SINGLE |  \
                                   HAL_TIMER_CHANNEL_A |       \
                                   HAL_TIMER_CHANNEL_B |       \
                                   HAL_TIMER_CHANNEL_C |       \
                                   HAL_TIMER_CHANNEL_D |       \
                                   HAL_TIMER_CHANNEL_E)
#define HAL_TIMER_CH_MAX      0x06

/* Timer 1 Channel 0 Channel compare mode definitions */
#define HAL_TIMER1_CH0_CMP_MODE_SET_ON_COMP            0x00    // 000: Set output on compare
#define HAL_TIMER1_CH0_CMP_MODE_CLR_ON_COMP            0x01    // 001: Clear output on compare
#define HAL_TIMER1_CH0_CMP_MODE_TOG_ON_COMP            0x02    // 010: Toggle output on compare
#define HAL_TIMER1_CH0_CMP_MODE_SET_ON_COMP_CLR_ON_0   0x03    // 011: Set output on compare-up, clear on 0
#define HAL_TIMER1_CH0_CMP_MODE_CLR_ON_COMP_SET_ON_0   0x04    // 100: Clear output on compare-up, set on 0
#define HAL_TIMER1_CH0_CMP_MODE_INIT                   0x07    // 111: Initialize output pin. CMP[2:0] is not changed

/* Timer 1 Channel 1-5 channel compare mode definitions */
#define HAL_TIMER1_CHn_CMP_MODE_SET_ON_COMP            0x00    // 000: Set output on compare                                                                   
#define HAL_TIMER1_CHn_CMP_MODE_CLR_ON_COMP            0x01    // 001: Clear output on compare                                                                 
#define HAL_TIMER1_CHn_CMP_MODE_TOG_ON_COMP            0x02    // 010: Toggle output on compare                                                                
#define HAL_TIMER1_CHn_CMP_MODE_SET_ON_COMP_CLR_ON_0   0x03    // 011: Set output on compare-up, clear on compare down in up-down mode. Otherwise set output on
                                                               // compare, clear on 0.                                                                         
#define HAL_TIMER1_CHn_CMP_MODE_CLR_ON_COMP_SET_ON_0   0x04    // 100: Clear output on compare-up, set on compare down in up-down mode. Otherwise clear output 
                                                               // on compare, set on 0                                                                         
#define HAL_TIMER1_CHn_CMP_MODE_CLR_ON_CH0_SET_ON_CHn  0x05    // 101: Clear when equal T1CC0, set when equal T1CC2                                            
#define HAL_TIMER1_CHn_CMP_MODE_SET_ON_CH0_CLR_ON_CHn  0x06    // 110: Set when equal T1CC0, clear when equal T1CC2                                            
#define HAL_TIMER1_CHn_CMP_MODE_INIT                   0x07    // 111: Initialize output pin. CMP[2:0] is not changed.                                         
#define HAL_TIMER1_CH_CMP_MODE_BITS                    0x38    // bits 5:3

/* Timer 1 Capture mode */
#define HAL_TIMER1_CH_CAP_MODE_NO      0x00 // 00: No capture             
#define HAL_TIMER1_CH_CAP_MODE_RISING  0x01 // 01: Capture on rising edge 
#define HAL_TIMER1_CH_CAP_MODE_FALLING 0x02 // 10: Capture on falling edge
#define HAL_TIMER1_CH_CAP_MODE_ALL     0x03 // 11: Capture on all edges   
#define HAL_TIMER1_CH_CAP_BITS         0x03 // bits 1:0
   
/* Channel mode definitions */
#define HAL_TIMER_CH_MODE_INPUT_CAPTURE   0x01    // Channel Mode Input-Capture
#define HAL_TIMER_CH_MODE_OUTPUT_COMPARE  0x02    // Channel Mode Output_Compare
#define HAL_TIMER_CH_MODE_OVERFLOW        0x04    // Channel Mode Overflow
#define HAL_TIMER_CH_MODE_MASK            (HAL_TIMER_CH_MODE_INPUT_CAPTURE |  \
                                           HAL_TIMER_CH_MODE_OUTPUT_COMPARE | \
                                           HAL_TIMER_CH_MODE_OVERFLOW)

/* Error Code */
#define HAL_TIMER_OK              0x00
#define HAL_TIMER_NOT_OK          0x01
#define HAL_TIMER_PARAMS_ERROR    0x02
#define HAL_TIMER_NOT_CONFIGURED  0x03
#define HAL_TIMER_INVALID_ID      0x04
#define HAL_TIMER_INVALID_CH_MODE 0x05
#define HAL_TIMER_INVALID_OP_MODE 0x06

/* Timer clock pre-scaler definitions for 16bit timer1 and timer3 */
#define HAL_TIMER3_16_TC_STOP    0x00  // No clock, timer stopped
#define HAL_TIMER3_16_TC_DIV1    0x01  // No clock pre-scaling
#define HAL_TIMER3_16_TC_DIV8    0x02  // Clock pre-scaled by 8
#define HAL_TIMER3_16_TC_DIV64   0x03  // Clock pre-scaled by 64
#define HAL_TIMER3_16_TC_DIV256  0x04  // Clock pre-scaled by 256
#define HAL_TIMER3_16_TC_DIV1024 0x05  // Clock pre-scaled by 1024
#define HAL_TIMER3_16_TC_EXTFE   0x06  // External clock (T2), falling edge
#define HAL_TIMER3_16_TC_EXTRE   0x07  // External clock (T2), rising edge

/* Timer clock pre-scaler definitions for 8bit timer0 and timer2 */
#define HAL_TIMER0_8_TC_STOP    0x00  // No clock, timer stopped
#define HAL_TIMER0_8_TC_DIV1    0x01  // No clock pre-scaling
#define HAL_TIMER0_8_TC_DIV8    0x02  // Clock pre-scaled by 8
#define HAL_TIMER0_8_TC_DIV32   0x03  // Clock pre-scaled by 32
#define HAL_TIMER0_8_TC_DIV64   0x04  // Clock pre-scaled by 64
#define HAL_TIMER0_8_TC_DIV128  0x05  // Clock pre-scaled by 128
#define HAL_TIMER0_8_TC_DIV256  0x06  // Clock pre-scaled by 256
#define HAL_TIMER0_8_TC_DIV1024 0x07  // Clock pre-scaled by 1024

/* Timer clock pre-scaler definitions for 8bit timer2 */
#define HAL_TIMER2_8_TC_STOP    0x00  // No clock, timer stopped
#define HAL_TIMER2_8_TC_DIV1    0x01  // No clock pre-scaling
#define HAL_TIMER2_8_TC_DIV8    0x02  // Clock pre-scaled by 8
#define HAL_TIMER2_8_TC_DIV64   0x03  // Clock pre-scaled by 32
#define HAL_TIMER2_8_TC_DIV256  0x04  // Clock pre-scaled by 64
#define HAL_TIMER2_8_TC_DIV1024 0x05  // Clock pre-scaled by 128
#define HAL_TIMER2_8_TC_EXTFE   0x06  // External clock (T2), falling edge
#define HAL_TIMER2_8_TC_EXTRE   0x07  // External clock (T2), rising edge


/***************************************************************************************************
 *                                             TYPEDEFS
 ***************************************************************************************************/
typedef void (*halTimerCBack_t) (uint8 timerId, uint8 channel, uint8 channelMode);

/***************************************************************************************************
 *                                         GLOBAL VARIABLES
 ***************************************************************************************************/


/***************************************************************************************************
 *                                          FUNCTIONS - API
 ***************************************************************************************************/

/*
 * Initialize Timer Service
 */
extern void HalTimerInit ( void );

/*
 * Configure channel in different modes
 */
extern uint8 HalTimerConfig ( uint8 timerId,
                              uint8 opMode,
                              uint8 channel,
                              uint8 channelMode,
                              bool intEnable,
                              halTimerCBack_t cback );

/*
 * Start a Timer
 */
extern uint8 HalTimerStart ( uint8 timerId, uint32 timePerTick );

/*
 * Stop a Timer
 */
extern uint8 HalTimerStop ( uint8 timerId );


/*
 * This is used for polling, provide the tick increment
 */
extern void HalTimerTick ( void );

/*
 * Enable and disable particular timer
 */
extern uint8 HalTimerInterruptEnable (uint8 timerId, uint8 channelMode, bool enable);

/*
 * Configures timer 1 to control 4 PWM outputs 
 */
void HalTimer1Init (halTimerCBack_t cBack);


/*
 * Set dutycycle on timer 1 PWM output channel
 */
void halTimer1SetChannelDuty (uint8 channel, uint16 promill);

/***************************************************************************************************
***************************************************************************************************/

#ifdef __cplusplus
}
#endif

#endif
