/*******************************************************************************
  Filename:       hal_sleep.c
  Revised:        $Date: 2013-07-26 10:28:58 -0700 (Fri, 26 Jul 2013) $
  Revision:       $Revision: 34783 $

  Description:    This module contains the HAL power management procedures for
                  the CC2540.

  Copyright 2006-2013 Texas Instruments Incorporated. All rights reserved.

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

/*******************************************************************************
 * INCLUDES
 */
#include "hal_types.h"
#include "hal_mcu.h"
#include "hal_board.h"
#include "hal_sleep.h"
#include "hal_led.h"
#include "hal_key.h"
#include "OSAL.h"
#include "OSAL_Timers.h"
#include "OSAL_Tasks.h"
#include "OSAL_PwrMgr.h"
#include "hal_drivers.h"
#include "hal_assert.h"
#include "ll_sleep.h"
#include "ll_timer2.h"
#include "ll_math.h"

/*******************************************************************************
 * MACROS
 */

#ifndef HAL_SLEEP_DEBUG_POWER_MODE
// Set CC2540 power mode; always use PM2.
#define HAL_SLEEP_PREP_POWER_MODE(mode)                                        \
  st( SLEEPCMD &= ~PMODE; /* clear mode bits */                                \
      SLEEPCMD |= mode;   /* set mode bits   */                                \
      while (!(STLOAD & LDRDY));                                               \
      halSleepPconValue = PCON_IDLE;                                           \
    )
#define HAL_SLEEP_SET_POWER_MODE()                                             \
  halSetSleepMode()

#else // HAL_SLEEP_DEBUG_POWER_MODE

// Debug: Don't set power mode, just block until sleep timer interrupt.
#define HAL_SLEEP_PREP_POWER_MODE(mode)     /* nothing */
#define HAL_SLEEP_SET_POWER_MODE()                                             \
  st( while(halSleepInt == FALSE);                                             \
      halSleepInt = FALSE;                                                     \
      HAL_DISABLE_INTERRUPTS();                                                \
    )
#endif // !HAL_SLEEP_DEBUG_POWER_MODE

// sleep timer interrupt control
#define HAL_SLEEP_TIMER_ENABLE_INT()        st(IEN0 |= STIE_BV;)  // enable sleep timer interrupt
#define HAL_SLEEP_TIMER_DISABLE_INT()       st(IEN0 &= ~STIE_BV;) // disable sleep timer interrupt
#define HAL_SLEEP_TIMER_CLEAR_INT()         st(IRCON &= ~0x80;)   // clear sleep interrupt flag

// backup interrupt enable registers before sleep
#define HAL_SLEEP_IE_BACKUP_AND_DISABLE( ien0, ien1, ien2 )                    \
  st( (ien0)  = IEN0;                                                          \
      (ien1)  = IEN1;                                                          \
      (ien2)  = IEN2;                                                          \
      IEN0 &= STIE_BV;                                                         \
      IEN1 &= P0IE_BV;                                                         \
      IEN2 &= (P1IE_BV|P2IE_BV);                                               \
    )

// restore interrupt enable registers before sleep
#define HAL_SLEEP_IE_RESTORE( ien0, ien1, ien2 )                               \
  st( IEN0 = (ien0);                                                           \
      IEN1 = (ien1);                                                           \
      IEN2 = (ien2);                                                           \
     )

// convert msec to 625 usec units with round
#define HAL_SLEEP_MS_TO_625US( ms )         (((((uint32) (ms)) * 8) + 4) / 5)

// convert msec to 32kHz units without round : the ratio of 32 kHz ticks to
// msec ticks is 32768/1000 = 32.768 or 4096/125
#define HAL_SLEEP_MS_TO_32KHZ( ms )         ((((uint32) (ms)) * 4096) / 125)

