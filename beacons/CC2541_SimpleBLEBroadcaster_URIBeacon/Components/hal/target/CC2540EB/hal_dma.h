/**************************************************************************************************
  Filename:      hal_dma.h
  Revised:        $Date: 2011-08-23 08:53:54 -0700 (Tue, 23 Aug 2011) $
  Revision:       $Revision: 27253 $

  Description:   This file contains the interface to the DMA Service.


  Copyright 2007-2010 Texas Instruments Incorporated. All rights reserved.

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

#ifndef HAL_DMA_H
#define HAL_DMA_H

#ifdef __cplusplus
extern "C"
{
#endif

/*********************************************************************
 * INCLUDES
 */

#include "hal_board.h"
#include "hal_types.h"

#if ((defined HAL_DMA) && (HAL_DMA == TRUE))

/*********************************************************************
 * MACROS
 */

#define HAL_DMA_SET_ADDR_DESC0( a ) \
  st( \
    DMA0CFGH = (uint8)( (uint16)(a) >> 8 );   \
    DMA0CFGL = (uint8)( (uint16)(a) & 0xFF ); \
  )

#define HAL_DMA_SET_ADDR_DESC1234( a ) \
  st( \
    DMA1CFGH = (uint8)( (uint16)(a) >> 8 );  \
    DMA1CFGL = (uint8)( (uint16)(a) & 0xFF); \
  )

#define HAL_DMA_GET_DESC0()           &dmaCh0

#define HAL_DMA_GET_DESC1234( a )     (dmaCh1234+((a)-1))

#define HAL_DMA_ARM_CH( ch )           DMAARM = (0x01 << (ch))

#define HAL_DMA_CH_ARMED( ch )        (DMAARM & (0x01 << (ch)))

#define HAL_DMA_ABORT_CH( ch )         DMAARM = (0x80 | (0x01 << (ch)))
#define HAL_DMA_MAN_TRIGGER( ch )      DMAREQ = (0x01 << (ch))
#define HAL_DMA_START_CH( ch )         HAL_DMA_MAN_TRIGGER( (ch) )

#define HAL_DMA_CLEAR_IRQ( ch )        DMAIRQ &= ~( 1 << (ch) )

#define HAL_DMA_CHECK_IRQ( ch )       (DMAIRQ & ( 1 << (ch) ))

// Macro for quickly setting the source address of a DMA structure.
#define HAL_DMA_SET_SOURCE( pDesc, src ) \
  st( \
    pDesc->srcAddrH = (uint8)((uint16)(src) >> 8); \
    pDesc->srcAddrL = (uint8)((uint16)(src) & 0xFF); \
  )

// Macro for quickly setting the destination address of a DMA structure.
#define HAL_DMA_SET_DEST( pDesc, dst ) \
  st( \
    pDesc->dstAddrH = (uint8)((uint16)(dst) >> 8); \
    pDesc->dstAddrL = (uint8)((uint16)(dst) & 0xFF); \
  )

// Macro for quickly setting the number of bytes to be transferred by the DMA,
// max length is 0x1FFF.
#define HAL_DMA_SET_LEN( pDesc, len ) \
  st( \
    pDesc->xferLenL = (uint8)(uint16)(len); \
    pDesc->xferLenV &= ~HAL_DMA_LEN_H; \
    pDesc->xferLenV |= (uint8)((uint16)(len) >> 8); \
  )

#define HAL_DMA_GET_LEN( pDesc ) \
  (((uint16)(pDesc->xferLenV & HAL_DMA_LEN_H) << 8) | pDesc->xferLenL)

#define HAL_DMA_SET_VLEN( pDesc, vMode ) \
  st( \
    pDesc->xferLenV &= ~HAL_DMA_LEN_V; \
    pDesc->xferLenV |= (vMode << 5); \
  )

#define HAL_DMA_SET_WORD_SIZE( pDesc, xSz ) \
  st( \
    pDesc->ctrlA &= ~HAL_DMA_WORD_SIZE; \
    pDesc->ctrlA |= (xSz << 7); \
  )

#define HAL_DMA_SET_TRIG_MODE( pDesc, tMode ) \
  st( \
    pDesc->ctrlA &= ~HAL_DMA_TRIG_MODE; \
    pDesc->ctrlA |= (tMode << 5); \
  )

#define HAL_DMA_GET_TRIG_MODE( pDesc ) ((pDesc->ctrlA >> 5) & 0x3)

#define HAL_DMA_SET_TRIG_SRC( pDesc, tSrc ) \
  st( \
    pDesc->ctrlA &= ~HAL_DMA_TRIG_SRC; \
    pDesc->ctrlA |= tSrc; \
  )

