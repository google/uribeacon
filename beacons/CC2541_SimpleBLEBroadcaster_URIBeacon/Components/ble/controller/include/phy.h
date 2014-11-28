/*******************************************************************************
  Filename:       phy.h
  Revised:        $Date: 2013-10-22 09:00:44 -0700 (Tue, 22 Oct 2013) $
  Revision:       $Revision: 35750 $

  Description:    This is the header file of Physical Layer (PHY) module. All
                  the exported functions and data types will be defined here.

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
#ifndef PHY_H
#define PHY_H

#ifdef __cplusplus
extern "C"
{
#endif

/*******************************************************************************
 * INCLUDES
 */
#include "bcomdef.h"
#include "hal_types.h"
#include "hal_defs.h"
#include "hal_mcu.h"

#if defined( CC2541) || defined( CC2541S )
#include "ioCC2541_extra.h"
#else // CC2540
#include "ioCC2540_extra.h"
#endif // CC2541 || CC2541S

/*******************************************************************************
 * MACROS
 */

#if !defined(USE_PHY_FUNCS)

// default to PHY macros to speed up processing

#define PHY_GET_CHIP_ID()           (*(P_INFOPAGE+3))
#define PHY_GET_CHIP_VER()          (CHVER)

#define PHY_WRITE_BYTE( data, len )                                            \
  {                                                                            \
    uint8 z;                                                                   \
    for (z=0; z<(len); z++) { RFD = (data)[z]; }                               \
  }

#define PHY_WRITE_BYTE_VAL( value ) (RFD = (value))

#define PHY_READ_BYTE( ptr, len )                                              \
  st( uint8 z;                                                                 \
      if ( (ptr) == NULL )                                                     \
      {                                                                        \
        for (z=0; z<(len); z++)                                                \
        { RFD; }                                                               \
      }                                                                        \
      else                                                                     \
      {                                                                        \
        for (z=0; z<(len); z++)                                                \
        { (ptr)[z] = RFD; }                                                    \
      }                                                                        \
    )

#define PHY_READ_BYTE_VAL()         (RFD)

#define PHY_RESET_TX_FIFO()         (RFST = PHY_FIFO_TX_RESET)

#define PHY_RESET_RX_FIFO()         (RFST = PHY_FIFO_RX_RESET)

#define PHY_RESET_TXRX_FIFO()       (RFST = PHY_FIFO_RESET)

#define PHY_COMMAND( cmd )                                                     \
  st( while( RFST != 0 );                                                      \
      if ( (cmd) != PHY_CMD_INVALID )                                          \
      {                                                                        \
        RFST = (cmd);                                                          \
      }                                                                        \
    )

#define PHY_CLEAR_COUNTERS()                                                   \
  st( BLE_NTXDONE     = 0;                                                     \
      BLE_NTXACK      = 0;                                                     \
      BLE_NTXCTRLACK  = 0;                                                     \
      BLE_NTXCTRL     = 0;                                                     \
      BLE_NTXRETRANS  = 0;                                                     \
      BLE_NTX         = 0;                                                     \
      BLE_NRXOK       = 0;                                                     \
      BLE_NRXCTRL     = 0;                                                     \
      BLE_NRXNOK      = 0;                                                     \
      BLE_NRXIGNORED  = 0;                                                     \
      BLE_NRXEMPTY    = 0;                                                     \
      BLE_NRXFIFOFULL = 0;                                                     \
    )

#define PHY_READ_COUNTERS( ptr )                                               \
  st( (*((ptr)+0))  = BLE_NTXDONE;                                             \
      (*((ptr)+1))  = BLE_NTXACK;                                              \
      (*((ptr)+2))  = BLE_NTXCTRLACK;                                          \
      (*((ptr)+3))  = BLE_NTXCTRL;                                             \
      (*((ptr)+4))  = BLE_NTXRETRANS;                                          \
      (*((ptr)+5))  = BLE_NTX;                                                 \
      (*((ptr)+6))  = BLE_NRXOK;                                               \
      (*((ptr)+7))  = BLE_NRXCTRL;                                             \
      (*((ptr)+8))  = BLE_NRXNOK;                                              \
      (*((ptr)+9))  = BLE_NRXIGNORED;                                          \
      (*((ptr)+10)) = BLE_NRXEMPTY;                                            \
      (*((ptr)+11)) = BLE_NRXFIFOFULL;                                         \
    )

#define PHY_TX_FIFO_LEN()           (RFTXFLEN)

#define PHY_RX_FIFO_LEN()           (RFRXFLEN)

#define PHY_INIT_LAST_RSSI()        (BLE_LAST_RSSI = PHY_RSSI_VALUE_INVALID)

#define PHY_GET_LAST_RSSI()         (BLE_LAST_RSSI)

#define PHY_GET_RSSI()              (RSSI)

#define PHY_CLR_WINCONF()           (BLE_WINCONF = 0);

#define PHY_SET_DYN_WINOFFSET( winOffset, connInterval )                       \
  st( BLE_WINOFFSET_0 = (winOffset) & 0xFF;                                    \
      BLE_WINOFFSET_1 = (winOffset) >> 8;                                      \
      BLE_WINMOD_0    = (connInterval) & 0xFF;                                 \
      BLE_WINMOD_1    = (connInterval) >> 8;                                   \
      BLE_WINCONF     = 0xC1;                                                  \
    )

#define PHY_TX_FIFO_BYTES_FREE()    (PHY_FIFO_SIZE - RFTXFLEN)

#define PHY_COMMIT_TX_FIFO()        (RFST = PHY_FIFO_TX_COMMIT)

#define PHY_ANCHOR_VALID()          (BLE_ANCHOR_VALID)

#define PHY_GET_DATA_CHAN()         (BLE_CHAN)

#else // PHY functions

// PHY functions
#define PHY_GET_CHIP_ID()                   PHY_GetChipID()
#define PHY_GET_CHIP_VER()                  PHY_GetChipVer()
#define PHY_WRITE_BYTE( data, len )         PHY_WriteByte( (data), (len) )
#define PHY_WRITE_BYTE_VAL( value )         PHY_WriteByteVal( (value) )
#define PHY_READ_BYTE( ptr, len )           PHY_ReadByte( (ptr), (len) )
#define PHY_READ_BYTE_VAL()                 PHY_ReadByteVal()
#define PHY_RESET_TX_FIFO()                 PHY_ResetTxFifo()
#define PHY_RESET_RX_FIFO()                 PHY_ResetRxFifo()
#define PHY_RESET_TXRX_FIFO()               PHY_ResetTxRxFifo()
#define PHY_COMMAND( cmd )                  PHY_Command( (cmd) )
#define PHY_CLEAR_COUNTERS()                PHY_ClearCounters()
#define PHY_READ_COUNTERS( ptr )            PHY_ReadCounters( (ptr) )
#define PHY_TX_FIFO_LEN()                   PHY_TxFifoLen()
#define PHY_RX_FIFO_LEN()                   PHY_RxFifoLen()
#define PHY_INIT_LAST_RSSI()                PHY_InitLastRssi()
#define PHY_GET_LAST_RSSI()                 PHY_GetLastRssi()
#define PHY_GET_RSSI()                      PHY_GetRssi()
#define PHY_CLR_WINCONF()                   PHY_ClearWindowConfig()
#define PHY_SET_DYN_WINOFFSET( winOff, ci ) PHY_SetDynamicWinOffset( (winOff), (ci) )
#define PHY_TX_FIFO_BYTES_FREE()            PHY_TxFifoBytesFree()
#define PHY_COMMIT_TX_FIFO()                PHY_CommitTxFifo()
#define PHY_GET_DATA_CHAN()                 PHY_GetDataChan()

#endif // !USE_PHY_FUNCS

/*******************************************************************************
 * CONSTANTS
 */

// Chip ID
#define PHY_CHIP_ID_CC2540    0x8D
#define PHY_CHIP_ID_CC2541    0x41
#define PHY_CHIP_ID_CC2541S   0x42

// Chip Version
#define PHY_CHIP_VER_C        0x12
#define PHY_CHIP_VER_D        0x13

// FIFO Size & Addresses
#define PHY_DATA_BANK_PTR     RFCORE_RAM
#define PHY_RX_FIFO_PTR       RXFIFO
#define PHY_TX_FIFO_PTR       TXFIFO

