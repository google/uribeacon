/*******************************************************************************
  Filename:       ll_sleep.h
  Revised:        $Date: 2013-02-27 09:25:28 -0800 (Wed, 27 Feb 2013) $
  Revision:       $Revision: 33310 $

  Description:    This file contains the Link Layer (LL) types, constants,
                  API's etc. for Sleep Management for the Bluetooth Low Energy
                  (ULE) Controller

  Copyright 2009-2013 Texas Instruments Incorporated. All rights reserved.

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

#ifndef LL_SLEEP_H
#define LL_SLEEP_H

#ifdef __cplusplus
extern "C"
{
#endif

/*******************************************************************************
 * INCLUDES
 */
#include "bcomdef.h"

/*******************************************************************************
 * MACROS
 */

/*******************************************************************************
 * CONSTANTS
 */

// NanoRisc Execution Mode
#define LL_NR_STATE_IDLE                     0
#define LL_NR_STATE_RUNNING                  1

// Sleep Management
#define LL_SLEEP_REQUEST_ALLOWED             0
#define LL_SLEEP_REQUEST_DENIED              1

/*******************************************************************************
 * TYPEDEFS
 */

/*******************************************************************************
 * LOCAL VARIABLES
 */

/*******************************************************************************
 * GLOBAL VARIABLES
 */

extern volatile DATA unsigned char phyState;
extern volatile DATA unsigned char clkState;

/*******************************************************************************
 * API
 */

extern NEAR_FUNC void HaltMcuUsingPhyFlag(void);
//
extern NEAR_FUNC void  LL_TimeToNextRfEvent( uint32 *sleepTimer, uint32 *timeout );
extern NEAR_FUNC uint8 LL_PowerOffReq( uint8 mode );
extern NEAR_FUNC void  LL_PowerOnReq( uint8 wakeFromPM3, uint8 wakeForRF );

#ifdef __cplusplus
}
#endif

#endif /* LL_SLEEP_H */

