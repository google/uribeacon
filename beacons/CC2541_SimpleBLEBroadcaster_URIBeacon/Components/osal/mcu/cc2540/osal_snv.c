/**************************************************************************************************
  Filename:       osal_snv.c
  Revised:        $Date: 2013-02-15 10:12:26 -0800 (Fri, 15 Feb 2013) $
  Revision:       $Revision: 33143 $

  Description:    This module contains the OSAL simple non-volatile memory functions.


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
**************************************************************************************************/

/*********************************************************************
 * INCLUDES
 */

#include "hal_adc.h"
#include "hal_flash.h"
#include "hal_types.h"
#include "comdef.h"
#include "OSAL.h"
#include "osal_snv.h"
#include "hal_assert.h"
#include "saddr.h"

#ifdef OSAL_SNV_UINT16_ID
# error "This OSAL SNV implementation does not support the extended ID space"
#endif

/*********************************************************************
 * CONSTANTS
 */

// NV page configuration
#define OSAL_NV_PAGE_SIZE       HAL_FLASH_PAGE_SIZE
#define OSAL_NV_PAGES_USED      HAL_NV_PAGE_CNT
#define OSAL_NV_PAGE_BEG        HAL_NV_PAGE_BEG
#define OSAL_NV_PAGE_END       (OSAL_NV_PAGE_BEG + OSAL_NV_PAGES_USED - 1)

// Default byte value when flash is erased
#define OSAL_NV_ERASED          0xFF

// NV page header size in bytes
#define OSAL_NV_PAGE_HDR_SIZE  4

// In case pages 0-1 are ever used, define a null page value.
#define OSAL_NV_PAGE_NULL       0

// In case item Id 0 is ever used, define a null item value.
#define OSAL_NV_ITEM_NULL       0

// Length in bytes of a flash word
#define OSAL_NV_WORD_SIZE       HAL_FLASH_WORD_SIZE

// NV page header offset within a page
#define OSAL_NV_PAGE_HDR_OFFSET 0


// Flag in a length field of an item header to indicate validity
// of the length field
#define OSAL_NV_INVALID_LEN_MARK 0x8000

// Flag in an ID field of an item header to indicate validity of
// the identifier field
#define OSAL_NV_INVALID_ID_MARK  0x8000


// Bit difference between active page state indicator value and
// transfer page state indicator value
#define OSAL_NV_ACTIVE_XFER_DIFF  0x00100000

// active page state indicator value
#define OSAL_NV_ACTIVE_PAGE_STATE OSAL_NV_ACTIVE_XFER_DIFF

// transfer page state indicator value
#define OSAL_NV_XFER_PAGE_STATE   (OSAL_NV_ACTIVE_PAGE_STATE ^ OSAL_NV_ACTIVE_XFER_DIFF)

#define OSAL_NV_MIN_COMPACT_THRESHOLD   70 // Minimum compaction threshold
#define OSAL_NV_MAX_COMPACT_THRESHOLD   95 // Maximum compaction threshold

/*********************************************************************
 * MACROS
 */

// Macro to check supply voltage
#if (defined HAL_MCU_CC2530 || defined HAL_MCU_CC2531)
# define  OSAL_NV_CHECK_BUS_VOLTAGE  (HalAdcCheckVdd(VDD_MIN_FLASH))
#elif defined HAL_MCU_CC2533
# define  OSAL_NV_CHECK_BUS_VOLTAGE  (HalBatMonRead( HAL_BATMON_MIN_FLASH ))
#else
// The radio chip does not support voltage monitoring
# define  OSAL_NV_CHECK_BUS_VOLTAGE TRUE
#endif

/*********************************************************************
 * TYPEDEFS
 */

// NV item header structure
typedef struct
{
  uint16 id;
  uint16 len;
} osalNvItemHdr_t;
// Note that osalSnvId_t and osalSnvLen_t cannot be bigger than uint16

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

extern bool HalAdcCheckVdd(uint8 limit);

/*********************************************************************
 * GLOBAL VARIABLES
 */