// nR RAM Registers
#define BLE_REG_BASE_ADDR     (0x6000)
#define BLE_RX_FIFO_BASE_ADDR (0x6080)
#define BLE_TX_FIFO_BASE_ADDR (0x6100)
#define BLE_BANK_SIZE         128
#define BLE_MAX_TX_FIFO_SIZE  128
#define BLE_MAX_RX_FIFO_SIZE  128

#define BLE_CHAN           XREG((BLE_REG_BASE_ADDR  + 0))   // BLE_LSI_CHAN
#define BLE_CHANMAP        XREG((BLE_REG_BASE_ADDR  + 1))   // BLE_A_CHANMAP
#define BLE_FIFO_CONF      XREG((BLE_REG_BASE_ADDR  + 2))   // BLE_FIFO_CONF
#define BLE_CONF           XREG((BLE_REG_BASE_ADDR  + 3))   // BLE_L_CONF
#define BLE_MAXNACK        XREG((BLE_REG_BASE_ADDR  + 4))   // BLE_L_MAXNACK

#define BLE_CRCINIT_0      XREG((BLE_REG_BASE_ADDR  + 5))   // BLE_L_CRCINIT
#define BLE_CRCINIT_1      XREG((BLE_REG_BASE_ADDR  + 6))   // BLE_L_CRCINIT
#define BLE_CRCINIT_2      XREG((BLE_REG_BASE_ADDR  + 7))   // BLE_L_CRCINIT

#define BLE_SEQSTAT        XREG((BLE_REG_BASE_ADDR  + 8))   // BLE_L_SEQSTAT

#define BLE_BLACKLIST      XREG((BLE_REG_BASE_ADDR  + 9))   // BLE_S_BLACKLIST

#define BLE_BACKOFFCNT_0   XREG((BLE_REG_BASE_ADDR  + 10))  // BLE_S_BACKOFFCNT
#define BLE_BACKOFFCNT_1   XREG((BLE_REG_BASE_ADDR  + 11))  // BLE_S_BACKOFFCNT

#define BLE_SCANCONF       XREG((BLE_REG_BASE_ADDR  + 12))  // BLE_S_CONF

#define BLE_WINCONF        XREG((BLE_REG_BASE_ADDR  + 13))  // BLE_I_WIN_CONF

#define BLE_WINOFFSET_0    XREG((BLE_REG_BASE_ADDR  + 14))  // BLE_I_WIN_OFFSET
#define BLE_WINOFFSET_1    XREG((BLE_REG_BASE_ADDR  + 15))  // BLE_I_WIN_OFFSET

#define BLE_WINMOD_0       XREG((BLE_REG_BASE_ADDR  + 16))  // BLE_I_WIN_MOD
#define BLE_WINMOD_1       XREG((BLE_REG_BASE_ADDR  + 17))  // BLE_I_WIN_MOD

#define BLE_MAX_LEN        XREG((BLE_REG_BASE_ADDR  + 18))  // BLE_MAX_LEN
#define BLE_LEN_BIT_MASK   XREG((BLE_REG_BASE_ADDR  + 19))  // BLE_LEN_BIT_MASK

#define BLE_WLPOLICY       XREG((BLE_REG_BASE_ADDR  + 20))  // BLE_ASI_WLPOLICY
#define BLE_ADDRTYPE       XREG((BLE_REG_BASE_ADDR  + 21))  // BLE_ASI_ADDRTYPE

#define BLE_OWNADDR_0      XREG((BLE_REG_BASE_ADDR  + 22))  // BLE_ASI_OWNADDR
#define BLE_OWNADDR_1      XREG((BLE_REG_BASE_ADDR  + 23))  // BLE_ASI_OWNADDR
#define BLE_OWNADDR_2      XREG((BLE_REG_BASE_ADDR  + 24))  // BLE_ASI_OWNADDR
#define BLE_OWNADDR_3      XREG((BLE_REG_BASE_ADDR  + 25))  // BLE_ASI_OWNADDR
#define BLE_OWNADDR_4      XREG((BLE_REG_BASE_ADDR  + 26))  // BLE_ASI_OWNADDR
#define BLE_OWNADDR_5      XREG((BLE_REG_BASE_ADDR  + 27))  // BLE_ASI_OWNADDR

#define BLE_PEERADDR_0     XREG((BLE_REG_BASE_ADDR  + 28))  // BLE_AI_PEERADDR
#define BLE_PEERADDR_1     XREG((BLE_REG_BASE_ADDR  + 29))  // BLE_AI_PEERADDR
#define BLE_PEERADDR_2     XREG((BLE_REG_BASE_ADDR  + 30))  // BLE_AI_PEERADDR
#define BLE_PEERADDR_3     XREG((BLE_REG_BASE_ADDR  + 31))  // BLE_AI_PEERADDR
#define BLE_PEERADDR_4     XREG((BLE_REG_BASE_ADDR  + 32))  // BLE_AI_PEERADDR
#define BLE_PEERADDR_5     XREG((BLE_REG_BASE_ADDR  + 33))  // BLE_AI_PEERADDR

#define BLE_WLVALID        XREG((BLE_REG_BASE_ADDR  + 34))  // BLE_ASI_WLVALID
#define BLE_WLADDRTYPE     XREG((BLE_REG_BASE_ADDR  + 35))  // BLE_ASI_WLADTYPE
#define BLE_WL_PTR         PXREG((BLE_REG_BASE_ADDR + 36))  // BLE_ASI_WHITELIST0..BLE_ASI_WHITELIST7

#define BLE_NTXDONE        XREG((BLE_REG_BASE_ADDR  + 84))  // BLE_L_NTXDONE
#define BLE_NTXACK         XREG((BLE_REG_BASE_ADDR  + 85))  // BLE_L_NTXACK
#define BLE_NTXCTRLACK     XREG((BLE_REG_BASE_ADDR  + 86))  // BLE_L_NTXCTRLACK
#define BLE_NTXCTRL        XREG((BLE_REG_BASE_ADDR  + 87))  // BLE_L_NTXCTRL
#define BLE_NTXRETRANS     XREG((BLE_REG_BASE_ADDR  + 88))  // BLE_L_NTXRETRANS
#define BLE_NTX            XREG((BLE_REG_BASE_ADDR  + 89))  // BLE_NTX
#define BLE_NRXOK          XREG((BLE_REG_BASE_ADDR  + 90))  // BLE_NRXOK
#define BLE_NRXCTRL        XREG((BLE_REG_BASE_ADDR  + 91))  // BLE_L_NRXCTRL
#define BLE_NRXNOK         XREG((BLE_REG_BASE_ADDR  + 92))  // BLE_NRXNOK
#define BLE_NRXIGNORED     XREG((BLE_REG_BASE_ADDR  + 93))  // BLE_NRXIGNORED
#define BLE_NRXEMPTY       XREG((BLE_REG_BASE_ADDR  + 94))  // BLE_L_NRXEMPTY
#define BLE_NRXFIFOFULL    XREG((BLE_REG_BASE_ADDR  + 95))  // BLE_NRXFIFOFULL

