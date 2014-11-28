/**
  @headerfile:       bcomdef.h

  <!--
  Revised:        $Date: 2012-11-27 14:14:39 -0800 (Tue, 27 Nov 2012) $
  Revision:       $Revision: 32324 $

  Description:    Type definitions and macros for BLE stack.


  Copyright 2009 - 2012 Texas Instruments Incorporated. All rights reserved.

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
  PROVIDED “AS IS?WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
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
  -->
**************************************************************************************************/

#ifndef BCOMDEF_H
#define BCOMDEF_H

#ifdef __cplusplus
extern "C"
{
#endif


/*********************************************************************
 * INCLUDES
 */

#include "comdef.h"

/*********************************************************************
 * CONSTANTS
 */

#if defined ( HOST_CONFIG )
  // Set the Controller Configuration
  #if ( HOST_CONFIG == ( CENTRAL_CFG | PERIPHERAL_CFG ) )
    #define CTRL_CONFIG   ( ADV_NCONN_CFG | ADV_CONN_CFG | SCAN_CFG | INIT_CFG )
  #elif ( HOST_CONFIG == ( CENTRAL_CFG | BROADCASTER_CFG ) )
    #define CTRL_CONFIG   ( ADV_NCONN_CFG | SCAN_CFG | INIT_CFG )
  #elif ( HOST_CONFIG == ( PERIPHERAL_CFG | OBSERVER_CFG ) )
    #define CTRL_CONFIG   ( ADV_NCONN_CFG | ADV_CONN_CFG | SCAN_CFG )
  #elif ( HOST_CONFIG == ( BROADCASTER_CFG | OBSERVER_CFG ) )
    #define CTRL_CONFIG   ( ADV_NCONN_CFG | SCAN_CFG )
  #elif ( HOST_CONFIG == CENTRAL_CFG )
    #define CTRL_CONFIG   ( SCAN_CFG | INIT_CFG )
  #elif ( HOST_CONFIG == PERIPHERAL_CFG )
    #define CTRL_CONFIG   ( ADV_NCONN_CFG | ADV_CONN_CFG )
  #elif ( HOST_CONFIG == OBSERVER_CFG )
    #define CTRL_CONFIG   SCAN_CFG
  #elif ( HOST_CONFIG == BROADCASTER_CFG )
    #define CTRL_CONFIG   ADV_NCONN_CFG
  #else
    #error "Build Configuration Error: Invalid Host Role!"
  #endif
#else
  // Controller Sanity Check: Stop build when no configuration is defined.
  #if !defined( CTRL_CONFIG ) || !( CTRL_CONFIG & ( ADV_NCONN_CFG | \
                                                    ADV_CONN_CFG  | \
                                                    SCAN_CFG      | \
                                                    INIT_CFG ) )
    #error "Build Configuration Error: At least one Controller build component required!"
  #endif // no Controller build components defined
#endif

#if !defined ( MAX_NUM_LL_CONN )
  #if ( CTRL_CONFIG & INIT_CFG )
    #define MAX_NUM_LL_CONN                       3
  #elif ( !( CTRL_CONFIG & INIT_CFG ) && ( CTRL_CONFIG & ADV_CONN_CFG ) )
    #define MAX_NUM_LL_CONN                       1
  #else // no connection needed
    #define MAX_NUM_LL_CONN                       0
  #endif // CTRL_CONFIG=INIT_CFG
#endif // !MAX_NUM_LL_CONN

/** @defgroup BLE_COMMON_DEFINES BLE Common Defines
 * @{
 */
//! Default Public and Random Address Length
#define B_ADDR_LEN                                6

//! Default key length
#define KEYLEN                                    16

//! BLE Channel Map length
#define B_CHANNEL_MAP_LEN                         5

//! BLE Event mask length
#define B_EVENT_MASK_LEN                          8

//! BLE Local Name length
#define B_LOCAL_NAME_LEN                          248

//! BLE Maximum Advertising Packet Length
#define B_MAX_ADV_LEN                             31

//! BLE Random Number Size
#define B_RANDOM_NUM_SIZE                         8

//! BLE Feature Supported length
#define B_FEATURE_SUPPORT_LENGTH                  8

/** @defgroup BLE_STATUS_VALUES BLE Default BLE Status Values
 * returned as bStatus_t
 * @{
 */
#define bleInvalidTaskID                INVALID_TASK  //!< Task ID isn't setup properly
#define bleNotReady                     0x10  //!< Not ready to perform task
#define bleAlreadyInRequestedMode       0x11  //!< Already performing that task
#define bleIncorrectMode                0x12  //!< Not setup properly to perform that task
#define bleMemAllocError                0x13  //!< Memory allocation error occurred
#define bleNotConnected                 0x14  //!< Can't perform function when not in a connection
#define bleNoResources                  0x15  //!< There are no resource available
#define blePending                      0x16  //!< Waiting
#define bleTimeout                      0x17  //!< Timed out performing function
#define bleInvalidRange                 0x18  //!< A parameter is out of range
#define bleLinkEncrypted                0x19  //!< The link is already encrypted
#define bleProcedureComplete            0x1A  //!< The Procedure is completed

