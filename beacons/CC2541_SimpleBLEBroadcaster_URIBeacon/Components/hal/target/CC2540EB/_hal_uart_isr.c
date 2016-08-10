/**************************************************************************************************
  Filename:       _hal_uart_isr.c
  Revised:        $Date: 2013-10-25 16:56:11 -0700 (Fri, 25 Oct 2013) $
  Revision:       $Revision: 35812 $

  Description: This file contains the interface to the H/W UART driver by ISR.
 
  Note that when using this interrupt service based UART configuration (as opposed to DMA) the
  higher baudrates, such as 115200bps, may suffer Rx overrun, especially when the radio is used
  simultaneously and/or there is heavy to full throughout and/or full-duplex data.

  Copyright 2006-2012 Texas Instruments Incorporated. All rights reserved.

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

/*********************************************************************
 * INCLUDES
 */

#include "hal_types.h"
#include "hal_assert.h"
#include "hal_board.h"
#include "hal_defs.h"
#include "hal_mcu.h"
#include "hal_uart.h"

/*********************************************************************
 * MACROS
 */

//#define HAL_UART_ASSERT(expr)        HAL_ASSERT((expr))
#define HAL_UART_ASSERT(expr)

#define HAL_UART_ISR_RX_AVAIL() \
  ((isrCfg.rxTail >= isrCfg.rxHead) ? \
  (isrCfg.rxTail - isrCfg.rxHead) : \
  (HAL_UART_ISR_RX_MAX - isrCfg.rxHead + isrCfg.rxTail))

#define HAL_UART_ISR_TX_AVAIL() \
  ((isrCfg.txHead > isrCfg.txTail) ? \
  (isrCfg.txHead - isrCfg.txTail - 1) : \
  (HAL_UART_ISR_TX_MAX - isrCfg.txTail + isrCfg.txHead - 1))

/*********************************************************************
 * CONSTANTS
 */

// UxCSR - USART Control and Status Register.
#define CSR_MODE                   0x80
#define CSR_RE                     0x40
#define CSR_SLAVE                  0x20
#define CSR_FE                     0x10
#define CSR_ERR                    0x08
#define CSR_RX_BYTE                0x04
#define CSR_TX_BYTE                0x02
#define CSR_ACTIVE                 0x01

// UxUCR - USART UART Control Register.
#define UCR_FLUSH                  0x80
#define UCR_FLOW                   0x40
#define UCR_D9                     0x20
#define UCR_BIT9                   0x10
#define UCR_PARITY                 0x08
#define UCR_SPB                    0x04
#define UCR_STOP                   0x02
#define UCR_START                  0x01

#define UTX0IE                     0x04
#define UTX1IE                     0x08

#define P2DIR_PRIPO                0xC0

// Incompatible redefinitions result with more than one UART driver sub-module.
#undef PxOUT
#undef PxDIR
#undef PxSEL
#undef UxCSR
#undef UxUCR
#undef UxDBUF
#undef UxBAUD
#undef UxGCR
#undef URXxIE
#undef URXxIF
#undef UTXxIE
#undef UTXxIF
#undef HAL_UART_PERCFG_BIT
#undef HAL_UART_Px_RTS
#undef HAL_UART_Px_CTS
#undef HAL_UART_Px_RX_TX
#undef HAL_UART_Px_RX
#undef PxIFG
#undef PxIF
#undef PxIEN
#undef PICTL_BIT
#undef IENx
#undef IEN_BIT
#if (HAL_UART_ISR == 1)
#define PxOUT                      P0
#define PxIN                       P0
#define PxDIR                      P0DIR
#define PxSEL                      P0SEL
#define UxCSR                      U0CSR
#define UxUCR                      U0UCR
#define UxDBUF                     U0DBUF
#define UxBAUD                     U0BAUD
#define UxGCR                      U0GCR
#define URXxIE                     URX0IE
#define URXxIF                     URX0IF
#define UTXxIE                     UTX0IE
#define UTXxIF                     UTX0IF

#define PxIFG                      P0IFG
#define PxIF                       P0IF
#define PxIEN                      P0IEN
#define PICTL_BIT                  0x01
#define IENx                       IEN1
#define IEN_BIT                    0x20