////////////////////////////////////////////////////////////////////////////////
// Raw Data Transmit and Receive Only
// Note: Only the following registers and BLE_ENDCAUSE are valid when the nR is
//       configured to operate as a Raw Transmit/Receive task. This task is
//       only used for Direct Test Mode.
//
#define BLE_RAW_CONF       XREG((BLE_REG_BASE_ADDR  + 81))  // BLE_RAW_CONF
#define BLE_RAW_FIFOCMD    XREG((BLE_REG_BASE_ADDR  + 82))  // BLE_RAW_FIFOCMD
#define BLE_RAW_WINIT      XREG((BLE_REG_BASE_ADDR  + 83))  // BLE_RAW_WINIT
#define BLE_RAW_CRCINIT0   XREG((BLE_REG_BASE_ADDR  + 84))  // BLE_RAW_CRCINIT
#define BLE_RAW_CRCINIT1   XREG((BLE_REG_BASE_ADDR  + 85))  // BLE_RAW_CRCINIT
#define BLE_RAW_CRCINIT2   XREG((BLE_REG_BASE_ADDR  + 86))  // BLE_RAW_CRCINIT
#define BLE_RAW_CRCINIT3   XREG((BLE_REG_BASE_ADDR  + 87))  // BLE_RAW_CRCINIT
//
#define BLE_RAW_NTX0       XREG((BLE_REG_BASE_ADDR  + 88))  // BLE_RAW_NTX
#define BLE_RAW_NTX1       XREG((BLE_REG_BASE_ADDR  + 89))  // BLE_RAW_NTX
#define BLE_RAW_NRXOK0     XREG((BLE_REG_BASE_ADDR  + 90))  // BLE_RAW_NRXOK
#define BLE_RAW_NRXOK1     XREG((BLE_REG_BASE_ADDR  + 91))  // BLE_RAW_NRXOK
#define BLE_RAW_NRXNOK0    XREG((BLE_REG_BASE_ADDR  + 92))  // BLE_RAW_NRXNOK
#define BLE_RAW_NRXNOK1    XREG((BLE_REG_BASE_ADDR  + 93))  // BLE_RAW_NRXNOK
//
#define BLE_RAW_TX_CRC_LEN XREG((BLE_REG_BASE_ADDR  + 94))  // BLE_TX_CRC_LENGTH
#define BLE_RAW_RX_LEN     XREG((BLE_REG_BASE_ADDR  + 95))  // BLE_RX_LENGTH
//
////////////////////////////////////////////////////////////////////////////////

#define BLE_LAST_RSSI      XREG((BLE_REG_BASE_ADDR  + 96))  // BLE_RX_RSSI
#define BLE_ENDCAUSE       XREG((BLE_REG_BASE_ADDR  + 97))  // BLE_ENDCAUSE
#define BLE_ANCHOR_VALID   XREG((BLE_REG_BASE_ADDR  + 98))  // BLE_ANCHOR_VALID
#define BLE_DF_BYTES       XREG((BLE_REG_BASE_ADDR  + 99))  // BLE_A_DF_BYTES
#define BLE_RX_TIMEOUT0    XREG((BLE_REG_BASE_ADDR  + 100)) // BLE_L_RXTIMEOUT LSO
#define BLE_RX_TIMEOUT1    XREG((BLE_REG_BASE_ADDR  + 101)) // BLE_L_RXTIMEOUT MSO
#define BLE_RFSYNTHCAL     XREG((BLE_REG_BASE_ADDR  + 102)) // BLE_RFSYNTHCAL

// White List Owner
#define PHY_SCAN_WL                        0
#define PHY_HOST_WL                        1

// RF Constants
#define PHY_FIFO_SIZE                      0x80
#define PHY_RSSI_VALUE_INVALID             0x80
#define PHY_NUM_RF_CHANS                   40
#define PHY_FIRST_RF_CHAN                  0
#define PHY_LAST_RF_CHAN                   (PHY_NUM_RF_CHANS-1)

// RF FIFO Command Values
#define PHY_FIFO_RX_RESET                  0x81
#define PHY_FIFO_RX_DEALLOC                0x82
#define PHY_FIFO_RX_RETRY                  0x83
#define PHY_FIFO_RX_DISCARD                0x84
#define PHY_FIFO_RX_COMMIT                 0x85
//
#define PHY_FIFO_TX_RESET                  0x91
#define PHY_FIFO_TX_DEALLOC                0x92
#define PHY_FIFO_TX_RETRY                  0x93
#define PHY_FIFO_TX_DISCARD                0x94
#define PHY_FIFO_TX_COMMIT                 0x95
//
#define PHY_FIFO_RESET                     0xF1
#define PHY_FIFO_DEALLOC                   0xF2
#define PHY_FIFO_RETRY                     0xF3
#define PHY_FIFO_DISCARD                   0xF4
#define PHY_FIFO_COMMIT                    0xF5

// RF Registers
#define PHY_PN7_WHITENER_BIT               BV(0)
#define PHY_PN9_WHITENER_BIT               BV(1)
//
#define PHY_MOD_NO_TONE                    0x04
#define PHY_MOD_GFSK_250KHZ_1MBPS_TONE     0x24
#define PHY_MOD_GFSK_156KHZ_250KBPS_TONE   0x28
#define PHY_MOD_GFSK_156KHZ_1MBPS_TONE     0x2C
#define PHY_MOD_MSK_250KBPS_TONE           0x30
#define PHY_MOD_MSK_500KBPS_TONE           0x32
#define PHY_MOD_MSK_1MBPS_TONE             0x34
//
#define PHY_CORRELATION_THRESHOLD          0x48
#define PHY_FREQ_OFFSET                    0x40

#if defined( CC2541) || defined( CC2541S )
#define PHY_TX_FILTER_CONFIG               0x03
#define PHY_TX_CTRL_DAC_CURRENT            0x19
#define PHY_TX_IV_CTRL                     0x1B
#define PHY_PREAMBLE_SYNC                  0x02
#define PHY_RX_CHARGE_PUMP_REV_C           0x0D
#define PHY_RX_CHARGE_PUMP_REV_D           0x09
#else // CC2540
#define PHY_TX_FILTER_CONFIG               0x07
#define PHY_TX_CTRL_DAC_CURRENT            0x69
#define PHY_TX_IV_CTRL                     0xFF // Don't Care as Not Used!
#define PHY_PREAMBLE_SYNC                  0x00
#define PHY_COMP_QUAD_SKEW                 0x16
#endif // CC2541 || CC2541S

// Frequency Tuning
#define PHY_FREQ_TUNE_NO_CAP_LOAD          0x0F

// Rx Gain
#define PHY_RX_LNA_GAIN_STD                0x3A
#define PHY_RX_LNA_GAIN_HIGH               0x7F
#define PHY_RX_CTRL_STD                    0x33
#define PHY_RX_CTRL_HIGH                   0x3F
#define PHY_RX_FSCTRL_STD                  0x55
#define PHY_RX_FSCTRL_HIGH                 0x5A
// CC2590
#define PHY_CC2590_HGM_LOW                 0x00
#define PHY_CC2590_HGM_HIGH                0x01

// RSSI Correction Based on Rx Gain
#define PHY_RSSI_DB2DBM_STD_GAIN           0x61
#define PHY_RSSI_DB2DBM_HIGH_GAIN          0x6A

// RSSI Correction Based on Rx Gain for CC2590 RF Range Extender
#define PHY_RSSI_DB2DBM_STD_GAIN_CC2590    0x67
#define PHY_RSSI_DB2DBM_HIGH_GAIN_CC2590   0x71

// Tx Output Power
#if defined( CC2541) || defined( CC2541S )
#define PHY_TX_POWER_REG_MINUS_23_DBM      0x31
#define PHY_TX_POWER_REG_MINUS_6_DBM       0xB1
#define PHY_TX_POWER_REG_0_DBM             0xE1
#define PHY_TX_POWER_REG_4_DBM             0xFF // Don't Care as Not Used!
#else // CC2540
#define PHY_TX_POWER_REG_MINUS_23_DBM      0x05
#define PHY_TX_POWER_REG_MINUS_6_DBM       0x95
#define PHY_TX_POWER_REG_0_DBM             0xD5
#define PHY_TX_POWER_REG_4_DBM             0xF5
#endif // CC2541 || CC2541S

// RF Normal Interrupts
#define PHY_RFIRQ0_TXTHSHDN_BIT            BV(0)
#define PHY_RFIRQ0_RXTHSHDN_BIT            BV(1)
#define PHY_RFIRQ0_TXTHSHUP_BIT            BV(2)
#define PHY_RFIRQ0_RXTHSHUP_BIT            BV(3)
//
#define PHY_RFIRQ1_RXOK_BIT                BV(0)
#define PHY_RFIRQ1_RXCTRL_BIT              BV(1)
#define PHY_RFIRQ1_RXNOK_BIT               BV(2)
#define PHY_RFIRQ1_RXIGNORED_BIT           BV(3)
#define PHY_RFIRQ1_RXEMPTY_BIT             BV(4)
#define PHY_RFIRQ1_TXDONE_BIT              BV(5)
#define PHY_RFIRQ1_TASKDONE_BIT            BV(6)
#define PHY_RFIRQ1_PINGRSP_BIT             BV(7)

