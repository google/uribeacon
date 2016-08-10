/*************************************************************************************************
  Filename:       osal_cbtimer.c
  Revised:        $Date: 2009-01-14 14:51:34 -0800 (Wed, 14 Jan 2009) $
  Revision:       $Revision: 18762 $

  Description:    This file contains the Callback Timer task(s). This module
                  provides 'callback' timers using the existing 'event' timers.
                  In other words, the registered callback function is called 
                  instead of an OSAL event being sent to the owner of the timer
                  when it expires.


  Copyright 2008-2009 Texas Instruments Incorporated. All rights reserved.

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
  PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, 
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

#include "OSAL.h"
#include "OSAL_Tasks.h"

#include "osal_cbtimer.h"

/*********************************************************************
 * MACROS
 */
// Macros to convert one-dimensional index 'timer id' to two-dimensional indices
// 'task id' and 'event id'.

// Find out event id using timer id
#define EVENT_ID( timerId )            ( 0x0001 << ( ( timerId ) % NUM_CBTIMERS_PER_TASK ) )

// Find out task id using timer id
#define TASK_ID( timerId )             ( ( ( timerId ) / NUM_CBTIMERS_PER_TASK ) + baseTaskID )

// Find out bank task id using task id
#define BANK_TASK_ID( taskId )         ( ( baseTaskID - ( taskId ) ) * NUM_CBTIMERS )

/*********************************************************************
 * CONSTANTS
 */
// Number of callback timers supported per task (limited by the number of OSAL event timers)
#define NUM_CBTIMERS_PER_TASK          15

// Total number of callback timers
#define NUM_CBTIMERS                   ( OSAL_CBTIMER_NUM_TASKS * NUM_CBTIMERS_PER_TASK )

/*********************************************************************
 * TYPEDEFS
 */
// Callback Timer structure
typedef struct
{
  pfnCbTimer_t pfnCbTimer; // callback function to be called when timer expires
  uint8 *pData;            // data to be passed in to callback function
} cbTimer_t;

/*********************************************************************
 * GLOBAL VARIABLES
 */
// Callback Timer base task id
uint16 baseTaskID = TASK_NO_TASK;

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */
// Callback Timers table.
#if ( NUM_CBTIMERS > 0 )
  cbTimer_t cbTimers[NUM_CBTIMERS];
#endif

/*********************************************************************
 * LOCAL FUNCTIONS
 */

/*********************************************************************
 * API FUNCTIONS
 */

/*********************************************************************
 * @fn          osal_CbTimerInit
 *
 * @brief       Callback Timer task initialization function. This function
 *              can be called more than once (OSAL_CBTIMER_NUM_TASKS times).
 *
 * @param       taskId - Message Timer task ID.
 *
 * @return      void
 */
void osal_CbTimerInit( uint8 taskId )
{
  if ( baseTaskID == TASK_NO_TASK )
  {
    // Only initialize the base task id
    baseTaskID = taskId;

    // Initialize all timer structures
    osal_memset( cbTimers, 0, sizeof( cbTimers ) );
  }
}

/*********************************************************************
 * @fn          osal_CbTimerProcessEvent
 *
 * @brief       Callback Timer task event processing function.
 *
 * @param       taskId - task ID.
 * @param       events - events.
 *
 * @return      events not processed
 */
uint16 osal_CbTimerProcessEvent( uint8 taskId, uint16 events )
{
  if ( events & SYS_EVENT_MSG )
  {
    // Process OSAL messages

    // return unprocessed events
    return ( events ^ SYS_EVENT_MSG );
  }

  if ( events )
  {
    uint8 i;
    uint16 event;

    // Process event timers
    for ( i = 0; i < NUM_CBTIMERS_PER_TASK; i++ )
    {
      if ( ( events >> i ) & 0x0001 )
      {
        cbTimer_t *pTimer = &cbTimers[BANK_TASK_ID( taskId )+i];

        // Found the first event
        event =  0x0001 << i;

        // Timer expired, call the registered callback function
        pTimer->pfnCbTimer( pTimer->pData );

        // Mark entry as free
        pTimer->pfnCbTimer = NULL;
        
        // Null out data pointer
        pTimer->pData = NULL;
      
        // We only process one event at a time
        break;
      }
    }

    // return unprocessed events
    return ( events ^ event );
  }

  // If reach here, the events are unknown
  // Discard or make more handlers
  return 0;
}