// max allowed sleep time in ms
// Note: When OSAL timer was updated to 32 bits, the call to halSleep was
//       changed to take a 32-bit osal_timeout value. But since the CC2540
//       previously used a 16 bit ll_McuPrecisionCount, halSleep was modified
//       to limit osal_timeout to 16 bits as well (please see SVN rev. 27618).
//       However, the max value of the 16 bit ll_McuPrecisionCount is about 41s,
//       which is shorter than the max sleep time of 65.535s! So it is possible
//       Timer2 rollover could occur during sleep, which could affect when an
//       OSAL timer event is generated. The OSAL timer software should
//       be updated to use the full 24bit value of Timer2, allowing timer
//       events of up to 2.9 hours, but until this can be done properly, the
//       max sleep duration will be limited to less than ll_McuPrecisionCount.
// Note: Not an issue for BLE as the max sleep time would have to be less
//       than 32s.
#define MAX_SLEEP_TIMEOUT                   40000

/*******************************************************************************
 * CONSTANTS
 */

// POWER CONSERVATION DEFINITIONS
// Sleep mode H/W definitions (enabled with POWER_SAVING compile option).
#define CC2540_PM0                          0                     // PM0, Clock oscillators on, voltage regulator on
#define CC2540_PM1                          1                     // PM1, 32.768 kHz oscillators on, voltage regulator on
#define CC2540_PM2                          2                     // PM2, 32.768 kHz oscillators on, voltage regulator off
#define CC2540_PM3                          3                     // PM3, All clock oscillators off, voltage regulator off

// HAL power management mode is set according to the power management state.
// The default setting is HAL_SLEEP_OFF. The actual value is tailored to
// different HW platform. Both HAL_SLEEP_TIMER and HAL_SLEEP_DEEP selections
// will turn off the system clock, and halt the MCU. HAL_SLEEP_TIMER can be
// woken up by sleep timer interrupt, I/O interrupt and reset. HAL_SLEEP_DEEP
// can be woken up by I/O interrupt and reset.
#define HAL_SLEEP_OFF                       CC2540_PM0
#define HAL_SLEEP_TIMER                     CC2540_PM2
#define HAL_SLEEP_DEEP                      CC2540_PM3

// MAX_SLEEP_TIME calculation:
// Sleep timer maximum duration = 0xFFFF7F / 32768 Hz = 511.996 seconds
// Round it to 510 seconds or 510000 ms
#define MAX_SLEEP_TIME                      16711680              // max time to sleep allowed by ST, in 32kHz ticks

// Minimum time to sleep:
// 1. avoid thrashing in-and-out of sleep with short OSAL timer
// 2. define minimum safe sleep period
#if !defined (PM_MIN_SLEEP_TIME)
#define PM_MIN_SLEEP_TIME                   66                    // default to min safe sleep time, in 32kHz ticks
#endif // !PM_MIN_SLEEP_TIME

// This value is used to adjust the sleep timer compare value such that the
// sleep timer compare takes into account the amount of processing time spent in
// function halSleep(). The first value is determined by measuring the number of
// sleep timer ticks from the beginning of the function to entering sleep mode.
// The second value is determined by measuring the number of sleep timer ticks
// from exit of sleep mode to the call to osal_adjust_timers().
#if defined( CC2541) || defined( CC2541S )
#define HAL_SLEEP_ADJ_TICKS                 25                    // default sleep adjustment, in 32kHz ticks
#else // CC2540
#define HAL_SLEEP_ADJ_TICKS                 35                    // default sleep adjustment, in 32kHz ticks
#endif // CC2541 || CC2541S

// sleep and external interrupt port masks
#define STIE_BV                             BV(5)
#define P0IE_BV                             BV(5)
#define P1IE_BV                             BV(4)
#define P2IE_BV                             BV(1)

// for optimized indexing of uint32
#if HAL_MCU_LITTLE_ENDIAN()
#define UINT32_NDX0                         0
#define UINT32_NDX1                         1
#define UINT32_NDX2                         2
#define UINT32_NDX3                         3
#else
#define UINT32_NDX0                         3
#define UINT32_NDX1                         2
#define UINT32_NDX2                         1
#define UINT32_NDX3                         0
#endif // HAL_MCU_LITTLE_ENDIAN()

/*******************************************************************************
 * TYPEDEFS
 */

/*******************************************************************************
 * LOCAL VARIABLES
 */