// RF Error Interrupts
#define PHY_RFERR_TXUNDERF_BIT             BV(0)
#define PHY_RFERR_RXUNDERF_BIT             BV(1)
#define PHY_RFERR_TXOVERF_BIT              BV(2)
#define PHY_RFERR_RXOVERF_BIT              BV(3)
#define PHY_RFERR_RXTXABO_BIT              BV(4)
#define PHY_RFERR_NRERR_BIT                BV(5)
#define PHY_RFERR_RXFIFOFULL_BIT           BV(6)

/*******************************************************************************
 * TYPEDEFS
 */

typedef unsigned char volatile __xdata * phyXRegPtr_t;
typedef unsigned char volatile __xdata * phyXDataPtr_t;

enum
{
  PHY_STATUS_SUCCESS,
  PHY_STATUS_FAILURE,
  PHY_STATUS_ERROR_BL_CKSUM_FAILURE,
  PHY_STATUS_WL_FULL,
  PHY_STATUS_WL_EMPTY,
  PHY_STATUS_WL_ENTRY_NOT_FOUND,
  PHY_STATUS_WL_ENTRY_FOUND
};
typedef uint8 phyStatus_t;

enum
{
  PHY_SEM_0,
  PHY_SEM_1,
  PHY_SEM_2
};
typedef uint8 phySemId_t;


enum
{
  PHY_ADDR_TYPE_PUBLIC,
  PHY_ADDR_TYPE_RANDOM
};
typedef uint8 phyAddrType_t;


enum
{
  PHY_ADV_CHANNEL_37 = BV(0),
  PHY_ADV_CHANNEL_38 = BV(1),
  PHY_ADV_CHANNEL_39 = BV(2)
};
typedef uint8 phyChanMap_t;


enum
{
  PHY_SCAN_MODE_PASSIVE,
  PHY_SCAN_MODE_ACTIVE
};
typedef uint8 phyScanMode_t;

enum
{
  PHY_SCAN_END_ON_ADV_REPORT_DISABLED,
  PHY_SCAN_END_ON_ADV_REPORT_ENABLED
};
typedef uint8 phyScanEnd_t;

enum
{
  PHY_END_CONN_CONTINUE,                          // set ENDC = 0
  PHY_END_CONN_AFTER_NEXT_PKT                     // set ENDC = 1
};
typedef uint8 phyEndConn_t;

enum
{
  PHY_MD_BIT_FROM_FIFO_ENTRY          = (0 << 1), // use bit in packet
  PHY_MD_BIT_BASED_ON_FIFO_CONTENTS   = (1 << 1), // set MD based on data in TX FIFO
  PHY_MD_BIT_ALWAYS_ZERO              = (2 << 1), // always 0
  PHY_MD_BIT_ALWAYS_ONE               = (3 << 1)  // always 1
};
typedef uint8 phyConfMD_t;

enum
{
  PHY_FIFO_DATA_DIABLE_ALL            = 0,
  PHY_FIFO_DATA_AUTO_FLUSH_IGNORED    = BV(0),
  PHY_FIFO_DATA_AUTO_FLUSH_CRC_ERROR  = BV(1),
  PHY_FIFO_DATA_AUTO_FLUSH_EMPTY_PKT  = BV(2),
  PHY_FIFO_DATA_APPEND_RX_STATUS      = BV(3)
};
typedef uint8 phyFifoData_t;

enum
{
  PHY_FIFO_CONFIG_DISABLE_ALL         = 0,
  PHY_FIFO_CONFIG_AUTO_DEALLOC_RX     = BV(0),
  PHY_FIFO_CONFIG_AUTO_COMMIT_RX      = BV(1),
  PHY_FIFO_CONFIG_AUTO_DEALLOC_TX     = BV(4),
  PHY_FIFO_CONFIG_AUTO_COMMIT_TX      = BV(5)
};
typedef uint8 phyFifoConfig_t;

enum
{
  PHY_ADVERTISER_WHITE_LIST_NONE,
  PHY_ADVERTISER_WHITE_LIST_SCAN_REQ,
  PHY_ADVERTISER_WHITE_LIST_CONNECT_REQ,
  PHY_ADVERTISER_WHITE_LIST_ALL
};
typedef uint8 phyAdvWlPolicy_t;

enum
{
  PHY_SCANNER_ALLOW_ALL_ADV_PKTS,
  PHY_SCANNER_USE_WHITE_LIST
};
typedef uint8 phyScanWlPolicy_t;

enum
{
  PHY_INITIATOR_USE_PEER_ADDR,
  PHY_INITIATOR_USE_WL
};
typedef uint8 phyInitWlPolicy_t;

enum
{
  PHY_SET_BLACKLIST_DISABLE,
  PHY_SET_BLACKLIST_ENABLE
};
typedef uint8 phyBlacklist_t;

enum
{
  PHY_DISABLE_ALL_WHITENER,
  PHY_ENABLE_BLE_WHITENER,
  PHY_DISABLE_BLE_WHITENER,
  PHY_ENABLE_PN9_WHITENER,
  PHY_DISABLE_PN9_WHITENER,
  PHY_ENABLE_ONLY_BLE_WHITENER,
  PHY_ENABLE_ONLY_PN9_WHITENER,
  PHY_ENABLE_ALL_WHITENER
};
typedef uint8 phyWhitener_t;

enum
{
  PHY_ENABLE_APPEND_RF_STATUS,
  PHY_DISABLE_APPEND_RF_STATUS
};
typedef uint8 phyAppendRfStatus_t;

enum
{
  PHY_RF_RAM_BANK_0,
  PHY_RF_RAM_BANK_1,
  PHY_RF_RAM_BANK_2,
  PHY_RF_RAM_BANK_3,
  PHY_RF_RAM_BANK_4,
  PHY_RF_RAM_BANK_5,
  PHY_RF_RAM_RX_FIFO,
  PHY_RF_RAM_TX_FIFO
};
typedef uint8 phyRfRamBank_t;

enum
{
  PHY_CMD_SHUTDOWN      = 1,    // stop operation immediately
  PHY_CMD_DEMOD_TEST    = 2,    // start demodulator w/o sync search
  PHY_CMD_RX_TEST       = 3,    // start demodulator & sync search
  PHY_CMD_TX_TEST       = 4,    // start TX, send zeros
  PHY_CMD_TX_CAL_TEST   = 5,    // calibrate, start TX, send zeros
  PHY_CMD_PING          = 6,    // ping RF

  PHY_CMD_BLE_SLAVE     = 16,   // Slave
  PHY_CMD_BLE_MASTER    = 17,   // Master

  PHY_CMD_BLE_ADV       = 18,   // Connectable Undirected Advertiser
  PHY_CMD_BLE_ADV_DIR   = 19,   // Connectable Directed Advertiser
  PHY_CMD_BLE_ADV_NC    = 20,   // Nonconnectable Advertiser
  PHY_CMD_BLE_ADV_DISC  = 21,   // Discoverable Undirected Advertiser

  PHY_CMD_BLE_SCAN      = 22,   // Scanner
  PHY_CMD_BLE_INITIATOR = 23,   // Initiator

  PHY_CMD_BLE_RX_RAW    = 24,   // RX raw data
  PHY_CMD_BLE_TX_RAW    = 25,   // TX a raw data message

  PHY_CMD_SEND_EVENT1   = 33,   // trigger Timer 2 Event 1
  PHY_CMD_SEND_EVENT2   = 34,   // trigger Timer 2 Event 2

  PHY_CMD_INVALID       = 0xFF
};
typedef uint8 phyCmd_t;

enum
{
  PHY_TASK_ENDOK        = 0,    // task ended normally
  PHY_TASK_RXTIMEOUT    = 1,    // timeout of first RX; slave, scan, init
  PHY_TASK_NOSYNC       = 2,    // timeout of subsequent RX
  PHY_TASK_RXERR        = 3,    // task ended due to RX error (CRC or other)
  PHY_TASK_CONNECT      = 4,    // CONNECT_REQ received; adv
  PHY_TASK_MAXNACK      = 5,    // max number retrans exceeded; master, slave
  PHY_TASK_ABORT        = 6,    // task aborted by command
  //
  // Note: If the following values are changed to non-negative numbers, all
  //       table entry indices for taskErrorEndCauses will have to be changed
  //       as well.
  //
  PHY_TASKERR_RXFIFO    = 250,  // overfull RX FIFO; adv, scan, init
  PHY_TASKERR_TXFIFO    = 251,  // empty TX FIFO when not permitted
  PHY_TASKERR_PAR       = 252,  // illegal parameter
  PHY_TASKERR_SEM       = 253,  // unable to obtain semaphore
  PHY_TASKERR_CMD       = 254,  // unknown command
  PHY_TASKERR_INTERNAL  = 255   // internal program error
};

