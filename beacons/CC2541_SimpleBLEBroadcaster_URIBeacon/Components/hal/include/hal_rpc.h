/**************************************************************************************************
  Filename:       hal_rpc.h
  Revised:        $Date: 2007-07-06 10:42:24 -0700 (Fri, 06 Jul 2007) $
  Revision:       $Revision: 13579 $

  Description:    This file contains information required by services that
                  use the Remote Procedure Call (RPC) standard.

  Copyright 2008 Texas Instruments Incorporated. All rights reserved.

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

#ifndef HAL_RPC_H
#define HAL_RPC_H

#ifdef __cplusplus
extern "C" {
#endif

/* ------------------------------------------------------------------------------------------------
 *                                          Includes
 * ------------------------------------------------------------------------------------------------
 */

/* ------------------------------------------------------------------------------------------------
 *                                          Constants
 * ------------------------------------------------------------------------------------------------
 */

///////////////////////////////////////////////////////////////////////////////////////////////////
// Common
///////////////////////////////////////////////////////////////////////////////////////////////////

// RPC Command Field Type
#define RPC_CMD_POLL        0x00
#define RPC_CMD_SREQ        0x20
#define RPC_CMD_AREQ        0x40
#define RPC_CMD_SRSP        0x60
#define RPC_CMD_RES4        0x80
#define RPC_CMD_RES5        0xA0
#define RPC_CMD_RES6        0xC0
#define RPC_CMD_RES7        0xE0

// RPC Command Field Subsystem
#define RPC_SYS_RES0        0
#define RPC_SYS_SYS         1
#define RPC_SYS_MAC         2
#define RPC_SYS_NWK         3
#define RPC_SYS_AF          4
#define RPC_SYS_ZDO         5
#define RPC_SYS_SAPI        6    // Simple API
#define RPC_SYS_UTIL        7
#define RPC_SYS_DBG         8
#define RPC_SYS_APP         9
#define RPC_SYS_RCAF        10   // Remote Control Application Framework
#define RPC_SYS_RCN         11   // Remote Control Network Layer
#define RPC_SYS_RCN_CLIENT  12   // Remote Control Network Layer Client
#define RPC_SYS_BOOT        13   // Serial Bootloader
#define RPC_SYS_MAX         14   // Maximum value, must be last

/* 1st byte is the length of the data field, 2nd/3rd bytes are command field. */
#define RPC_FRAME_HDR_SZ    3

/* The 3 MSB's of the 1st command field byte are for command type. */
#define RPC_CMD_TYPE_MASK   0xE0

/* The 5 LSB's of the 1st command field byte are for the subsystem. */
#define RPC_SUBSYSTEM_MASK  0x1F

/* position of fields in the general format frame */
#define RPC_POS_LEN         0
#define RPC_POS_CMD0        1
#define RPC_POS_CMD1        2
#define RPC_POS_DAT0        3

/* Error codes */
#define RPC_SUCCESS         0     /* success */
#define RPC_ERR_SUBSYSTEM   1     /* invalid subsystem */
#define RPC_ERR_COMMAND_ID  2     /* invalid command ID */
#define RPC_ERR_PARAMETER   3     /* invalid parameter */
#define RPC_ERR_LENGTH      4     /* invalid length */

///////////////////////////////////////////////////////////////////////////////////////////////////
// UART Specific
///////////////////////////////////////////////////////////////////////////////////////////////////

#define RPC_UART_FRAME_OVHD 2

// Start of frame character value
#define RPC_UART_SOF        0xFE

///////////////////////////////////////////////////////////////////////////////////////////////////
// SPI Specific
///////////////////////////////////////////////////////////////////////////////////////////////////


/* ------------------------------------------------------------------------------------------------
 *                                           Typedefs
 * ------------------------------------------------------------------------------------------------
 */

/**************************************************************************************************
*/

#ifdef __cplusplus
};
#endif

#endif /* HAL_RPC_H */
