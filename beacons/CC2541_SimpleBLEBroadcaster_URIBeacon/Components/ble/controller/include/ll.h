/*******************************************************************************
  Filename:       ll.h
  Revised:        $Date: 2013-08-15 12:30:41 -0700 (Thu, 15 Aug 2013) $
  Revision:       $Revision: 34982 $

  Description:    This file contains the Link Layer (LL) API for the Bluetooth
                  Low Energy (BLE) Controller. It provides the defines, types,
                  and functions for all supported Bluetooth Low Energy (BLE)
                  commands.

                  This API is based on the Bluetooth Core Specification,
                  V4.0.0, Vol. 6.

  Copyright 2009-2013 Texas Instruments Incorporated. All rights reserved.

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

#ifndef LL_H
#define LL_H

#ifdef __cplusplus
extern "C"
{
#endif

/*******************************************************************************
 * INCLUDES
 */
#include "bcomdef.h"

/*******************************************************************************
 * MACROS
 */

// check if connection parameter ranges for CI (min/max), SL, and LSTO are valid
#define LL_INVALID_CONN_TIME_PARAM( ciMin, ciMax, sl, lsto )                   \
  (((ciMin) < LL_CONN_INTERVAL_MIN) ||                                         \
   ((ciMin) > LL_CONN_INTERVAL_MAX) ||                                         \
   ((ciMax) < LL_CONN_INTERVAL_MIN) ||                                         \
   ((ciMax) > LL_CONN_INTERVAL_MAX) ||                                         \
   ((ciMax) < (ciMin))              ||                                         \
   ((sl)    > LL_SLAVE_LATENCY_MAX) ||                                         \
   ((lsto)  < LL_CONN_TIMEOUT_MIN)  ||                                         \
   ((lsto)  > LL_CONN_TIMEOUT_MAX))

// check if the CI/SL/LSTO combination is valid
// based on: LSTO > (1 + Slave Latency) * (Connection Interval * 2)
// Note: The CI * 2 requirement based on ESR05 V1.0, Erratum 3904.
// Note: LSTO time is normalized to units of 1.25ms (i.e. 10ms = 8 * 1.25ms).
#define LL_INVALID_CONN_TIME_PARAM_COMBO( ci, sl, lsto )                       \
  ((uint32)((lsto)*8) <= ((uint32)(1+(sl)) * (uint32)((ci)*2)))

/*******************************************************************************
 * CONSTANTS
 */

/*
** LL API Status Codes
**
** Note: These status values map directly to the HCI Error Codes.
**       Per the Bluetooth Core Specification, V4.0.0, Vol. 2, Part D.
*/
#define LL_STATUS_SUCCESS                              0x00 // Success
#define LL_STATUS_ERROR_UNKNOWN_CONN_HANDLE            0x02 // Unknown Connection Identifier
#define LL_STATUS_ERROR_INACTIVE_CONNECTION            0x02 // Unknown Connection Identifier for now; may be needed for multiple connections
#define LL_STATUS_ERROR_AUTH_FAILURE                   0x05 // Authentication Failure
#define LL_STATUS_ERROR_PIN_OR_KEY_MISSING             0x06 // Pin or Key Missing
#define LL_STATUS_ERROR_OUT_OF_CONN_RESOURCES          0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_OUT_OF_TX_MEM                  0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_OUT_OF_RX_MEM                  0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_OUT_OF_HEAP                    0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_WL_TABLE_FULL                  0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_TX_DATA_QUEUE_FULL             0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_TX_DATA_QUEUE_EMPTY            0x07 // Memory Capacity Exceeded
#define LL_STATUS_ERROR_CONNECTION_TIMEOUT             0x08 // Connection Timeout
#define LL_STATUS_ERROR_CONNECTION_LIMIT_EXCEEDED      0x09 // Connection Limit Exceeded
#define LL_STATUS_ERROR_COMMAND_DISALLOWED             0x0C // Command Disallowed
#define LL_STATUS_ERROR_DUE_TO_LIMITED_RESOURCES       0x0D // Command Rejected Due To Limited Resources
#define LL_STATUS_ERROR_DUE_TO_DELAYED_RESOURCES       0x0D // Command Delayed Due To Limited Resources
#define LL_STATUS_ERROR_FEATURE_NOT_SUPPORTED          0x11 // Unsupported Feature or Parameter Value
#define LL_STATUS_ERROR_UNEXPECTED_PARAMETER           0x12 // Invalid HCI Command Parameters
#define LL_STATUS_ERROR_ILLEGAL_PARAM_COMBINATION      0x12 // Invalid HCI Command Parameters
#define LL_STATUS_ERROR_BAD_PARAMETER                  0x12 // Invalid HCI Command Parameters or 0x30: Parameter Out of Mandatory Range?
#define LL_STATUS_ERROR_UNKNOWN_ADV_EVT_TYPE           0x12 // Invalid HCI Command Parameters or 0x30: Parameter Out of Mandatory Range?
#define LL_STATUS_ERROR_PEER_TERM                      0x13 // Remote User Terminated Connection
#define LL_STATUS_ERROR_PEER_DEVICE_TERM_LOW_RESOURCES 0x14 // Remote Device Terminated Connection Due To Low Resources
#define LL_STATUS_ERROR_PEER_DEVICE_TERM_POWER_OFF     0x15 // Remote Device Terminated Connection Due To Power Off
#define LL_STATUS_ERROR_HOST_TERM                      0x16 // Connection Terminated By Local Host
#define LL_STATUS_ERROR_UNSUPPORTED_REMOTE_FEATURE     0x1A // Unsupported Remote Feature
#define LL_STATUS_ERROR_WL_ENTRY_NOT_FOUND             0x1F // Unspecified Error
#define LL_STATUS_ERROR_WL_TABLE_EMPTY                 0x1F // Unspecified Error
#define LL_STATUS_ERROR_RNG_FAILURE                    0x1F // Unspecified Error
#define LL_STATUS_ERROR_DISCONNECT_IMMEDIATE           0x1F // Unspecified Error
#define LL_STATUS_ERROR_DATA_PACKET_QUEUED             0x1F // Unspecified Error
#define LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE          0x21 // Role Change Not Allowed
#define LL_STATUS_ERROR_LL_TIMEOUT                     0x22 // Link Layer Response Timeout
#define LL_STATUS_ERROR_LL_TIMEOUT_HOST                0x22 // Link Layer Response Timeout
#define LL_STATUS_ERROR_LL_TIMEOUT_PEER                0x22 // Link Layer Response Timeout
#define LL_STATUS_ERROR_INSTANT_PASSED                 0x28 // Instant Passed
#define LL_STATUS_ERROR_INSTANT_PASSED_HOST            0x28 // Instant Passed
#define LL_STATUS_ERROR_INSTANT_PASSED_PEER            0x28 // Instant Passed
#define LL_STATUS_ERROR_KEY_PAIRING_NOT_SUPPORTED      0x29 // Pairing With Unit Key Not Supported
#define LL_STATUS_ERROR_NO_ADV_CHAN_FOUND              0x30 // Parameter Out Of Mandatory Range
#define LL_STATUS_ERROR_PARAM_OUT_OF_RANGE             0x30 // Parameter Out Of Mandatory Range
#define LL_STATUS_ERROR_UPDATE_CTRL_PROC_PENDING       0x3A // Controller Busy
#define LL_STATUS_ERROR_CTRL_PROC_ALREADY_ACTIVE       0x3A // Controller Busy
#define LL_STATUS_ERROR_VER_INFO_REQ_ALREADY_PENDING   0x3A // Controller Busy
#define LL_STATUS_ERROR_UNACCEPTABLE_CONN_INTERVAL     0x3B // Unacceptable Connection Interval
#define LL_STATUS_ERROR_DIRECTED_ADV_TIMEOUT           0x3C // Directed Advertising Timeout
#define LL_STATUS_ERROR_CONN_TERM_DUE_TO_MIC_FAILURE   0x3D // Connection Terminated Due To MIC Failure
#define LL_STATUS_ERROR_CONN_FAILED_TO_BE_ESTABLISHED  0x3E // Connection Failed To Be Established
#define LL_STATUS_ERROR_CONN_TIMING_FAILURE            0x3F // MAC Connection Failed
// Internal
#define LL_STATUS_WARNING_TX_DISABLED                  0xFF // only used internally, so value doesn't matter
#define LL_STATUS_WARNING_FLAG_UNCHANGED               0xFF // only used internally, so value doesn't matter

// Encryption Key Request Reason Codes
#define LL_ENC_KEY_REQ_ACCEPTED                        LL_STATUS_SUCCESS
#define LL_ENC_KEY_REQ_REJECTED                        LL_STATUS_ERROR_PIN_OR_KEY_MISSING
#define LL_ENC_KEY_REQ_UNSUPPORTED_FEATURE             LL_STATUS_ERROR_UNSUPPORTED_REMOTE_FEATURE

// Disconnect Reason Codes
#define LL_SUPERVISION_TIMEOUT_TERM                    LL_STATUS_ERROR_CONNECTION_TIMEOUT
#define LL_PEER_REQUESTED_TERM                         LL_STATUS_ERROR_PEER_TERM
#define LL_PEER_REQUESTED_LOW_RESOURCES_TERM           LL_STATUS_ERROR_PEER_DEVICE_TERM_LOW_RESOURCES
#define LL_PEER_REQUESTED_POWER_OFF_TERM               LL_STATUS_ERROR_PEER_DEVICE_TERM_POWER_OFF
#define LL_HOST_REQUESTED_TERM                         LL_STATUS_ERROR_HOST_TERM
#define LL_CTRL_PKT_TIMEOUT_TERM                       LL_STATUS_ERROR_LL_TIMEOUT
#define LL_CTRL_PKT_TIMEOUT_HOST_TERM                  LL_STATUS_ERROR_LL_TIMEOUT_HOST
#define LL_CTRL_PKT_TIMEOUT_PEER_TERM                  LL_STATUS_ERROR_LL_TIMEOUT_PEER
#define LL_CTRL_PKT_INSTANT_PASSED_TERM                LL_STATUS_ERROR_INSTANT_PASSED
#define LL_CTRL_PKT_INSTANT_PASSED_HOST_TERM           LL_STATUS_ERROR_INSTANT_PASSED_HOST
#define LL_CTRL_PKT_INSTANT_PASSED_PEER_TERM           LL_STATUS_ERROR_INSTANT_PASSED_PEER
#define LL_UNACCEPTABLE_CONN_INTERVAL_TERM             LL_STATUS_ERROR_UNACCEPTABLE_CONN_INTERVAL
#define LL_MIC_FAILURE_TERM                            LL_STATUS_ERROR_CONN_TERM_DUE_TO_MIC_FAILURE
#define LL_CONN_ESTABLISHMENT_FAILED_TERM              LL_STATUS_ERROR_CONN_FAILED_TO_BE_ESTABLISHED