enum
{
  PHY_FREQ_MODE_MODULATED_CW,
  PHY_FREQ_MODE_UNMODULATED_CW
};
typedef uint8 phyFreqMode_t;

enum
{
  PHY_RAW_RUN_ONCE,
  PHY_RAW_RUN_REPEAT
};
typedef uint8 phyRawRunMode_t;

enum
{
  PHY_RAW_START_IMMEDIATE,
  PHY_RAW_START_ON_TIMER_EVENT
};
typedef uint8 phyRawStartMode_t;

/*******************************************************************************
 * LOCAL VARIABLES
 */

/*******************************************************************************
 * GLOBAL VARIABLES
 */

/*******************************************************************************
 * API
 */

/*******************************************************************************
 * @fn          PHY_Init
 *
 * @brief       Loads the nanoRisc image and initialises PHY module.
 *
 *              Note: This routine does not verify the nR image after loading.
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
void PHY_Init( void );


/*******************************************************************************
 * @fn          PHY_Reset
 *
 * @brief       Holds the nR in Reset, then releases it.
 *
 *              Note: This will clear any nR pending interrupts.
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
void PHY_Reset( void );


/*******************************************************************************
 * @fn          PHY_LoadNR
 *
 * @brief       Copies nanoRisc image from the array to nanoRisc memory
 *              location.
 *
 *              Note: After the load, the nanoRisc is left in reset.
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
void PHY_LoadNR( void );


/*******************************************************************************
 * @fn          PHY_VerifyNR
 *
 * @brief       Verifies that the nanoRisc image in the memory is not corrupted.
 *              Compares the RAM memory data with the image array which is
 *              located in the code section.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      PHY_STATUS_SUCCESS, PHY_STATUS_FAILURE
 */
phyStatus_t PHY_VerifyNR( void );


/*******************************************************************************
 * @fn          PHY_ClearAllRegsAndFifos
 *
 * @brief       Clears the nanoRisc register bank, and the TX and RX FIFO banks.
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
void PHY_ClearAllRegsAndFifos( void );


/*******************************************************************************
 * @fn          PHY_GetSem
 *
 * @brief       Get the specified semaphore.
 *
 * input parameters
 *
 * @param       semId - PHY_SEM_0, PHY_SEM_1, PHY_SEM_2.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      PHY_STATUS_SUCCESS: Semaphore was free and was taken.
 *              PHY_STATUS_FAILURE: Semaphore is not free.
 */
phyStatus_t PHY_GetSem( phySemId_t semId );


/*******************************************************************************
 * @fn          PHY_ReleaseSem
 *
 * @brief       Release the specified semaphore.
 *
 * input parameters
 *
 * @param       semId - PHY_SEM_0, PHY_SEM_1, PHY_SEM_2
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      PHY_STATUS_SUCCESS: Semaphore was released.
 *              PHY_STATUS_FAILURE: Semaphore was already free.
 */
phyStatus_t PHY_ReleaseSem( phySemId_t semId );


/*******************************************************************************
 * @fn          PHY_SetOwnAddr
 *
 * @brief       Set Own device address and address type.
 *
 * input parameters
 *
 * @param       addr     - Pointer to Own device BLE address.
 * @param       addrType - PHY_ADDR_TYPE_PUBLIC, PHY_ADDR_TYPE_RANDOM
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetOwnAddr( uint8         *addr,
                     phyAddrType_t addrType);


/*******************************************************************************
 * @fn          PHY_SetPeerAddr
 *
 * @brief       Set Peer device address and address type.
 *
 * input parameters
 *
 * @param       addr     - Pointer to Peer device BLE address.
 * @param       addrType - PHY_ADDR_TYPE_PUBLIC, PHY_ADDR_TYPE_RANDOM
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetPeerAddr( uint8         *addr,
                      phyAddrType_t addrType);


/*******************************************************************************
 * @fn          PHY_SetCRCInit
 *
 * @brief       Set initial CRC value used in a connection.
 *
 * input parameters
 *
 * @param       crcInit - Initial 3 byte CRC value.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetCRCInit( uint32 crcInit );


/*******************************************************************************
 * @fn          PHY_GetAdvChans
 *
 * @brief       Read the configured advertising channels to be used.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Three bit Advertising bit map of a combination of:
 *              PHY_ADV_CHANNEL_37, PHY_ADV_CHANNEL_38, and PHY_ADV_CHANNEL_39.
 */
phyChanMap_t PHY_GetAdvChans( void );


/*******************************************************************************
 * @fn          PHY_SetAdvChans
 *
 * @brief       Configure the advertising channels to be used.
 *
 * input parameters
 *
 * @param       advChanMap - Three bit Advertising bit map of a combination of:
 *                           PHY_ADV_CHANNEL_37, PHY_ADV_CHANNEL_38, and
 *                           PHY_ADV_CHANNEL_39.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetAdvChans( uint8 advChanMap );


/*******************************************************************************
 * @fn          PHY_SetDataChan
 *
 * @brief       Configure the data channel to be used:
 *              Master/Slave use data channels 0-36.
 *              Scanner/Initiator use data channels 37-39.
 *
 * input parameters
 *
 * @param       dataChan - Data channel value.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
NEAR_FUNC void PHY_SetDataChan( uint8 dataChan );


/*******************************************************************************
 * @fn          PHY_GetDataChan
 *
 * @brief       Read the configured data channel:
 *              Master/Slave use data channels 0-36.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      dataChan - Data channel value.
 */
NEAR_FUNC uint8 PHY_GetDataChan( void );


/*******************************************************************************
 * @fn          PHY_SetEndConnection
 *
 * @brief       Configure the ENDC bit in pkt. If set, the connection is closed
 *              after next packet is received from the slave.
 *
 * input parameters
 *
 * @param       endConn - PHY_END_CONN_CONTINUE, PHY_END_CONN_AFTER_NEXT_PKT.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetEndConnection( phyEndConn_t endConn );


/*******************************************************************************
 * @fn          PHY_ConfigureMD
 *
 * @brief       Configures how the More Data (MD) bit is populated in the data
 *              PDU.
 *
 * input parameters
 *
 * @param       moreData - PHY_MD_BIT_FROM_FIFO_ENTRY,
 *                         PHY_MD_BIT_BASED_ON_FIFO_CONTENTS,
 *                         PHY_MD_BIT_ALWAYS_ZERO,
 *                         PHY_MD_BIT_ALWAYS_ONE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ConfigureMD( phyConfMD_t moreData );


/*******************************************************************************
 * @fn          PHY_SetBackoffCnt
 *
 * @brief       Set the Scanner backoff count.
 *
 * input parameters
 *
 * @param       count - Backoff count: 1-256.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetBackoffCnt( uint16 count );


/*******************************************************************************
 * @fn          PHY_SetScanMode
 *
 * @brief       Configure Scanner to scan active or passive.
 *
 * input parameters
 *
 * @param       scanMode - PHY_SCAN_MODE_PASSIVE, PHY_SCAN_MODE_ACTIVE.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetScanMode( phyScanMode_t scanMode );


/*******************************************************************************
 * @fn          PHY_SetScanEnd
 *
 * @brief       Configure Scanner to end after a Scan Request is received for
 *              which no Scan Response will be sent.
 *
 * input parameters
 *
 * @param       scanEnd - PHY_SCAN_END_ON_ADV_REPORT_DISABLED,
 *                        PHY_SCAN_END_ON_ADV_REPORT_ENABLED.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetScanEnd( phyScanEnd_t scanEnd );


/*******************************************************************************
 * @fn          PHY_GetEndCause
 *
 * @brief       Returns the task end cause.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      The LLE nR task end cause.
 */
uint8 PHY_GetEndCause( void );


/*******************************************************************************
 * @fn          PHY_AnchorValid
 *
 * @brief       Check if there is a valid anchor point value.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      TRUE:  Valid anchor point value.
 *              FALSE: The anchor point is not valid.
 */