#else
#define PxOUT                      P1
#define PxIN                       P1
#define PxDIR                      P1DIR
#define PxSEL                      P1SEL
#define UxCSR                      U1CSR
#define UxUCR                      U1UCR
#define UxDBUF                     U1DBUF
#define UxBAUD                     U1BAUD
#define UxGCR                      U1GCR
#define URXxIE                     URX1IE
#define URXxIF                     URX1IF
#define UTXxIE                     UTX1IE
#define UTXxIF                     UTX1IF

#define PxIFG                      P1IFG
#define PxIF                       P1IF
#define PxIEN                      P1IEN
#define PICTL_BIT                  0x04
#define IENx                       IEN2
#define IEN_BIT                    0x10
#endif

#if (HAL_UART_ISR == 1)
#define HAL_UART_PERCFG_BIT        0x01         // USART0 on P0, Alt-1; so clear this bit.
#define HAL_UART_Px_RX_TX          0x0C         // Peripheral I/O Select for Rx/Tx.
#define HAL_UART_Px_RX             0x04         // Peripheral I/O Select for Rx.
#define HAL_UART_Px_RTS            0x20         // Peripheral I/O Select for RTS.
#define HAL_UART_Px_CTS            0x10         // Peripheral I/O Select for CTS.
#else
#define HAL_UART_PERCFG_BIT        0x02         // USART1 on P1, Alt-2; so set this bit.
#define HAL_UART_Px_RTS            0x20         // Peripheral I/O Select for RTS.
#define HAL_UART_Px_CTS            0x10         // Peripheral I/O Select for CTS.
#define HAL_UART_Px_RX_TX          0xC0         // Peripheral I/O Select for Rx/Tx.
#define HAL_UART_Px_RX             0x80         // Peripheral I/O Select for Rx.
#endif

// The timeout tick is at 32-kHz, so multiply msecs by 33.
#define HAL_UART_MSECS_TO_TICKS    33

#if !defined HAL_UART_ISR_RX_MAX
#define HAL_UART_ISR_RX_MAX        128
#endif
#if !defined HAL_UART_ISR_TX_MAX
#define HAL_UART_ISR_TX_MAX        HAL_UART_ISR_RX_MAX
#endif
#if !defined HAL_UART_ISR_HIGH
#define HAL_UART_ISR_HIGH         (HAL_UART_ISR_RX_MAX / 2 - 16)
#endif
#if !defined HAL_UART_ISR_IDLE
#define HAL_UART_ISR_IDLE         (0 * HAL_UART_MSECS_TO_TICKS)
#endif

/*********************************************************************
 * TYPEDEFS
 */

#if HAL_UART_ISR_RX_MAX <= 256
typedef uint8 rxIdx_t;
#else
typedef uint16 rxIdx_t;
#endif

#if HAL_UART_ISR_TX_MAX <= 256
typedef uint8 txIdx_t;
#else
typedef uint16 txIdx_t;
#endif

typedef struct
{
  uint8 rxBuf[HAL_UART_ISR_RX_MAX];
  rxIdx_t rxHead;
  volatile rxIdx_t rxTail;
  uint8 rxTick;
  uint8 rxShdw;

  uint8 txBuf[HAL_UART_ISR_TX_MAX];
  volatile txIdx_t txHead;
  txIdx_t txTail;
  uint8 txMT;

  halUARTCBack_t uartCB;
} uartISRCfg_t;

/*********************************************************************
 * LOCAL VARIABLES
 */

static uartISRCfg_t isrCfg;

/*********************************************************************
 * LOCAL FUNCTIONS
 */

static void HalUARTInitISR(void);
static void HalUARTOpenISR(halUARTCfg_t *config);
static uint16 HalUARTReadISR(uint8 *buf, uint16 len);
static uint16 HalUARTWriteISR(uint8 *buf, uint16 len);
static void HalUARTPollISR(void);
static uint16 HalUARTRxAvailISR(void);
static uint8 HalUARTBusyISR(void);
static void HalUARTSuspendISR(void);
static void HalUARTResumeISR(void);