// Disconnect API Parameter
#define LL_DISCONNECT_AUTH_FAILURE                     LL_STATUS_ERROR_AUTH_FAILURE
#define LL_DISCONNECT_REMOTE_USER_TERM                 LL_STATUS_ERROR_PEER_TERM
#define LL_DISCONNECT_REMOTE_DEV_LOW_RESOURCES         LL_STATUS_ERROR_PEER_DEVICE_TERM_LOW_RESOURCES
#define LL_DISCONNECT_REMOTE_DEV_POWER_OFF             LL_STATUS_ERROR_PEER_DEVICE_TERM_POWER_OFF
#define LL_DISCONNECT_UNSUPPORTED_REMOTE_FEATURE       LL_STATUS_ERROR_UNSUPPORTED_REMOTE_FEATURE
#define LL_DISCONNECT_KEY_PAIRING_NOT_SUPPORTED        LL_STATUS_ERROR_KEY_PAIRING_NOT_SUPPORTED
#define LL_DISCONNECT_UNACCEPTABLE_CONN_INTERVAL       LL_STATUS_ERROR_UNACCEPTABLE_CONN_INTERVAL

/*
** LL Buffers Supported
*/
#define LL_MAX_NUM_DATA_BUFFERS                        12
#define LL_MAX_NUM_CMD_BUFFERS                         1

/*
** LL API Parameters
*/

// LL Parameter Limits
#define LL_ADV_CONN_INTERVAL_MIN                       32        // 20ms in 625us
#define LL_ADV_CONN_INTERVAL_MAX                       16384     // 10.24s in 625us
#define LL_ADV_NONCONN_INTERVAL_MIN                    160       // 100ms in 625us
#define LL_ADV_NONCONN_INTERVAL_MAX                    16384     // 10.24s in 625us
#define LL_ADV_DELAY_MIN                               0         // in ms
#define LL_ADV_DELAY_MAX                               10        // in ms
#define LL_SCAN_INTERVAL_MIN                           4         // 2.5ms in 625us
#define LL_SCAN_INTERVAL_MAX                           16384     // 10.24s in 625us
#define LL_SCAN_WINDOW_MIN                             4         // 2.5ms in 625us
#define LL_SCAN_WINDOW_MAX                             16384     // 10.24s in 625us
#define LL_CONN_INTERVAL_MIN                           6         // 7.5ms in 1.25ms
#define LL_CONN_INTERVAL_MAX                           3200      // 4s in 1.25ms
#define LL_CONN_TIMEOUT_MIN                            10        // 100ms in 10ms
#define LL_CONN_TIMEOUT_MAX                            3200      // 32s in 10ms
#define LL_SLAVE_LATENCY_MIN                           0
#define LL_SLAVE_LATENCY_MAX                           499
#define LL_HOP_LENGTH_MIN                              5
#define LL_HOP_LENGTH_MAX                              16
#define LL_INSTANT_NUMBER_MIN                          6

// LL Advertiser Channels
#define LL_ADV_CHAN_37                                 1
#define LL_ADV_CHAN_38                                 2
#define LL_ADV_CHAN_39                                 4
#define LL_ADV_CHAN_ALL                                (LL_ADV_CHAN_37 | LL_ADV_CHAN_38 | LL_ADV_CHAN_39)

// LL Advertiser Events
#define LL_ADV_CONNECTABLE_UNDIRECTED_EVT              0
#define LL_ADV_CONNECTABLE_HDC_DIRECTED_EVT            1  // High Duty Cycle
#define LL_ADV_SCANNABLE_UNDIRECTED_EVT                2
#define LL_ADV_NONCONNECTABLE_UNDIRECTED_EVT           3
#define LL_ADV_CONNECTABLE_LDC_DIRECTED_EVT            4  // Low Duty Cycle

// LL Address Type
#define LL_DEV_ADDR_TYPE_PUBLIC                        0
#define LL_DEV_ADDR_TYPE_RANDOM                        1

// Advertiser White List Policy
#define LL_ADV_WL_POLICY_ANY_REQ                       0  // any scan request or connect request
#define LL_ADV_WL_POLICY_WL_SCAN_REQ                   1  // any connect request, white list scan request
#define LL_ADV_WL_POLICY_WL_CONNECT_REQ                2  // any scan request, white list connect request
#define LL_ADV_WL_POLICY_WL_ALL_REQ                    3  // white list scan request and connect request

// Scanner White List Policy
#define LL_SCAN_WL_POLICY_ANY_ADV_PKTS                 0
#define LL_SCAN_WL_POLICY_USE_WHITE_LIST               1

// Initiator White List Policy
#define LL_INIT_WL_POLICY_USE_PEER_ADDR                0
#define LL_INIT_WL_POLICY_USE_WHITE_LIST               1

// Black List Control
#define LL_SET_BLACKLIST_DISABLE                       0
#define LL_SET_BLACKLIST_ENABLE                        1

// Advertiser Commands
#define LL_ADV_MODE_OFF                                0
#define LL_ADV_MODE_ON                                 1
#define LL_ADV_MODE_RESERVED                           2

// LL Scan Commands
#define LL_SCAN_STOP                                   0
#define LL_SCAN_START                                  1

// LL Scan Filtering
#define LL_FILTER_REPORTS_DISABLE                      0
#define LL_FILTER_REPORTS_ENABLE                       1

// LL Scan Types
#define LL_SCAN_PASSIVE                                0
#define LL_SCAN_ACTIVE                                 1

// LL Tx Power Types
#define LL_READ_CURRENT_TX_POWER_LEVEL                 0
#define LL_READ_MAX_TX_POWER_LEVEL                     1

// Data Fragmentation Flag
#define LL_DATA_FIRST_PKT_HOST_TO_CTRL                 0
#define LL_DATA_CONTINUATION_PKT                       1
#define LL_DATA_FIRST_PKT_CTRL_TO_HOST                 2

// Connection Complete Role
#define LL_LINK_CONNECT_COMPLETE_MASTER                0
#define LL_LINK_CONNECT_COMPLETE_SLAVE                 1

// Encryption Related
#define LL_ENCRYPTION_OFF                              0
#define LL_ENCRYPTION_ON                               1

// Feature Set Related
#define LL_MAX_FEATURE_SET_SIZE                        8  // in bytes
//
#define LL_FEATURE_RFU                                 0  // all bits in a byte
#define LL_FEATURE_ENCRYPTION                          1  // byte 0, bit 0

// Receive Flow Control
#define LL_DISABLE_RX_FLOW_CONTROL                     0
#define LL_ENABLE_RX_FLOW_CONTROL                      1

// Direct Test Mode
#define LL_DIRECT_TEST_NUM_RF_CHANS                    40   // PHY_NUM_RF_CHANS
#define LL_DIRECT_TEST_MAX_PAYLOAD_LEN                 37
//
#define LL_DIRECT_TEST_PAYLOAD_PRBS9                   0
#define LL_DIRECT_TEST_PAYLOAD_0x0F                    1
#define LL_DIRECT_TEST_PAYLOAD_0x55                    2
#define LL_DIRECT_TEST_PAYLOAD_PRBS15                  3
#define LL_DIRECT_TEST_PAYLOAD_0xFF                    4
#define LL_DIRECT_TEST_PAYLOAD_0x00                    5
#define LL_DIRECT_TEST_PAYLOAD_0xF0                    6
#define LL_DIRECT_TEST_PAYLOAD_0xAA                    7
#define LL_DIRECT_TEST_PAYLOAD_UNDEFINED               0xFF
//
#define LL_DIRECT_TEST_MODE_TX                         0
#define LL_DIRECT_TEST_MODE_RX                         1
//
#define LL_RF_RSSI_UNDEFINED                           PHY_RSSI_VALUE_INVALID

// Vendor Specific
#define LL_EXT_RX_GAIN_STD                             0
#define LL_EXT_RX_GAIN_HIGH                            1
//
#define LL_EXT_TX_POWER_MINUS_23_DBM                   0
#define LL_EXT_TX_POWER_MINUS_6_DBM                    1
#define LL_EXT_TX_POWER_0_DBM                          2
#define LL_EXT_TX_POWER_4_DBM                          3

#if defined( CC2541) || defined( CC2541S )

#define LL_EXT_MAX_TX_POWER                            LL_EXT_TX_POWER_0_DBM

#else // CC2540

#define LL_EXT_MAX_TX_POWER                            LL_EXT_TX_POWER_4_DBM

#endif // CC2541 || CC2541S