#define HAL_DMA_SET_SRC_INC( pDesc, srcInc ) \
  st( \
    pDesc->ctrlB &= ~HAL_DMA_SRC_INC; \
    pDesc->ctrlB |= (srcInc << 6); \
  )

#define HAL_DMA_SET_DST_INC( pDesc, dstInc ) \
  st( \
    pDesc->ctrlB &= ~HAL_DMA_DST_INC; \
    pDesc->ctrlB |= (dstInc << 4); \
  )

#define HAL_DMA_SET_IRQ( pDesc, enable ) \
  st( \
    pDesc->ctrlB &= ~HAL_DMA_IRQ_MASK; \
    pDesc->ctrlB |= (enable << 3); \
  )

#define HAL_DMA_SET_M8( pDesc, m8 ) \
  st( \
    pDesc->ctrlB &= ~HAL_DMA_M8; \
    pDesc->ctrlB |= (m8 << 2); \
  )

#define HAL_DMA_SET_PRIORITY( pDesc, pri ) \
  st( \
    pDesc->ctrlB &= ~HAL_DMA_PRIORITY; \
    pDesc->ctrlB |= pri; \
  )

/*********************************************************************
 * CONSTANTS
 */

// Use LEN for transfer count
#define HAL_DMA_VLEN_USE_LEN            0x00
// Transfer the first byte + the number of bytes indicated by the first byte
#define HAL_DMA_VLEN_1_P_VALOFFIRST     0x01
// Transfer the number of bytes indicated by the first byte (starting with the first byte)
#define HAL_DMA_VLEN_VALOFFIRST         0x02
// Transfer the first byte + the number of bytes indicated by the first byte + 1 more byte
#define HAL_DMA_VLEN_1_P_VALOFFIRST_P_1 0x03
// Transfer the first byte + the number of bytes indicated by the first byte + 2 more bytes
#define HAL_DMA_VLEN_1_P_VALOFFIRST_P_2 0x04

#define HAL_DMA_WORDSIZE_BYTE           0x00 /* Transfer a byte at a time. */
#define HAL_DMA_WORDSIZE_WORD           0x01 /* Transfer a 16-bit word at a time. */

#define HAL_DMA_TMODE_SINGLE            0x00 /* Transfer a single byte/word after each DMA trigger. */
#define HAL_DMA_TMODE_BLOCK             0x01 /* Transfer block of data (length len) after each DMA trigger. */
#define HAL_DMA_TMODE_SINGLE_REPEATED   0x02 /* Transfer single byte/word (after len transfers, rearm DMA). */
#define HAL_DMA_TMODE_BLOCK_REPEATED    0x03 /* Transfer block of data (after len transfers, rearm DMA). */

#define HAL_DMA_TRIG_NONE           0   /* No trigger, setting DMAREQ.DMAREQx bit starts transfer. */
#define HAL_DMA_TRIG_PREV           1   /* DMA channel is triggered by completion of previous channel. */
#define HAL_DMA_TRIG_T1_CH0         2   /* Timer 1, compare, channel 0. */
#define HAL_DMA_TRIG_T1_CH1         3   /* Timer 1, compare, channel 1. */
#define HAL_DMA_TRIG_T1_CH2         4   /* Timer 1, compare, channel 2. */
#define HAL_DMA_TRIG_T2_COMP        5   /* Timer 2, compare. */
#define HAL_DMA_TRIG_T2_OVFL        6   /* Timer 2, overflow. */
#define HAL_DMA_TRIG_T3_CH0         7   /* Timer 3, compare, channel 0. */
#define HAL_DMA_TRIG_T3_CH1         8   /* Timer 3, compare, channel 1. */
#define HAL_DMA_TRIG_T4_CH0         9   /* Timer 4, compare, channel 0. */
#define HAL_DMA_TRIG_T4_CH1        10   /* Timer 4, compare, channel 1. */
#define HAL_DMA_TRIG_ST            11   /* Sleep Timer compare. */
#define HAL_DMA_TRIG_IOC_0         12   /* Port 0 I/O pin input transition. */
#define HAL_DMA_TRIG_IOC_1         13   /* Port 1 I/O pin input transition. */
#define HAL_DMA_TRIG_URX0          14   /* USART0 RX complete. */
#define HAL_DMA_TRIG_UTX0          15   /* USART0 TX complete. */
#define HAL_DMA_TRIG_URX1          16   /* USART1 RX complete. */
#define HAL_DMA_TRIG_UTX1          17   /* USART1 TX complete. */
#define HAL_DMA_TRIG_FLASH         18   /* Flash data write complete. */
#define HAL_DMA_TRIG_RADIO         19   /* RF packet byte received/transmit. */
#define HAL_DMA_TRIG_ADC_CHALL     20   /* ADC end of a conversion in a sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH0       21   /* ADC end of conversion channel 0 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH1       22   /* ADC end of conversion channel 1 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH2       23   /* ADC end of conversion channel 2 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH3       24   /* ADC end of conversion channel 3 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH4       25   /* ADC end of conversion channel 4 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH5       26   /* ADC end of conversion channel 5 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH6       27   /* ADC end of conversion channel 6 in sequence, sample ready. */
#define HAL_DMA_TRIG_ADC_CH7       28   /* ADC end of conversion channel 7 in sequence, sample ready. */
#define HAL_DMA_TRIG_ENC_DW        29   /* AES encryption processor requests download input data. */
#define HAL_DMA_TRIG_ENC_UP        30   /* AES encryption processor requests upload output data. */