/******************************************************************************
 * @fn      HalUARTInitISR
 *
 * @brief   Initialize the UART
 *
 * @param   none
 *
 * @return  none
 *****************************************************************************/
static void HalUARTInitISR(void)
{
  // Set P2 priority - USART0 over USART1 if both are defined.
  P2DIR &= ~P2DIR_PRIPO;
  P2DIR |= HAL_UART_PRIPO;

#if (HAL_UART_ISR == 1)
  PERCFG &= ~HAL_UART_PERCFG_BIT;    // Set UART0 I/O location to P0.
#else
  PERCFG |= HAL_UART_PERCFG_BIT;     // Set UART1 I/O location to P1.
#endif
  PxSEL  |= HAL_UART_Px_RX_TX;       // Enable Tx and Rx on P1.
  ADCCFG &= ~HAL_UART_Px_RX_TX;      // Make sure ADC doesnt use this.
  UxCSR = CSR_MODE;                  // Mode is UART Mode.
  UxUCR = UCR_FLUSH;                 // Flush it.
}

/******************************************************************************
 * @fn      HalUARTOpenISR
 *
 * @brief   Open a port according tp the configuration specified by parameter.
 *
 * @param   config - contains configuration information
 *
 * @return  none
 *****************************************************************************/
static void HalUARTOpenISR(halUARTCfg_t *config)
{
  isrCfg.uartCB = config->callBackFunc;
  // Only supporting subset of baudrate for code size - other is possible.
  HAL_UART_ASSERT((config->baudRate == HAL_UART_BR_9600) ||
                  (config->baudRate == HAL_UART_BR_19200) ||
                  (config->baudRate == HAL_UART_BR_38400) ||
                  (config->baudRate == HAL_UART_BR_57600) ||
                  (config->baudRate == HAL_UART_BR_115200));

  if (config->baudRate == HAL_UART_BR_57600 ||
      config->baudRate == HAL_UART_BR_115200)
  {
    UxBAUD = 216;
  }
  else
  {
    UxBAUD = 59;
  }

  switch (config->baudRate)
  {
    case HAL_UART_BR_9600:
      UxGCR = 8;
      break;
    case HAL_UART_BR_19200:
      UxGCR = 9;
      break;
    case HAL_UART_BR_38400:
    case HAL_UART_BR_57600:
      UxGCR = 10;
      break;
    default:
      // HAL_UART_BR_115200
      UxGCR = 11;
      break;
  }

  // 8 bits/char; no parity; 1 stop bit; stop bit hi.
  if (config->flowControl)
  {
    UxUCR = UCR_FLOW | UCR_STOP;
    PxSEL |= HAL_UART_Px_RTS | HAL_UART_Px_CTS;
    PxOUT &= ~HAL_UART_Px_RTS;
    PxDIR |=  HAL_UART_Px_RTS;
  }
  else
  {
    UxUCR = UCR_STOP;
  }

  URXxIE = 1;
  UTXxIF = 1;  // Prime the ISR pump.
  UxCSR = (CSR_MODE | CSR_RE);
}

/*****************************************************************************
 * @fn      HalUARTReadISR
 *
 * @brief   Read a buffer from the UART
 *
 * @param   buf  - valid data buffer at least 'len' bytes in size
 *          len  - max length number of bytes to copy to 'buf'
 *
 * @return  length of buffer that was read
 *****************************************************************************/
static uint16 HalUARTReadISR(uint8 *buf, uint16 len)
{
  uint16 cnt = 0;

  while ((isrCfg.rxHead != isrCfg.rxTail) && (cnt < len))
  {
    *buf++ = isrCfg.rxBuf[isrCfg.rxHead++];
    if (isrCfg.rxHead >= HAL_UART_ISR_RX_MAX)
    {
      isrCfg.rxHead = 0;
    }
    cnt++;
  }

  return cnt;
}

/******************************************************************************
 * @fn      HalUARTWriteISR
 *
 * @brief   Write a buffer to the UART.
 *
 * @param   buf - pointer to the buffer that will be written, not freed
 *          len - length of
 *
 * @return  length of the buffer that was sent
 *****************************************************************************/
