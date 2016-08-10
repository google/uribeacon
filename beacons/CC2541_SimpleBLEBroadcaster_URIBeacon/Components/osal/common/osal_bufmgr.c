/**************************************************************************************************
  Filename:       osal_bufmgr.c
  Revised:        $Date: 2009-01-29 09:58:32 -0800 (Thu, 29 Jan 2009) $
  Revision:       $Revision: 18882 $

  Description:    This file contains the buffer management APIs. These APIs
                  are not reentrant hence cannot be called from an interrupt
                  contex.


  Copyright 2008-2013 Texas Instruments Incorporated. All rights reserved.

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
#include "OSAL.h"
#include "OnBoard.h"
#include "osal_bufmgr.h"

/*********************************************************************
 * MACROS
 */
// 'bd_ptr' used with these macros must be of the type 'bm_desc_t *'
#define START_PTR( bd_ptr )  ( (bd_ptr) + 1 )
#define END_PTR( bd_ptr )    ( (uint8 *)START_PTR( bd_ptr ) + (bd_ptr)->payload_len )

/*********************************************************************
 * CONSTANTS
 */

/*********************************************************************
 * TYPEDEFS
 */
typedef struct bm_desc
{
  struct bm_desc *next_ptr;    // pointer to next buffer descriptor
  uint16          payload_len; // length of user's buffer
} bm_desc_t;

/*********************************************************************
 * GLOBAL VARIABLES
 */

/*********************************************************************
 * GLOBAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */
// Linked list of allocated buffer descriptors
static bm_desc_t *bm_list_ptr = NULL;

/*********************************************************************
 * LOCAL FUNCTIONS
 */
static bm_desc_t *bm_desc_from_payload ( uint8 *payload_ptr );

/*********************************************************************
 * @fn      osal_bm_alloc
 *
 * @brief   Implementation of the allocator functionality.
 *
 *          WARNING: THIS FUNCTION CAN BE CALLED ONLY FROM THE BOTTOM
 *                   LAYER OF THE STACK (HCI FOR DUAL MODE AND LINK
 *                   LAYER FOR SINGLE MODE).
 *
 * @param   size - number of bytes to allocate from the heap.
 *
 * @return  pointer to the heap allocation; NULL if error or failure.
 */
void *osal_bm_alloc( uint16 size )
{
  halIntState_t  cs;
  bm_desc_t     *bd_ptr;

  HAL_ENTER_CRITICAL_SECTION(cs);

  bd_ptr = osal_mem_alloc( sizeof( bm_desc_t ) + size );

  if ( bd_ptr != NULL )
  {
    // set the buffer descriptor info
    bd_ptr->payload_len  = size;

    // add item to the beginning of the list
    bd_ptr->next_ptr = bm_list_ptr;
    bm_list_ptr = bd_ptr;

    // return start of the buffer
    bd_ptr = START_PTR( bd_ptr );
  }

  HAL_EXIT_CRITICAL_SECTION(cs);

  return ( (void *)bd_ptr );
}

/*********************************************************************
 * @fn      osal_bm_free
 *
 * @brief   Implementation of the de-allocator functionality.
 *
 * @param   payload_ptr - pointer to the memory to free.
 *
 * @return  none
 */
void osal_bm_free( void *payload_ptr )
{
  halIntState_t cs;
  bm_desc_t *loop_ptr;
  bm_desc_t *prev_ptr;

  HAL_ENTER_CRITICAL_SECTION(cs);

  prev_ptr = NULL;

  loop_ptr = bm_list_ptr;
  while ( loop_ptr != NULL )
  {
    if ( payload_ptr >= (void *)START_PTR( loop_ptr ) &&
         payload_ptr <= (void *)END_PTR( loop_ptr) )
    {
      // unlink item from the linked list
      if ( prev_ptr == NULL )
      {
        // it's the first item on the list
        bm_list_ptr = loop_ptr->next_ptr;
      }
      else
      {
        prev_ptr->next_ptr = loop_ptr->next_ptr;
      }

      // free the memory
      osal_mem_free( loop_ptr );

      // we're done here
      break;
    }

    // move on to next item
    prev_ptr = loop_ptr;
    loop_ptr = loop_ptr->next_ptr;
  }

  HAL_EXIT_CRITICAL_SECTION(cs);

  return;
}

/*********************************************************************
 * @fn      osal_bm_adjust_header
 *
 * @brief   Add or remove header space for the payload pointer. A positive
 *          adjustment adds header space, and negative removes header space.
 *
 * @param   payload_ptr - pointer to payload
 * @param   size - +/- number of bytes to move (affecting header area)
 *
 * @return  pointer to payload at the new adjusted location
 */
void *osal_bm_adjust_header( void *payload_ptr, int16 size )
{
  bm_desc_t *bd_ptr;
  uint8 *new_payload_ptr;

  bd_ptr = bm_desc_from_payload( (uint8 *)payload_ptr );
  if ( bd_ptr != NULL )
  {
    new_payload_ptr = (uint8 *)( (uint8 *)payload_ptr - size );

    // make sure the new payload is within valid range
    if ( new_payload_ptr >= (uint8 *)START_PTR( bd_ptr ) &&
         new_payload_ptr <= (uint8 *)END_PTR( bd_ptr ) )
    {
      // return new payload pointer
      return ( (void *)new_payload_ptr );
    }
  }

  // return original value
  return ( payload_ptr );
}

/*********************************************************************
 * @fn      osal_bm_adjust_tail
 *
 * @brief   Add or remove tail space for the payload pointer. A positive
 *          adjustment adds tail space, and negative removes tail space.
 *
 * @param   payload_ptr - pointer to payload
 * @param   size - +/- number of bytes to move (affecting header area)
 *
 * @return  pointer to payload at the new adjusted location
 */
void *osal_bm_adjust_tail( void *payload_ptr, int16 size )
{
  bm_desc_t *bd_ptr;
  uint8 *new_payload_ptr;

  bd_ptr = bm_desc_from_payload( (uint8 *)payload_ptr );
  if ( bd_ptr != NULL )
  {
    new_payload_ptr = (uint8 *)END_PTR( bd_ptr ) - size;

    // make sure the new payload is within valid range
    if ( new_payload_ptr >= (uint8 *)START_PTR( bd_ptr ) &&
         new_payload_ptr <= (uint8 *)END_PTR( bd_ptr ) )
    {
      // return new payload pointer
      return ( (void *)new_payload_ptr );
    }
  }

  // return original value
  return ( payload_ptr );
}

/*********************************************************************
 * @fn      bm_desc_from_payload
 *
 * @brief   Find buffer descriptor from payload pointer
 *
 * @param   payload_ptr - pointer to payload
 *
 * @return  pointer to buffer descriptor
 */
static bm_desc_t *bm_desc_from_payload ( uint8 *payload_ptr )
{
  bm_desc_t *loop_ptr;

  loop_ptr = bm_list_ptr;
  while ( loop_ptr != NULL )
  {
    if ( payload_ptr >= (uint8 *)START_PTR( loop_ptr ) &&
         payload_ptr <= (uint8 *)END_PTR( loop_ptr) )
    {
      // item found
      break;
    }

    // move on to next item
    loop_ptr = loop_ptr->next_ptr;
  }

  return ( loop_ptr );
}


/****************************************************************************
****************************************************************************/