#ifndef OAD_KEEP_NV_PAGES
// When NV pages are to remain intact during OAD download,
// the image itself should not include NV pages.
#pragma location="BLENV_ADDRESS_SPACE"
__no_init uint8 _nvBuf[OSAL_NV_PAGES_USED * OSAL_NV_PAGE_SIZE];
#pragma required=_nvBuf
#endif // OAD_KEEP_NV_PAGES

#if defined MAKE_CRC_SHDW
#pragma location="CRC_SHDW"
const CODE uint16 _crcShdw = 0xFFFF;
#pragma required=_crcShdw
#endif

/*********************************************************************
 * LOCAL VARIABLES
 */

// active page
static uint8 activePg;

// active page offset
static uint16 pgOff;

// flag to indicate that an error has occurred while writing to or erasing the
// flash device. Once this flag indicates failure, it is unsafe to attempt
// another write or erase.
static uint8 failF;

/*********************************************************************
 * LOCAL FUNCTIONS
 */

static uint8  initNV( void );

static void   setActivePage( uint8 pg );
static void   setXferPage(void);
static void   erasePage( uint8 pg );
static void   cleanErasedPage( uint8 pg );
static void   findOffset( void );
static void   compactPage( uint8 pg );

static void   writeWord( uint8 pg, uint16 offset, uint8 *pBuf );
static void   writeWordM( uint8 pg, uint16 offset, uint8 *pBuf, osalSnvLen_t cnt );


// NOTE: Triggering erase upon power up may cause fast aging of the flash device
//       if there is power switch debounce issue, etc.
//       Improvement of this is to add a certain delay upon power up before
//       osal_nv_init() is called.

/*********************************************************************
 * @fn      initNV
 *
 * @brief   Initialize the NV flash pages.
 *
 * @param   none
 *
 * @return  TRUE if initialization succeeds. FALSE, otherwise.
 */
static uint8 initNV( void )
{
  uint32 pgHdr;
  uint8 xferPg = OSAL_NV_PAGE_NULL;
  uint8 pg;

  failF = FALSE;
  activePg = OSAL_NV_PAGE_NULL;

  // Pick active page and clean up erased page if necessary
  for ( pg = OSAL_NV_PAGE_BEG; pg <= OSAL_NV_PAGE_END; pg++ )
  {
    HalFlashRead(pg, OSAL_NV_PAGE_HDR_OFFSET, (uint8 *)(&pgHdr), OSAL_NV_PAGE_HDR_SIZE);

    if ( pgHdr == OSAL_NV_ACTIVE_PAGE_STATE)
    {
      if (activePg != OSAL_NV_PAGE_NULL)
      {
        // Both pages are active only when power failed during flash erase and
        // with very low probability.
        // As it is hard (code size intensive) to figure out which page is the real active page,
        // and theoretically impossible as well in lowest probability, erase both pages
        // in this case
        cleanErasedPage(activePg);
        cleanErasedPage(pg);
        activePg = OSAL_NV_PAGE_NULL;
      }
      else
      {
        activePg = pg;
      }
    }
    else if ( pgHdr == OSAL_NV_XFER_PAGE_STATE)
    {
      xferPg = pg;
    }
    else
    {
      // Erase this page if it is not erased.
      // This is to ensure that any page that were in the middle of
      // compacting gets erased.
      cleanErasedPage(pg);
    }
  }

  if (activePg == OSAL_NV_PAGE_NULL)
  {
    if (xferPg == OSAL_NV_PAGE_NULL)
    {
      // Both pages are erased. This must be initial state.
      // Pick one page as active page.
      setActivePage(OSAL_NV_PAGE_BEG);
      pgOff = OSAL_NV_PAGE_HDR_SIZE;

      // If setting active page from a completely erased page failed,
      // it is not recommended to operate any further.
      // Other cases, even if non-active page is corrupt, NV module can still read
      // the active page content and hence this function could return TRUE.
      return (!failF);
    }
    else
    {
      // Compacting a page hasn't completed in previous power cycle.
      // Complete the compacting.
      activePg = xferPg;
      findOffset();

      compactPage(xferPg);
    }
  }
  else
  {
    if (xferPg != OSAL_NV_PAGE_NULL)
    {
      // Compacting has completed except for the final step of erasing
      // the xferPage.
      erasePage(xferPg);
    }

    // find the active page offset to write a new variable location item
    findOffset();
  }

  return TRUE;
}