//
#define LL_EXT_DISABLE_ONE_PKT_PER_EVT                 0
#define LL_EXT_ENABLE_ONE_PKT_PER_EVT                  1
//
#define LL_EXT_DISABLE_CLK_DIVIDE_ON_HALT              0
#define LL_EXT_ENABLE_CLK_DIVIDE_ON_HALT               1
//
#define LL_EXT_NV_NOT_IN_USE                           0
#define LL_EXT_NV_IN_USE                               1
//
#define LL_EXT_DISABLE_FAST_TX_RESP_TIME               0
#define LL_EXT_ENABLE_FAST_TX_RESP_TIME                1
//
#define LL_EXT_DISABLE_SL_OVERRIDE                     0
#define LL_EXT_ENABLE_SL_OVERRIDE                      1
//
#define LL_EXT_TX_MODULATED_CARRIER                    0
#define LL_EXT_TX_UNMODULATED_CARRIER                  1
//
#define LL_EXT_SET_FREQ_TUNE_DOWN                      0
#define LL_EXT_SET_FREQ_TUNE_UP                        1
//
#define LL_EXT_PM_IO_PORT_P0                           0
#define LL_EXT_PM_IO_PORT_P1                           1
#define LL_EXT_PM_IO_PORT_P2                           2
#define LL_EXT_PM_IO_PORT_NONE                         0xFF
#define LL_EXT_PM_IO_DISABLE                           LL_EXT_PM_IO_PORT_NONE
//
#define LL_EXT_PM_IO_PORT_PIN0                         0
#define LL_EXT_PM_IO_PORT_PIN1                         1
#define LL_EXT_PM_IO_PORT_PIN2                         2
#define LL_EXT_PM_IO_PORT_PIN3                         3
#define LL_EXT_PM_IO_PORT_PIN4                         4
#define LL_EXT_PM_IO_PORT_PIN5                         5
#define LL_EXT_PM_IO_PORT_PIN6                         6
#define LL_EXT_PM_IO_PORT_PIN7                         7
//
#define LL_EXT_PER_RESET                               0
#define LL_EXT_PER_READ                                1
//
#define LL_EXT_HALT_DURING_RF_DISABLE                  0
#define LL_EXT_HALT_DURING_RF_ENABLE                   1
//
#define LL_EXT_SET_USER_REVISION                       0
#define LL_EXT_READ_BUILD_REVISION                     1
//
#define LL_EXT_RESET_SYSTEM_DELAY                      100 // in ms
#define LL_EXT_RESET_SYSTEM_HARD                       0
#define LL_EXT_RESET_SYSTEM_SOFT                       1
//
#define LL_EXT_DISABLE_OVERLAPPED_PROCESSING           0
#define LL_EXT_ENABLE_OVERLAPPED_PROCESSING            1
//
#define LL_EXT_DISABLE_NUM_COMPL_PKTS_ON_EVENT         0
#define LL_EXT_ENABLE_NUM_COMPL_PKTS_ON_EVENT          1

// Packet Lengths
#define LL_DEVICE_ADDR_LEN                             6
#define LL_MAX_ADV_DATA_LEN                            31
#define LL_MAX_ADV_PAYLOAD_LEN                         (LL_DEVICE_ADDR_LEN + LL_MAX_ADV_DATA_LEN)
#define LL_MAX_SCAN_DATA_LEN                           31
#define LL_MAX_SCAN_PAYLOAD_LEN                        (LL_DEVICE_ADDR_LEN + LL_MAX_SCAN_DATA_LEN)
#define LL_MAX_LINK_DATA_LEN                           27

/*
** Event Parameters
*/

// Advertising Report Data
#define LL_ADV_RPT_ADV_IND                             LL_ADV_CONNECTABLE_UNDIRECTED_EVT
#define LL_ADV_RPT_ADV_DIRECT_IND                      LL_ADV_CONNECTABLE_HDC_DIRECTED_EVT
#define LL_ADV_RPT_ADV_SCANNABLE_IND                   LL_ADV_SCANNABLE_UNDIRECTED_EVT
#define LL_ADV_RPT_ADV_NONCONN_IND                     LL_ADV_NONCONNECTABLE_UNDIRECTED_EVT
#define LL_ADV_RPT_SCAN_RSP                            (LL_ADV_NONCONNECTABLE_UNDIRECTED_EVT + 1)
#define LL_ADV_RPT_INVALID                             0xFF
//
#define LL_RSSI_NOT_AVAILABLE                          127

// Sleep Clock Accuracy (SCA)
#define LL_SCA_500_PPM                                 0
#define LL_SCA_250_PPM                                 1
#define LL_SCA_150_PPM                                 2
#define LL_SCA_100_PPM                                 3
#define LL_SCA_75_PPM                                  4
#define LL_SCA_50_PPM                                  5
#define LL_SCA_30_PPM                                  6
#define LL_SCA_20_PPM                                  7

/*
** Miscellaneous
*/

#define LL_MAX_NUM_DATA_CHAN                           37   // 0 - 36

/*******************************************************************************
 * TYPEDEFS
 */

typedef uint8 llStatus_t;

// Packet Error Rate Information By Channel
typedef struct
{
  uint16 numPkts[ LL_MAX_NUM_DATA_CHAN ];
  uint16 numCrcErr[ LL_MAX_NUM_DATA_CHAN ];
} perByChan_t;

/*******************************************************************************
 * LOCAL VARIABLES
 */

/*******************************************************************************
 * GLOBAL VARIABLES
 */

/*******************************************************************************
 * LL OSAL Functions
 */

/*******************************************************************************
 * @fn          LL_Init
 *
 * @brief       This is the Link Layer task initialization called by OSAL. It
 *              must be called once when the software system is started and
 *              before any other function in the LL API is called.
 *
 * input parameters
 *
 * @param       taskId - Task identifier assigned by OSAL.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_Init( uint8 taskId );


/*******************************************************************************
 * @fn          LL_ProcessEvent
 *
 * @brief       This is the Link Layer process event handler called by OSAL.
 *
 * input parameters
 *
 * @param       taskId - Task identifier assigned by OSAL.
 *              events - Event flags to be processed by this task.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Unprocessed event flags.
 */
extern uint16 LL_ProcessEvent( uint8 task_id, uint16 events );


/*******************************************************************************
 * LL API for HCI
 */

/*******************************************************************************
 * @fn          LL_TX_bm_alloc API
 *
 * @brief       This API is used to allocate memory using buffer management.
 *
 *              Note: This function should never be called by the application.
 *                    It is only used by HCI and L2CAP_bm_alloc.
 *
 * input parameters
 *
 * @param       size - Number of bytes to allocate from the heap.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Pointer to buffer, or NULL.
 */
extern void *LL_TX_bm_alloc( uint16 size );


/*******************************************************************************
 * @fn          LL_RX_bm_alloc API
 *
 * @brief       This API is used to allocate memory using buffer management.
 *
 *              Note: This function should never be called by the application.
 *                    It is only used by HCI and L2CAP_bm_alloc.
 *
 * input parameters
 *
 * @param       size - Number of bytes to allocate from the heap.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Pointer to buffer, or NULL.
 */
extern void *LL_RX_bm_alloc( uint16 size );


/*******************************************************************************
 * @fn          LL_Reset API
 *
 * @brief       This function is used by the HCI to reset and initialize the
 *              LL Controller.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_Reset( void );


/*******************************************************************************
 * @fn          LL_ReadBDADDR API
 *
 * @brief       This API is called by the HCI to read the controller's
 *              own public device address.
 *
 *              Note: The device's address is stored in NV memory.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       bdAddr  - A pointer to a buffer to hold this device's address.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadBDADDR( uint8 *bdAddr );


/*******************************************************************************
 *
 * @fn          LL_SetRandomAddress API
 *
 * @brief       This function is used to save this device's random address. It
 *              is provided by the Host for devices that are unable to store a
 *              IEEE assigned public address in NV memory.
 *
 * input parameters
 *
 * @param       devAddr - Pointer to a random address (LSO..MSO).
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 *
 */
extern llStatus_t LL_SetRandomAddress( uint8 *devAddr );


/*******************************************************************************
 * @fn          LL_ClearWhiteList API
 *
 * @brief       This API is called by the HCI to clear the White List.
 *
 *              Note: If Scanning is enabled using filtering, and the white
 *                    list policy is "Any", then this command will be
 *                    disallowed.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ClearWhiteList( void );


/*******************************************************************************
 * @fn          LL_AddWhiteListDevice API
 *
 * @brief       This API is called by the HCI to add a device address and its
 *              type to the White List.
 *
 * input parameters
 *
 * @param       devAddr      - Pointer to a 6 byte device address.
 * @param       addrType     - Public or Random device address.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_WL_TABLE_FULL
 */
extern llStatus_t LL_AddWhiteListDevice( uint8 *devAddr,
                                         uint8 addrType );

/*******************************************************************************
 * @fn          LL_RemoveWhiteListDevice API
 *
 * @brief       This API is called by the HCI to remove a device address and
 *              it's type from the White List.
 *
 * input parameters
 *
 * @param       devAddr  - Pointer to a 6 byte device address.
 * @param       addrType - Public or Random device address.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_WL_TABLE_EMPTY,
 *              LL_STATUS_ERROR_WL_ENTRY_NOT_FOUND
 */
extern llStatus_t LL_RemoveWhiteListDevice( uint8 *devAddr,
                                            uint8 addrType );


/*******************************************************************************
 * @fn          LL_ReadWlSize API
 *
 * @brief       This API is called by the HCI to get the total number of white
 *              list entries that can be stored in the Controller.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       *numEntries - Total number of available White List entries.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadWlSize( uint8 *numEntries );


/*******************************************************************************
 * @fn          LL_NumEmptyWlEntries API
 *
 * @brief       This API is called by the HCI to get the number of White List
 *              entries that are empty.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       *numEmptyEntries - number of empty entries in the White List.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_NumEmptyWlEntries( uint8 *numEmptyEntries );


/*******************************************************************************
 * @fn          LL_Encrypt API
 *
 * @brief       This API is called by the HCI to request the LL to encrypt the
 *              data in the command using the key given in the command.
 *
 *              Note: The parameters are byte ordered MSO to LSO.
 *
 * input parameters
 *
 * @param       *key           - A 128 bit key to be used to calculate the
 *                               session key.
 * @param       *plaintextData - A 128 bit block that is to be encrypted.
 *
 * output parameters
 *
 * @param       *encryptedData - A 128 bit block that is encrypted.
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_Encrypt( uint8 *key,
                              uint8 *plaintextData,
                              uint8 *encryptedData );


/*******************************************************************************
 * @fn          LL_Rand API
 *
 * @brief       This API is called by the HCI to request the LL Controller to
 *              provide a data block with random content.
 *
 *              Note: If the radio is in use, then this operation has to be
 *                    delayed until the radio finishes.
 *
 * input parameters
 *
 * @param       *randData - Pointer to buffer to place a random block of data.
 * @param        dataLen  - The length of the random data block, from 1-255.
 *
 * output parameters
 *
 * @param       *randData - Pointer to buffer containing a block of true random
 *                          data.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_DUE_TO_LIMITED_RESOURCES,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED,
 *              LL_STATUS_ERROR_BAD_PARAMETER, LL_STATUS_ERROR_RNG_FAILURE
 */
