/**************************************************************************************************
  Filename:       observer.c
  Revised:        $Date: 2011-05-05 10:12:10 -0700 (Thu, 05 May 2011) $
  Revision:       $Revision: 25871 $

  Description:    GAP Observer Role


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
  PROVIDED �AS IS� WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
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
#include "bcomdef.h"
#include "OSAL.h"
#include "osal_cbTimer.h"
#include "osal_snv.h"
#include "hci_tl.h"
#include "gap.h"

#include "observer.h"

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

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */

// Task ID
static uint8 gapObserverRoleTaskId;

// App callbacks
static gapObserverRoleCB_t *pGapObserverRoleCB;

/*********************************************************************
 * Profile Parameters - reference GAPCENTRALROLE_PROFILE_PARAMETERS for
 * descriptions
 */

static uint8  gapObserverRoleBdAddr[B_ADDR_LEN];
static uint8  gapObserverRoleMaxScanRes = 0;

/*********************************************************************
 * LOCAL FUNCTIONS
 */
static void gapObserverRole_ProcessOSALMsg( osal_event_hdr_t *pMsg );
static void gapObserverRole_ProcessGAPMsg( gapEventHdr_t *pMsg );

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

/**
 * @brief   Start the device in Observer role.  This function is typically
 *          called once during system startup.
 *
 * Public function defined in observer.h.
 */
bStatus_t GAPObserverRole_StartDevice( gapObserverRoleCB_t *pAppCallbacks )
{
  if ( pAppCallbacks )
  {
    pGapObserverRoleCB = pAppCallbacks;
  }

  return GAP_DeviceInit( gapObserverRoleTaskId, GAP_PROFILE_OBSERVER,
                         gapObserverRoleMaxScanRes, NULL, NULL, NULL );
}

/**
 * @brief   Set a parameter in the Observer Profile.
 *
 * Public function defined in observer.h.
 */
bStatus_t GAPObserverRole_SetParameter( uint16 param, uint8 len, void *pValue )
{
  bStatus_t ret = SUCCESS;

  switch ( param )
  {
    case GAPOBSERVERROLE_MAX_SCAN_RES:
      if ( len == sizeof ( uint8 ) )
      {
        gapObserverRoleMaxScanRes = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;

    default:
      ret = INVALIDPARAMETER;
      break;
  }

  return ret;
}

/**
 * @brief   Get a parameter in the Observer Profile.
 *
 * Public function defined in observer.h.
 */
bStatus_t GAPObserverRole_GetParameter( uint16 param, void *pValue )
{
  bStatus_t ret = SUCCESS;

  switch ( param )
  {
    case GAPOBSERVERROLE_BD_ADDR:
      VOID osal_memcpy( pValue, gapObserverRoleBdAddr, B_ADDR_LEN ) ;
      break;

    case GAPOBSERVERROLE_MAX_SCAN_RES:
      *((uint8*)pValue) = gapObserverRoleMaxScanRes;
      break;

    default:
      ret = INVALIDPARAMETER;
      break;
  }

  return ret;
}

/**
 * @brief   Start a device discovery scan.
 *
 * Public function defined in observer.h.
 */
bStatus_t GAPObserverRole_StartDiscovery( uint8 mode, uint8 activeScan, uint8 whiteList )
{
  gapDevDiscReq_t params;

  params.taskID = gapObserverRoleTaskId;
  params.mode = mode;
  params.activeScan = activeScan;
  params.whiteList = whiteList;

  return GAP_DeviceDiscoveryRequest( &params );
}

/**
 * @brief   Cancel a device discovery scan.
 *
 * Public function defined in observer.h.
 */
bStatus_t GAPObserverRole_CancelDiscovery( void )
{
  return GAP_DeviceDiscoveryCancel( gapObserverRoleTaskId );
}

/**
 * @brief   Observer Profile Task initialization function.
 *
 * @param   taskId - Task ID.
 *
 * @return  void
 */
void GAPObserverRole_Init( uint8 taskId )
{
  gapObserverRoleTaskId = taskId;

  // Register for HCI messages (for RSSI)
  GAP_RegisterForHCIMsgs( taskId );
}

/**
 * @brief   Observer Profile Task event processing function.
 *
 * @param   taskId - Task ID
 * @param   events - Events.
 *
 * @return  events not processed
 */
uint16 GAPObserverRole_ProcessEvent( uint8 taskId, uint16 events )
{
  if ( events & SYS_EVENT_MSG )
  {
    uint8 *pMsg;

    if ( (pMsg = osal_msg_receive( gapObserverRoleTaskId )) != NULL )
    {
      gapObserverRole_ProcessOSALMsg( (osal_event_hdr_t *) pMsg );

      // Release the OSAL message
      VOID osal_msg_deallocate( pMsg );
    }

    // return unprocessed events
    return (events ^ SYS_EVENT_MSG);
  }

  // Discard unknown events
  return 0;
}

/*********************************************************************
 * @fn      gapObserverRole_ProcessOSALMsg
 *
 * @brief   Process an incoming task message.
 *
 * @param   pMsg - message to process
 *
 * @return  none
 */
static void gapObserverRole_ProcessOSALMsg( osal_event_hdr_t *pMsg )
{
  switch ( pMsg->event )
  {
    case HCI_GAP_EVENT_EVENT:
      if ( pMsg->status == HCI_COMMAND_COMPLETE_EVENT_CODE )
      {
        //hciEvt_CmdComplete_t *pPkt = (hciEvt_CmdComplete_t *) pMsg;
      }
      break;

    case GAP_MSG_EVENT:
      gapObserverRole_ProcessGAPMsg( (gapEventHdr_t *) pMsg );
      break;

    default:
      break;
  }
}

/*********************************************************************
 * @fn      gapObserverRole_ProcessGAPMsg
 *
 * @brief   Process an incoming task message from GAP.
 *
 * @param   pMsg - message to process
 *
 * @return  none
 */
static void gapObserverRole_ProcessGAPMsg( gapEventHdr_t *pMsg )
{
  switch ( pMsg->opcode )
  {
    case GAP_DEVICE_INIT_DONE_EVENT:
      {
        gapDeviceInitDoneEvent_t *pPkt = (gapDeviceInitDoneEvent_t *) pMsg;

        if ( pPkt->hdr.status == SUCCESS )
        {
          // Save off the information
          VOID osal_memcpy( gapObserverRoleBdAddr, pPkt->devAddr, B_ADDR_LEN );
        }
      }
      break;

    default:
      break;
  }

  // Pass event to app
  if ( pGapObserverRoleCB && pGapObserverRoleCB->eventCB )
  {
    pGapObserverRoleCB->eventCB( (gapObserverRoleEvent_t *) pMsg );
  }
}

/*********************************************************************
*********************************************************************/
