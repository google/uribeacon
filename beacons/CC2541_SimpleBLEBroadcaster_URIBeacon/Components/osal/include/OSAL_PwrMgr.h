/**************************************************************************************************
  Filename:       OSAL_PwrMgr.h
  Revised:        $Date: 2007-10-28 18:41:49 -0700 (Sun, 28 Oct 2007) $
  Revision:       $Revision: 15799 $

  Description:    This file contains the OSAL Power Management API.


  Copyright 2004-2007 Texas Instruments Incorporated. All rights reserved.

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

#ifndef OSAL_PWRMGR_H
#define OSAL_PWRMGR_H

#ifdef __cplusplus
extern "C"
{
#endif

/*********************************************************************
 * INCLUDES
 */
 
/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * TYPEDEFS
 */

/* These attributes define sleep beheaver. The attributes can be changed
 * for each sleep cycle or when the device characteristic change.
 */
typedef struct
{
  uint16 pwrmgr_task_state;
  uint16 pwrmgr_next_timeout;
  uint16 accumulated_sleep_time;
  uint8  pwrmgr_device;
} pwrmgr_attribute_t;

/* With PWRMGR_ALWAYS_ON selection, there is no power savings and the
 * device is most likely on mains power. The PWRMGR_BATTERY selection allows
 * the HAL sleep manager to enter SLEEP LITE state or SLEEP DEEP state.
 */
#define PWRMGR_ALWAYS_ON  0
#define PWRMGR_BATTERY    1

/* The PWRMGR_CONSERVE selection turns power savings on, all tasks have to
 * agree. The PWRMGR_HOLD selection turns power savings off.
 */
#define PWRMGR_CONSERVE 0
#define PWRMGR_HOLD     1


/*********************************************************************
 * GLOBAL VARIABLES
 */

/* This global variable stores the power management attributes.
 */
extern pwrmgr_attribute_t pwrmgr_attribute;

/*********************************************************************
 * FUNCTIONS
 */

  /*
   * Initialize the power management system.
   *   This function is called from OSAL.
   *
   */
  extern void osal_pwrmgr_init( void );

  /*
   * This function is called by each task to state whether or not this
   * task wants to conserve power. The task will call this function to
   * vote whether it wants the OSAL to conserve power or it wants to
   * hold off on the power savings. By default, when a task is created,
   * its own power state is set to conserve. If the task always wants
   * to converse power, it doesn't need to call this function at all.
   * It is important for the task that changed the power manager task
   * state to PWRMGR_HOLD to switch back to PWRMGR_CONSERVE when the
   * hold period ends.
   */
  extern uint8 osal_pwrmgr_task_state( uint8 task_id, uint8 state );

  /*
   * This function is called on power-up, whenever the device characteristic
   * change (ex. Battery backed coordinator). This function works with the timer
   * to set HAL's power manager sleep state when power saving is entered.
   * This function should be called form HAL initialization. After power up
   * initialization, it should only be called from NWK or ZDO.
   */
  extern void osal_pwrmgr_device( uint8 pwrmgr_device );

  /*
   * This function is called from the main OSAL loop when there are
   * no events scheduled and shouldn't be called from anywhere else.
   */
  extern void osal_pwrmgr_powerconserve( void );

/*********************************************************************
*********************************************************************/

#ifdef __cplusplus
}
#endif

#endif /* OSAL_PWRMGR_H */
