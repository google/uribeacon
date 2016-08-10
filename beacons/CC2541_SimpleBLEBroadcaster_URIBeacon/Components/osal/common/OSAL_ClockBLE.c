/**************************************************************************************************
  Filename:       OSAL_ClockBLE.c
  Revised:        $Date: 2008-12-15 15:42:47 -0800 (Mon, 15 Dec 2008) $
  Revision:       $Revision: 18616 $

  Description:    OSAL Clock definition and manipulation functions for BLE projects.

  Copyright 2008-2011 Texas Instruments Incorporated. All rights reserved.

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
#include "OSAL_Clock.h"

/*********************************************************************
 * MACROS
 */

#define	YearLength(yr)	(IsLeapYear(yr) ? 366 : 365)

/*********************************************************************
 * CONSTANTS
 */

// (MAXCALCTICKS * 5) + (max remainder) must be <= (uint16 max),
// so: (13105 * 5) + 7 <= 65535
#define MAXCALCTICKS  ((uint16)(13105))

#define	BEGYEAR	        2000     // UTC started at 00:00:00 January 1, 2000

#define	DAY             86400UL  // 24 hours * 60 minutes * 60 seconds

/*********************************************************************
 * TYPEDEFS
 */

/*********************************************************************
 * GLOBAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */
extern uint16 ll_McuPrecisionCount(void);

/*********************************************************************
 * LOCAL VARIABLES
 */
static uint16 previousLLTimerTick = 0;
static uint16 remUsTicks = 0;
static uint16 timeMSec = 0;

// number of seconds since 0 hrs, 0 minutes, 0 seconds, on the
// 1st of January 2000 UTC
UTCTime OSAL_timeSeconds = 0;

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */
static uint8 monthLength( uint8 lpyr, uint8 mon );

static void osalClockUpdate( uint16 elapsedMSec );

/*********************************************************************
 * FUNCTIONS
 *********************************************************************/

/*********************************************************************
 * @fn      osalTimeUpdate
 *
 * @brief   Uses the free running rollover count of the MAC backoff timer;
 *          this timer runs freely with a constant 320 usec interval.  The
 *          count of 320-usec ticks is converted to msecs and used to update
 *          the OSAL clock and Timers by invoking osalClockUpdate() and
 *          osalTimerUpdate().  This function is intended to be invoked
 *          from the background, not interrupt level.
 *
 * @param   None.
 *
 * @return  None.
 */
void osalTimeUpdate( void )
{
  uint16 tmp;
  uint16 ticks625us;
  uint16 elapsedMSec = 0;

  // Get the free-running count of 625us timer ticks
  tmp = ll_McuPrecisionCount();

  if ( tmp != previousLLTimerTick )
  {
    // Calculate the elapsed ticks of the free-running timer.
    ticks625us = tmp - previousLLTimerTick;

    // Store the LL Timer tick count for the next time through this function.
    previousLLTimerTick = tmp;

    /* It is necessary to loop to convert the usecs to msecs in increments so as
     * not to overflow the 16-bit variables.
     */
    while ( ticks625us > MAXCALCTICKS )
    {
      ticks625us -= MAXCALCTICKS;
      elapsedMSec += MAXCALCTICKS * 5 / 8;
      remUsTicks += MAXCALCTICKS * 5 % 8;
    }

    // update converted number with remaining ticks from loop and the
    // accumulated remainder from loop
    tmp = (ticks625us * 5) + remUsTicks;

    // Convert the 625 us ticks into milliseconds and a remainder
    elapsedMSec += tmp / 8;
    remUsTicks = tmp % 8;

    // Update OSAL Clock and Timers
    if ( elapsedMSec )
    {
      osalClockUpdate( elapsedMSec );
      osalTimerUpdate( elapsedMSec );
    }
  }
}

/*********************************************************************
 * @fn      osalClockUpdate
 *
 * @brief   Updates the OSAL Clock time with elapsed milliseconds.
 *
 * @param   elapsedMSec - elapsed milliseconds
 *
 * @return  none
 */
