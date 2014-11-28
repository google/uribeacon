/**************************************************************************************************
  Filename:       hal_aes.c
  Revised:        $Date: 2009-10-29 16:57:32 -0700 (Thu, 29 Oct 2009) $
  Revision:       $Revision: 21020 $

  Description:    Support for Hardware AES encryption.

  Copyright 2007-2009 Texas Instruments Incorporated. All rights reserved.

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

/******************************************************************************
 * INCLUDES
 */
#include "osal.h"
#include "hal_aes.h"
#include "hal_dma.h"

/******************************************************************************
 * MACROS
 */

/******************************************************************************
 * CONSTANTS
 */

/******************************************************************************
 * TYPEDEFS
 */

/******************************************************************************
 * LOCAL VARIABLES
 */

/******************************************************************************
 * GLOBAL VARIABLES
 */

void (*pSspAesEncrypt)( uint8 *, uint8 * ) = (void*)NULL;

/******************************************************************************
 * FUNCTION PROTOTYPES
 */
void aesDmaInit( void );

#if ((defined HAL_DMA) && (HAL_DMA == TRUE))
/******************************************************************************
 * @fn      aesDmaInit
 *
 * @brief   Initilize DMA for AES engine
 *
 * input parameters
 *
 * @param   None
 *
 * @return  None
 */
void aesDmaInit( void )
{
  halDMADesc_t *ch;

  /* Fill in DMA channel 1 descriptor and define it as input */
  ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_IN );
  HAL_DMA_SET_DEST( ch, HAL_AES_IN_ADDR );              /* Input of the AES module */
  HAL_DMA_SET_VLEN( ch, HAL_DMA_VLEN_USE_LEN );         /* Using the length field */
  HAL_DMA_SET_WORD_SIZE( ch, HAL_DMA_WORDSIZE_BYTE );   /* One byte is transferred each time */
  HAL_DMA_SET_TRIG_MODE( ch, HAL_DMA_TMODE_SINGLE );    /* A single byte is transferred each time */
  HAL_DMA_SET_TRIG_SRC( ch, HAL_DMA_TRIG_ENC_DW );      /* Setting the AES module to generate the DMA trigger */
  HAL_DMA_SET_SRC_INC( ch, HAL_DMA_SRCINC_1 );          /* The address for data fetch is incremented by 1 byte */
  HAL_DMA_SET_DST_INC( ch, HAL_DMA_DSTINC_0 );          /* The destination address is constant */
  HAL_DMA_SET_IRQ( ch, HAL_DMA_IRQMASK_DISABLE );       /* The DMA complete interrupt flag is not set at completion */
  HAL_DMA_SET_M8( ch, HAL_DMA_M8_USE_8_BITS );          /* Transferring all 8 bits in each byte */
  HAL_DMA_SET_PRIORITY( ch, HAL_DMA_PRI_HIGH );         /* DMA has priority */

  /* Fill in DMA channel 2 descriptor and define it as output */
  ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_OUT );
  HAL_DMA_SET_SOURCE( ch, HAL_AES_OUT_ADDR );           /* Start address of the segment */
  HAL_DMA_SET_VLEN( ch, HAL_DMA_VLEN_USE_LEN );         /* Using the length field */
  HAL_DMA_SET_WORD_SIZE( ch, HAL_DMA_WORDSIZE_BYTE );   /* One byte is transferred each time */
  HAL_DMA_SET_TRIG_MODE( ch, HAL_DMA_TMODE_SINGLE );    /* A single byte is transferred each time */
  HAL_DMA_SET_TRIG_SRC( ch, HAL_DMA_TRIG_ENC_UP );      /* Setting the AES module to generate the DMA trigger */
  HAL_DMA_SET_SRC_INC( ch, HAL_DMA_SRCINC_0 );          /* The address for data fetch is constant */
  HAL_DMA_SET_DST_INC( ch, HAL_DMA_DSTINC_1 );          /* The destination address is incremented by 1 byte */
  HAL_DMA_SET_IRQ( ch, HAL_DMA_IRQMASK_DISABLE );       /* The DMA complete interrupt flag is not set at completion */
  HAL_DMA_SET_M8( ch, HAL_DMA_M8_USE_8_BITS );          /* Transferring all 8 bits in each byte */
  HAL_DMA_SET_PRIORITY( ch, HAL_DMA_PRI_HIGH );         /* DMA has priority */
}
#endif