extern llStatus_t LL_Rand( uint8 *randData,
                           uint8 dataLen );


/*******************************************************************************
 * @fn          LL_ReadSupportedStates API
 *
 * @brief       This function is used to provide the HCI with the Link Layer
 *              supported states and supported state/role combinations.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       *states - Eight byte Bit map of supported states/combos.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadSupportedStates( uint8 *states );


/*******************************************************************************
 * @fn          LL_ReadLocalSupportedFeatures API
 *
 * @brief       This API is called by the HCI to read the controller's
 *              Features Set. The Controller indicates which features it
 *              supports.
 *
 * input parameters
 *
 * @param       featureSet  - A pointer to the Feature Set where each bit:
 *                            0: Feature not supported.
 *                            1: Feature supported by controller.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadLocalSupportedFeatures( uint8 *featureSet );


/*******************************************************************************
 * @fn          LL_ReadLocalVersionInfo API
 *
 * @brief       This API is called by the HCI to read the controller's
 *              Version information.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       verNum    - Version of the Bluetooth Controller specification.
 * @param       comId     - Company identifier of the manufacturer of the
 *                          Bluetooth Controller.
 * @param       subverNum - A unique value for each implementation or revision
 *                          of an implementation of the Bluetooth Controller.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadLocalVersionInfo( uint8  *verNum,
                                           uint16 *comId,
                                           uint16 *subverNum );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_CtrlToHostFlowControl API
 *
 * @brief       This function is used to indicate if the LL enable/disable
 *              receive FIFO processing. This function provides support for
 *              Controller to Host flow control.
 *
 * input parameters
 *
 * @param       mode: LL_ENABLE_RX_FLOW_CONTROL, LL_DISABLE_RX_FLOW_CONTROL
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_CtrlToHostFlowControl( uint8 mode );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ReadRemoteVersionInfo API
 *
 * @brief       This API is called by the HCI to read the peer controller's
 *              Version Information. If the peer's Version Information has
 *              already been received by its request for our Version
 *              Information, then this data is already cached and can be
 *              directly returned to the Host. If the peer's Version Information
 *              is not already cached, then it will be requested from the peer,
 *              and when received, returned to the Host via the
 *              LL_ReadRemoteVersionInfoCback callback.
 *
 *              Note: Only one Version Indication is allowed for a connection.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_VER_IND_ALREADY_SENT
 */
extern llStatus_t LL_ReadRemoteVersionInfo( uint16 connId );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ReadTxPowerLevel
 *
 * @brief       This function is used to read a connection's current transmit
 *              power level or the maximum transmit power level.
 *
 * input parameters
 *
 * @param       connId   - The LL connection handle.
 * @param       type     - LL_READ_CURRENT_TX_POWER_LEVEL or
 *                         LL_READ_MAX_TX_POWER_LEVEL
 * @param       *txPower - A signed value from -30..+20, in dBm.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_PARAM_OUT_OF_RANGE,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
llStatus_t LL_ReadTxPowerLevel( uint8 connId,
                                uint8 type,
                                int8  *txPower );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ReadChanMap API
 *
 * @brief       This API is called by the HCI to read the channel map that the
 *              LL controller is using for the LL connection.
 *
 * input parameters
 *
 * @param       connId  - The LL connection handle.
 *
 * output parameters
 *
 * @param       chanMap - A five byte array containing one bit per data channel
 *                        where a 1 means the channel is "used" and a 0 means
 *                        the channel is "unused".
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_ReadChanMap( uint8 connId,
                                  uint8 *chanMap );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_ReadRssi API
 *
 * @brief       This API is called by the HCI to request RSSI. If there is an
 *              active connection for the given connection ID, then the RSSI of
 *              the last received data packet in the LL will be returned. If a
 *              receiver Modem Test is running, then the RF RSSI for the last
 *              received data will be returned. If no valid RSSI value is
 *              available, then LL_RSSI_NOT_AVAILABLE will be returned.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to read last RSSI.
 *
 * output parameters
 *
 * @param       *lastRssi - The last data RSSI received.
 *                          Range: -127dBm..+20dBm, 127=Not Available.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_ReadRssi( uint16 connId,
                               int8   *lastRssi );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_Disconnect API
 *
 * @brief       This API is called by the HCI to terminate a LL connection.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 * @param       reason - The reason for the Host connection termination.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 *              LL_STATUS_ERROR_CTRL_PROC_ALREADY_ACTIVE
 */
extern llStatus_t LL_Disconnect( uint16 connId,
                                 uint8  reason );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_TxData API
 *
 * @brief       This API is called by the HCI to transmit a buffer of data on a
 *              given LL connection. If fragmentation is supported, the HCI must
 *              also indicate whether this is the first Host packet, or a
 *              continuation Host packet. When fragmentation is not supported,
 *              then a start packet should always specified. If the device is in
 *              a connection as a Master and the current connection ID is the
 *              connection for this data, or is in a connection as a Slave, then
 *              the data is written to the TX FIFO (even if the radio is
 *              curerntly active). If this is a Slave connection, and Fast TX is
 *              enabled and Slave Latency is being used, then the amount of time
 *              to the next event is checked. If there's at least a connection
 *              interval plus some overhead, then the next event is re-aligned
 *              to the next event boundary. Otherwise, in all cases, the buffer
 *              pointer will be retained for transmission, and the callback
 *              event LL_TxDataCompleteCback will be generated to the HCI when
 *              the buffer pointer is no longer needed by the LL.
 *
 *              Note: If the return status is LL_STATUS_ERROR_OUT_OF_TX_MEM,
 *                    then the HCI must not release the buffer until it receives
 *                    the LL_TxDataCompleteCback callback, which indicates the
 *                    LL has copied the transmit buffer.
 *
 *              Note: The HCI should not call this routine if a buffer is still
 *                    pending from a previous call. This is fatal!
 *
 *              Note: If the connection should be terminated within the LL
 *                    before the Host knows, attempts by the HCI to send more
 *                    data (after receiving a LL_TxDataCompleteCback) will
 *                    fail (LL_STATUS_ERROR_INACTIVE_CONNECTION).
 *
 * input parameters
 *
 * @param       connId   - The LL connection ID on which to send this data.
 * @param       *pBuf    - A pointer to the data buffer to transmit.
 * @param       pktLen   - The number of bytes to transmit on this connection.
 * @param       fragFlag - LL_DATA_FIRST_PKT_HOST_TO_CTRL:
 *                           Indicates buffer is the start of a
 *                           Host-to-Controller packet.
 *                         LL_DATA_CONTINUATION_PKT:
 *                           Indicates buffer is a continuation of a
 *                           Host-to-Controller packet.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION,
 *              LL_STATUS_ERROR_OUT_OF_TX_MEM,
 *              LL_STATUS_ERROR_UNEXPECTED_PARAMETER
 */
extern llStatus_t LL_TxData( uint16 connId,
                             uint8  *pBuf,
                             uint8  pktLen,
                             uint8  fragFlag );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_DirectTestTxTest API
 *
 * @brief       This function is used to initiate a BLE PHY level Transmit Test
 *              in Direct Test Mode where the DUT generates test reference
 *              packets at fixed intervals. This test will make use of the
 *              nanoRisc Raw Data Transmit and Receive task.
 *
 *              Note: The BLE device is to transmit at maximum power.
 *              Note: A LL reset should be issued when done using DTM!
 *
 * input parameters
 *
 * @param       txFreq      - Tx RF frequency k=0..39, where F=2402+(k*2MHz).
 * @param       payloadLen  - Number of bytes (0..37)in payload for each packet.
 * @param       payloadType - The type of pattern to transmit.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_DirectTestTxTest( uint8 txFreq,
                                       uint8 payloadLen,
                                       uint8 payloadType );


/*******************************************************************************
 * @fn          LL_DirectTestRxTest API
 *
 * @brief       This function is used to initiate a BLE PHY level Receive Test
 *              in Direct Test Mode where the DUT receives test reference
 *              packets at fixed intervals. This test will make use of the
 *              nanoRisc Raw Data Transmit and Receive task. The received
 *              packets are verified based on the CRC, and metrics are kept.
 *
 *              Note: A LL reset should be issued when done using DTM!
 *
 * input parameters
 *
 * @param       rxFreq - Rx RF frequency k=0..39, where F=2402+(k*2MHz).
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_DirectTestRxTest( uint8 rxFreq );


/*******************************************************************************
 * @fn          LL_DirectTestEnd API
 *
 * @brief       This function is used to end the Direct Test Transmit or Direct
 *              Test Receive tests executing in Direct Test mode. When the raw
 *              task is ended, the LL_DirectTestEndDoneCback callback is called.
 *              If a Direct Test mode operation is not currently active, an
 *              error is returned.
 *
 *              Note: A LL reset is issued upon completion!
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_DirectTestEnd( void );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_SetAdvParam API
 *
 * @brief       This API is called by the HCI to set the Advertiser's
 *              parameters.
 *
 * input parameters
 * @param       advIntervalMin - The minimum Adv interval.
 * @param       advIntervalMax - The maximum Adv interval.
 * @param       advEvtType     - The type of advertisment event.
 * @param       ownAddrType    - The Adv's address type of public or random.
 * @param       directAddrType - Only used for directed advertising.
 * @param       *directAddr    - Only used for directed advertising (NULL otherwise).
 * @param       advChanMap     - A byte containing 1 bit per advertising
 *                               channel. A bit set to 1 means the channel is
 *                               used. The bit positions define the advertising
 *                               channels as follows:
 *                               Bit 0: 37, Bit 1: 38, Bit 2: 39.
 * @param       advWlPolicy    - The Adv white list filter policy.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_NO_ADV_CHAN_FOUND
 */