// GAP Status Return Values - returned as bStatus_t
#define bleGAPUserCanceled              0x30  //!< The user canceled the task
#define bleGAPConnNotAcceptable         0x31  //!< The connection was not accepted
#define bleGAPBondRejected              0x32  //!< The bound information was rejected.

// ATT Status Return Values - returned as bStatus_t
#define bleInvalidPDU                   0x40  //!< The attribute PDU is invalid
#define bleInsufficientAuthen           0x41  //!< The attribute has insufficient authentication
#define bleInsufficientEncrypt          0x42  //!< The attribute has insufficient encryption
#define bleInsufficientKeySize          0x43  //!< The attribute has insufficient encryption key size

// L2CAP Status Return Values - returned as bStatus_t

#define INVALID_TASK_ID                 0xFF  //!< Task ID isn't setup properly
/** @} End BLE_STATUS_VALUES */

/** @defgroup BLE_NV_IDS BLE Non-volatile IDs
 * @{
 */
// Device NV Items -    Range 0 - 0x1F
#define BLE_NVID_IRK                    0x02  //!< The Device's IRK
#define BLE_NVID_CSRK                   0x03  //!< The Device's CSRK
#define BLE_NVID_SIGNCOUNTER            0x04  //!< The Device's Sign Counter

// Bonding NV Items -   Range  0x20 - 0x5F    - This allows for 10 bondings
#define BLE_NVID_GAP_BOND_START         0x20  //!< Start of the GAP Bond Manager's NV IDs
#define BLE_NVID_GAP_BOND_END           0x5f  //!< End of the GAP Bond Manager's NV IDs Range

// GATT Configuration NV Items - Range  0x70 - 0x79 - This must match the number of Bonding entries
#define BLE_NVID_GATT_CFG_START         0x70  //!< Start of the GATT Configuration NV IDs
#define BLE_NVID_GATT_CFG_END           0x79  //!< End of the GATT Configuration NV IDs
/** @} End BLE_NV_IDS */

/*********************************************************************
 * BLE OSAL GAP GLOBAL Events
 */
#define GAP_EVENT_SIGN_COUNTER_CHANGED  0x4000  //!< The device level sign counter changed


/** @defgroup BLE_MSG_IDS BLE OSAL Message ID Events
 *      Reserved Message ID Event Values:<BR>
 *        0xC0 - Key Presses<BR>
 *        0xE0 to 0xFC - App<BR>
 * @{
 */
// GAP - Messages IDs (0xD0 - 0xDF)
#define GAP_MSG_EVENT                         0xD0 //!< Incoming GAP message

// SM - Messages IDs (0xC1 - 0xCF)
#define SM_NEW_RAND_KEY_EVENT                 0xC1 //!< New Rand Key Event message

// GATT - Messages IDs (0xB0 - 0xBF)
#define GATT_MSG_EVENT                        0xB0 //!< Incoming GATT message
#define GATT_SERV_MSG_EVENT                   0xB1 //!< Incoming GATT Serv App message

// L2CAP - Messages IDs (0xA0 - 0xAF)
#define L2CAP_DATA_EVENT                      0xA0 //!< Incoming data on a channel
#define L2CAP_SIGNAL_EVENT                    0xA2 //!< Incoming Signaling message

// HCI - Messages IDs (0x90 - 0x9F)
#define HCI_DATA_EVENT                        0x90 //!< HCI Data Event message
#define HCI_GAP_EVENT_EVENT                   0x91 //!< GAP Event message
#define HCI_SMP_EVENT_EVENT                   0x92 //!< SMP Event message
#define HCI_EXT_CMD_EVENT                     0x93 //!< HCI Extended Command Event message
/** @} End BLE_MSG_IDS */

/*********************************************************************
 * TYPEDEFS
 */

  //! BLE Generic Status return: @ref BLE_STATUS_VALUES
typedef Status_t bStatus_t;

/** @} End GAP_MSG_EVENT_DEFINES */


/*********************************************************************
 * System Events
 */

/*********************************************************************
 * Global System Messages
 */

/*********************************************************************
 * MACROS
 */

// TI Base 128-bit UUID: F000XXXX-0451-4000-B000-000000000000
#define TI_BASE_UUID_128( uuid )  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xB0, \
                                  0x00, 0x40, 0x51, 0x04, LO_UINT16( uuid ), HI_UINT16( uuid ), 0x00, 0xF0

/*********************************************************************
 * GLOBAL VARIABLES
 */

/*********************************************************************
 * FUNCTIONS
 */

/*********************************************************************
*********************************************************************/

#ifdef __cplusplus
}
#endif

#endif /* BCOMDEF_H */