// HAL power management mode is set according to the power management state.
static uint8 halPwrMgtMode = HAL_SLEEP_OFF;

// Flag to indicate if wake is due to impending radio event.
static uint8 wakeForRF;

#ifdef HAL_SLEEP_DEBUG_POWER_MODE
static bool halSleepInt = FALSE;
#endif // HAL_SLEEP_DEBUG_POWER_MODE

/*******************************************************************************
 * GLOBAL VARIABLES
 */

// PCON register value to program when setting power mode
volatile __data uint8 halSleepPconValue = PCON_IDLE;

/*******************************************************************************
 * Prototypes
 */

// The PCON instruction must be 4-byte aligned. The following code may cause
// excessive power consumption if not aligned. See linker file ".xcl" for
// actual placement.
#pragma location = "SLEEP_CODE"
void halSetSleepMode(void);

void   halSleepSetTimer( uint32 sleepTime, uint32 timeout );
uint32 halSleepReadTimer( void );
uint32 TimerElapsed( void );

/*******************************************************************************
 * @fn          halSleep
 *
 * @brief       This function put the CC2540 to sleep. The PCON instruction must
 *              be 4-byte aligned. The following code may cause excessive power
 *              consumption if not aligned. See linker file ".xcl" for actual
 *              placement.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
#pragma optimize=none
void halSetSleepMode(void)
{
  // WARNING: DO NOT ADD ANY ADDITIONAL CODE; THIS IS A FIXED SIZED SEGMENT!
  PCON = halSleepPconValue;
  // Disallow waking ISR from running in order to give the highest priority to LL_PowerOnReq().
  HAL_DISABLE_INTERRUPTS();
}

/*******************************************************************************
 * @fn          halSleep
 *
 * @brief       This function is called from the OSAL task loop using and
 *              existing OSAL interface.  It sets the low power mode of the LL
 *              and the CC2540.
 *
 * input parameters
 *
 * @param       osal_timeout - Next OSAL timer timeout, in msec.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void halSleep( uint32 osal_timeout )
{
  uint32 timeout;
  uint32 llTimeout;
  uint32 sleepTimer;

#ifdef DEBUG_GPIO
  // TEMP
  P1_0 = 1;
#endif // DEBUG_GPIO

  // max allowed sleep time in ms
  if (osal_timeout > MAX_SLEEP_TIMEOUT)
  {
    osal_timeout = MAX_SLEEP_TIMEOUT;
  }

  // get LL timeout value already converted to 32kHz ticks
  LL_TimeToNextRfEvent( &sleepTimer, &llTimeout );

  // check if no OSAL timeout
  // Note: If the next wake event is due to an OSAL timeout, then wakeForRF
  //       will already be FALSE, and the call to LL_TimeToNExtRfEvent will
  //       already have taken a snapshot of the Sleep Timer.
  if (osal_timeout == 0)
  {
    // use common variable
    timeout = llTimeout;

    // check if there's time before the next radio event
    // Note: Since the OSAL timeout is zero, then if the radio timeout is
    //       not zero, the next wake (if one) will be due to the radio event.
    wakeForRF = (timeout != 0) ? TRUE : FALSE;
  }
  else // OSAL timeout is non-zero
  {
    // convet OSAL timeout to sleep time
    // Note: Could be early by one 32kHz timer tick due to rounding.
    timeout = HAL_SLEEP_MS_TO_32KHZ( osal_timeout );

    // so check time to radio event is non-zero, and if so, use shorter value
    if ((llTimeout != 0) && (llTimeout < timeout))
    {
      // use common variable
      timeout = llTimeout;

      // the next ST wake time is due to radio
      wakeForRF = TRUE;
    }
    else // OSAL timeout will be used to wake
    {
      // so take a snapshot of the sleep timer for sleep based on OSAL timeout
      sleepTimer = halSleepReadTimer();

      // the next ST wake time is not due to radio
      wakeForRF = FALSE;
    }
  }

  // HAL_SLEEP_PM3 is entered only if the timeout is zero
  halPwrMgtMode = (timeout == 0) ? HAL_SLEEP_DEEP : HAL_SLEEP_TIMER;

#ifdef DEBUG_GPIO
  // TEMP
  P1_0 = 0;
#endif // DEBUG_GPIO

  // check if sleep should be entered
  if ( (timeout > PM_MIN_SLEEP_TIME) || (timeout == 0) )
  {
    halIntState_t ien0, ien1, ien2;

#ifdef DEBUG_GPIO
    // TEMP
    P1_0 = 1;
#endif // DEBUG_GPIO

    HAL_ASSERT( HAL_INTERRUPTS_ARE_ENABLED() );
    HAL_DISABLE_INTERRUPTS();

    // check if radio allows sleep, and if so, preps system for shutdown
    if ( halSleepPconValue && ( LL_PowerOffReq(halPwrMgtMode) == LL_SLEEP_REQUEST_ALLOWED ) )
    {
#if ((defined HAL_KEY) && (HAL_KEY == TRUE))
      // get peripherals ready for sleep
      HalKeyEnterSleep();
#endif // ((defined HAL_KEY) && (HAL_KEY == TRUE))

#ifdef HAL_SLEEP_DEBUG_LED
      HAL_TURN_OFF_LED3();
#else
      // use this to turn LEDs off during sleep
      HalLedEnterSleep();
#endif // HAL_SLEEP_DEBUG_LED

      // enable sleep timer interrupt
      if (timeout != 0)
      {
        // check if the time to next wake event is greater than max sleep time
        if (timeout > MAX_SLEEP_TIME )
        {
          // it is, so limit to max allowed sleep time (~510s)
          halSleepSetTimer( sleepTimer, MAX_SLEEP_TIME );
        }
        else // not more than allowed sleep time
        {
          // so set sleep time to actual amount
          halSleepSetTimer( sleepTimer, timeout );
        }
      }

      // prep CC254x power mode
      HAL_SLEEP_PREP_POWER_MODE(halPwrMgtMode);

      // save interrupt enable registers and disable all interrupts
      HAL_SLEEP_IE_BACKUP_AND_DISABLE(ien0, ien1, ien2);
      HAL_ENABLE_INTERRUPTS();

#ifdef DEBUG_GPIO
      // TEMP
      P1_0 = 0;
#endif // DEBUG_GPIO

      // set CC254x power mode; interrupts are disabled after this function
      // Note: Any ISR that could wake the device from sleep needs to use
      //       CLEAR_SLEEP_MODE(), which will clear the halSleepPconValue flag
      //       used to enter sleep mode, thereby preventing the device from
      //       missing this interrupt.
      HAL_SLEEP_SET_POWER_MODE();

#ifdef DEBUG_GPIO
      // TEMP
      P1_0 = 1;
#endif // DEBUG_GPIO

      // check if ST interrupt pending, and if not, clear wakeForRF flag
      // Note: This is needed in case we are not woken by the sleep timer but
      //       by for example a key press. In this case, the flag has to be
      //       cleared as we are not just before a radio event.
      // Note: There is the possiblity that we may wake from an interrupt just
      //       before the sleep timer would have woken us just before a radio
      //       event, in which case power will be wasted as we will probably
      //       enter this routine one or more times before the radio event.
      //       However, this is presumably unusual, and isn't expected to have
      //       much impact on average power consumption.
      if ( (wakeForRF == TRUE) && !(IRCON & 0x80) )
      {
        wakeForRF = FALSE;
      }

      // restore interrupt enable registers
      HAL_SLEEP_IE_RESTORE(ien0, ien1, ien2);

      // power on the LL; blocks until completion
      // Note: This is done here to ensure the 32MHz XOSC has stablized, in
      //       case it is needed (e.g. the ADC is used by the joystick).
      LL_PowerOnReq( (halPwrMgtMode == CC2540_PM3), wakeForRF );

#ifdef HAL_SLEEP_DEBUG_LED
      HAL_TURN_ON_LED3();
#else //!HAL_SLEEP_DEBUG_LED
      // use this to turn LEDs back on after sleep
      HalLedExitSleep();
#endif // HAL_SLEEP_DEBUG_LED

#if ((defined HAL_KEY) && (HAL_KEY == TRUE))
      // handle peripherals
      (void)HalKeyExitSleep();
#endif // ((defined HAL_KEY) && (HAL_KEY == TRUE))
    }

    HAL_ENABLE_INTERRUPTS();
  }

#ifdef DEBUG_GPIO
      // TEMP
      P1_0 = 0;
#endif // DEBUG_GPIO

  return;
}


/*******************************************************************************
 * @fn          halSleepSetTimer
 *
 * @brief       This function sets the CC2540 sleep timer compare value based
 *              on a given snapshot of the sleep timer, and a timeout that is
 *              relative to that snapshot. The snapshot is provided as it may
 *              need to be taken as close to the snapshot of Timer 2 (the radio
 *              timer) as possible so that the time to the next radio event,
 *              when converted to 32kHz ticks, is as accurate as possible in
 *              terms of sleep time. In addition, the offset is adjusted based
 *              on a configurable adjustment to take the sleep handler's
 *              execution time into account. The sleep timer interrupt is then
 *              setup for wake.
 *
 * input parameters
 *
 * @param       sleepTimer - Sleep timer value timeout is relative to.
 * @param       timeout    - Timeout value in 32kHz units.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void halSleepSetTimer( uint32 sleepTimer, uint32 timeout )
{
  HAL_SLEEP_TIMER_DISABLE_INT();

  // compute sleep timer compare value
  sleepTimer += timeout;

  // subtract the processing time spent in function halSleep()
  sleepTimer -= HAL_SLEEP_ADJ_TICKS;

  // set sleep timer compare; ST0 must be written last
  ST2 = ((uint8 *)&sleepTimer)[UINT32_NDX2];
  ST1 = ((uint8 *)&sleepTimer)[UINT32_NDX1];
  ST0 = ((uint8 *)&sleepTimer)[UINT32_NDX0];

  HAL_SLEEP_TIMER_CLEAR_INT();
  HAL_SLEEP_TIMER_ENABLE_INT();

  return;
}


/*******************************************************************************
 * @fn          halSleepReadTimer
 *
 * @brief       This function reads the CC2540 sleep timer.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      A snapshot of the 24 bit sleep timer.
 */