extern llStatus_t LL_SetAdvParam( uint16 advIntervalMin,
                                  uint16 advIntervalMax,
                                  uint8  advEvtType,
                                  uint8  ownAddrType,
                                  uint8  directAddrType,
                                  uint8  *directAddr,
                                  uint8  advChanMap,
                                  uint8  advWlPolicy );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_SetAdvData API
 *
 * @brief       This API is called by the HCI to set the Advertiser's data.
 *
 *              Note: If the Advertiser is restarted without intervening calls
 *                    to this routine to make updates, then the previously
 *                    defined data will be reused.
 *
 *              Note: If the data happens to be changed while advertising, then
 *                    the new data will be sent on the next advertising event.
 *
 * input parameters
 *
 * @param       advDataLen - The number of scan response bytes: 0..31.
 * @param       advData    - Pointer to the advertiser data, or NULL.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_SetAdvData( uint8 advDataLen,
                                 uint8 *advData );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_SetScanRspData API
 *
 * @brief       This API is called by the HCI to set the Advertiser's Scan
 *              Response data.
 *
 *              Note: If the Advertiser is restarted without intervening calls
 *                    to this routine to make updates, then the previously
 *                    defined data will be reused.
 *
 * input parameters
 *
 * @param       scanRspLen   - The number of scan response bytes: 0..31.
 * @param       *scanRspData - Pointer to the scan response data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_SetScanRspData( uint8 scanRspLen,
                                     uint8 *scanRspData );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_SetAdvControl API
 *
 * @brief       This API is called by the HCI to request the Controller to start
 *              or stop advertising.
 *
 * input parameters
 *
 * @param       advMode - LL_ADV_MODE_ON or LL_ADV_MODE_OFF.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_UNEXPECTED_PARAMETER,
 *              LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_SetAdvControl( uint8 advMode );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_ReadAdvChanTxPower
 *
 * @brief       This function is used to read the transmit power level used
 *              for BLE advertising channel packets. Currently, only two
 *              settings are possible, a standard setting of 0 dBm, and a
 *              maximum setting of 4 dBm.
 *
 * input parameters
 *
 * @param       *txPower - A non-null pointer.
 *
 * output parameters
 *
 * @param       *txPower - A signed value from -20..+10, in dBm.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_PARAM_OUT_OF_RANGE
 */
extern llStatus_t LL_ReadAdvChanTxPower( int8 *txPower );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & SCAN_CFG)
/*******************************************************************************
 * @fn          LL_SetScanParam API
 *
 * @brief       This API is called by the HCI to set the Scanner's parameters.
 *
 * input parameters
 *
 * @param       scanType     - Passive or Active scan type.
 * @param       scanInterval - Time between scan events.
 * @param       scanWindow   - Duration of a scan. When the same as the scan
 *                             interval, then scan continuously.
 * @param       ownAddrType  - Address type (Public or Random) to use in the
 *                             SCAN_REQ packet.
 * @param       advWlPolicy  - Either allow all Adv packets, or only those that
 *                             are in the white list.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_SetScanParam( uint8  scanType,
                                   uint16 scanInterval,
                                   uint16 scanWindow,
                                   uint8  ownAddrType,
                                   uint8  advWlPolicy );
#endif // CTRL_CONFIG=SCAN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & SCAN_CFG)
/*******************************************************************************
 * @fn          LL_SetScanControl API
 *
 * @brief       This API is called by the HCI to start or stop the Scanner. It
 *              also specifies whether the LL will filter duplicate advertising
 *              reports to the Host, or generate a report for each packet
 *              received.
 *
 * input parameters
 *
 * @param       scanMode      - LL_SCAN_START or LL_SCAN_STOP.
 * @param       filterReports - LL_FILTER_REPORTS_DISABLE or
 *                              LL_FILTER_REPORTS_ENABLE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_PARAMETER,
 *              LL_STATUS_ERROR_OUT_OF_TX_MEM,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_SetScanControl( uint8 scanMode,
                                     uint8 filterReports );
#endif // CTRL_CONFIG=SCAN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EncLtkReply API
 *
 * @brief       This API is called by the HCI to provide the controller with
 *              the Long Term Key (LTK) for encryption. This command is
 *              actually a reply to the link layer's LL_EncLtkReqCback, which
 *              provided the random number and encryption diversifier received
 *              from the Master during an encryption setup.
 *
 *              Note: The key parameter is byte ordered LSO to MSO.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 * @param       *key   - A 128 bit key to be used to calculate the session key.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_EncLtkReply( uint16 connId,
                                  uint8  *key );
#endif // CTRL_CONFIG=ADV_CONN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EncLtkNegReply API
 *
 * @brief       This API is called by the HCI to indicate to the controller
 *              that the Long Term Key (LTK) for encryption can not be provided.
 *              This command is actually a reply to the link layer's
 *              LL_EncLtkReqCback, which provided the random number and
 *              encryption diversifier received from the Master during an
 *              encryption setup. How the LL responds to the negative reply
 *              depends on whether this is part of a start encryption or a
 *              re-start encryption after a pause. For the former, an
 *              encryption request rejection is sent to the peer device. For
 *              the latter, the connection is terminated.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_EncLtkNegReply( uint16 connId );
#endif // CTRL_CONFIG=ADV_CONN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_CreateConn API
 *
 * @brief       This API is called by the HCI to create a connection.
 *
 * input parameters
 *
 * @param       scanInterval    - The scan interval.
 * @param       scanWindow      - The scan window.
 * @param       initWlPolicy    - Filter Adv address directly or using WL.
 * @param       peerAddrType    - Peer address is Public or Random.
 * @param       *peerAddr       - The Adv address, or NULL for WL policy.
 * @param       ownAddrType     - This device's address is Public or Random.
 * @param       connIntervalMin - Defines minimum connection interval value.
 * @param       connIntervalMax - Defines maximum connection interval value.
 * @param       connLatency     - The connection's Slave Latency.
 * @param       connTimeout     - The connection's Supervision Timeout.
 * @param       minLength       - Info parameter about min length of connection.
 * @param       maxLength       - Info parameter about max length of connection.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE,
 *              LL_STATUS_ERROR_ILLEGAL_PARAM_COMBINATION,
 *              LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_CreateConn( uint16 scanInterval,
                                 uint16 scanWindow,
                                 uint8  initWlPolicy,
                                 uint8  peerAddrType,
                                 uint8  *peerAddr,
                                 uint8  ownAddrType,
                                 uint16 connIntervalMin,
                                 uint16 connIntervalMax,
                                 uint16 connLatency,
                                 uint16 connTimeout,
                                 uint16 minLength,
                                 uint16 maxLength );
#endif // CTRL_CONFIG=INIT_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_CreateConnCancel API
 *
 * @brief       This API is called by the HCI to cancel a previously given LL
 *              connection creation command that is still pending. This command
 *              should only be used after the LL_CreateConn command as been
 *              issued, but before the LL_ConnComplete callback.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_CreateConnCancel( void );
#endif // CTRL_CONFIG=INIT_CFG


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ConnActive
 *
 * @brief       This API is called by the HCI to check if a connection
 *              given by the connection handle is active.
 *
 * input parameters
 *
 * @param       connId - Connection handle.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_ConnActive( uint16 connId );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_ConnUpdate API
 *
 * @brief       This API is called by the HCI to update the connection
 *              parameters by initiating a connection update control procedure.
 *
 * input parameters
 *
 * @param       connId          - The connection ID on which to send this data.
 * @param       connIntervalMin - Defines minimum connection interval value.
 * @param       connIntervalMax - Defines maximum connection interval value.
 * @param       connLatency     - The connection's Slave Latency.
 * @param       connTimeout     - The connection's Supervision Timeout.
 * @param       minLength       - Info parameter about min length of connection.
 * @param       maxLength       - Info parameter about max length of connection.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_INACTIVE_CONNECTION
 *              LL_STATUS_ERROR_CTRL_PROC_ALREADY_ACTIVE,
 *              LL_STATUS_ERROR_ILLEGAL_PARAM_COMBINATION
 */
extern llStatus_t LL_ConnUpdate( uint16 connId,
                                 uint16 connIntervalMin,
                                 uint16 connIntervalMax,
                                 uint16 connLatency,
                                 uint16 connTimeout,
                                 uint16 minLength,
                                 uint16 maxLength );
#endif // CTRL_CONFIG=INIT_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_ChanMapUpdate API
 *
 * @brief       This API is called by the HCI to update the Host data channels
 *              initiating an Update Data Channel control procedure.
 *
 *              Note: While it isn't specified, it is assumed that the Host
 *                    expects an update channel map on all active connections.
 *
 *              Note: This LL currently only supports one connection.
 *
 * input parameters
 *
 * @param       chanMap - A five byte array containing one bit per data channel
 *                        where a 1 means the channel is "used".
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_ILLEGAL_PARAM_COMBINATION
 */
extern llStatus_t LL_ChanMapUpdate( uint8 *chanMap );
#endif // CTRL_CONFIG=INIT_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_StartEncrypt API
 *
 * @brief       This API is called by the Master HCI to setup encryption and to
 *              update encryption keys in the LL connection. If the connection
 *              is already in encryption mode, then this command will first
 *              pause the encryption before subsequently running the encryption
 *              setup.
 *
 *              Note: The parameters are byte ordered LSO to MSO.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 * @param       *rand  - Random vector used in device identification.
 * @param       *eDiv  - Encrypted diversifier.
 * @param       *key   - A 128 bit key to be used to calculate the session key.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_FEATURE_NOT_SUPPORTED
 */
extern llStatus_t LL_StartEncrypt( uint16 connId,
                                   uint8  *rand,
                                   uint8  *eDiv,
                                   uint8  *ltk );
#endif // CTRL_CONFIG=INIT_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_ReadRemoteUsedFeatures API
 *
 * @brief       This API is called by the Master HCI to initiate a feature
 *              setup control process.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_ReadRemoteUsedFeatures( uint16 connId );
#endif // CTRL_CONFIG=INIT_CFG


/*
** Vendor Specific Command API
*/