/*********************************************************************
 * @fn      setActivePage
 *
 * @brief   Set page header active state to be active.
 *
 * @param   pg - Valid NV page to activate.
 *
 * @return  none
 */
static void setActivePage( uint8 pg )
{
  uint32 pgHdr;

  pgHdr = OSAL_NV_ACTIVE_PAGE_STATE;

  writeWord( pg, OSAL_NV_PAGE_HDR_OFFSET, (uint8*) &pgHdr );
  if (!failF)
  {
    activePg = pg;
  }
}

/*********************************************************************
 * @fn      setXferPage
 *
 * @brief   Set active page header state to be transfer state.
 *
 * @param   none
 *
 * @return  none
 */
static void setXferPage(void)
{
  uint32 pgHdr;

  // erase difference bit between active state and xfer state
  pgHdr = OSAL_NV_XFER_PAGE_STATE;

  writeWord( activePg, OSAL_NV_PAGE_HDR_OFFSET, (uint8*)&pgHdr );
}

/*********************************************************************
 * @fn      erasePage
 *
 * @brief   Erases a page in Flash.
 *
 * @param   pg - Valid NV page to erase.
 *
 * @return  none
 */
static void erasePage( uint8 pg )
{
  if ( !OSAL_NV_CHECK_BUS_VOLTAGE || failF)
  {
    failF = TRUE;
    return;
  }

  HalFlashErase(pg);

  {
    // Verify the erase operation
    uint16 offset;
    uint8 tmp;

    for (offset = 0; offset < OSAL_NV_PAGE_SIZE; offset ++)
    {
      HalFlashRead(pg, offset, &tmp, 1);
      if (tmp != OSAL_NV_ERASED)
      {
        failF = TRUE;
        break;
      }
    }
  }
}

/*********************************************************************
 * @fn      cleanErasedPage
 *
 * @brief   Erases a page in Flash if the page is not completely erased.
 *
 * @param   pg - Valid NV page to erase.
 *
 * @return  none
 */
static void cleanErasedPage( uint8 pg )
{
  uint8 buf;
  uint16 offset;

  for (offset = 0; offset < OSAL_NV_PAGE_SIZE; offset ++)
  {
    HalFlashRead(pg, offset, &buf, 1);
    if (buf != OSAL_NV_ERASED)
    {
      erasePage(pg);
      break;
    }
  }
}

/*********************************************************************
 * @fn      findOffset
 *
 * @brief   find an offset of an empty space in active page
 *          where to write a new item to.
 *
 * @param   None
 *
 * @return  none
 */
static void findOffset(void)
{
  uint16 offset;
  for (offset = OSAL_NV_PAGE_SIZE - OSAL_NV_WORD_SIZE;
       offset >= OSAL_NV_PAGE_HDR_SIZE;
       offset -= OSAL_NV_WORD_SIZE)
  {
    uint32 tmp;

    HalFlashRead(activePg, offset, (uint8 *)&tmp, OSAL_NV_WORD_SIZE);
    if (tmp != 0xFFFFFFFF)
    {
      break;
    }
  }
  pgOff = offset + OSAL_NV_WORD_SIZE;
}

/*********************************************************************
 * @fn      findItem
 *
 * @brief   find a valid item from a designated page and offset
 *
 * @param   pg       - NV page
 * @param   offset   - offset in the NV page from where to start
 *                     search up.
 *                     Usually this paramter is set to the empty space
 *                     offset.
 * @param   id       - NV item ID to search for
 *
 * @return  offset of the item, 0 when not found
 */