static uint16 HalUARTWriteISR(uint8 *buf, uint16 len)
{
  uint16 cnt;

  // Enforce all or none.
  if (HAL_UART_ISR_TX_AVAIL() < len)
  {
    return 0;
  }

  for (cnt = 0; cnt < len; cnt++)
  {
    isrCfg.txBuf[isrCfg.txTail] = *buf++;
    isrCfg.txMT = 0;

    if (isrCfg.txTail >= HAL_UART_ISR_TX_MAX-1)
    {
      isrCfg.txTail = 0;
    }
    else
    {
      isrCfg.txTail++;
    }

    // Keep re-enabling ISR as it might be keeping up with this loop due to other ints.
    IEN2 |= UTXxIE;
  }

  return cnt;
}

/******************************************************************************
 * @fn      HalUARTPollISR
 *
 * @brief   Poll a USART module implemented by ISR.
 *
 * @param   none
 *
 * @return  none
 *****************************************************************************/
static void HalUARTPollISR(void)
{
  uint16 cnt = HAL_UART_ISR_RX_AVAIL();
  uint8 evt = 0;

  if (isrCfg.rxTick)
  {
    // Use the LSB of the sleep timer (ST0 must be read first anyway).
    uint8 decr = ST0 - isrCfg.rxShdw;

    if (isrCfg.rxTick > decr)
    {
      isrCfg.rxTick -= decr;
    }
    else
    {
      isrCfg.rxTick = 0;
    }
  }
  isrCfg.rxShdw = ST0;

  if (cnt >= HAL_UART_ISR_RX_MAX-1)
  {
    evt = HAL_UART_RX_FULL;
  }
  else if (cnt >= HAL_UART_ISR_HIGH)
  {
    evt = HAL_UART_RX_ABOUT_FULL;
  }
  else if (cnt && !isrCfg.rxTick)
  {
    evt = HAL_UART_RX_TIMEOUT;
  }

  if (isrCfg.txMT)
  {
    isrCfg.txMT = 0;
    evt |= HAL_UART_TX_EMPTY;
  }

  if (evt && (isrCfg.uartCB != NULL))
  {
    isrCfg.uartCB(HAL_UART_ISR-1, evt);
  }
}

/**************************************************************************************************
 * @fn      HalUARTRxAvailISR()
 *
 * @brief   Calculate Rx Buffer length - the number of bytes in the buffer.
 *
 * @param   none
 *
 * @return  length of current Rx Buffer
 **************************************************************************************************/
static uint16 HalUARTRxAvailISR(void)
{
  return HAL_UART_ISR_RX_AVAIL();
}

/******************************************************************************
 * @fn      HalUARTBusyISR
 *
 * @brief   Query the UART hardware & buffers before entering PM mode 1, 2 or 3.
 *
 * @param   None
 *
 * @return  TRUE if the UART H/W is busy or buffers are not empty; FALSE otherwise.
 *****************************************************************************/
uint8 HalUARTBusyISR( void )
{
  return ((!(UxCSR & (CSR_ACTIVE | CSR_RX_BYTE))) &&
          (isrCfg.rxHead == isrCfg.rxTail) && (isrCfg.txHead == isrCfg.txTail));
}

/******************************************************************************
 * @fn      HalUARTSuspendISR
 *
 * @brief   Suspend UART hardware before entering PM mode 1, 2 or 3.
 *
 * @param   None
 *
 * @return  None
 *****************************************************************************/
static void HalUARTSuspendISR( void )
{
#if defined POWER_SAVING
  UxCSR = CSR_MODE;              // Suspend Rx operations.

  if (UxUCR & UCR_FLOW)
  {
    PxOUT |= HAL_UART_Px_RTS;  // Disable Rx flow.
    PxIFG = (HAL_UART_Px_CTS ^ 0xFF);
    PxIEN |= HAL_UART_Px_CTS;  // Enable the CTS ISR.
  }
  else
  {
    PxIFG = (HAL_UART_Px_RX ^ 0xFF);
    PxIEN |= HAL_UART_Px_RX;   // Enable the Rx ISR.
  }

#if defined HAL_UART_GPIO_ISR
  PxIF  =  0;                  // Clear the next level.
  PICTL |= PICTL_BIT;          // Interrupt on falling edge of input.
  IENx |= IEN_BIT;
#endif
#endif
}

