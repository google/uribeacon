/**************************************************************************************************
  Filename:       OSAL_Timers.c
  Revised:        $Date: 2012-11-28 00:37:02 -0800 (Wed, 28 Nov 2012) $
  Revision:       $Revision: 32329 $

  Description:    OSAL Timer definition and manipulation functions.


  Copyright 2004-2012 Texas Instruments Incorporated. All rights reserved.

  IMPORTANT: Your use of this Software is limited to those specific rights
  granted under the terms of a software license agreement between the user
  who downloaded the software, his/her employer (which must be your employer)
  and Texas Instruments Incorporated (the "License"). You may not use this
  Software unless you agree to abide by the terms of the License. The License
  limits your use, and you acknowledge, that the Software may not be modified,
  copied or distributed unless embedded on a Texas Instruments microcontroller
  or used solely and exclusively in conjunction with a Texas Instruments radio
  frequency transceiver, which is integrated into your product. Other than for
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
#include "OSAL_Timers.h"
#include "hal_timer.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * TYPEDEFS
 */

typedef union {
  uint32 time32;
  uint16 time16[2];
  uint8 time8[4];
} osalTime_t;

typedef struct
{
  void   *next;
  osalTime_t timeout;
  uint16 event_flag;
  uint8  task_id;
  uint32 reloadTimeout;
} osalTimerRec_t;

/*********************************************************************
 * GLOBAL VARIABLES
 */

osalTimerRec_t *timerHead;

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */
// Milliseconds since last reboot
static uint32 osal_systemClock;

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */
osalTimerRec_t  *osalAddTimer( uint8 task_id, uint16 event_flag, uint32 timeout );
osalTimerRec_t *osalFindTimer( uint8 task_id, uint16 event_flag );
void osalDeleteTimer( osalTimerRec_t *rmTimer );

/*********************************************************************
 * FUNCTIONS
 *********************************************************************/

/*********************************************************************
 * @fn      osalTimerInit
 *
 * @brief   Initialization for the OSAL Timer System.
 *
 * @param   none
 *
 * @return
 */
void osalTimerInit( void )
{
  osal_systemClock = 0;
}

/*********************************************************************
 * @fn      osalAddTimer
 *
 * @brief   Add a timer to the timer list.
 *          Ints must be disabled.
 *
 * @param   task_id
 * @param   event_flag
 * @param   timeout
 *
 * @return  osalTimerRec_t * - pointer to newly created timer
 */
osalTimerRec_t * osalAddTimer( uint8 task_id, uint16 event_flag, uint32 timeout )
{
  osalTimerRec_t *newTimer;
  osalTimerRec_t *srchTimer;

  // Look for an existing timer first
  newTimer = osalFindTimer( task_id, event_flag );
  if ( newTimer )
  {
    // Timer is found - update it.
    newTimer->timeout.time32 = timeout;

    return ( newTimer );
  }
  else
  {
    // New Timer
    newTimer = osal_mem_alloc( sizeof( osalTimerRec_t ) );

    if ( newTimer )
    {
      // Fill in new timer
      newTimer->task_id = task_id;
      newTimer->event_flag = event_flag;
      newTimer->timeout.time32 = timeout;
      newTimer->next = (void *)NULL;
      newTimer->reloadTimeout = 0;

      // Does the timer list already exist
      if ( timerHead == NULL )
      {
        // Start task list
        timerHead = newTimer;
      }
      else
      {
        // Add it to the end of the timer list
        srchTimer = timerHead;

        // Stop at the last record
        while ( srchTimer->next )
          srchTimer = srchTimer->next;

        // Add to the list
        srchTimer->next = newTimer;
      }

      return ( newTimer );
    }
    else
    {
      return ( (osalTimerRec_t *)NULL );
    }
  }
}

/*********************************************************************
 * @fn      osalFindTimer
 *
 * @brief   Find a timer in a timer list.
 *          Ints must be disabled.
 *
 * @param   task_id
 * @param   event_flag
 *
 * @return  osalTimerRec_t *
 */
osalTimerRec_t *osalFindTimer( uint8 task_id, uint16 event_flag )
{
  osalTimerRec_t *srchTimer;

  // Head of the timer list
  srchTimer = timerHead;

  // Stop when found or at the end
  while ( srchTimer )
  {
    if ( srchTimer->event_flag == event_flag &&
         srchTimer->task_id == task_id )
    {
      break;
    }

    // Not this one, check another
    srchTimer = srchTimer->next;
  }

  return ( srchTimer );
}

/*********************************************************************
 * @fn      osalDeleteTimer
 *
 * @brief   Delete a timer from a timer list.
 *
 * @param   table
 * @param   rmTimer
 *
 * @return  none
 */