static uint16 findItem(uint8 pg, uint16 offset, osalSnvId_t id)
{
  offset -= OSAL_NV_WORD_SIZE;

  while (offset >= OSAL_NV_PAGE_HDR_SIZE)
  {
    osalNvItemHdr_t hdr;

    HalFlashRead(pg, offset, (uint8 *) &hdr, OSAL_NV_WORD_SIZE);

    if (hdr.id == id)
    {
      // item found
      // length field could be corrupt. Mask invalid length mark.
      uint8 len = hdr.len & ~OSAL_NV_INVALID_LEN_MARK;
      return offset - len;
    }
    else if (hdr.len & OSAL_NV_INVALID_LEN_MARK)
    {
      offset -= OSAL_NV_WORD_SIZE;
    }
    else
    {
      // valid length field
      if (hdr.len + OSAL_NV_WORD_SIZE <= offset)
      {
        // valid length
        offset -= hdr.len + OSAL_NV_WORD_SIZE;
      }
      else
      {
        // active page is corrupt
        // This could happen if NV initialization failed upon failure to erase
        // page and active page is set to uncleanly erased page.
        HAL_ASSERT_FORCED();
        return 0;
      }
    }
  }
  return 0;
}

/*********************************************************************
 * @fn      writeItem
 *
 * @brief   Write a data item to NV. Function can write an entire item to NV
 *
 * @param   pg     - Page number
 * @param   offset - offset within the NV page where to write the new item
 * @param   id     - NV item ID
 * @param   alignedLen - Length of data to write, alinged in flash word
 *                       boundary
 * @param  *pBuf   - Data to write.
 *
 * @return  none
 */
static void writeItem( uint8 pg, uint16 offset, osalSnvId_t id, uint16 alignedLen, uint8 *pBuf )
{
  osalNvItemHdr_t hdr;

  hdr.id = 0xFFFF;
  hdr.len = alignedLen | OSAL_NV_INVALID_LEN_MARK;

  // Write the len portion of the header first
  writeWord(pg, offset + alignedLen, (uint8 *) &hdr);

  // remove invalid len mark
  hdr.len &= ~OSAL_NV_INVALID_LEN_MARK;
  writeWord(pg, offset + alignedLen, (uint8 *) &hdr);

  // Copy over the data
  writeWordM(pg, offset, pBuf, alignedLen / OSAL_NV_WORD_SIZE);

  // value is valid. Write header except for the most significant bit.
  hdr.id = id | OSAL_NV_INVALID_ID_MARK;
  writeWord(pg, offset + alignedLen, (uint8 *) &hdr);

  // write the most significant bit
  hdr.id &= ~OSAL_NV_INVALID_ID_MARK;
  writeWord(pg, offset + alignedLen, (uint8 *) &hdr);
}

/*********************************************************************
 * @fn      xferItem
 *
 * @brief   Copy an NV item from the active page to a designated page.
 *
 * @param   pg         - NV page where to copy the item to.
 * @param   offset     - NV page offset where to copy the item to.
 * @param   alignedLen - Length of data to write, aligned in flash word
 *                       boundary.
 * @param   srcOff     - NV page offset of the original data in active page
 *
 * @return  none.
 */
static void xferItem( uint8 pg, uint16 offset, uint16 alignedLen, uint16 srcOff )
{
  uint8 tmp[OSAL_NV_WORD_SIZE];
  uint16 i = 0;

  // Copy over the data
  while (i <= alignedLen)
  {
    HalFlashRead(activePg, srcOff + i, tmp, OSAL_NV_WORD_SIZE);
    writeWord(pg, offset + i, tmp);

    i += OSAL_NV_WORD_SIZE;
  }
}

/*********************************************************************
 * @fn      compactPage
 *
 * @brief   Compacts the page specified.
 *
 * @param   srcPg - Valid NV page to compact from.
 *                  The page must have changed its state (header) to xfer state
 *                  prior to this function call. This function will not
 *                  modify the state of its header to xfer state before starting
 *                  to compact.
 *
 * @return  none.
 */