/*******************************************************************************
 * @fn          LL_EXT_SetRxGain Vendor Specific API
 *
 * @brief       This function is used to to set the RF RX gain.
 *
 * input parameters
 *
 * @param       rxGain - LL_EXT_RX_GAIN_STD, LL_EXT_RX_GAIN_HIGH
 *
 * output parameters
 *
 * @param       cmdComplete - Boolean to indicate the command is still pending.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetRxGain( uint8 rxGain,
                                    uint8 *cmdComplete );


/*******************************************************************************
 * @fn          LL_EXT_SetTxPower Vendor Specific API
 *
 * @brief       This function is used to to set the RF TX power.
 *
 * input parameters
 *
 * @param       txPower - LL_EXT_TX_POWER_0_DBM, LL_EXT_TX_POWER_4_DBM
 *
 * output parameters
 *
 * @param       cmdComplete - Boolean to indicate the command is still pending.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetTxPower( uint8 txPower,
                                     uint8 *cmdComplete );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_OnePacketPerEvent Vendor Specific API
 *
 * @brief       This function is used to enable or disable allowing only one
 *              packet per event.
 *
 * input parameters
 *
 * @param       control - LL_EXT_ENABLE_ONE_PKT_PER_EVT,
 *                        LL_EXT_DISABLE_ONE_PKT_PER_EVT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_OnePacketPerEvent( uint8 control );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_EXT_ClkDivOnHalt Vendor Specific API
 *
 * @brief       This function is used to enable or disable dividing down the
 *              system clock while halted.
 *
 *              Note: This command is disallowed if haltDuringRf is not defined.
 *
 * input parameters
 *
 * @param       control - LL_EXT_ENABLE_CLK_DIVIDE_ON_HALT,
 *                        LL_EXT_DISABLE_CLK_DIVIDE_ON_HALT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_ClkDivOnHalt( uint8 control );


/*******************************************************************************
 * @fn          LL_EXT_DeclareNvUsage Vendor Specific API
 *
 * @brief       This HCI Extension API is used to indicate to the Controller
 *              whether or not the Host will be using the NV memory during BLE
 *              operations.
 *
 * input parameters
 *
 * @param       mode - HCI_EXT_NV_IN_USE, HCI_EXT_NV_NOT_IN_USE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_DeclareNvUsage( uint8 mode );


/*******************************************************************************
 * @fn          LL_EXT_Decrypt API
 *
 * @brief       This API is called by the HCI to request the LL to decrypt the
 *              data in the command using the key given in the command.
 *
 *              Note: The parameters are byte ordered MSO to LSO.
 *
 * input parameters
 *
 * @param       *key           - A 128 bit key to be used to calculate the
 *                               session key.
 * @param       *encryptedData - A 128 bit block that is encrypted.
 *
 * output parameters
 *
 * @param       *plaintextData - A 128 bit block that is to be encrypted.
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_EXT_Decrypt( uint8 *key,
                                  uint8 *encryptedData,
                                  uint8 *plaintextData );


/*******************************************************************************
 * @fn          LL_EXT_SetLocalSupportedFeatures API
 *
 * @brief       This API is called by the HCI to indicate to the Controller
 *              which features can or can not be used.
 *
 *              Note: Not all features indicated by the Host to the Controller
 *                    are valid. If invalid, they shall be ignored.
 *
 * input parameters
 *
 * @param       featureSet  - A pointer to the Feature Set where each bit:
 *                            0: Feature shall not be used.
 *                            1: Feature can be used.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_EXT_SetLocalSupportedFeatures( uint8 *featureSet );


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EXT_SetFastTxResponseTime API
 *
 * @brief       This API is used to enable or disable the fast TX response
 *              time feature. This can be helpful when a short connection
 *              interval is used in combination with slave latency. In such
 *              a scenario, the response time for sending the TX data packet
 *              can effectively shorten or eliminate slave latency, thereby
 *              increasing power consumption. By disabling, this feature
 *              trades fast response time for less power consumption.
 *
 * input parameters
 *
 * @param       control - LL_EXT_ENABLE_FAST_TX_RESP_TIME,
 *                        LL_EXT_DISABLE_FAST_TX_RESP_TIME
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED,
 *              LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetFastTxResponseTime( uint8 control );
#endif // CTRL_CONFIG=ADV_CONN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EXT_SetSlaveLatencyOverride API
 *
 * @brief       This API is used to enable or disable the suspention of slave
 *              latency. This can be helpful when the Slave application knows
 *              it will soon receive something that needs to be handled without
 *              delay.
 *
 * input parameters
 *
 * @param       control - LL_EXT_DISABLE_SL_OVERRIDE,
 *                        LL_EXT_ENABLE_SL_OVERRIDE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED,
 *              LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetSlaveLatencyOverride( uint8 control );
#endif // CTRL_CONFIG=ADV_CONN_CFG


/*******************************************************************************
 * @fn          LL_EXT_ModemTestTx
 *
 * @brief       This API is used start a continuous transmitter modem test,
 *              using either a modulated or unmodulated carrier wave tone, at
 *              the frequency that corresponds to the specified RF channel. Use
 *              LL_EXT_EndModemTest command to end the test.
 *
 *              Note: A LL reset will be issued by LL_EXT_EndModemTest!
 *              Note: The BLE device will transmit at maximum power.
 *              Note: This API can be used to verify this device meets Japan's
 *                    TELEC regulations.
 *
 * input parameters
 *
 * @param       cwMode - LL_EXT_TX_MODULATED_CARRIER,
 *                       LL_EXT_TX_UNMODULATED_CARRIER
 *              txFreq - Transmit RF channel k=0..39, where BLE F=2402+(k*2MHz).
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_EXT_ModemTestTx( uint8 cwMode,
                                      uint8 txFreq );


/*******************************************************************************
 * @fn          LL_EXT_ModemHopTestTx
 *
 * @brief       This API is used to start a continuous transmitter direct test
 *              mode test using a modulated carrier wave and transmitting a
 *              37 byte packet of Pseudo-Random 9-bit data. A packet is
 *              transmitted on a different frequency (linearly stepping through
 *              all RF channels 0..39) every 625us. Use LL_EXT_EndModemTest
 *              command to end the test.
 *
 *              Note: A LL reset will be issued by LL_EXT_EndModemTest!
 *              Note: The BLE device will transmit at maximum power.
 *              Note: This API can be used to verify this device meets Japan's
 *                    TELEC regulations.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_EXT_ModemHopTestTx( void );


/*******************************************************************************
 * @fn          LL_EXT_ModemTestRx
 *
 * @brief       This API is used to start a continuous receiver modem test
 *              using a modulated carrier wave tone, at the frequency that
 *              corresponds to the specific RF channel. Any received data is
 *              discarded. Receiver gain may be adjusted using the
 *              LL_EXT_SetRxGain command. RSSI may be read during this test by
 *              using the LL_ReadRssi command. Use LL_EXT_EndModemTest command
 *              to end the test.
 *
 *              Note: A LL reset will be issued by LL_EXT_EndModemTest!
 *              Note: The BLE device will transmit at maximum power.
 *
 * input parameters
 *
 * @param       rxFreq - Receiver RF channel k=0..39, where BLE F=2402+(k*2MHz).
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_EXT_ModemTestRx( uint8 rxFreq );


/*******************************************************************************
 * @fn          LL_EXT_EndModemTest
 *
 * @brief       This API is used to shutdown a modem test. A complete link
 *              layer reset will take place.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_UNEXPECTED_STATE_ROLE
 */
extern llStatus_t LL_EXT_EndModemTest( void );


/*******************************************************************************
 * @fn          LL_EXT_SetBDADDR
 *
 * @brief       This API is used to set this device's BLE address (BDADDR).
 *
 *              Note: This command is only allowed when the device's state is
 *                    Standby.
 *
 * input parameters
 *
 * @param       bdAddr  - A pointer to a buffer to hold this device's address.
 *                        An invalid address (i.e. all FF's) will restore this
 *                        device's address to the address set at initialization.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_SetBDADDR( uint8 *bdAddr );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_SetSCA
 *
 * @brief       This API is used to set this device's Sleep Clock Accuracy.
 *
 *              Note: For a slave device, this value is directly used, but only
 *                    if power management is enabled. For a master device, this
 *                    value is converted into one of eight ordinal values
 *                    representing a SCA range, as specified in Table 2.2,
 *                    Vol. 6, Part B, Section 2.3.3.1 of the Core specification.
 *
 *              Note: This command is only allowed when the device is not in a
 *                    connection.
 *
 *              Note: The device's SCA value remains unaffected by a HCI_Reset.
 *
 * input parameters
 *
 * @param       scaInPPM - This device's SCA in PPM from 0..500.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_SetSCA( uint16 scaInPPM );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_EXT_SetFreqTune
 *
 * @brief       This API is used to set the Frequncy Tuning up or down. If the
 *              current setting is already at the max/min value, then no
 *              update is performed.
 *
 *              Note: This is a Production Test Mode only command!
 *
 * input parameters
 *
 * @param       step - LL_EXT_SET_FREQ_TUNE_UP or LL_EXT_SET_FREQ_TUNE_DOWN
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetFreqTune( uint8 step );


/*******************************************************************************
 * @fn          LL_EXT_SaveFreqTune
 *
 * @brief       This API is used to save the current Frequency Tuning value to
 *              flash memory. It is restored on reboot or wake from sleep.
 *
 *              Note: This is a Production Test Mode only command!
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_SaveFreqTune( void );


/*******************************************************************************
 * @fn          LL_EXT_SetMaxDtmTxPower Vendor Specific API
 *
 * @brief       This function is used to set the max RF TX power to be used
 *              when using Direct Test Mode.
 *
 * input parameters
 *
 * @param       txPower - LL_EXT_TX_POWER_MINUS_23_DBM,
 *                        LL_EXT_TX_POWER_MINUS_6_DBM,
 *                        LL_EXT_TX_POWER_0_DBM,
 *                        LL_EXT_TX_POWER_4_DBM
 *
 * output parameters
 *
 * @param       cmdComplete - Boolean to indicate the command is still pending.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_SetMaxDtmTxPower( uint8 txPower );


/*******************************************************************************
 * @fn          LL_EXT_MapPmIoPort Vendor Specific API
 *
 * @brief       This function is used to configure and map a CC254x I/O Port as
 *              a General Purpose I/O (GPIO) output signal that reflects the
 *              Power Management (PM) state of the CC254x device. The GPIO
 *              output will be High on Wake, and Low upon entering Sleep. This
 *              feature can be disabled by specifying LL_EXT_PM_IO_PORT_NONE for
 *              the ioPort (ioPin is then ignored). The system default value
 *              upon hardware reset is disabled. This command can be used to
 *              control an external DC-DC Converter (its actual intent) such has
 *              the TI TPS62730 (or any similar converter that works the same
 *              way). This command should be used with extreme care as it will
 *              override how the Port/Pin was previously configured! This
 *              includes the mapping of Port 0 pins to 32kHz clock output,
 *              Analog I/O, UART, Timers; Port 1 pins to Observables, Digital
 *              Regulator status, UART, Timers; Port 2 pins to an external 32kHz
 *              XOSC. The selected Port/Pin will be configured as an output GPIO
 *              with interrupts masked. Careless use can result in a
 *              reconfiguration that could disrupt the system. It is therefore
 *              the user's responsibility to ensure the selected Port/Pin does
 *              not cause any conflicts in the system.
 *
 *              Note: Only Pins 0, 3 and 4 are valid for Port 2 since Pins 1
 *                    and 2 are mapped to debugger signals DD and DC.
 *
 *              Note: Port/Pin signal change will only occur when Power Savings
 *                    is enabled.
 *
 * input parameters
 *
 * @param       ioPort - LL_EXT_PM_IO_PORT_P0,
 *                       LL_EXT_PM_IO_PORT_P1,
 *                       LL_EXT_PM_IO_PORT_P2,
 *                       LL_EXT_PM_IO_PORT_NONE
 *
 * @param       ioPin  - LL_EXT_PM_IO_PORT_PIN0,
 *                       LL_EXT_PM_IO_PORT_PIN1,
 *                       LL_EXT_PM_IO_PORT_PIN2,
 *                       LL_EXT_PM_IO_PORT_PIN3,
 *                       LL_EXT_PM_IO_PORT_PIN4,
 *                       LL_EXT_PM_IO_PORT_PIN5,
 *                       LL_EXT_PM_IO_PORT_PIN6,
 *                       LL_EXT_PM_IO_PORT_PIN7
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER,
 *              LL_STATUS_ERROR_COMMAND_DISALLOWED
 */