uint8 PHY_AnchorValid( void );


/*******************************************************************************
 * @fn          PHY_InitSeqStat
 *
 * @brief       Initialize the connection sequencing status to the default
 *              value.
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
void PHY_InitSeqStat( void );


/*******************************************************************************
 * @fn          PHY_FirstPktReceived
 *
 * @brief       Returns an indication of whether the first packet in a new
 *              connection, or updated connection, has been received.
 *
 *              Note: The logic of returned value is true-low.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      TRUE:  First packet has not yet been received.
 *              FALSE: First packet has been received.
 */
uint8 PHY_FirstPktReceived( void );


/*******************************************************************************
 * @fn          PHY_CtrlAckPending
 *
 * @brief       Returns an indication of whether the last successfully received
 *              packet that was a LL control packet has been ACK'ed or not.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      TRUE:  ACK for last received control packet is pending.
 *              FALSE: Last received control packet has been ACK'ed.
 */
uint8 PHY_CtrlAckPending( void );


/*******************************************************************************
 * @fn          PHY_SetSyncWord
 *
 * @brief       Set synchronization word.
 *
 *              Note: The synch word lenght is fixed at four bytes.
 *
 * input parameters
 *
 * @param       syncWord - Synchronization word (a.k.a. access address).
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetSyncWord( uint32 syncWord );


/*******************************************************************************
 * @fn          PHY_SetMaxNack
 *
 * @brief       Set the maximum number of NACKs allowed to be received before
 *              ending the connection event.
 *
 * input parameters
 *
 * @param       maxNackCount - Maximum number of NACKs allowed count, or zero
 *                             to disable this feature.
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetMaxNack( uint8 maxNackCount );


/*******************************************************************************
 * @fn          PHY_ClearWhitelist
 *
 * @brief       Clears the white list table and the number of table entries.
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
void PHY_ClearWhitelist( void );


/*******************************************************************************
 * @fn          PHY_ClearWlEntry
 *
 * @brief       Clears the white list table entry.
 *
 * input parameters
 *
 * @param       wlEntryIndex - Whhite list table entry index.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ClearWlEntry( uint8 wlEntryIndex );


/*******************************************************************************
 * @fn          PHY_ClearWlBasedOnBl
 *
 * @brief       Clears the white list table entries that correspond to
 *              blacklist entries.
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
void PHY_ClearWlBasedOnBl( void );


/*******************************************************************************
 * @fn          PHY_GetNumFreeWlEntries
 *
 * @brief       Returns the number of available white list entries.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of available white list entries.
 */
uint8 PHY_GetNumFreeWlEntries( void );


/*******************************************************************************
 * @fn          PHY_GetWlSize
 *
 * @brief       Returns the number of white list entries.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of white list entries.
 */
uint8 PHY_GetWlSize( void );


/*******************************************************************************
 * @fn          PHY_AddWlEntry
 *
 * @brief       Adds a white list entry. The address is added at the next
 *              available index. If the table is full, it returns a failure.
 *
 * input parameters
 *
 * @param       addr         - Pointer to device address to be added.
 * @param       addrType     - PHY_ADDR_TYPE_PUBLIC, PHY_ADDR_TYPE_RANDOM
 * @param       setBlacklist - PHY_SET_BLACKLIST_DISABLE,
 *                             PHY_SET_BLACKLIST_ENABLE
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      PHY_STATUS_SUCCESS, PHY_STATUS_WL_FULL, PHY_STATUS_FAILURE
 */
phyStatus_t PHY_AddWlEntry( uint8          *addr,
                            phyAddrType_t  addrType,
                            phyBlacklist_t setBlacklist );


/*******************************************************************************
 * @fn          PHY_RemoveWlEntry
 *
 * @brief       Removes a white list entry based on its address and address
 *              type. If located, then the entry is set invalid, and the address
 *              is cleared. If the table is empty, or the address and address
 *              type is not located, it returns a failure.
 *
 * input parameters
 *
 * @param       addr     - Pointer to device address to be added.
 * @param       addrType - PHY_ADDR_TYPE_PUBLIC, PHY_ADDR_TYPE_RANDOM
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      PHY_STATUS_SUCCESS, PHY_STATUS_WL_EMPTY,
 *              PHY_STATUS_WL_ENTRY_NOT_FOUND
 */
phyStatus_t PHY_RemoveWlEntry( uint8         *addr,
                               phyAddrType_t addrType );


/*******************************************************************************
 * @fn          PHY_SetAdvWlPolicy
 *
 * @brief       Set the Advertiser's white list policy.
 *
 * input parameters
 *
 * @param       wlPolicy - PHY_ADVERTISER_WHITE_LIST_NONE,
 *                         PHY_ADVERTISER_WHITE_LIST_SCAN_REQ,
 *                         PHY_ADVERTISER_WHITE_LIST_CONNECT_REQ,
 *                         PHY_ADVERTISER_WHITE_LIST_ALL
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetAdvWlPolicy( phyAdvWlPolicy_t wlPolicy );


/*******************************************************************************
 * @fn          PHY_SetScanWlPolicy
 *
 * @brief       Set the Advertiser's white list policy.
 *
 * input parameters
 *
 * @param       wlPolicy - PHY_SCANNER_ALLOW_ALL_ADV_PKTS,
 *                         PHY_SCANNER_USE_WHITE_LIST
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetScanWlPolicy( phyScanWlPolicy_t wlPolicy );


/*******************************************************************************
 * @fn          PHY_SetInitWlPolicy
 *
 * @brief       Set the Initiator's white list policy.
 *
 * input parameters
 *
 * @param       wlPolicy - PHY_INITIATOR_USE_PEER_ADDR,
 *                         PHY_INITIATOR_USE_WL
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetInitWlPolicy( phyInitWlPolicy_t wlPolicy );


/*******************************************************************************
 * @fn          PHY_SetBlacklistIndex
 *
 * @brief       Set a white list table entry as being black listed. That is,
 *              Advertising packets received with the address and address type
 *              of the white list table entry that is black listed are ignored.
 *
 *              Note: This feature is only used by the Scanner.
 *
 * input parameters
 *
 * @param       blackListIndex - The white list table entry that is blacklisted.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetBlacklistIndex( uint8 blackListIndex );


/*******************************************************************************
 * @fn          PHY_SetBlacklistEntry
 *
 * @brief       Search the white list table for an entry, and if found, set the
 *              corresponding blacklist entry index.
 *
 *              Note: This feature is only used by the Scanner.
 *
 * input parameters
 *
 * @param       addr     - Pointer to device address to be added.
 * @param       addrType - PHY_ADDR_TYPE_PUBLIC, PHY_ADDR_TYPE_RANDOM
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
phyStatus_t PHY_SetBlacklistEntry( uint8         *addr,
                                   phyAddrType_t addrType );


/*******************************************************************************
 * @fn          PHY_ClearBlacklistIndex
 *
 * @brief       Clear a blacklist table entry based on a white list entry index.
 *
 * input parameters
 *
 * @param       blackListIndex - The white list table entry that is blacklisted.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ClearBlacklistIndex( uint8 blackListIndex );


/*******************************************************************************
 * @fn          PHY_ClearBlacklist
 *
 * @brief       Clear the black list. All white list table entries are used as
 *              normal white list entries.
 *
 *              Note: This feature is only used by the Scanner.
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
void PHY_ClearBlacklist( void );


/*******************************************************************************
 * @fn          PHY_SaveWhiteList
 *
 * @brief       This routine saves any valid white list entries to Bank 1. This
 *              routine is only used when Scanning with filter enabled and
 *              the white list policy is "Any".
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
void PHY_SaveWhiteList( uint8 wlOwner );


/*******************************************************************************
 * @fn          PHY_RestoreWhiteList
 *
 * @brief       This routine restores any valid white list entries from Bank 1.
 *              This routine is only used when Scanning with filter enabled and
 *              the white list policy is "Any".
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
void PHY_RestoreWhiteList( uint8 wlOwner );


/*******************************************************************************
 * @fn          PHY_ClearSavedWhiteList
 *
 * @brief       This routine clears any valid white list entries from Bank 1.
 *              This routine is only used when Scanning with filter enabled and
 *              the white list policy is "Any".
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
void PHY_ClearSavedWhiteList( uint8 wlOwner );


/*******************************************************************************
 * @fn          PHY_Command
 *
 * @brief       Send a command to the LLE nanoRisc.
 *
 * input parameters
 *
 * @param       cmd - LLE nanoRisc command.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_Command( phyCmd_t cmd );


/*******************************************************************************
 * @fn          PHY_ClearCounters
 *
 * @brief       Clears all LLE nanoRisc counters.
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
void PHY_ClearCounters( void );


/*******************************************************************************
 * @fn          PHY_ReadCounters
 *
 * @brief       Reads all LLE nanoRisc counters.
 *
 * input parameters
 *
 * @param       cntPtr - Pointer to array to hold LLE nanoRisc counters.
 *
 * output parameters
 *
 * @param       cntPtr - Pointer to populated array of LLE nanoRisc counters.
 *
 * @return      None.
 */