static void compactPage( uint8 srcPg )
{
  uint16 srcOff, dstOff;
  uint8 dstPg;
  osalSnvId_t lastId = (osalSnvId_t) 0xFFFF;

  dstPg = (srcPg == OSAL_NV_PAGE_BEG)? OSAL_NV_PAGE_END : OSAL_NV_PAGE_BEG;

  dstOff = OSAL_NV_PAGE_HDR_SIZE;

  // Read from the latest value
  srcOff = pgOff - sizeof(osalNvItemHdr_t);

  while (srcOff >= OSAL_NV_PAGE_HDR_SIZE)
  {
    osalNvItemHdr_t hdr;

    if (failF)
    {
      // Failure during transfer item will make next findItem error prone.
      return;
    }

    HalFlashRead(srcPg, srcOff, (uint8 *) &hdr, OSAL_NV_WORD_SIZE);

    if (hdr.id == 0xFFFF)
    {
      // Invalid entry. Skip this one.
      if (hdr.len & OSAL_NV_INVALID_LEN_MARK)
      {
        srcOff -= OSAL_NV_WORD_SIZE;
      }
      else
      {
        if (hdr.len + OSAL_NV_WORD_SIZE <= srcOff)
        {
          srcOff -= hdr.len + OSAL_NV_WORD_SIZE;
        }
        else
        {
          // invalid length. Source page must be a corrupt page.
          // This is possible only if the NV initialization failed upon erasing
          // what is selected as active page.
          // This is supposed to be a very rare case, as power should be
          // shutdown exactly during erase and then the page header is
          // still retained as either the Xfer or the Active state.

          // For production code, it might be useful to attempt to erase the page
          // so that at next power cycle at least the device is runnable
          // (with all entries removed).
          // However, it might be still better not to attempt erasing the page
          // just to see if this very rare case actually happened.
          //erasePage(srcPg);

          HAL_ASSERT_FORCED();
          return;
        }
      }

      continue;
    }

    // Consider only valid item
    if (!(hdr.id & OSAL_NV_INVALID_ID_MARK) && hdr.id != lastId)
    {
      // lastId is used to speed up compacting in case the same item ID
      // items were neighboring each other contiguously.
      lastId = (osalSnvId_t) hdr.id;

      // Check if the latest value of the item was already written
      if (findItem(dstPg, dstOff, lastId) == 0)
      {
        // This item was not copied over yet.
        // This must be the latest value.
        // Write the latest value to the destination page

        xferItem(dstPg, dstOff, hdr.len, srcOff - hdr.len);

        dstOff += hdr.len + OSAL_NV_WORD_SIZE;
      }
    }
    srcOff -= hdr.len + OSAL_NV_WORD_SIZE;
  }

  // All items copied.
  // Activate the new page
  setActivePage(dstPg);

  if (!failF)
  {
    pgOff = dstOff; // update active page offset
  }

  // Erase the currently active page
  erasePage(srcPg);
}

/*********************************************************************
 * @fn      verifyWordM
 *
 * @brief   verify the written word.
 *
 * @param   pg - A valid NV Flash page.
 * @param   offset - A valid offset into the page.
 * @param   pBuf - Pointer to source buffer.
 * @param   cnt - Number of 4-byte blocks to verify.
 *
 * @return  none
 */
static void verifyWordM( uint8 pg, uint16 offset, uint8 *pBuf, osalSnvLen_t cnt )
{
  uint8 tmp[OSAL_NV_WORD_SIZE];

  while (cnt--)
  {
    // Reading byte per byte will reduce code size but will slow down
    // and not sure it will meet the timing requirements.
    HalFlashRead(pg, offset, tmp, OSAL_NV_WORD_SIZE);
    if (FALSE == osal_memcmp(tmp, pBuf, OSAL_NV_WORD_SIZE))
    {
      failF = TRUE;
      return;
    }
    offset += OSAL_NV_WORD_SIZE;
    pBuf += OSAL_NV_WORD_SIZE;
  }
}

/*********************************************************************
 * @fn      writeWord
 *
 * @brief   Writes a Flash-WORD to NV.
 *
 * @param   pg - A valid NV Flash page.
 * @param   offset - A valid offset into the page.
 * @param   pBuf - Pointer to source buffer.
 *
 * @return  none
 */
static void writeWord( uint8 pg, uint16 offset, uint8 *pBuf )
{
  uint16 addr = (offset >> 2) + ((uint16)pg << 9);

  if ( !failF )
  {
    HalFlashWrite(addr, pBuf, 1);
    verifyWordM(pg, offset, pBuf, 1);
  }
}

/*********************************************************************
 * @fn      writeWordM
 *
 * @brief   Writes multiple Flash-WORDs to NV.
 *
 * @param   pg - A valid NV Flash page.
 * @param   offset - A valid offset into the page.
 * @param   buf - Pointer to source buffer.
 * @param   cnt - Number of 4-byte blocks to write.
 *
 * @return  none
 */