#if (!defined HAL_AES_DMA) || (HAL_AES_DMA == FALSE)
/******************************************************************************
 * @fn      AesLoadBlock
 *
 * @brief   Write a block to AES engine
 *
 * input parameters
 *
 * @param   ptr  - Pointer to date to be written.
 *
 * @return  None
 */
void AesLoadBlock( uint8 *ptr )
{
  uint8 i;

  /* Kick it off */
  AES_START();

  /* Copy block to encryption input register */
  for (i = 0; i < STATE_BLENGTH; i++)
  {
    ENCDI = *ptr++;
  }
}

/******************************************************************************
 * @fn      AesStartBlock
 *
 * @brief   Write and read a block to and from the AES engine
 *
 * input parameters
 *
 * @param   out  - Pointer to result to be read.
 *          in   - pointer to data to be written.
 *
 * @return  None
 */
void AesStartBlock( uint8 *out, uint8 *in )
{
  uint8 i;

  /* Kick it off */
  AES_START();

  /* Copy data to encryption input register */
  for (i = 0; i < STATE_BLENGTH; i++)
  {
    ENCDI = *in++;
  }

  /* Delay is required for non-DMA AES */
  HAL_AES_DELAY();

  /* Copy encryption output register to out */
  for (i = 0; i < STATE_BLENGTH; i++)
  {
    *out++ = ENCDO;
  }
}

/******************************************************************************
 * @fn      AesStartShortBlock
 *
 * @brief   Write and read a block to and from the AES engine. When using CFB,
 *          OFB, and CTR mode, the 128 bits blocks are divided into four 32 bit
 *          blocks.
 *
 * input parameters
 *
 * @param   out  - Pointer to result to be read.
 *          in   - pointer to data to be written.
 *
 * @return  None
 */
void AesStartShortBlock( uint8 *out, uint8 *in )
{
  uint8 i, j;

  AES_START();
  for (i = 0; i < 4; i++)
  {
    /* Copy in block to encryption input register */
    for (j = 0; j < 4; j++)
    {
      ENCDI = *in++;
    }

    /* Delay is required for non-DMA AES */
    HAL_AES_DELAY();

    /* Copy encryptioon output register to out block */
    for (j = 0; j < 4; j++)
    {
      *out++ = ENCDO;
    }
  }
}
#endif

/******************************************************************************
 * @fn      AesLoadIV
 *
 * @brief   Writes IV into the CC2540
 *
 * input parameters
 *
 * @param   IV  - Pointer to IV.
 *
 * @return  None
 */
void AesLoadIV( uint8 *IV )
{
#if (defined HAL_AES_DMA) && (HAL_AES_DMA == TRUE)
  halDMADesc_t *ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_IN );

  /* Modify descriptors for channel 1 */
  HAL_DMA_SET_SOURCE( ch, IV );
  HAL_DMA_SET_LEN( ch, STATE_BLENGTH );

  /* Arm DMA channel 1 */
  HAL_DMA_CLEAR_IRQ( HAL_DMA_AES_IN );
  HAL_DMA_ARM_CH( HAL_DMA_AES_IN );
  do {
    asm("NOP");
  } while (!HAL_DMA_CH_ARMED(HAL_DMA_AES_IN));

  /* Set AES mode */
  AES_SET_ENCR_DECR_KEY_IV( AES_LOAD_IV );

  /* Kick it off, block until AES is ready */
  AES_START();
  while( !(ENCCS & 0x08) );
#else
  /* Set AES mode */
  AES_SET_ENCR_DECR_KEY_IV( AES_LOAD_IV );

  /* Load the block */
  AesLoadBlock( IV );
#endif
}

/******************************************************************************
 * @fn      AesLoadKey
 *
 * @brief   Writes the key into the CC2540
 *
 * input parameters
 *
 * @param   AesKey  - Pointer to AES Key.
 *
 * @return  None
 */
void AesLoadKey( uint8 *AesKey )
{
#if (defined HAL_AES_DMA) && (HAL_AES_DMA == TRUE)
  halDMADesc_t *ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_IN );

  /* Modify descriptors for channel 1 */
  HAL_DMA_SET_SOURCE( ch, AesKey );
  HAL_DMA_SET_LEN( ch, KEY_BLENGTH );

  /* Arm DMA channel 1 */
  HAL_DMA_CLEAR_IRQ( HAL_DMA_AES_IN );
  HAL_DMA_ARM_CH( HAL_DMA_AES_IN );
  do {
    asm("NOP");
  } while (!HAL_DMA_CH_ARMED(HAL_DMA_AES_IN));

  /* Set AES mode */
  AES_SET_ENCR_DECR_KEY_IV( AES_LOAD_KEY );

  /* Kick it off, block until AES is ready */
  AES_START();
  while( !(ENCCS & 0x08) );