void PHY_ReadCounters( uint8 *cntPtr );


/*******************************************************************************
 * @fn          PHY_ConfigureFifoDataProcessing
 *
 * @brief       Configure FIFO data processing. This includes flushing ignored
 *              packets, packets that fail the CRC, and empty packets. Also,
 *              whether a RX status containing the packets's data channel number
 *              and RSSI should be appended to the end of the packet.
 *
 * input parameters
 *
 * @param       fifoDataConf - Bit mapped configuration value where each
 *                             included bit enables a feature:
 *                             PHY_FIFO_DATA_DIABLE_ALL or some combination of:
 *                             PHY_FIFO_DATA_AUTO_FLUSH_IGNORED
 *                             PHY_FIFO_DATA_AUTO_FLUSH_CRC_ERROR
 *                             PHY_FIFO_DATA_AUTO_FLUSH_EMPTY_PKT
 *                             PHY_FIFO_DATA_APPEND_RX_STATUS
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ConfigureFifoDataProcessing( phyFifoData_t fifoDataConf );


/*******************************************************************************
 * @fn          PHY_ConfigureAppendRfStatus
 *
 * @brief       Configure if nanoRisc should append the RF status to the end of
 *              received packets.
 *
 * input parameters
 *
 * @param       appendRfStatus - PHY_ENABLE_APPEND_RF_STATUS,
 *                               PHY_DISABLE_APPEND_RF_STATUS
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ConfigureAppendRfStatus( phyAppendRfStatus_t appendRfStatus );


/*******************************************************************************
 * @fn          PHY_SetFifoConfig
 *
 * @brief       Configure FIFO configuration operations. This includes automatic
 *              dellocation of RX packet and commit of TX packet by MCU, and
 *              automatic commit of RX packet and deallocate of TX packet by nR.
 *
 * input parameters
 *
 * @param       fifoConfig - Bit mapped configuration value where each included
 *                           bit enables a FIFO automatic operation:
 *                           PHY_FIFO_CONFIG_DISABLE_ALL or some combination of:
 *                           PHY_FIFO_CONFIG_AUTO_DEALLOC_RX
 *                           PHY_FIFO_CONFIG_AUTO_COMMIT_RX
 *                           PHY_FIFO_CONFIG_AUTO_DEALLOC_TX
 *                           PHY_FIFO_CONFIG_AUTO_COMMIT_TX
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetFifoConfig( phyFifoConfig_t fifoConfig );


/*******************************************************************************
 * @fn          PHY_InitLastRssi
 *
 * @brief       Set the Last RSSI value to invalid.
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
void PHY_InitLastRssi( void );


/*******************************************************************************
 * @fn          PHY_GetLastRssi
 *
 * @brief       Get the Last RSSI value captured by the nanoRisc.
 *
 *              Note: The returned value is returned as an unsigned integer!
 *              Note: The returned value is not corrected for RX gain.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      The last data RSSI received by the nanoRisc, as an unsigned
 *              integer. Invalid value: 0x80.
 */
uint8 PHY_GetLastRssi( void );


/*******************************************************************************
 * @fn          PHY_GetRssi
 *
 * @brief       Get the RF RSSI value.
 *
 *              Note: The returned value is returned as an unsigned integer!
 *              Note: This call should only be made when RX is active.
 *              Note: An invalid value is -128.
 *              Note: The returned value is not corrected for RX gain.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      The RF RSSI, as an unsigned integer. Invalid value: 0x80.
 */
uint8 PHY_GetRssi( void );


/*******************************************************************************
 * @fn          Clear Window Configuration
 *
 * @brief       Clear the window connfiguration register so that the connect
 *              request packet uses the window offset stored in the packet.
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
void PHY_ClearWinConfig( void );


/*******************************************************************************
 * @fn          Set Dynamic Window Offset
 *
 * @brief       This routine sets the window offset and connection interval
 *              used during dynamic window offset control when forming multiple
 *              connections. For this feature, once the Initiator has started,
 *              each T2E1 pulse will decrement the winOffset. When the Init
 *              responds with a CONNECT_REQ, it will contain the window offset
 *              based on this dynamic adjustment. This is needed to start the
 *              new connection at a precise time. But since it is not known
 *              when the CONNECT_REQ will happen, the window offset is adjusted
 *              (i.e. corrected) on each system tick.
 *
 * input parameters
 *
 * @param       uint16 winOffset    - Initial window offset, in 625us ticks.
 * @param       uint16 connInterval - New connection's interval in 625us ticks.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetDynamicWinOffset( uint16 winOffset,
                              uint16 connInterval );


/*******************************************************************************
 * @fn          PHY_SetWhitener
 *
 * @brief       Configure the whitener as none, only BLE Pseudo Number 7 (PN7),
 *              only the TI Pseudo Number 9 (PN9), or both.
 *
 * input parameters
 *
 * @param       whitener - PHY_DISABLE_ALL_WHITENER,
 *                         PHY_ENABLE_BLE_WHITENER,
 *                         PHY_DISABLE_BLE_WHITENER,
 *                         PHY_ENABLE_PN9_WHITENER,
 *                         PHY_DISABLE_PN9_WHITENER,
 *                         PHY_ENABLE_ONLY_BLE_WHITENER,
 *                         PHY_ENABLE_ONLY_PN9_WHITENER,
 *                         PHY_ENABLE_ALL_WHITENER
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ConfigWhitener( phyWhitener_t whitener );


/*******************************************************************************
 * @fn          PHY_ResetTxFifo
 *
 * @brief       Reset the TX FIFO.
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
void PHY_ResetTxFifo( void );


/*******************************************************************************
 * @fn          PHY_ResetRxFifo
 *
 * @brief       Reset the RX FIFO.
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
void PHY_ResetRxFifo( void );


/*******************************************************************************
 * @fn          PHY_ResetTxRxFifo
 *
 * @brief       Reset the TX and RX FIFOs.
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
void PHY_ResetTxRxFifo( void );


/*******************************************************************************
 * @fn          PHY_TxFifoBytesFree
 *
 * @brief       Reads the number of available bytes in the TX FIFO.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of available bytes, 0..127.
 */
uint8 PHY_TxFifoBytesFree( void );


/*******************************************************************************
 * @fn          PHY_TxFifoLen
 *
 * @brief       Reads the number of used bytes in the TX FIFO.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of used bytes, 0..127.
 */
uint8 PHY_TxFifoLen( void );


/*******************************************************************************
 * @fn          PHY_RxFifoLen
 *
 * @brief       Reads the number of used bytes in the RX FIFO.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      Number of used bytes, 0..127.
 */
uint8 PHY_RxFifoLen( void );