static void writeWordM( uint8 pg, uint16 offset, uint8 *buf, osalSnvLen_t cnt )
{
  uint16 addr = (offset >> 2) + ((uint16)pg << 9);

  if ( !failF )
  {
    HalFlashWrite(addr, buf, cnt);
    verifyWordM(pg, offset, buf, cnt);
  }
}

/*********************************************************************
 * @fn      osal_snv_init
 *
 * @brief   Initialize NV service.
 *
 * @return  SUCCESS if initialization succeeds. FAILURE, otherwise.
 */
uint8 osal_snv_init( void )
{
  if (!initNV())
  {
    // NV initialization failed
    HAL_ASSERT_FORCED();

    return FAILURE;
  }

  return SUCCESS;
}

/*********************************************************************
 * @fn      osal_snv_write
 *
 * @brief   Write a data item to NV.
 *
 * @param   id  - Valid NV item Id.
 * @param   len - Length of data to write.
 * @param   *pBuf - Data to write.
 *
 * @return  SUCCESS if successful, NV_OPER_FAILED if failed.
 */
uint8 osal_snv_write( osalSnvId_t id, osalSnvLen_t len, void *pBuf )
{
  uint16 alignedLen;

  {
    uint16 offset = findItem(activePg, pgOff, id);

    if (offset > 0)
    {
      uint8 tmp;
      osalSnvLen_t i;

      for (i = 0; i < len; i++)
      {
        HalFlashRead(activePg, offset, &tmp, 1);
        if (tmp != ((uint8 *)pBuf)[i])
        {
          break;
        }
        offset++;
      }

      if (i == len)
      {
        // Changed value is the same value as before.
        // Return here instead of re-writing the same value to NV.
        return SUCCESS;
      }
    }
  }

  alignedLen = ((len + OSAL_NV_WORD_SIZE - 1) / OSAL_NV_WORD_SIZE) * OSAL_NV_WORD_SIZE;

  if ( pgOff + alignedLen + OSAL_NV_WORD_SIZE > OSAL_NV_PAGE_SIZE )
  {
    setXferPage();
    compactPage(activePg);
  }

  // pBuf shall be referenced beyond its valid length to save code size.
  writeItem(activePg, pgOff, id, alignedLen, pBuf);
  if (failF)
  {
    return NV_OPER_FAILED;
  }

  pgOff += alignedLen + OSAL_NV_WORD_SIZE;

  return SUCCESS;
}

/*********************************************************************
 * @fn      osal_snv_read
 *
 * @brief   Read data from NV.
 *
 * @param   id  - Valid NV item Id.
 * @param   len - Length of data to read.
 * @param   *pBuf - Data is read into this buffer.
 *
 * @return  SUCCESS if successful.
 *          Otherwise, NV_OPER_FAILED for failure.
 */
uint8 osal_snv_read( osalSnvId_t id, osalSnvLen_t len, void *pBuf )
{
  uint16 offset = findItem(activePg, pgOff, id);

  if (offset != 0)
  {
    HalFlashRead(activePg, offset, pBuf, len);
    return SUCCESS;
  }
  return NV_OPER_FAILED;
}

/*********************************************************************
 * @fn      osal_snv_compact
 *
 * @brief   Compacts NV if its usage has reached a specific threshold.
 *
 * @param   threshold - compaction threshold
 *
 * @return  SUCCESS if successful,
 *          NV_OPER_FAILED if failed, or
 *          INVALIDPARAMETER if threshold invalid.
 */
uint8 osal_snv_compact( uint8 threshold )
{
  if ( ( threshold < OSAL_NV_MIN_COMPACT_THRESHOLD ) ||
       ( threshold > OSAL_NV_MAX_COMPACT_THRESHOLD ) )
  {
    return INVALIDPARAMETER;
  }

  // See if NV active page usage has reached compaction threshold
  if ( ( (uint32)pgOff * 100 ) >= ( OSAL_NV_PAGE_SIZE * (uint32)threshold ) )
  {
    setXferPage();
    compactPage(activePg);

    return SUCCESS;
  }

  return NV_OPER_FAILED;
}

/*********************************************************************
*********************************************************************/