extern llStatus_t LL_EXT_MapPmIoPort( uint8 ioPort, uint8 ioPin );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_DisconnectImmed Vendor Specific API
 *
 * @brief       This function is used to disconnect the connection immediately.
 *
 *              Note: The connection (if valid) is immediately terminated
 *                    without notifying the remote device. The Host is still
 *                    notified.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID on which to send this data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_EXT_DisconnectImmed( uint16 connId );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_PacketErrorRate Vendor Specific API
 *
 * @brief       This function is used to Reset or Read the Packet Error Rate
 *              counters for a connection. When Reset, the counters are cleared;
 *              when Read, the total number of packets received, the number of
 *              packets received with a CRC error, the number of events, and the
 *              number of missed events are returned via a callback.
 *
 *              Note: The counters are only 16 bits. At the shortest connection
 *                    interval, this provides a bit over 8 minutes of data.
 *
 * input parameters
 *
 * @param       connId  - The LL connection ID on which to send this data.
 * @param       command - LL_EXT_PER_RESET, LL_EXT_PER_READ
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_EXT_PacketErrorRate( uint16 connId, uint8 command );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_PERbyChan Vendor Specific API
 *
 * @brief       This API is called by the HCI to start or end Packet Error Rate
 *              by Channel counter accumulation for a connection. If the
 *              pointer is not NULL, it is assumed there is sufficient memory
 *              for the PER data, per the type perByChan_t. If NULL, then
 *              the operation is considered disabled.
 *
 *              Note: It is the user's responsibility to make sure there is
 *                    sufficient memory for the data, and that the counters
 *                    are cleared prior to first use.
 *
 *              Note: The counters are only 16 bits. At the shortest connection
 *                    interval, this provides a bit over 8 minutes of data.
 *
 * input parameters
 *
 * @param       connId    - The LL connection ID on which to send this data.
 * @param       perByChan - Pointer to PER by Channel data, or NULL.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_INACTIVE_CONNECTION
 */
extern llStatus_t LL_EXT_PERbyChan( uint16 connId, perByChan_t *perByChan );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_EXT_ExtendRfRange Vendor Specific API
 *
 * @brief       This function is used to Extend Rf Range using the TI CC2590
 *              2.4 GHz RF Front End device.
 *
 * input parameters
 *
 * @param       cmdComplete - Pointer to get indicatin if command is done.
 *
 * output parameters
 *
 * @param       cmdComplete - Boolean to indicate the command is still pending.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern llStatus_t LL_EXT_ExtendRfRange( uint8 *cmdComplete );


/*******************************************************************************
 * @fn          LL_EXT_HaltDuringRf Vendor Specfic API
 *
 * @brief       This function is used to enable or disable halting the
 *              CPU during RF. The system defaults to enabled.
 *
 * input parameters
 *
 * @param       mode - LL_EXT_HALT_DURING_RF_ENABLE,
 *                     LL_EXT_HALT_DURING_RF_DISABLE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_COMMAND_DISALLOWED,
 *              LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_HaltDuringRf( uint8 mode );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_NCONN_CFG) || (CTRL_CONFIG & ADV_CONN_CFG))
/*******************************************************************************
 * @fn          LL_EXT_AdvEventNotice Vendor Specific API
 *
 * @brief       This API is called to enable or disable a notification to the
 *              specified task using the specified task event whenever a Adv
 *              event ends. A non-zero taskEvent value is taken to be "enable",
 *              while a zero valued taskEvent is taken to be "disable".
 *
 * input parameters
 *
 * @param       taskID    - User's task ID.
 * @param       taskEvent - User's task event.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_AdvEventNotice( uint8 taskID, uint16 taskEvent );
#endif // CTRL_CONFIG=(ADV_NCONN_CFG | ADV_CONN_CFG)


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EXT_ConnEventNotice Vendor Specific API
 *
 * @brief       This API is called to enable or disable a notification to the
 *              specified task using the specified task event whenever a
 *              Connection event ends. A non-zero taskEvent value is taken to
 *              be "enable", while a zero valued taskEvent is taken to be
 *              "disable".
 *
 *              Note: Currently, only a Slave connection is supported.
 *
 * input parameters
 *
 * @param       taskID    - User's task ID.
 * @param       taskEvent - User's task event.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_INACTIVE_CONNECTION,
 *              LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_ConnEventNotice( uint8 taskID, uint16 taskEvent );
#endif // CTRL_CONFIG=ADV_CONN_CFG


/*******************************************************************************
 * @fn          LL_EXT_BuildRevision Vendor Specific API
 *
 * @brief       This API is used to to set a user revision number or read the
 *              build revision number.
 *
 * input parameters
 *
 * @param       mode       - LL_EXT_SET_USER_REVISION |
 *                           LL_EXT_READ_BUILD_REVISION
 * @param       userRevNum - A 16 bit value the user can set as their own
 *                           revision number
 *
 * output parameters
 *
 * @param       buildRev   - Pointer to returned build revision, if any.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_BuildRevision( uint8 mode, uint16 userRevNum, uint8 *buildRev );


/*******************************************************************************
 * @fn          LL_EXT_DelaySleep Vendor Specific API
 *
 * @brief       This API is used to to set the sleep delay.
 *
 * input parameters
 *
 * @param       delay - 0 .. 1000, in milliseconds.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_DelaySleep( uint16 delay );


/*******************************************************************************
 * @fn          LL_EXT_ResetSystem Vendor Specific API
 *
 * @brief       This API is used to to issue a soft or hard system reset.
 *
 * input parameters
 *
 * @param       mode - LL_EXT_RESET_SYSTEM_HARD | LL_EXT_RESET_SYSTEM_SOFT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_STATUS_ERROR_BAD_PARAMETER
 */
extern llStatus_t LL_EXT_ResetSystem( uint8 mode );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_OverlappedProcessing Vendor Specific API
 *
 * @brief       This API is used to enable or disable overlapped processing.
 *
 * input parameters
 *
 * @param       mode - LL_EXT_ENABLE_OVERLAPPED_PROCESSING |
 *                     LL_EXT_DISABLE_OVERLAPPED_PROCESSING
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_ERROR_CODE_INVALID_HCI_CMD_PARAMS
 */
extern llStatus_t LL_EXT_OverlappedProcessing( uint8 mode );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EXT_NumComplPktsLimit Vendor Specific API
 *
 * @brief       This API is used to set the minimum number of
 *              completed packets which must be met before a Number of
 *              Completed Packets event is returned. If the limit is not
 *              reach by the end of the connection event, then a Number of
 *              Completed Packets event will be returned (if non-zero) based
 *              on the flushOnEvt flag.
 *
 * input parameters
 *
 * @param       limit      - From 1 to LL_MAX_NUM_DATA_BUFFERS.
 * @param       flushOnEvt - LL_EXT_DISABLE_NUM_COMPL_PKTS_ON_EVENT |
 *                           LL_EXT_ENABLE_NUM_COMPL_PKTS_ON_EVENT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS, LL_ERROR_CODE_INVALID_HCI_CMD_PARAMS
 */
extern llStatus_t LL_EXT_NumComplPktsLimit( uint8 limit,
                                            uint8 flushOnEvt );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*
**  LL Callbacks to HCI
*/