uint32 halSleepReadTimer( void )
{
  uint32 sleepTimer;

  // read the sleep timer
  // Note: Read of ST0 latches ST1 and ST2.
  ((uint8 *)&sleepTimer)[UINT32_NDX0] = ST0;
  ((uint8 *)&sleepTimer)[UINT32_NDX1] = ST1;
  ((uint8 *)&sleepTimer)[UINT32_NDX2] = ST2;
  ((uint8 *)&sleepTimer)[UINT32_NDX3] = 0;

  return( sleepTimer );
}


/*******************************************************************************
 * @fn          TimerElapsed
 *
 * @brief       Determine the number of OSAL timer ticks elapsed during sleep.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of timer ticks elapsed during sleep.
 */
uint32 TimerElapsed( void )
{
  return( 0 );
}


/*******************************************************************************
 * @fn          halRestoreSleepLevel
 *
 * @brief       Restore the deepest timer sleep level.
 *
 * input parameters
 *
 * @param       None
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void halRestoreSleepLevel( void )
{
  // Stub
#ifdef PM_TEST
  osal_start_timerEx (Hal_TaskID, HAL_SLEEP_TIMER_EVENT, 1000);
#endif // PM_TEST
}


/*******************************************************************************
 * @fn          halSleepTimerIsr
 *
 * @brief       Sleep timer ISR.
 *
 * input parameters
 *
 * None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
HAL_ISR_FUNCTION(halSleepTimerIsr, ST_VECTOR)
{
  HAL_ENTER_ISR();

  HAL_SLEEP_TIMER_CLEAR_INT();

#ifdef HAL_SLEEP_DEBUG_POWER_MODE
  halSleepInt = TRUE;
#endif // HAL_SLEEP_DEBUG_POWER_MODE

  CLEAR_SLEEP_MODE();

  HAL_EXIT_ISR();

  return;
}

/*******************************************************************************
 */