void osalDeleteTimer( osalTimerRec_t *rmTimer )
{
  // Does the timer list really exist
  if ( rmTimer )
  {
    // Clear the event flag and osalTimerUpdate() will delete
    // the timer from the list.
    rmTimer->event_flag = 0;
  }
}

/*********************************************************************
 * @fn      osal_start_timerEx
 *
 * @brief
 *
 *   This function is called to start a timer to expire in n mSecs.
 *   When the timer expires, the calling task will get the specified event.
 *
 * @param   uint8 taskID - task id to set timer for
 * @param   uint16 event_id - event to be notified with
 * @param   uint32 timeout_value - in milliseconds.
 *
 * @return  SUCCESS, or NO_TIMER_AVAIL.
 */
uint8 osal_start_timerEx( uint8 taskID, uint16 event_id, uint32 timeout_value )
{
  halIntState_t intState;
  osalTimerRec_t *newTimer;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

  // Add timer
  newTimer = osalAddTimer( taskID, event_id, timeout_value );

  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  return ( (newTimer != NULL) ? SUCCESS : NO_TIMER_AVAIL );
}

/*********************************************************************
 * @fn      osal_start_reload_timer
 *
 * @brief
 *
 *   This function is called to start a timer to expire in n mSecs.
 *   When the timer expires, the calling task will get the specified event
 *   and the timer will be reloaded with the timeout value.
 *
 * @param   uint8 taskID - task id to set timer for
 * @param   uint16 event_id - event to be notified with
 * @param   UNINT16 timeout_value - in milliseconds.
 *
 * @return  SUCCESS, or NO_TIMER_AVAIL.
 */
uint8 osal_start_reload_timer( uint8 taskID, uint16 event_id, uint32 timeout_value )
{
  halIntState_t intState;
  osalTimerRec_t *newTimer;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

  // Add timer
  newTimer = osalAddTimer( taskID, event_id, timeout_value );
  if ( newTimer )
  {
    // Load the reload timeout value
    newTimer->reloadTimeout = timeout_value;
  }

  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  return ( (newTimer != NULL) ? SUCCESS : NO_TIMER_AVAIL );
}

/*********************************************************************
 * @fn      osal_stop_timerEx
 *
 * @brief
 *
 *   This function is called to stop a timer that has already been started.
 *   If ZSUCCESS, the function will cancel the timer and prevent the event
 *   associated with the timer from being set for the calling task.
 *
 * @param   uint8 task_id - task id of timer to stop
 * @param   uint16 event_id - identifier of the timer that is to be stopped
 *
 * @return  SUCCESS or INVALID_EVENT_ID
 */
uint8 osal_stop_timerEx( uint8 task_id, uint16 event_id )
{
  halIntState_t intState;
  osalTimerRec_t *foundTimer;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

  // Find the timer to stop
  foundTimer = osalFindTimer( task_id, event_id );
  if ( foundTimer )
  {
    osalDeleteTimer( foundTimer );
  }

  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  return ( (foundTimer != NULL) ? SUCCESS : INVALID_EVENT_ID );
}

/*********************************************************************
 * @fn      osal_get_timeoutEx
 *
 * @brief
 *
 * @param   uint8 task_id - task id of timer to check
 * @param   uint16 event_id - identifier of timer to be checked
 *
 * @return  Return the timer's tick count if found, zero otherwise.
 */
uint32 osal_get_timeoutEx( uint8 task_id, uint16 event_id )
{
  halIntState_t intState;
  uint32 rtrn = 0;
  osalTimerRec_t *tmr;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

  tmr = osalFindTimer( task_id, event_id );

  if ( tmr )
  {
    rtrn = tmr->timeout.time32;
  }

  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  return rtrn;
}

/*********************************************************************
 * @fn      osal_timer_num_active
 *
 * @brief
 *
 *   This function counts the number of active timers.
 *
 * @return  uint8 - number of timers
 */
uint8 osal_timer_num_active( void )
{
  halIntState_t intState;
  uint8 num_timers = 0;
  osalTimerRec_t *srchTimer;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

  // Head of the timer list
  srchTimer = timerHead;

  // Count timers in the list
  while ( srchTimer != NULL )
  {
    num_timers++;
    srchTimer = srchTimer->next;
  }

  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  return num_timers;
}

/*********************************************************************
 * @fn      osalTimerUpdate
 *
 * @brief   Update the timer structures for a timer tick.
 *
 * @param   none
 *
 * @return  none
 *********************************************************************/
