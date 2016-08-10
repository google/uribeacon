/**************************************************************************************************
  Filename:       OSAL_pwrmgr.c
  Revised:        $Date: 2011-09-16 19:09:24 -0700 (Fri, 16 Sep 2011) $
  Revision:       $Revision: 27618 $

  Description:    This file contains the OSAL Power Management API.


  Copyright 2004-2011 Texas Instruments Incorporated. All rights reserved.

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

/*********************************************************************
 * INCLUDES
 */

#include "comdef.h"
#include "OnBoard.h"
#include "OSAL.h"
#include "OSAL_Tasks.h"
#include "OSAL_Timers.h"
#include "OSAL_PwrMgr.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * GLOBAL VARIABLES
 */

/* This global variable stores the power management attributes.
 */
pwrmgr_attribute_t pwrmgr_attribute;

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */

/*********************************************************************
 * FUNCTIONS
 *********************************************************************/

/*********************************************************************
 * @fn      osal_pwrmgr_init
 *
 * @brief   Initialize the power management system.
 *
 * @param   none.
 *
 * @return  none.
 */
void osal_pwrmgr_init( void )
{
  pwrmgr_attribute.pwrmgr_device = PWRMGR_ALWAYS_ON; // Default to no power conservation.
  pwrmgr_attribute.pwrmgr_task_state = 0;            // Cleared.  All set to conserve
}

/*********************************************************************
 * @fn      osal_pwrmgr_device
 *
 * @brief   Sets the device power characteristic.
 *
 * @param   pwrmgr_device - type of power devices. With PWRMGR_ALWAYS_ON
 *          selection, there is no power savings and the device is most
 *          likely on mains power. The PWRMGR_BATTERY selection allows the
 *          HAL sleep manager to enter sleep.
 *
 * @return  none
 */
void osal_pwrmgr_device( uint8 pwrmgr_device )
{
  pwrmgr_attribute.pwrmgr_device = pwrmgr_device;
}

/*********************************************************************
 * @fn      osal_pwrmgr_task_state
 *
 * @brief   This function is called by each task to state whether or
 *          not this task wants to conserve power.
 *
 * @param   task_id - calling task ID.
 *          state - whether the calling task wants to
 *          conserve power or not.
 *
 * @return  SUCCESS if task complete
 */
uint8 osal_pwrmgr_task_state( uint8 task_id, uint8 state )
{
  if ( task_id >= tasksCnt )
    return ( INVALID_TASK );

  if ( state == PWRMGR_CONSERVE )
  {
    // Clear the task state flag
    pwrmgr_attribute.pwrmgr_task_state &= ~(1 << task_id );
  }
  else
  {
    // Set the task state flag
    pwrmgr_attribute.pwrmgr_task_state |= (1 << task_id);
  }

  return ( SUCCESS );
}

#if defined( POWER_SAVING )
/*********************************************************************
 * @fn      osal_pwrmgr_powerconserve
 *
 * @brief   This function is called from the main OSAL loop when there are
 *          no events scheduled and shouldn't be called from anywhere else.
 *
 * @param   none.
 *
 * @return  none.
 */
void osal_pwrmgr_powerconserve( void )
{
  uint32        next;
  halIntState_t intState;

  // Should we even look into power conservation
  if ( pwrmgr_attribute.pwrmgr_device != PWRMGR_ALWAYS_ON )
  {
    // Are all tasks in agreement to conserve
    if ( pwrmgr_attribute.pwrmgr_task_state == 0 )
    {
      // Hold off interrupts.
      HAL_ENTER_CRITICAL_SECTION( intState );

      // Get next time-out
      next = osal_next_timeout();

      // Re-enable interrupts.
      HAL_EXIT_CRITICAL_SECTION( intState );

      // Put the processor into sleep mode
      OSAL_SET_CPU_INTO_SLEEP( next );
    }
  }
}
#endif /* POWER_SAVING */

/*********************************************************************
*********************************************************************/