/*********************************************************************
 * @fn      osal_CbTimerStart
 *
 * @brief   This function is called to start a callback timer to expire 
 *          in n mSecs. When the timer expires, the registered callback
 *          function will be called.
 *
 * @param   pfnCbTimer - callback function to be called when timer expires
 * @param   pData - data to be passed in to callback function
 * @param   timeout - in milliseconds.
 * @param   pTimerId - will point to new timer Id (if not null)
 *
 * @return  Success, or Failure.
 */
Status_t osal_CbTimerStart( pfnCbTimer_t pfnCbTimer, uint8 *pData,  
                           uint16 timeout, uint8 *pTimerId )
{
  uint8 i;
 
  // Validate input parameters
  if ( pfnCbTimer == NULL )
  {
    return ( INVALIDPARAMETER );
  }

  // Look for an unused timer first
  for ( i = 0; i < NUM_CBTIMERS; i++ )
  {
    if ( cbTimers[i].pfnCbTimer == NULL )
    {
      // Start the OSAL event timer first
      if ( osal_start_timerEx( TASK_ID( i ), EVENT_ID( i ), timeout ) == SUCCESS )
      {
        // Set up the callback timer
        cbTimers[i].pfnCbTimer = pfnCbTimer;
        cbTimers[i].pData = pData;

        if ( pTimerId != NULL )
        {
          // Caller is intreseted in the timer id
          *pTimerId = i;
        }

        return ( SUCCESS );
      }
    }
  }

  // No timer available
  return ( NO_TIMER_AVAIL );
}

/*********************************************************************
 * @fn      osal_CbTimerUpdate
 *
 * @brief   This function is called to update a message timer that has
 *          already been started. If SUCCESS, the function will update
 *          the timer's timeout value. If INVALIDPARAMETER, the timer 
 *          either doesn't exit.
 *
 * @param   timerId - identifier of the timer that is to be updated
 * @param   timeout - new timeout in milliseconds.
 *
 * @return  SUCCESS or INVALIDPARAMETER if timer not found
 */
Status_t osal_CbTimerUpdate( uint8 timerId, uint16 timeout )
{
  // Look for the existing timer
  if ( timerId < NUM_CBTIMERS )
  {
    if ( cbTimers[timerId].pfnCbTimer != NULL )
    {
      // Make sure the corresponding OSAL event timer is still running
      if ( osal_get_timeoutEx( TASK_ID( timerId ), EVENT_ID( timerId ) ) != 0 )
      {
        // Timer exists; update it
        osal_start_timerEx( TASK_ID( timerId ), EVENT_ID( timerId ), timeout );

        return (  SUCCESS );
      }
    }
  }

  // Timer not found
  return ( INVALIDPARAMETER );
}


/*********************************************************************
 * @fn      osal_CbTimerStop
 *
 * @brief   This function is called to stop a timer that has already been
 *          started. If SUCCESS, the function will cancel the timer. If 
 *          INVALIDPARAMETER, the timer doesn't exit.
 *
 * @param   timerId - identifier of the timer that is to be stopped
 *
 * @return  SUCCESS or INVALIDPARAMETER if timer not found
 */
Status_t osal_CbTimerStop( uint8 timerId )
{
  // Look for the existing timer
  if ( timerId < NUM_CBTIMERS )
  {
    if ( cbTimers[timerId].pfnCbTimer != NULL )
    {
      // Timer exists; stop the OSAL event timer first
      osal_stop_timerEx( TASK_ID( timerId ), EVENT_ID( timerId ) );

      // Mark entry as free
      cbTimers[timerId].pfnCbTimer = NULL;

      // Null out data pointer
      cbTimers[timerId].pData = NULL;

      return ( SUCCESS );
    }
  }

  // Timer not found
  return ( INVALIDPARAMETER );
}

/****************************************************************************
****************************************************************************/