/******************************************************************************
 * @fn      HalUARTResumeISR
 *
 * @brief   Resume UART hardware after exiting PM mode 1, 2 or 3.
 *
 * @param   None
 *
 * @return  None
 *****************************************************************************/
static void HalUARTResumeISR( void )
{
#if defined POWER_SAVING
#if defined HAL_UART_GPIO_ISR
  IENx &= (IEN_BIT ^ 0xFF);
#endif

  if (UxUCR & UCR_FLOW)
  {
    PxIEN &= (HAL_UART_Px_CTS ^ 0xFF);  // Disable the CTS ISR.
    PxOUT &= (HAL_UART_Px_RTS ^ 0xFF);  // Re-enable Rx flow.
  }
  else
  {
    PxIEN &= (HAL_UART_Px_RX ^ 0xFF);   // Enable the Rx ISR.
  }

  UxUCR |= UCR_FLUSH;
  UxCSR = (CSR_MODE | CSR_RE);
#endif
}

/***************************************************************************************************
 * @fn      halUartRxIsr
 *
 * @brief   UART Receive Interrupt
 *
 * @param   None
 *
 * @return  None
 ***************************************************************************************************/
#if (HAL_UART_ISR == 1)
HAL_ISR_FUNCTION( halUart0RxIsr, URX0_VECTOR )
#else
HAL_ISR_FUNCTION( halUart1RxIsr, URX1_VECTOR )
#endif
{
  HAL_ENTER_ISR();

  uint8 tmp = UxDBUF;
  isrCfg.rxBuf[isrCfg.rxTail] = tmp;

  // Re-sync the shadow on any 1st byte received.
  if (isrCfg.rxHead == isrCfg.rxTail)
  {
    isrCfg.rxShdw = ST0;
  }

  if (++isrCfg.rxTail >= HAL_UART_ISR_RX_MAX)
  {
    isrCfg.rxTail = 0;
  }

  isrCfg.rxTick = HAL_UART_ISR_IDLE;

  HAL_EXIT_ISR();
}

/***************************************************************************************************
 * @fn      halUartTxIsr
 *
 * @brief   UART Transmit Interrupt
 *
 * @param   None
 *
 * @return  None
 ***************************************************************************************************/
#if (HAL_UART_ISR == 1)
HAL_ISR_FUNCTION( halUart0TxIsr, UTX0_VECTOR )
#else
HAL_ISR_FUNCTION( halUart1TxIsr, UTX1_VECTOR )
#endif
{
  HAL_ENTER_ISR();

  if (isrCfg.txHead == isrCfg.txTail)
  {
    IEN2 &= ~UTXxIE;
    isrCfg.txMT = 1;
  }
  else
  {
    UTXxIF = 0;
    UxDBUF = isrCfg.txBuf[isrCfg.txHead++];

    if (isrCfg.txHead >= HAL_UART_ISR_TX_MAX)
    {
      isrCfg.txHead = 0;
    }
  }

  HAL_EXIT_ISR();
}

#if (defined POWER_SAVING && defined HAL_UART_GPIO_ISR)
/***************************************************************************************************
 * @fn      PortX Interrupt Handler
 *
 * @brief   This function is the PortX interrupt service routine.
 *
 * @param   None.
 *
 * @return  None.
 ***************************************************************************************************/
#if (HAL_UART_ISR == 1)
HAL_ISR_FUNCTION(port0Isr, P0INT_VECTOR)
#else
HAL_ISR_FUNCTION(port1Isr, P1INT_VECTOR)
#endif
{
  HAL_ENTER_ISR();

  HalUARTResume();
  if (dmaCfg.uartCB != NULL)
  {
    dmaCfg.uartCB(HAL_UART_DMA-1, HAL_UART_RX_WAKEUP);
  }
  PxIFG = 0;
  PxIF = 0;

  HAL_EXIT_ISR();
}
#endif

/******************************************************************************
******************************************************************************/