static void osalClockUpdate( uint16 elapsedMSec )
{
  // Add elapsed milliseconds to the saved millisecond portion of time
  timeMSec += elapsedMSec;

  // Roll up milliseconds to the number of seconds
  if ( timeMSec >= 1000 )
  {
    OSAL_timeSeconds += timeMSec / 1000;
    timeMSec = timeMSec % 1000;
  }
}

/*********************************************************************
 * @fn      osal_setClock
 *
 * @brief   Set the new time.  This will only set the seconds portion
 *          of time and doesn't change the factional second counter.
 *
 * @param   newTime - number of seconds since 0 hrs, 0 minutes,
 *                    0 seconds, on the 1st of January 2000 UTC
 *
 * @return  none
 */
void osal_setClock( UTCTime newTime )
{
  OSAL_timeSeconds = newTime;
}

/*********************************************************************
 * @fn      osal_getClock
 *
 * @brief   Gets the current time.  This will only return the seconds
 *          portion of time and doesn't include the factional second
 *          counter.
 *
 * @param   none
 *
 * @return  number of seconds since 0 hrs, 0 minutes, 0 seconds,
 *          on the 1st of January 2000 UTC
 */
UTCTime osal_getClock( void )
{
  return ( OSAL_timeSeconds );
}

/*********************************************************************
 * @fn      osal_ConvertUTCTime
 *
 * @brief   Converts UTCTime to UTCTimeStruct
 *
 * @param   tm - pointer to breakdown struct
 *
 * @param   secTime - number of seconds since 0 hrs, 0 minutes,
 *          0 seconds, on the 1st of January 2000 UTC
 *
 * @return  none
 */
void osal_ConvertUTCTime( UTCTimeStruct *tm, UTCTime secTime )
{
  // calculate the time less than a day - hours, minutes, seconds
  {
    uint32 day = secTime % DAY;
    tm->seconds = day % 60UL;
    tm->minutes = (day % 3600UL) / 60UL;
    tm->hour = day / 3600UL;
  }

  // Fill in the calendar - day, month, year
  {
    uint16 numDays = secTime / DAY;
    tm->year = BEGYEAR;
    while ( numDays >= YearLength( tm->year ) )
    {
      numDays -= YearLength( tm->year );
      tm->year++;
    }

    tm->month = 0;
    while ( numDays >= monthLength( IsLeapYear( tm->year ), tm->month ) )
    {
      numDays -= monthLength( IsLeapYear( tm->year ), tm->month );
      tm->month++;
    }

    tm->day = numDays;
  }
}

/*********************************************************************
 * @fn      monthLength
 *
 * @param   lpyr - 1 for leap year, 0 if not
 *
 * @param   mon - 0 - 11 (jan - dec)
 *
 * @return  number of days in specified month
 */
static uint8 monthLength( uint8 lpyr, uint8 mon )
{
  uint8 days = 31;

  if ( mon == 1 ) // feb
  {
    days = ( 28 + lpyr );
  }
  else
  {
    if ( mon > 6 ) // aug-dec
    {
      mon--;
    }

    if ( mon & 1 )
    {
      days = 30;
    }
  }

  return ( days );
}

/*********************************************************************
 * @fn      osal_ConvertUTCSecs
 *
 * @brief   Converts a UTCTimeStruct to UTCTime
 *
 * @param   tm - pointer to provided struct
 *
 * @return  number of seconds since 00:00:00 on 01/01/2000 (UTC)
 */
UTCTime osal_ConvertUTCSecs( UTCTimeStruct *tm )
{
  uint32 seconds;

  /* Seconds for the partial day */
  seconds = (((tm->hour * 60UL) + tm->minutes) * 60UL) + tm->seconds;

  /* Account for previous complete days */
  {
    /* Start with complete days in current month */
    uint16 days = tm->day;

    /* Next, complete months in current year */
    {
      int8 month = tm->month;
      while ( --month >= 0 )
      {
        days += monthLength( IsLeapYear( tm->year ), month );
      }
    }

    /* Next, complete years before current year */
    {
      uint16 year = tm->year;
      while ( --year >= BEGYEAR )
      {
        days += YearLength( year );
      }
    }

    /* Add total seconds before partial day */
    seconds += (days * DAY);
  }

  return ( seconds );
}
