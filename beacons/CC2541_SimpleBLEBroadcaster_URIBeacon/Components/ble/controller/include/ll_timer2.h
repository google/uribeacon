/*******************************************************************************
  Filename:       ll_timer2.h
  Revised:        $Date: 2012-07-13 06:58:11 -0700 (Fri, 13 Jul 2012) $
  Revision:       $Revision: 30922 $

  Description:    This file contains the Bluetooth Low Energy (BLE) Link
                  Layer (LL) software timer management software for Timer2.

  Copyright 2009-2011 Texas Instruments Incorporated. All rights reserved.

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
*******************************************************************************/

#ifndef LL_TIMER2_H
#define LL_TIMER2_H

#ifdef __cplusplus
extern "C"
{
#endif

/*******************************************************************************
 * INCLUDES
 */

/*******************************************************************************
 * MACROS
 */

#define llDisableT2Event1()    (T2CSPCFG |= LL_T2_DISABLE_EVENT1;)
#define llDisableT2Event2()    (T2CSPCFG |= LL_T2_DISABLE_EVENT2;)
#define llAddTime( time, val ) (((time) + (val)) & LL_T2OVF_PERIOD_VALUE)
#define llSubTime( time, val ) (((time) - (val)) & LL_T2OVF_PERIOD_VALUE)

/*******************************************************************************
 * CONSTANTS
 */

// Timer 2 Control Register
#define LL_T2_CTRL_INIT_NO_SYNCH  0x08
#define LL_T2_CTRL_INIT_SYNCH     0x0A

/*
** Timer 2 Multiplex Select
*/
#define LL_T2_TIMER_SEL           0x00
#define LL_T2_CAPTURE_SEL         0x01
#define LL_T2_PERIOD_SEL          0x02
#define LL_T2_COMPARE1_SEL        0x03
#define LL_T2_COMPARE2_SEL        0x04
//
#define LL_T2OVF_TIMER_SEL        0x00
#define LL_T2OVF_CAPTURE_SEL      0x10
#define LL_T2OVF_PERIOD_SEL       0x20
#define LL_T2OVF_COMPARE1_SEL     0x30
#define LL_T2OVF_COMPARE2_SEL     0x40

/*
** Timer 2 Interrupt Masks and Flags
*/
#define LL_T2_PERIOD_FLAG         0x01
#define LL_T2_COMPARE1_FLAG       0x02
#define LL_T2_COMPARE2_FLAG       0x04
//
#define LL_T2OVF_PERIOD_FLAG      0x08
#define LL_T2OVF_COMPARE1_FLAG    0x10
#define LL_T2OVF_COMPARE2_FLAG    0x20
//
#if defined( CC2541) || defined( CC2541S ) // CC2541_T2
#define LL_T2_LONG_COMPARE1_FLAG  0x40
#define LL_T2_LONG_COMPARE2_FLAG  0x80
#endif // CC2541 || CC2541S

/*
** Timer 2 Interrupt Masks
*/
#define LL_T2_PERIOD_MASK         0x01
#define LL_T2_COMPARE1_MASK       0x02
#define LL_T2_COMPARE2_MASK       0x04
//
#define LL_T2OVF_PERIOD_MASK      0x08
#define LL_T2OVF_COMPARE1_MASK    0x10
#define LL_T2OVF_COMPARE2_MASK    0x20
//
#if defined( CC2541) || defined( CC2541S ) // CC2541_T2
#define LL_T2_LONG_COMPARE1_MASK  0x40
#define LL_T2_LONG_COMPARE2_MASK  0x80
#endif // CC2541 || CC2541S

/*
** Timer 2 Event Configuration
*/
#define LL_T2_PERIOD_EVENT1       0x00
#define LL_T2_COMPARE1_EVENT1     0x01
#define LL_T2_COMPARE2_EVENT1     0x02
//
#define LL_T2OVF_PERIOD_EVENT1    0x03
#define LL_T2OVF_COMPARE1_EVENT1  0x04
#define LL_T2OVF_COMPARE2_EVENT1  0x05
//
#if defined( CC2541) || defined( CC2541S ) // CC2541_T2
#define LL_T2OVF_LONG_CMP1_EVENT1 0x08
#define LL_T2OVF_LONG_CMP2_EVENT1 0x09
#define LL_T2_DISABLE_EVENT1      0x0F
#else // CC2540
#define LL_T2_DISABLE_EVENT1      0x07
#endif // CC2541 || CC2541S
//
#define LL_T2_PERIOD_EVENT2       0x00
#define LL_T2_COMPARE1_EVENT2     0x10
#define LL_T2_COMPARE2_EVENT2     0x20
//
#define LL_T2OVF_PERIOD_EVENT2    0x30
#define LL_T2OVF_COMPARE1_EVENT2  0x40
#define LL_T2OVF_COMPARE2_EVENT2  0x50
//
#if defined( CC2541) || defined( CC2541S ) // CC2541_T2
#define LL_T2OVF_LONG_CMP1_EVENT2 0x80
#define LL_T2OVF_LONG_CMP2_EVENT2 0x90
#define LL_T2_DISABLE_EVENT2      0xF0
#else // CC2540
#define LL_T2_DISABLE_EVENT2      0x70
#endif // CC2541 || CC2541S

/*
** Timer 2 Miscellanous
*/

#define LL_T2_RUN_BIT             0x01        // 0: stop timer; 1: run timer
#define LL_T2_SYNCH_BIT           0x02        // 0: start/stop immediate; 1: start/stop synch with 32kHz
#define LL_T2_STATE_BIT           0x04
#define LL_T2_PERIOD_VALUE        0x4E20      // 20K 32MHz ticks = 625us
#define LL_T2_PERIOD_1US_VALUE    0x20        // 1us in 31.25ns ticks
#define LL_T2OVF_PERIOD_VALUE     0x00FFFFFF  // full 24 bit range, or 2.913 hours
#define LL_T2_MAX_VALUE           0x01000000  // full 24 bit range

/*******************************************************************************
 * TYPEDEFS
 */

/*
** Anchor Point
*/
typedef struct
{
  uint32 coarse;                       // number of 625us ticks at SFD capture
  uint16 fine;                         // number of 31.25ns ticks at SFD capture
} sysTime_t;

/*******************************************************************************
 * LOCAL VARIABLES
 */

/*******************************************************************************
 * GLOBAL VARIABLES
 */

/*******************************************************************************
 * FUNCTIONS
 */

extern void   llInitTimer2( void );
extern void   llStopTimer2( void );
extern void   llStartTimer2( void );
extern void   llSetT2C1Timeout( uint16 timeout );
extern void   llSetT2C2Timeout( uint16 timeout );
extern void   llSetT2OVFC1Timeout( uint32 timeout );
extern void   llSetT2OVFC2Timeout( uint32 timeout );
extern void   llSetT2LongC1Timeout( uint32 timeout1, uint16 timeout2 );
extern void   llSetT2LongC2Timeout( uint32 timeout1, uint16 timeout2 );
extern void   llSetT2C1Event1( uint16 timeout );
extern void   llSetT2OVFC1Event1( uint32 timeout );
extern void   llSetT2LongC1Event1( uint32 timeout1, uint16 timeout2 );
extern void   llSetT2OVFC2Event2( uint32 timeout );
extern void   llSetT2Delta( uint16 count );
extern void   llDisableT2Ints( void );
extern void   llDisableT2IntsEvts( void );
extern void   llDisableT2E1( void );
extern void   llGetTimer2Capture( uint32 *coarseTime, uint16 *fineTime );
extern uint32 llGetCurrentTime( void );
extern uint16 llGetCurrentFineTime( void );
extern void   llGetFullCurrentTime( uint32 *coarse, uint16 *fine );
//extern void   llGetFullCTandST( uint32 *coarseTime, uint16 *fineTime, uint32 *sleepTime );
extern void   llGetFullCTandST( sysTime_t *curTime, uint32 *sleepTime );
extern uint32 llGetT2OVFC1( void );
extern uint32 llGetT2OVFC2( void );
extern uint16 ll_McuPrecisionCount( void );
//
extern NEAR_FUNC uint8  ll24BitTimeCompare( uint32 time1, uint32 time2 );
extern NEAR_FUNC uint32 ll24BitTimeDelta( uint32 time1, uint32 time2 );
extern NEAR_FUNC uint32 ll24BitAbsTimeDelta( uint32 time1, uint32 time2 );
extern NEAR_FUNC void   llAdjustTimeForward( sysTime_t *curTime, sysTime_t *adjTime );
extern NEAR_FUNC void   llAdjustTimeBackward( sysTime_t *curTime, sysTime_t *adjTime );

#ifdef __cplusplus
}
#endif

#endif /* LL_TIMER2_H */