void osalTimerUpdate( uint32 updateTime )
{
  halIntState_t intState;
  osalTimerRec_t *srchTimer;
  osalTimerRec_t *prevTimer;

  osalTime_t timeUnion;
  timeUnion.time32 = updateTime;

  HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.
  // Update the system time
  osal_systemClock += updateTime;
  HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

  // Look for open timer slot
  if ( timerHead != NULL )
  {
    // Add it to the end of the timer list
    srchTimer = timerHead;
    prevTimer = (void *)NULL;

    // Look for open timer slot
    while ( srchTimer )
    {
      osalTimerRec_t *freeTimer = NULL;

      HAL_ENTER_CRITICAL_SECTION( intState );  // Hold off interrupts.

      // To minimize time in this critical section, avoid 32-bit math
      if ((timeUnion.time16[1] == 0) && (timeUnion.time8[1] == 0))
      {
        // If upper 24 bits are zero, check lower 8 bits for roll over
        if (srchTimer->timeout.time8[0] >= timeUnion.time8[0])
        {
          // 8-bit math
          srchTimer->timeout.time8[0] -= timeUnion.time8[0];
        }
        else
        {
          // 32-bit math
          if (srchTimer->timeout.time32 > timeUnion.time32)
          {
            srchTimer->timeout.time32 -= timeUnion.time32;
          }
          else
          {
            srchTimer->timeout.time32 = 0;
          }
        }
      }
      else
      {
          // 32-bit math
        if (srchTimer->timeout.time32 > timeUnion.time32)
        {
          srchTimer->timeout.time32 -= timeUnion.time32;
        }
        else
        {
          srchTimer->timeout.time32 = 0;
        }
      }

      // Check for reloading
      if ( (srchTimer->timeout.time16[0] == 0) && (srchTimer->timeout.time16[1] == 0) &&
           (srchTimer->reloadTimeout) && (srchTimer->event_flag) )
      {
        // Notify the task of a timeout
        osal_set_event( srchTimer->task_id, srchTimer->event_flag );

        // Reload the timer timeout value
        srchTimer->timeout.time32 = srchTimer->reloadTimeout;
      }

      // When timeout or delete (event_flag == 0)
      if ( ((srchTimer->timeout.time16[0] == 0) && (srchTimer->timeout.time16[1] == 0)) ||
            (srchTimer->event_flag == 0) )
      {
        // Take out of list
        if ( prevTimer == NULL )
        {
          timerHead = srchTimer->next;
        }
        else
        {
          prevTimer->next = srchTimer->next;
        }

        // Setup to free memory
        freeTimer = srchTimer;

        // Next
        srchTimer = srchTimer->next;
      }
      else
      {
        // Get next
        prevTimer = srchTimer;
        srchTimer = srchTimer->next;
      }

      HAL_EXIT_CRITICAL_SECTION( intState );   // Re-enable interrupts.

      if ( freeTimer )
      {
        if ( (freeTimer->timeout.time16[0] == 0) && (freeTimer->timeout.time16[1] == 0) )
        {
          osal_set_event( freeTimer->task_id, freeTimer->event_flag );
        }
        osal_mem_free( freeTimer );
      }
    }
  }
}

#ifdef POWER_SAVING
/*********************************************************************
 * @fn      osal_adjust_timers
 *
 * @brief   Update the timer structures for elapsed ticks.
 *
 * @param   none
 *
 * @return  none
 *********************************************************************/
void osal_adjust_timers( void )
{
  uint32 eTime;

  if ( timerHead != NULL )
  {
    // Compute elapsed time (msec)
    eTime = TimerElapsed() / TICK_COUNT;

    if ( eTime )
    {
      osalTimerUpdate( eTime );
    }
  }
}

/*********************************************************************
 * @fn      osal_next_timeout
 *
 * @brief
 *
 *   Search timer table to return the lowest timeout value. If the
 *   timer list is empty, then the returned timeout will be zero.
 *
 * @param   none
 *
 * @return  none
 *********************************************************************/
uint32 osal_next_timeout( void )
{
  uint32 nextTimeout;
  osalTimerRec_t *srchTimer;

  if ( timerHead != NULL )
  {
    // Head of the timer list
    srchTimer = timerHead;
    nextTimeout = OSAL_TIMERS_MAX_TIMEOUT;

    // Look for the next timeout timer
    while ( srchTimer != NULL )
    {
      if (srchTimer->timeout.time32 < nextTimeout)
      {
        nextTimeout = srchTimer->timeout.time32;
      }
      // Check next timer
      srchTimer = srchTimer->next;
    }
  }
  else
  {
    // No timers
    nextTimeout = 0;
  }

  return ( nextTimeout );
}
#endif // POWER_SAVING

/*********************************************************************
 * @fn      osal_GetSystemClock()
 *
 * @brief   Read the local system clock.
 *
 * @param   none
 *
 * @return  local clock in milliseconds
 */
uint32 osal_GetSystemClock( void )
{
  return ( osal_systemClock );
}

/*********************************************************************
*********************************************************************/