#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ConnectionCompleteCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              a new connection has been created. For the Slave, this means
 *              a CONNECT_REQ message was received from an Initiator. For the
 *              Master, this means a CONNECT_REQ message was sent in response
 *              to a directed or undirected message addressed to the Initiator.
 *
 * input parameters
 *
 * @param       reasonCode    - LL_STATUS_SUCCESS or ?
 * @param       connId        - The LL connection ID for new connection.
 * @param       role          - LL_LINK_CONNECT_COMPLETE_MASTER or
 *                              LL_LINK_CONNECT_COMPLETE_SLAVE.
 * @param       peerAddrType  - Peer address type (public or random).
 * @param       peerAddr      - Peer address.
 * @param       connInterval  - Connection interval.
 * @param       slaveLatency  - The connection's Slave Latency.
 * @param       connTimeout   - The connection's Supervision Timeout.
 * @param       clockAccuracy - The sleep clock accurracy of the Master. Only
 *                              valid on the Slave. Set to 0x00 for the Master.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_ConnectionCompleteCback( uint8  reasonCode,
                                        uint16 connId,
                                        uint8  role,
                                        uint8  peerAddrType,
                                        uint8  *peerAddr,
                                        uint16 connInterval,
                                        uint16 slaveLatency,
                                        uint16 connTimeout,
                                        uint8  clockAccuracy );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_DisconnectCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              the connection has been terminated. The cause is given by the
 *              reason code.
 *
 * input parameters
 *
 * @param       connId - The LL connection ID.
 * @param       reason - The reason the connection was terminated.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_DisconnectCback( uint16 connId,
                                uint8  reason );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ConnParamUpdateCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              the update parameters control procedure has completed. It is
 *              always made to the Master's Host when the update request has
 *              been sent. It is only made to the Slave's Host when the update
 *              results in a change to the connection interval, and/or the
 *              connection latency, and/or the connection timeout.
 *
 * input parameters
 *
 * @param       connId       - The LL connection ID.
 * @param       connInterval - Connection interval.
 * @param       connLatency  - The connection's Slave Latency.
 * @param       connTimeout  - The connection's Supervision Timeout.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_ConnParamUpdateCback( uint16 connId,
                                     uint16 connInterval,
                                     uint16 connLatency,
                                     uint16 connTimeout );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_ReadRemoteVersionInfoCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host the
 *              requested peer's Version information.
 *
 * input parameters
 *
 * @param       status    - Status of callback.
 * @param       connId    - The LL connection ID.
 * @param       verNum    - Version of the Bluetooth Controller specification.
 * @param       comId     - Company identifier of the manufacturer of the
 *                          Bluetooth Controller.
 * @param       subverNum - A unique value for each implementation or revision
 *                          of an implementation of the Bluetooth Controller.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_ReadRemoteVersionInfoCback( uint8  status,
                                           uint16 connId,
                                           uint8  verNum,
                                           uint16 comId,
                                           uint16 subverNum );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EncChangeCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              an encryption change has taken place. This results when
 *              the host performs a LL_StartEncrypt when encryption is not
 *              already enabled.
 *
 *              Note: If the key request was rejected, then encryption will
 *                    remain off.
 *
 * input parameters
 *
 * @param       connId  - The LL connection ID for new connection.
 * @param       reason  - LL_ENC_KEY_REQ_ACCEPTED or LL_ENC_KEY_REQ_REJECTED.
 * @param       encEnab - LL_ENCRYPTION_OFF or LL_ENCRYPTION_ON.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_EncChangeCback( uint16 connId,
                               uint8  reason,
                               uint8  encEnab );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_EncKeyRefreshCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              an encryption key change has taken place. This results when
 *              the host performs a LL_StartEncrypt when encryption is already
 *              enabled.
 *
 * input parameters
 *
 * @param       connId  - The LL connection ID for new connection.
 * @param       reason  - LL_ENC_KEY_REQ_ACCEPTED.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_EncKeyRefreshCback( uint16 connId,
                                   uint8  reason );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & SCAN_CFG)
/*******************************************************************************
 * @fn          LL_AdvReportCback Callback
 *
 * @brief       This Callback is used by the LL to provide information about
 *              advertisers from which an advertising packet was received.
 *
 * input parameters
 *
 * @param       eventType   - Type of advertisement packet received by Scanner
 *                            or Initiator, and scan response for Initiator.
 * @param       advAddrType - Advertiser address type (public or random).
 * @param       advAddr     - Advertiser address.
 * @param       dataLen     - Size in bytes of advertisement packet.
 * @param       data        - Advertisement data.
 * @param       rssi        - RSSI value (-127..20dBm), or not available
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_AdvReportCback( uint8 eventType,
                               uint8 advAddrType,
                               uint8 *advAddr,
                               uint8 dataLen,
                               uint8 *data,
                               int8  rssi );
#endif // CTRL_CONIFIG=SCAN_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & INIT_CFG)
/*******************************************************************************
 * @fn          LL_ReadRemoteUsedFeaturesCompleteCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the Host that
 *              the Read Remote Feature Support command as completed.
 *
 * input parameters
 *
 * @param       status      - SUCCESS or control procedure timeout.
 * @param       connId      - The LL connection ID for new connection.
 * @param       featureSet  - A pointer to the Feature Set.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_ReadRemoteUsedFeaturesCompleteCback( uint8  status,
                                                    uint16 connId,
                                                    uint8  *featureSet );
#endif // CTRL_CONIFIG=INIT_CFG


#if defined(CTRL_CONFIG) && (CTRL_CONFIG & ADV_CONN_CFG)
/*******************************************************************************
 * @fn          LL_EncLtkReqCback Callback
 *
 * @brief       This Callback is used by the LL to provide to the Host the
 *              Master's random number and encryption diversifier, and to
 *              request the Host's Long Term Key (LTK).
 *
 * input parameters
 *
 * @param       connId  - The LL connection ID for new connection.
 * @param       randNum - Random vector used in device identification.
 * @param       encDiv  - Encrypted diversifier.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_EncLtkReqCback( uint16 connId,
                               uint8  *randNum,
                               uint8  *encDiv );
#endif // CTRL_CONIFIG=ADV_CONN_CFG


/*******************************************************************************
 * @fn          LL_DirectTestEndDone Callback
 *
 * @brief       This Callback is used by the LL to notify the HCI that the
 *              Direct Test End command has completed.
 *
 *
 * input parameters
 *
 * @param       numPackets - The number of packets received. Zero for transmit.
 * @param       mode       - LL_DIRECT_TEST_MODE_TX or LL_DIRECT_TEST_MODE_RX.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      LL_STATUS_SUCCESS
 */
extern void LL_DirectTestEndDoneCback( uint16 numPackets,
                                       uint8  mode );


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_TxDataCompleteCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the HCI that
 *              the HCI's buffer is free for its own use again.
 *
 * input parameters
 *
 * @param       connId   - The LL connection ID on which to send this data.
 * @param       *pBuf    - A pointer to the data buffer to transmit, or NULL.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 ******************************************************************************/
extern void LL_TxDataCompleteCback( uint16 connId,
                                    uint8  *pBuf );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


#if defined(CTRL_CONFIG) && ((CTRL_CONFIG & ADV_CONN_CFG) || (CTRL_CONFIG & INIT_CFG))
/*******************************************************************************
 * @fn          LL_RxDataCompleteCback Callback
 *
 * @brief       This Callback is used by the LL to indicate to the HCI that
 *              data has been received and placed in the buffer provided by
 *              the HCI.
 *
 * input parameters
 *
 * @param       connId   - The LL connection ID on which data was received.
 * @param       *pBuf    - A pointer to the receive data buffer provided by
 *                           the HCI.
 * @param       len      - The number of bytes received on this connection.
 * @param       fragFlag - LL_DATA_FIRST_PKT indicates buffer is the start of
 *                           a Host packet.
 *                         LL_DATA_CONTINUATION_PKT: Indicates buffer is a
 *                           continuation of a Host packet.
 * @param       rssi     - The RSSI of this received packet as a signed byte.
 *                         Range: -127dBm..+20dBm, 127=Not Available.
 *
 * output parameters
 *
 * @param       **pBuf   - A double pointer updated to the next receive data
 *                         buffer, or NULL if no next buffer is available.
 *
 * @return      None.
 */
extern void LL_RxDataCompleteCback( uint16 connId,
                                    uint8  *ppBuf,
                                    uint8  len,
                                    uint8  fragFlag,
                                    int8   rssi );
#endif // CTRL_CONFIG=(ADV_CONN_CFG | INIT_CFG)


/*******************************************************************************
 * @fn          LL_RandCback API
 *
 * @brief       This Callback is used by the LL to notify the HCI that the true
 *              random number command has been completed.
 *
 *              Note: The length is always given by B_RANDOM_NUM_SIZE.
 *
 * input parameters
 *
 * @param       *randData - Pointer to buffer to place a random block of data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_RandCback( uint8 *randData );


/*******************************************************************************
 * @fn          LL_EXT_SetRxGainCback Callback
 *
 * @brief       This Callback is used by the LL to notify the HCI that the set
 *              RX gain command has been completed.
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
extern void LL_EXT_SetRxGainCback( void );


/*******************************************************************************
 * @fn          LL_EXT_SetTxPowerCback Callback
 *
 * @brief       This Callback is used by the LL to notify the HCI that the set
 *              TX power command has been completed.
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
extern void LL_EXT_SetTxPowerCback( void );


/*******************************************************************************
 * @fn          LL_EXT_PacketErrorRateCback Callback
 *
 * @brief       This Callback is used by the LL to notify the HCI that the
 *              Packet Error Rate Read command has been completed.
 *
 *              Note: The counters are only 16 bits. At the shortest connection
 *                    interval, this provides a bit over 8 minutes of data.
 *
 * input parameters
 *
 * @param       numPkts   - Number of Packets received.
 * @param       numCrcErr - Number of Packets received with a CRC error.
 * @param       numEvents - Number of Connection Events.
 * @param       numPkts   - Number of Missed Connection Events.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
extern void LL_EXT_PacketErrorRateCback( uint16 numPkts,
                                         uint16 numCrcErr,
                                         uint16 numEvents,
                                         uint16 numMissedEvts );


/*******************************************************************************
 * @fn          LL_EXT_ExtendRfRangeCback Callback
 *
 * @brief       This Callback is used by the LL to notify the HCI that the
 *              Extend Rf Range command has been completed.
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
extern void LL_EXT_ExtendRfRangeCback( void );


#ifdef __cplusplus
}
#endif

#endif /* LL_H */

