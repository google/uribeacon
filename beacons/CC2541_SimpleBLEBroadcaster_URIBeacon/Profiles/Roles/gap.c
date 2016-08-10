/*************************************************************************************************
  Filename:       gap.c
  Revised:        $Date: 2013-05-06 13:33:47 -0700 (Mon, 06 May 2013) $
  Revision:       $Revision: 34153 $

  Description:    This file contains the GAP Configuration API.


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

#include "bcomdef.h"
#include "gap.h"
#include "sm.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
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

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */

/*********************************************************************
 * API FUNCTIONS
 */

/*********************************************************************
 * Called to setup the device.  Call just once.
 *
 * Public function defined in gap.h.
 */
bStatus_t GAP_DeviceInit(  uint8 taskID,
                           uint8 profileRole,
                           uint8 maxScanResponses,
                           uint8 *pIRK,
                           uint8 *pSRK,
                           uint32 *pSignCounter )
{
  bStatus_t stat = INVALIDPARAMETER;   // Return status

  // Valid profile roles and supported combinations
  switch ( profileRole )
  {
    case GAP_PROFILE_BROADCASTER:
      #if ( HOST_CONFIG & ( BROADCASTER_CFG | PERIPHERAL_CFG ) )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case GAP_PROFILE_OBSERVER:
      #if ( HOST_CONFIG & ( OBSERVER_CFG | CENTRAL_CFG ) )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case GAP_PROFILE_PERIPHERAL:
      #if ( HOST_CONFIG & PERIPHERAL_CFG )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case GAP_PROFILE_CENTRAL:
      #if ( HOST_CONFIG & CENTRAL_CFG )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case (GAP_PROFILE_BROADCASTER | GAP_PROFILE_OBSERVER):
      #if ( ( HOST_CONFIG & ( BROADCASTER_CFG | PERIPHERAL_CFG ) ) && \
            ( HOST_CONFIG & ( OBSERVER_CFG | CENTRAL_CFG ) ) )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case (GAP_PROFILE_PERIPHERAL | GAP_PROFILE_OBSERVER):
      #if ( ( HOST_CONFIG & PERIPHERAL_CFG ) && \
            ( HOST_CONFIG & ( OBSERVER_CFG | CENTRAL_CFG ) ) )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    case (GAP_PROFILE_CENTRAL | GAP_PROFILE_BROADCASTER):
      #if ( ( HOST_CONFIG & CENTRAL_CFG ) && \
            ( HOST_CONFIG & ( BROADCASTER_CFG | PERIPHERAL_CFG ) ) )
      {
        stat = SUCCESS;
      }
      #endif
      break;

    // Invalid profile roles
    default:
      stat = INVALIDPARAMETER;
      break;
  }

  if ( stat == SUCCESS )
  {
    // Setup the device configuration parameters
    stat = GAP_ParamsInit( taskID, profileRole );
    if ( stat == SUCCESS )
    {
      #if ( HOST_CONFIG & ( CENTRAL_CFG | PERIPHERAL_CFG ) )
      {
        GAP_SecParamsInit( pIRK, pSRK, pSignCounter );
      }
      #endif

      #if ( HOST_CONFIG & ( CENTRAL_CFG | OBSERVER_CFG ) )
      {
        if ( (profileRole == GAP_PROFILE_BROADCASTER) ||
             (profileRole == GAP_PROFILE_PERIPHERAL) )
        {
          maxScanResponses = 0; // Can't scan, so no need for responses. Force 0.
        }

        // Initialize GAP Central Device Manager
        VOID GAP_CentDevMgrInit( maxScanResponses );

        #if ( HOST_CONFIG & CENTRAL_CFG )
        {
          // Register GAP Central Connection processing functions
          GAP_CentConnRegister();

          // Initialize SM Initiator
          VOID SM_InitiatorInit();
        }
        #endif
      }
      #endif

      #if ( HOST_CONFIG & ( PERIPHERAL_CFG | BROADCASTER_CFG ) )
      {
        // Initialize GAP Peripheral Device Manager
        VOID GAP_PeriDevMgrInit();

        #if ( HOST_CONFIG & PERIPHERAL_CFG )
        {
          // Initialize SM Responder
          VOID SM_ResponderInit();
        }
        #endif
      }
      #endif
    }
  }

  return ( stat );
}

/*********************************************************************
*********************************************************************/
