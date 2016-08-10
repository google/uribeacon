/**
  @headerfile:       observer.h
  $Date: 2011-03-11 15:48:31 -0800 (Fri, 11 Mar 2011) $
  $Revision: 25393 $

  @mainpage TI BLE GAP Observer Role

  This GAP profile only discovers.

  Copyright 2011 Texas Instruments Incorporated. All rights reserved.

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
*/

#ifndef OBSERVER_H
#define OBSERVER_H

#ifdef __cplusplus
extern "C"
{
#endif

/*********************************************************************
 * INCLUDES
 */
#include "bcomdef.h"
#include "OSAL.h"
#include "gap.h"

/*********************************************************************
 * CONSTANTS
 */

/** @defgroup GAPOBSERVERROLE_PROFILE_PARAMETERS GAP Observer Role Parameters
 * @{
 */
#define GAPOBSERVERROLE_BD_ADDR        0x400  //!< Device's Address. Read Only. Size is uint8[B_ADDR_LEN]. This item is read from the controller.
#define GAPOBSERVERROLE_MAX_SCAN_RES   0x401  //!< Maximum number of discover scan results to receive. Default is 0 = unlimited.
/** @} End GAPOBSERVERROLE_PROFILE_PARAMETERS */

/*********************************************************************
 * VARIABLES
 */

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * TYPEDEFS
 */

/**
 * Observer Event Structure
 */
typedef union
{
  gapEventHdr_t             gap;                //!< GAP_MSG_EVENT and status.
  gapDeviceInitDoneEvent_t  initDone;           //!< GAP initialization done.
  gapDeviceInfoEvent_t      deviceInfo;         //!< Discovery device information event structure.
  gapDevDiscEvent_t         discCmpl;           //!< Discovery complete event structure.
} gapObserverRoleEvent_t;

/**
 * RSSI Read Callback Function
 */
typedef void (*pfnGapObserverRoleRssiCB_t)
(
  uint16 connHandle,                    //!< Connection handle.
  int8  rssi                            //!< New RSSI value.
);

/**
 * Observer Event Callback Function
 */
typedef void (*pfnGapObserverRoleEventCB_t)
(
  gapObserverRoleEvent_t *pEvent         //!< Pointer to event structure.
);

/**
 * Observer Callback Structure
 */
typedef struct
{
  pfnGapObserverRoleRssiCB_t   rssiCB;   //!< RSSI callback.
  pfnGapObserverRoleEventCB_t  eventCB;  //!< Event callback.
} gapObserverRoleCB_t;

/*********************************************************************
 * VARIABLES
 */

/*********************************************************************
 * API FUNCTIONS
 */

/*-------------------------------------------------------------------
 * Observer Profile Public APIs
 */

/**
 * @defgroup OBSERVER_PROFILE_API Observer Profile API Functions
 *
 * @{
 */

/**
 * @brief   Start the device in Observer role.  This function is typically
 *          called once during system startup.
 *
 * @param   pAppCallbacks - pointer to application callbacks
 *
 * @return  SUCCESS: Operation successful.<BR>
 *          bleAlreadyInRequestedMode: Device already started.<BR>
 */
extern bStatus_t GAPObserverRole_StartDevice( gapObserverRoleCB_t *pAppCallbacks );

/**
 * @brief   Set a parameter in the Observer Profile.
 *
 * @param   param - profile parameter ID: @ref GAPOBSERVERROLE_PROFILE_PARAMETERS
 * @param   len - length of data to write
 * @param   pValue - pointer to data to write.  This is dependent on
 *          the parameter ID and WILL be cast to the appropriate
 *          data type.
 *
 * @return  SUCCESS: Operation successful.<BR>
 *          INVALIDPARAMETER: Invalid parameter ID.<BR>
 */
extern bStatus_t GAPObserverRole_SetParameter( uint16 param, uint8 len, void *pValue );

/**
 * @brief   Get a parameter in the Observer Profile.
 *
 * @param   param - profile parameter ID: @ref GAPOBSERVERROLE_PROFILE_PARAMETERS
 * @param   pValue - pointer to buffer to contain the read data
 *
 * @return  SUCCESS: Operation successful.<BR>
 *          INVALIDPARAMETER: Invalid parameter ID.<BR>
 */
extern bStatus_t GAPObserverRole_GetParameter( uint16 param, void *pValue );

/**
 * @brief   Start a device discovery scan.
 *
 * @param   mode - discovery mode: @ref GAP_DEVDISC_MODE_DEFINES
 * @param   activeScan - TRUE to perform active scan
 * @param   whiteList - TRUE to only scan for devices in the white list
 *
 * @return  SUCCESS: Discovery scan started.<BR>
 *          bleIncorrectMode: Invalid profile role.<BR>
 *          bleAlreadyInRequestedMode: Not available.<BR>
 */
extern bStatus_t GAPObserverRole_StartDiscovery( uint8 mode, uint8 activeScan, uint8 whiteList );

/**
 * @brief   Cancel a device discovery scan.
 *
 * @return  SUCCESS: Cancel started.<BR>
 *          bleInvalidTaskID: Not the task that started discovery.<BR>
 *          bleIncorrectMode: Not in discovery mode.<BR>
 */
extern bStatus_t GAPObserverRole_CancelDiscovery( void );

/**
 * @}
 */

/*-------------------------------------------------------------------
 * TASK API - These functions must only be called by OSAL.
 */

/**
 * @internal
 *
 * @brief   Observer Profile Task initialization function.
 *
 * @param   taskId - Task ID.
 *
 * @return  void
 */
extern void GAPObserverRole_Init( uint8 taskId );

/**
 * @internal
 *
 * @brief   Observer Profile Task event processing function.
 *
 * @param   taskId - Task ID
 * @param   events - Events.
 *
 * @return  events not processed
 */
extern uint16 GAPObserverRole_ProcessEvent( uint8 taskId, uint16 events );

/*********************************************************************
*********************************************************************/

#ifdef __cplusplus
}
#endif

#endif /* OBSERVER_H */
