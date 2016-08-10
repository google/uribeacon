/**************************************************************************************************
  Filename:       _hal_crc.c
  Revised:        $Date: 2012-12-04 18:36:53 -0800 (Tue, 04 Dec 2012) $
  Revision:       $Revision: 32433 $

  Description: This file defines the interface to the H/W CRC driver by LFSR.


  Copyright 2012 Texas Instruments Incorporated. All rights reserved.

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

/* ------------------------------------------------------------------------------------------------
 *                                          Includes
 * ------------------------------------------------------------------------------------------------
 */

#include "hal_board.h"
#include "hal_crc.h"
#include "hal_types.h"

/**************************************************************************************************
 * @fn          HalCRCCalc
 *
 * @brief       Calculate the H/W CRC result.
 *
 * input parameters
 *
 * None.
 *
 * output parameters
 *
 * None.
 *
 * @return      The H/W CRC result.
 */
uint16 HalCRCCalc(void)
{
  uint16 crc = RNDH;
  crc = (crc << 8) | RNDL;

  return crc;
}

/**************************************************************************************************
 * @fn          HalCRCExec
 *
 * @brief       Execute the H/W CRC calculation on the input byte.
 *
 * input parameters
 *
 * @param       ch - The byte on which to execute the H/W CRC calculation.
 *
 * output parameters
 *
 * None.
 *
 * @return      None.
 */
void HalCRCExec(uint8 ch)
{
  RNDH = ch;
}

/**************************************************************************************************
 * @fn          HalCRCInit
 *
 * @brief       Initialize the H/W for a new CRC calculation.
 *
 * input parameters
 *
 * @param       seed - The CRC seed.
 *
 * output parameters
 *
 * None.
 *
 * @return      None.
 */
void HalCRCInit(uint16 seed)
{
  ADCCON1 &= 0xF3;  // CRC configuration of LRSR.

  RNDL = HI_UINT16(seed);
  RNDL = LO_UINT16(seed);
}

/**************************************************************************************************
*/