#else
  /* Set AES mode */
  AES_SET_ENCR_DECR_KEY_IV( AES_LOAD_KEY );

  /* Load the block */
  AesLoadBlock( AesKey );
#endif
}

#if (defined HAL_AES_DMA) && (HAL_AES_DMA == TRUE)
/******************************************************************************
 * @fn      AesDmaSetup
 *
 * @brief   Sets up DMA of 16 byte block using CC2540 HW aes encryption engine
 *
 * input parameters
 *
 * @param   Cstate  - Pointer to output data.
 * @param   msg_out_len - message out length
 * @param   msg_in  - pointer to input data.
 * @param   msg_in_len - message in length
 *
 * output parameters
 *
 * @param   Cstate  - Pointer to encrypted data.
 *
 * @return  None
 *
 */
void AesDmaSetup( uint8 *Cstate, uint16 msg_out_len, uint8 *msg_in, uint16 msg_in_len )
{
  halDMADesc_t *ch;

  /* Modify descriptors for channel 1 */
  ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_IN );
  HAL_DMA_SET_SOURCE( ch, msg_in );
  HAL_DMA_SET_LEN( ch, msg_in_len );

  /* Modify descriptors for channel 2 */
  ch = HAL_DMA_GET_DESC1234( HAL_DMA_AES_OUT );
  HAL_DMA_SET_DEST( ch, Cstate );
  HAL_DMA_SET_LEN( ch, msg_out_len );

  /* Arm DMA channels 1 and 2 */
  HAL_DMA_CLEAR_IRQ( HAL_DMA_AES_IN );
  HAL_DMA_ARM_CH( HAL_DMA_AES_IN );
  do {
    asm("NOP");
  } while (!HAL_DMA_CH_ARMED(HAL_DMA_AES_IN));
  HAL_DMA_CLEAR_IRQ( HAL_DMA_AES_OUT );
  HAL_DMA_ARM_CH( HAL_DMA_AES_OUT );
  do {
    asm("NOP");
  } while (!HAL_DMA_CH_ARMED(HAL_DMA_AES_OUT));
}
#endif

/******************************************************************************
 * @fn      HalAesInit
 *
 * @brief   Initilize AES engine
 *
 * input parameters
 *
 * @param   None
 *
 * @return  None
 */
void HalAesInit( void )
{
#if (defined HAL_AES_DMA) && (HAL_AES_DMA == TRUE)
  /* Init DMA channels 1 and 2 */
  aesDmaInit();
#endif
}

/******************************************************************************
 * @fn      ssp_HW_KeyInit
 *
 * @brief   Writes the key into AES engine
 *
 * input parameters
 *
 * @param   AesKey  - Pointer to AES Key.
 *
 * @return  None
 */
void ssp_HW_KeyInit( uint8 *AesKey )
{
  AES_SETMODE(ECB);
  AesLoadKey( AesKey );
}

/******************************************************************************
 * @fn      sspAesEncryptHW
 *
 * @brief   Encrypts 16 byte block using AES encryption engine
 *
 * input parameters
 *
 * @param   AesKey  - Pointer to AES Key.
 * @param   Cstate  - Pointer to input data.
 *
 * output parameters
 *
 * @param   Cstate  - Pointer to encrypted data.
 *
 * @return  None
 *
 */
void sspAesEncryptHW( uint8 *AesKey, uint8 *Cstate )
{
  (void)AesKey;

#if (defined HAL_AES_DMA) && (HAL_AES_DMA == TRUE)
  /* Setup DMA for AES encryption */
  AesDmaSetup( Cstate, STATE_BLENGTH, Cstate, STATE_BLENGTH );
  AES_SET_ENCR_DECR_KEY_IV( AES_ENCRYPT );

  /* Kick it off, block until DMA is done */
  AES_START();
  while( !HAL_DMA_CHECK_IRQ( HAL_DMA_AES_OUT ) );
#else
  /* Set ECB mode for AES encryption */
  AES_SETMODE(ECB);
  AES_SET_ENCR_DECR_KEY_IV( AES_ENCRYPT );

  /* Load and start the block */
  AesStartBlock( Cstate, Cstate );
#endif
}