/*******************************************************************************
 * @fn          PHY_RetryTxFifo
 *
 * @brief       Issue a TX FIFO Retry operation.
 *
 *              Note: Normally the TX FIFO read pointers are managed by the
 *                    nanoRisc. However, for Raw mode, after the packet has
 *                    been sent, then a retry must be issued to reuse the
 *                    packet already in the TX FIFO.
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
void PHY_RetryTxFifo( void );


/*******************************************************************************
 * @fn          PHY_RetryRxFifo
 *
 * @brief       Issue a RX FIFO Retry operation.
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
void PHY_RetryRxFifo( void );


/*******************************************************************************
 * @fn          PHY_CommitTxFifo
 *
 * @brief       Issue a TX FIFO Commit operation.
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
void PHY_CommitTxFifo( void );


/*******************************************************************************
 * @fn          PHY_CommitRxFifo
 *
 * @brief       Issue a RX FIFO Commit operation.
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
void PHY_CommitRxFifo( void );


/*******************************************************************************
 * @fn          PHY_DiscardTxFifo
 *
 * @brief       Issue a RX FIFO Discard operation.
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
void PHY_DiscardTxFifo( void );


/*******************************************************************************
 * @fn          PHY_DeallocateRxFifo
 *
 * @brief       Issue a RX FIFO Deallocate operation.
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
void PHY_DeallocateRxFifo( void );


/*******************************************************************************
 * @fn          PHY_SetRxFifoThreshold
 *
 * @brief       Set the RX FIFO Threshold.
 *
 * input parameters
 *
 * @param       thresLen - FIFO watermark, 0..127.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetRxFifoThreshold( uint8 thresLen );


/*******************************************************************************
 * @fn          PHY_GetTxSWP
 *
 * @brief       Get the TX FIFO Start Write Pointer.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      TX FIFO Start Write Pointer.
 */
uint8 *PHY_GetTxSWP( void );


/*******************************************************************************
 * @fn          PHY_GetRxSRP
 *
 * @brief       Get the RX FIFO Start Read Pointer.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      RX FIFO Start Read Pointer.
 */
uint8 *PHY_GetRxSRP( void );


/*******************************************************************************
 * @fn          PHY_WriteByte
 *
 * @brief       Write multiple bytes of data to TX FIFO.
 *
 *              Note: It is assumed the caller does not write more than the
 *                    amount of free space available in the TX FIFO.
 *
 * input parameters
 *
 * @param       data - Pointer to data to be written.
 * @param       len  - Number of bytes to be written.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_WriteByte( uint8 *data,
                    uint8 len );


/*******************************************************************************
 * @fn          PHY_WriteByteVal
 *
 * @brief       Write a single byte of data to TX FIFO.
 *
 *              Note: It is assumed the caller does not write more than the
 *                    amount of free space available in the TX FIFO.
 *
 * input parameters
 *
 * @param       value - A single byte of data.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_WriteByteVal( uint8 value );


/*******************************************************************************
 * @fn          PHY_ReadByte
 *
 * @brief       Read multiple bytes of data from the RX FIFO.
 *
 *              Note: It is assumed the caller does not read more than the
 *                    amount of available data in the RX FIFO.
 *
 * input parameters
 *
 * @param       ptr - Pointer to buffer to place read data. If NULL, the read
 *                    data is discarded, but RX FIFO read pointer is still
 *                    advanced.
 * @param       len  - Number of bytes to be read.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_ReadByte( uint8 *ptr,
                   uint8 len );


/*******************************************************************************
 * @fn          PHY_ReadByteVal
 *
 * @brief       Read a single byte of data from the RX FIFO.
 *
 *              Note: It is assumed the caller does not read more than the
 *                    amount of available data in the RX FIFO.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      A single byte of data from the RX FIFO.
 */
uint8 PHY_ReadByteVal( void );


/*******************************************************************************
 * @fn          PHY_MapDataBank
 *
 * @brief       Map a data bank to the specified RF RAM bank.
 *
 * input parameters
 *
 * @param       bankNum - The RF RAM bank given by one of the following values:
 *                        PHY_RF_RAM_BANK_0,
 *                        PHY_RF_RAM_BANK_1,
 *                        PHY_RF_RAM_BANK_2,
 *                        PHY_RF_RAM_BANK_3,
 *                        PHY_RF_RAM_BANK_4,
 *                        PHY_RF_RAM_BANK_5,
 *                        PHY_RF_RAM_RX_FIFO,
 *                        PHY_RF_RAM_TX_FIFO
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_MapDataBank( phyRfRamBank_t bankNum );


/*******************************************************************************
 * @fn          PHY_SaveRestoreConn
 *
 * @brief       Saves and restores the TX and RX FIFO for the specified
 *              connection to/from the LLE RAM bank memory. Called when the
 *              current connection's FIFO data is to be backed up and another
 *              connections FIFO is to be restored. The restored connection
 *              could be a connection that did not exist in the backup. In this
 *              case, the FIFO parameters will be set to reset conditions.
 *
 * input parameters
 *
 * @param       saveConnId    - Connection ID of FIFOs to be saved.
 * @param       restoreConnId - Connection ID of FIFOs to be restored.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SaveRestoreConn( uint8 saveConnId,
                          uint8 restoreConnId );


/*******************************************************************************
 * @fn          PHY_RestoreConn
 *
 * @brief       Restores TX and RX FIFOs for the specified connection. The data
 *              currently in the TX and RX FIFO is lost/overwritten. Call this
 *              when the current FIFO data does not need to be saved.
 *
 * input parameters
 *
 * @param       connId - Connection ID of FIFOs to be restored.
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_RestoreConn( uint8 connId );


/*******************************************************************************
 * @fn          PHY_RawTxInit
 *
 * @brief       Initializes Raw TX Task start operation (immediate or based on
 *              a timer event) and run operation (single shot, or repeat).
 *
 * input parameters
 *
 * @param       startMode - PHY_RAW_START_IMMEDIATE,
 *                          PHY_RAW_START_ON_TIMER_EVENT
 * @param       runMode   - PHY_RAW_RUN_ONCE,
 *                          PHY_RAW_RUN_REPEAT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_RawTxInit( phyRawStartMode_t startMode,
                    phyRawRunMode_t   runMode );


/*******************************************************************************
 * @fn          PHY_RawRxInit
 *
 * @brief       Initializes Raw RX Task start operation (immediate or based on
 *              a timer event) and run operation (single shot, or repeat).
 *
 * input parameters
 *
 * @param       startMode - PHY_RAW_START_IMMEDIATE,
 *                          PHY_RAW_START_ON_TIMER_EVENT
 * @param       runMode   - PHY_RAW_RUN_ONCE,
 *                          PHY_RAW_RUN_REPEAT
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_RawRxInit( phyRawStartMode_t startMode,
                    phyRawRunMode_t   runMode );


/*******************************************************************************
 * @fn          PHY_RawGetNumTxPkts
 *
 * @brief       Gets the number of transmitted packets.
 *
 * input parameters
 *
 * @param       numTxPkts - Word pointer.
 *
 * output parameters
 *
 * @param       numTxPkts - The number of transmitted packets.
 *
 * @return      None.
 */
void PHY_RawGetNumTxPkts( uint16 *numTxPkts );


/*******************************************************************************
 * @fn          PHY_RawGetNumRxPkts
 *
 * @brief       Gets the number of successfully received packets.
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       numRxPkts - The number of successfully received packets.
 *
 * @return      None.
 */
void PHY_RawGetNumRxPkts( uint16 *numRxPkts );


/*******************************************************************************
 * @fn          PHY_RawGetRxData
 *
 * @brief       Gets the number of successfully received packets, the number
 *              of packets received with a CRC error, and the last received
 *              RSSI (as an unsigned integer).
 *
 * input parameters
 *
 * @param       None.
 *
 * output parameters
 *
 * @param       numRxPkts    - Number of successfully received packets.
 * @param       numRxErrPkts - Number of received packets with a CRC error.
 * @param       rxRSSI       - Last received RSSI (as an unsigned integer).
 *
 * @return      None.
 */
void PHY_RawGetRxData( uint16 *numRxPkts,
                       uint16 *numRxErrPkts,
                       uint8  *rxRSSI );


/*******************************************************************************
 * @fn          PHY_SetRfFreq
 *
 * @brief       Sets the RF frequency for the given RF channel.
 *
 *              Note: BLE frequency range is 2402..2480MHz over 40 RF channels.
 *              Note: The RF channel is given, not the BLE channel.
 *
 * input parameters
 *
 * @param       rfChan  -  The RF channel to set the Tx or Rx frequency (0..39).
 * @param       freqMode - PHY_FREQ_MODE_MODULATED_CW,
 *                         PHY_FREQ_MODE_UNMODULATED_CW
 *
 * output parameters
 *
 * @param       None.
 *
 * @return      None.
 */
void PHY_SetRfFreq( uint8         rfChan,
                    phyFreqMode_t freqMode );


#ifdef __cplusplus
}
#endif

#endif /* PHY_H */