#define HAL_DMA_SRCINC_0         0x00 /* Increment source pointer by 0 bytes/words after each transfer. */
#define HAL_DMA_SRCINC_1         0x01 /* Increment source pointer by 1 bytes/words after each transfer. */
#define HAL_DMA_SRCINC_2         0x02 /* Increment source pointer by 2 bytes/words after each transfer. */
#define HAL_DMA_SRCINC_M1        0x03 /* Decrement source pointer by 1 bytes/words after each transfer. */

#define HAL_DMA_DSTINC_0         0x00 /* Increment destination pointer by 0 bytes/words after each transfer. */
#define HAL_DMA_DSTINC_1         0x01 /* Increment destination pointer by 1 bytes/words after each transfer. */
#define HAL_DMA_DSTINC_2         0x02 /* Increment destination pointer by 2 bytes/words after each transfer. */
#define HAL_DMA_DSTINC_M1        0x03 /* Decrement destination pointer by 1 bytes/words after each transfer. */

#define HAL_DMA_IRQMASK_DISABLE  0x00 /* Disable interrupt generation. */
#define HAL_DMA_IRQMASK_ENABLE   0x01 /* Enable interrupt generation upon DMA channel done. */

#define HAL_DMA_M8_USE_8_BITS    0x00 /* Use all 8 bits for transfer count. */
#define HAL_DMA_M8_USE_7_BITS    0x01 /* Use 7 LSB for transfer count. */

#define HAL_DMA_PRI_LOW          0x00 /* Low, CPU has priority. */
#define HAL_DMA_PRI_GUARANTEED   0x01 /* Guaranteed, DMA at least every second try. */
#define HAL_DMA_PRI_HIGH         0x02 /* High, DMA has priority. */
#define HAL_DMA_PRI_ABSOLUTE     0x03 /* Highest, DMA has priority. Reserved for DMA port access.. */

#define HAL_DMA_MAX_ARM_CLOCKS   45   // Maximum number of clocks required if arming all 5 at once.

/*********************************************************************
 * TYPEDEFS
 */

// Bit fields of the 'lenModeH'
#define HAL_DMA_LEN_V     0xE0
#define HAL_DMA_LEN_H     0x1F

// Bit fields of the 'ctrlA'
#define HAL_DMA_WORD_SIZE 0x80
#define HAL_DMA_TRIG_MODE 0x60
#define HAL_DMA_TRIG_SRC  0x1F

// Bit fields of the 'ctrlB'
#define HAL_DMA_SRC_INC   0xC0
#define HAL_DMA_DST_INC   0x30
#define HAL_DMA_IRQ_MASK  0x08
#define HAL_DMA_M8        0x04
#define HAL_DMA_PRIORITY  0x03

typedef struct {
  uint8 srcAddrH;
  uint8 srcAddrL;
  uint8 dstAddrH;
  uint8 dstAddrL;
  uint8 xferLenV;
  uint8 xferLenL;
  uint8 ctrlA;
  uint8 ctrlB;
} halDMADesc_t;

/*********************************************************************
 * GLOBAL VARIABLES
 */

extern halDMADesc_t dmaCh0;
extern halDMADesc_t dmaCh1234[4];

/*********************************************************************
 * FUNCTIONS - API
 */

void HalDmaInit( void );

#endif  // #if (defined HAL_DMA) && (HAL_DMA == TRUE)

#ifdef __cplusplus
}
#endif

#endif  // #ifndef HAL_DMA_H

/******************************************************************************
******************************************************************************/
