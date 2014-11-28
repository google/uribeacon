/**************************************************************************************************
  Filename:       gapperiphbondmgr.c
  Revised:        $Date: 2011-03-02 12:41:23 -0800 (Wed, 02 Mar 2011) $
  Revision:       $Revision: 25256 $

  Description:    GAP peripheral profile manages bonded connections


  Copyright 2010 Texas Instruments Incorporated. All rights reserved.

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
#include "bcomdef.h"
#include "OSAL.h"
#include "osal_snv.h"
#include "gap.h"
#include "linkdb.h"
#include "gatt.h"

#include "gattservapp.h"
#include "gapgattserver.h"
#include "gapperiphbondmgr.h"

/*********************************************************************
 * MACROS
 */

/*********************************************************************
 * CONSTANTS
 */
// Profile Events

// Bonded State Flags
#define GAP_BONDED_STATE_AUTHENTICATED                  0x0001
#define GAP_BONDED_STATE_SERVICE_CHANGED                0x0002

/**
 * GAP Bond Manager NV layout
 *  
 * The NV definitions:
 *     BLE_NVID_GAP_BOND_START - starting NV ID
 *     GAP_BONDINGS_MAX - Maximum number of bonding allowed (10 is max for number of NV IDs allocated in bcomdef.h).
 * 
 * A single bonding entry consists of 6 components (NV items):
 *     Bond Record - defined as gapBondRec_t and uses GAP_BOND_REC_ID_OFFSET for an NV ID
 *     local LTK Info - defined as gapBondLTK_t and uses GAP_BOND_LOCAL_LTK_OFFSET for an NV ID
 *     device LTK Info - defined as gapBondLTK_t and uses GAP_BOND_DEV_LTK_OFFSET for an NV ID
 *     device IRK - defined as "uint8 devIRK[KEYLEN]" and uses GAP_BOND_DEV_IRK_OFFSET for an NV ID
 *     device CSRK - defined as "uint8 devCSRK[KEYLEN]" and uses GAP_BOND_DEV_CSRK_OFFSET for an NV ID
 *     device Sign Counter - defined as a uint32 and uses GAP_BOND_DEV_SIGN_COUNTER_OFFSET for an NV ID
 * 
 * When the device is initialized for the first time, all (GAP_BONDINGS_MAX) NV items are created and 
 * initialized to all 0xFF's. A bonding record of all 0xFF's indicates that the bonding record is empty
 * and free to use.
 * 
 * The calculation for each bonding records NV IDs:
 *    mainRecordNvID = ((bondIdx * GAP_BOND_REC_IDS) + BLE_NVID_GAP_BOND_START)
 *    localLTKNvID = (((bondIdx * GAP_BOND_REC_IDS) + GAP_BOND_LOCAL_LTK_OFFSET) + BLE_NVID_GAP_BOND_START)
 * 
 */
#define GAP_BOND_REC_ID_OFFSET              0 //!< NV ID for the main bonding record
#define GAP_BOND_LOCAL_LTK_OFFSET           1 //!< NV ID for the bonding record's local LTK information
#define GAP_BOND_DEV_LTK_OFFSET             2 //!< NV ID for the bonding records' device LTK information
#define GAP_BOND_DEV_IRK_OFFSET             3 //!< NV ID for the bonding records' device IRK
#define GAP_BOND_DEV_CSRK_OFFSET            4 //!< NV ID for the bonding records' device CSRK
#define GAP_BOND_DEV_SIGN_COUNTER_OFFSET    5 //!< NV ID for the bonding records' device Sign Counter

#define GAP_BOND_REC_IDS                    6

// Macros to calculate the index/offset in to NV space
#define calcNvID(Idx, offset)               (((((Idx) * GAP_BOND_REC_IDS) + (offset))) + BLE_NVID_GAP_BOND_START)
#define mainRecordNvID(bondIdx)             (calcNvID((bondIdx), GAP_BOND_REC_ID_OFFSET))
#define localLTKNvID(bondIdx)               (calcNvID((bondIdx), GAP_BOND_LOCAL_LTK_OFFSET))
#define devLTKNvID(bondIdx)                 (calcNvID((bondIdx), GAP_BOND_DEV_LTK_OFFSET))
#define devIRKNvID(bondIdx)                 (calcNvID((bondIdx), GAP_BOND_DEV_IRK_OFFSET))
#define devCSRKNvID(bondIdx)                (calcNvID((bondIdx), GAP_BOND_DEV_CSRK_OFFSET))
#define devSignCounterNvID(bondIdx)         (calcNvID((bondIdx), GAP_BOND_DEV_SIGN_COUNTER_OFFSET))

// Key Size Limits
#define MIN_ENC_KEYSIZE       7   //!< Minimum number of bytes for the encryption key
#define MAX_ENC_KEYSIZE       16  //!< Maximum number of bytes for the encryption key


/*********************************************************************
 * TYPEDEFS
 */

// Structure of NV data for the connected device's encryption information
typedef struct
{
  uint8   LTK[KEYLEN];              // Long Term Key (LTK)
  uint16  div;                      // LTK eDiv
  uint8   rand[B_RANDOM_NUM_SIZE];  // LTK random number
  uint8   keySize;                  // LTK key size
} gapBondLTK_t;

// Structure of NV data for the connected device's address information
typedef struct
{
  uint8   publicAddr[B_ADDR_LEN];     // Master's address
  uint8   reconnectAddr[B_ADDR_LEN];  // Privacy Reconnection Address
  uint16  stateFlags;                 // State flags: SM_AUTH_STATE_AUTHENTICATED & SM_AUTH_STATE_BONDING
} gapBondRec_t;

/*********************************************************************
 * GLOBAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL VARIABLES
 */

/*********************************************************************
 * EXTERNAL FUNCTIONS
 */

/*********************************************************************
 * LOCAL VARIABLES
 */
static uint8 gapBondMgr_TaskID;   // Task ID for internal task/event processing

// GAPBonding Parameters
static uint8 gapBond_PairingMode = GAPBOND_PAIRING_MODE_WAIT_FOR_REQ;
static uint16 gapBond_InitiateWait = 1000;  // Default to 1 second
static uint8 gapBond_MITM = FALSE;
static uint8 gapBond_IOCap = GAPBOND_IO_CAP_DISPLAY_ONLY;
static uint8 gapBond_OOBDataFlag = FALSE;
static uint8 gapBond_OOBData[KEYLEN] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
static uint8 gapBond_Bonding = FALSE;
static uint8 gapBond_AutoFail = FALSE;
static uint8 gapBond_AutoFailReason = SMP_PAIRING_FAILED_NOT_SUPPORTED;
static uint8 gapBond_KeyDistList = 
(
  GAPBOND_KEYDIST_SENCKEY     // sEncKey enabled, to send the encryption key
   | GAPBOND_KEYDIST_SIDKEY   // sIdKey enabled, to send the IRK, and BD_ADDR
                              // sSign disabled, 
                              // mEncKey disabled,
   | GAPBOND_KEYDIST_MIDKEY  // mIdKey enabled, to get the master's IRK and BD_ADDR
   | GAPBOND_KEYDIST_MSIGN   // mSign enabled, to get the master's CSRK
);
static uint32 gapBond_Passcode = 0;
static uint8  gapBond_KeySize = MAX_ENC_KEYSIZE;

static gapBondCBs_t *pGapBondCB = NULL;

/*********************************************************************
 * LOCAL FUNCTIONS
 */
static uint8 gapBondMgrChangeState( uint8 idx, uint16 state, uint8 set );
static uint8 gapBondMgrAddBond( gapBondRec_t *pBondRec, 
                                gapBondLTK_t *pLocalLTK, gapBondLTK_t *pDevLTK,
                                uint8 *pIRK, uint8 *pSRK, uint32 signCounter );
static uint8 gapBondMgrGetStateFlags( uint8 idx );
static bStatus_t gapBondMgrGetPublicAddr( uint8 idx, uint8 *pAddr );
static uint8 gapBondMgrFindReconnectAddr( uint8 *pReconnectAddr );
static uint8 gapBondMgrFindAddr( uint8 *pDevAddr );
static uint8 gapBondMgrFindEmpty( void );
static uint8 gapBondMgrBondTotal( void );
static bStatus_t gapBondMgrEraseAllBondings( void );
static bStatus_t gapBondMgrEraseBonding( uint8 idx );
static void gapBondMgr_ProcessOSALMsg( osal_event_hdr_t *pMsg );
static void gapBondMgrSendServiceChange( linkDBItem_t *pLinkItem );
static void gapBondMgr_ProcessGATTMsg( gattMsgEvent_t *pMsg );
static void gapBondSetupPrivFlag( void );

/*********************************************************************
 * NETWORK LAYER CALLBACKS
 */

/*********************************************************************
 * PUBLIC FUNCTIONS
 */

/*********************************************************************
 * @brief   Set a GAP Bond Manager parameter.
 *
 * Public function defined in gapperiphbondmgr.h.
 */
bStatus_t GAPBondMgr_SetParameter( uint16 param, uint8 len, void *pValue )
{
  bStatus_t ret = SUCCESS;  // return value

  switch ( param )
  {
    case GAPBOND_PAIRING_MODE:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= GAPBOND_PAIRING_MODE_INITIATE) )
      {
        gapBond_PairingMode = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
    
    case GAPBOND_INITIATE_WAIT:
      if ( len == sizeof ( uint16 ) )
      {
        gapBond_InitiateWait = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
    
    case GAPBOND_MITM_PROTECTION:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= TRUE) )
      {
        gapBond_MITM = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
    
    case GAPBOND_IO_CAPABILITIES:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= GAPBOND_IO_CAP_KEYBOARD_DISPLAY) )
      {
        gapBond_IOCap = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_OOB_ENABLED:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= TRUE) )
      {
        gapBond_OOBDataFlag = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_OOB_DATA:
      if ( len == KEYLEN )
      {
        VOID osal_memcpy( gapBond_OOBData, pValue, KEYLEN ) ;
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_BONDING_ENABLED:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= TRUE) )
      {
        gapBond_Bonding = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_KEY_DIST_LIST:
      if ( len == sizeof ( uint8 ) )
      {
        gapBond_KeyDistList = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_DEFAULT_PASSCODE:
      if ( (len == sizeof ( uint32 )) 
          && (*((uint32*)pValue) <= GAP_PASSCODE_MAX) )
      {
        gapBond_Passcode = *((uint32*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_ERASE_ALLBONDS:
      if ( len == 0 )
      {
        VOID gapBondMgrEraseAllBondings();
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_AUTO_FAIL_PAIRING:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= TRUE) )
      {
        gapBond_AutoFail = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_AUTO_FAIL_REASON:
      if ( (len == sizeof ( uint8 )) && (*((uint8*)pValue) <= SMP_PAIRING_FAILED_REPEATED_ATTEMPTS) )
      {
        gapBond_AutoFailReason = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    case GAPBOND_KEYSIZE:
      if ( (len == sizeof ( uint8 )) 
          && ((*((uint8*)pValue) >= MIN_ENC_KEYSIZE) && (*((uint8*)pValue) <= MAX_ENC_KEYSIZE)) )
      {
        gapBond_KeySize = *((uint8*)pValue);
      }
      else
      {
        ret = bleInvalidRange;
      }
      break;
      
    default:
      // The param value isn't part of this profile, try the GAP.
      if ( (param < TGAP_PARAMID_MAX) && (len == sizeof ( uint16 )) )
      {
        ret = GAP_SetParamValue( param, *((uint16*)pValue) );
      }
      else
      {
        ret = INVALIDPARAMETER;
      }
      break;
  }

  return ( ret );
}

/*********************************************************************
 * @brief   Get a GAP Bond Manager parameter.
 *
 * Public function defined in gapperiphbondmgr.h.
 */
bStatus_t GAPBondMgr_GetParameter( uint16 param, void *pValue )
{
  bStatus_t ret = SUCCESS;  // return value
  
  switch ( param )
  {
    case GAPBOND_PAIRING_MODE:
      *((uint8*)pValue) = gapBond_PairingMode;
      break;
      
    case GAPBOND_INITIATE_WAIT:
      *((uint16*)pValue) = gapBond_InitiateWait;
      break;
      
    case GAPBOND_MITM_PROTECTION:
      *((uint8*)pValue) = gapBond_MITM;
      break;
      
    case GAPBOND_IO_CAPABILITIES:
      *((uint8*)pValue) = gapBond_IOCap;
      break;
      
    case GAPBOND_OOB_ENABLED:
      *((uint8*)pValue) = gapBond_OOBDataFlag;
      break;
      
    case GAPBOND_OOB_DATA:
      VOID osal_memcpy( pValue, gapBond_OOBData, KEYLEN ) ;
      break;

    case GAPBOND_BONDING_ENABLED:
      *((uint8*)pValue) = gapBond_Bonding;
      break;
      
    case GAPBOND_KEY_DIST_LIST:
      *((uint8*)pValue) = gapBond_KeyDistList;
      break;
      
    case GAPBOND_DEFAULT_PASSCODE:
      *((uint32*)pValue) = gapBond_Passcode;
      break;
      
    case GAPBOND_AUTO_FAIL_PAIRING:
      *((uint8*)pValue) = gapBond_AutoFail;
      break;
      
    case GAPBOND_AUTO_FAIL_REASON:
      *((uint8*)pValue) = gapBond_AutoFailReason;
      break;
      
    case GAPBOND_KEYSIZE:
      *((uint8*)pValue) = gapBond_KeySize;
      break;
      
    default:
      // The param value isn't part of this profile, try the GAP.
      if ( param < TGAP_PARAMID_MAX )
      {
        *((uint16*)pValue) = GAP_GetParamValue( param );
      }
      else
      {
        ret = INVALIDPARAMETER;
      }
      break;
  }

  return ( ret );
}

/*********************************************************************
 * @brief   Notify the Bond Manager that a connection has been made.
 *
 * Public function defined in gapperiphbondmgr.h.
 */
bStatus_t GAPBondMgr_LinkEst( uint8 addrType, uint8 *pDevAddr, uint16 connHandle )
{
  uint8 idx;                          // NV Index
  uint8 publicAddr[B_ADDR_LEN]        // Place to put the public address
      = {0, 0, 0, 0, 0, 0};
  
  idx = GAPBondMgr_ResolveAddr( addrType, pDevAddr, publicAddr );
  if ( idx < GAP_BONDINGS_MAX )
  {
    // Bonding found
    uint8 stateFlags;             // Bond state flags
    smSecurityInfo_t localLTK;    // LTK information
    smSigningInfo_t signingInfo;  // Signature information
    
    // Initialize the NV structures
    osal_memset( &localLTK, 0, sizeof ( smSecurityInfo_t ) );
    osal_memset( &signingInfo, 0, sizeof ( smSigningInfo_t ) );
    
    // Check if key is valid, then load the key
    stateFlags = gapBondMgrGetStateFlags( idx );
    if ( osal_snv_read( localLTKNvID(idx), sizeof ( smSecurityInfo_t ), &localLTK ) == SUCCESS )
    {
      if ( (localLTK.keySize >= MIN_ENC_KEYSIZE) && (localLTK.keySize <= MAX_ENC_KEYSIZE) )
      {
        // Load the key information for the bonding
        VOID GAP_Bond( connHandle, 
                      ((stateFlags & GAP_BONDED_STATE_AUTHENTICATED) ? TRUE : FALSE),
                      &localLTK );
      }
    }
    
    // Load the Signing Key
    if ( osal_snv_read( devCSRKNvID(idx), KEYLEN, signingInfo.srk ) == SUCCESS )
    {
      if ( osal_isbufset( signingInfo.srk, 0xFF, KEYLEN ) == FALSE )
      {
        // Load the signing information for this connection
        VOID osal_snv_read( devSignCounterNvID(idx), sizeof ( uint32 ), &(signingInfo.signCounter) );
        VOID GAP_Signable( connHandle, 
                          ((stateFlags & GAP_BONDED_STATE_AUTHENTICATED) ? TRUE : FALSE),
                          &signingInfo );
      }
    }
    
    // Has there been a service change?
    if ( stateFlags & GAP_BONDED_STATE_SERVICE_CHANGED )
    {
      VOID GATT_ServiceChangedInd( connHandle, gapBondMgr_TaskID );
    }
  }
  else
  {
    // Not bound
    if ( gapBond_PairingMode == GAPBOND_PAIRING_MODE_INITIATE )
    {
      // Could add code to setup a timeout to initiate security
      // with a Slave Security Request.
    }
  }
  
  return ( SUCCESS );
}

/*********************************************************************
 * @brief   Resolve an address from bonding information.
 *
 * Public function defined in gapperiphbondmgr.h.
 */
uint8 GAPBondMgr_ResolveAddr( uint8 addrType, uint8 *pDevAddr, uint8 *pResolvedAddr )
{
  uint8 idx = GAP_BONDINGS_MAX;
  
  switch ( addrType )
  {
    case ADDRTYPE_PUBLIC:
    case ADDRTYPE_STATIC:
      idx = gapBondMgrFindAddr( pDevAddr );
      if ( (idx < GAP_BONDINGS_MAX) && (pResolvedAddr) )
      {
        VOID osal_memcpy( pResolvedAddr, pDevAddr, B_ADDR_LEN );
      }
      break;
      
    case ADDRTYPE_PRIVATE_NONRESOLVE:
      // This could be a reconnection address
      idx = gapBondMgrFindReconnectAddr( pDevAddr );
      if ( (idx < GAP_BONDINGS_MAX) && (pResolvedAddr) )
      {
        VOID gapBondMgrGetPublicAddr( idx, pResolvedAddr );
      }
      break;
      
    case ADDRTYPE_PRIVATE_RESOLVE:
      // Master's don't use Private Resolvable addresses
      break;
      
    default:
      break;
  }

  return ( idx );  
}

/*********************************************************************
 * @brief   Set/clear the service change indication in a bond record.
 *
 * Public function defined in gapperiphbondmgr.h.
 */
bStatus_t GAPBondMgr_ServiceChangeInd( uint16 connectionHandle, uint8 setParam )
{
  bStatus_t ret = bleNoResources; // return value
  
  if ( connectionHandle == 0xFFFF )
  {
    uint8 idx;  // loop counter
    
    // Run through the bond database and update the Service Change indication
    for ( idx = 0; idx < GAP_BONDINGS_MAX; idx++ )
    {
      if ( gapBondMgrChangeState( idx, GAP_BONDED_STATE_SERVICE_CHANGED, setParam ) )
      {
        ret = SUCCESS;
      }
    }

    // If the service change indication is TRUE, tell the connected devices
    if ( setParam )
    {
      // Run connected database
      linkDB_PerformFunc( gapBondMgrSendServiceChange );
    }
  }
  else
  {
    // Find connection information
    linkDBItem_t *pLinkItem = linkDB_Find( connectionHandle );
    if ( pLinkItem )
    {
      uint8 idx; // loop counter
      idx = GAPBondMgr_ResolveAddr( pLinkItem->addrType, pLinkItem->addr, NULL );
      if ( idx < GAP_BONDINGS_MAX )
      {
        // Bond found, update it.
        VOID gapBondMgrChangeState( idx, GAP_BONDED_STATE_SERVICE_CHANGED, setParam );
        ret = SUCCESS;
      }
      
      // If the service change indication is TRUE, tell the connected device
      if ( setParam )
      {
        gapBondMgrSendServiceChange( pLinkItem );
      }
    }
    else
    {
      ret = bleNotConnected;
    }
  }
  
  return ( ret );
}

/**
 * @brief       Register callback functions with the bond manager.
 * 
 *   NOTE:      There is no need to register a passcode callback function
 *              if the passcode will be handled with the GAPBOND_DEFAULT_PASSCODE parameter.
 *
 * @param       pCB - pointer to callback function structure.
 *
 * @return      none
 */
void GAPBondMgr_Register( gapBondCBs_t *pCB )
{
  pGapBondCB = pCB;
}

/**
 * @brief       Respond to a passcode request.
 * 
 * @param       connectionHandle - connection handle of the connected device or 0xFFFF
 *                                 if all devices in database.
 * @param       status - SUCCESS if passcode is available, otherwise see @ref SMP_PAIRING_FAILED_DEFINES.
 * @param       passcode - integer value containing the passcode.
 *
 * @return      SUCCESS - bond record found and changed,<BR>
 *              bleIncorrectMode - Link not found.
 */
bStatus_t GAPBondMgr_PasscodeRsp( uint16 connectionHandle, uint8 status, uint32 passcode )
{
  bStatus_t ret = SUCCESS;
  
  if ( status == SUCCESS )
  {
    // Truncate the passcode
    passcode = passcode % (GAP_PASSCODE_MAX + 1);
    
    ret = GAP_PasscodeUpdate( passcode, connectionHandle );
    if ( ret != SUCCESS )
    {
      VOID GAP_TerminateAuth( connectionHandle, SMP_PAIRING_FAILED_PASSKEY_ENTRY_FAILED );
    }
  }
  else
  {
    VOID GAP_TerminateAuth( connectionHandle, status );
  }
  
  return ret;
}

/*********************************************************************
 * LOCAL FUNCTION PROTOTYPES
 */

/*********************************************************************
 * @fn      gapBondMgrChangeState
 *
 * @brief   Change a state flag in the stateFlags field of the bond record.
 *
 * @param   idx - Bond NV index
 * @param   state - state flage to set or clear
 * @param   set - TRUE to set the flag, FALSE to clear the flag
 *
 * @return  TRUE if NV Record exists, FALSE if NV Record is empty
 */
static uint8 gapBondMgrChangeState( uint8 idx, uint16 state, uint8 set )
{
  gapBondRec_t bondRec;   // Space to read a Bond record from NV
  
  // Look for public address that is used (not all 0xFF's)
  if ( (osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec ) == SUCCESS) 
      && (osal_isbufset( bondRec.publicAddr, 0xFF, B_ADDR_LEN ) == FALSE) )
  {
    // Update the state of the bonded device.
    uint8 stateFlags = bondRec.stateFlags;
    if ( set )
    {
      stateFlags |= state;
    }
    else
    {
      stateFlags &= ~(state);
    }
    
    if ( stateFlags != bondRec.stateFlags )
    {
      bondRec.stateFlags = stateFlags;
      VOID osal_snv_write( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec );
    }
    return ( TRUE );
  }
  return ( FALSE );
}

/*********************************************************************
 * @fn      gapBondMgrAddBond
 *
 * @brief   Save a bond from a GAP Auth Complete Event
 *
 * @param   pBondRec - basic bond record
 * @param   pLocalLTK - LTK used by this device during pairing
 * @param   pDevLTK - LTK used by the connected device during pairing
 * @param   pIRK - IRK used by the connected device during pairing
 * @param   pSRK - SRK used by the connected device during pairing
 * @param   signCounter - Sign counter used by the connected device during pairing
 *
 * @return  bond index
 */
static uint8 gapBondMgrAddBond( gapBondRec_t *pBondRec, 
                                gapBondLTK_t *pLocalLTK, gapBondLTK_t *pDevLTK,
                                uint8 *pIRK, uint8 *pSRK, uint32 signCounter )
{
  uint8 idx;
  
  if ( pBondRec == NULL )
  {
    return ( GAP_BONDINGS_MAX );
  }
  
  // First see if we already have an existing bond for this device
  idx = gapBondMgrFindAddr( pBondRec->publicAddr );
  if ( idx >= GAP_BONDINGS_MAX )
  {
    idx = gapBondMgrFindEmpty();
  }
  
  if ( idx < GAP_BONDINGS_MAX )
  {
    // Save the main information
    VOID osal_snv_write( mainRecordNvID(idx), sizeof ( gapBondRec_t ), pBondRec );

    // If available, save the LTK information    
    if ( pLocalLTK )
    {
      VOID osal_snv_write( localLTKNvID(idx), sizeof ( gapBondLTK_t ), pLocalLTK );
    }
    
    
    // If availabe, save the connected device's LTK information
    if ( pDevLTK )
    {
      VOID osal_snv_write( devLTKNvID(idx), sizeof ( gapBondLTK_t ), pDevLTK );
    }
    
    // If available, save the connected device's IRK 
    if ( pIRK )
    {
      VOID osal_snv_write( devIRKNvID(idx), KEYLEN, pIRK );
    }
    
    // If available, save the connected device's Signature information 
    if ( pSRK )
    {
      VOID osal_snv_write( devCSRKNvID(idx), KEYLEN, pSRK );
      VOID osal_snv_write( devSignCounterNvID(idx), sizeof ( uint32 ), &signCounter );
    }
  }
  
  // Update the GAP Privacy Flag Properties
  gapBondSetupPrivFlag();
  
  return ( idx );
}

/*********************************************************************
 * @fn      gapBondMgrGetStateFlags
 *
 * @brief   Gets the state flags field of a bond record in NV
 *
 * @param   idx
 *
 * @return  stateFlags field
 */
static uint8 gapBondMgrGetStateFlags( uint8 idx )
{
  gapBondRec_t bondRec;
  
  VOID osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec );
  
  return ( bondRec.stateFlags );
  
}

/*********************************************************************
 * @fn      gapBondMgrGetPublicAddr
 *
 * @brief   Copy the public Address from a bonding record
 *
 * @param   idx - Bond record index
 * @param   pAddr - a place to put the public address from NV
 *
 * @return  SUCCESS if successful.
 *          Otherwise failure.
 */
static bStatus_t gapBondMgrGetPublicAddr( uint8 idx, uint8 *pAddr )
{
  bStatus_t stat;         // return value
  gapBondRec_t bondRec;   // Work space for main bond record

  // Check parameters  
  if ( (idx >= GAP_BONDINGS_MAX) || (pAddr == NULL) )
  {
    return ( INVALIDPARAMETER );
  }
  
  stat = osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec );
  
  if ( stat == SUCCESS ) 
  {
    VOID osal_memcpy( pAddr, bondRec.publicAddr, KEYLEN );
  }
  
  return ( stat );
}

/*********************************************************************
 * @fn      gapBondMgrFindReconnectAddr
 *
 * @brief   Look through the bonding NV entries to find a 
 *          reconnection address.
 *
 * @param   pReconnectAddr - device address to look for
 *
 * @return  index to found bonding (0 - (GAP_BONDINGS_MAX-1),
 *          GAP_BONDINGS_MAX if no empty entries
 */
static uint8 gapBondMgrFindReconnectAddr( uint8 *pReconnectAddr )
{
  // Item doesn't exist, so create all the items
  for ( uint8 idx = 0; idx < GAP_BONDINGS_MAX; idx++ )
  {
    gapBondRec_t bondRec; // Bond record work place
    
    // Read in NV Main Bond Record and compare reconnection address
    if ( (osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec ) == SUCCESS) 
        && (osal_memcmp( bondRec.reconnectAddr, pReconnectAddr, KEYLEN )) )
    {
      return ( idx ); // Found it
    }
  }
  
  return ( GAP_BONDINGS_MAX );
}

/*********************************************************************
 * @fn      gapBondMgrFindAddr
 *
 * @brief   Look through the bonding NV entries to find an address.
 *
 * @param   pDevAddr - device address to look for
 *
 * @return  index to empty bonding (0 - (GAP_BONDINGS_MAX-1),
 *          GAP_BONDINGS_MAX if no empty entries
 */
static uint8 gapBondMgrFindAddr( uint8 *pDevAddr )
{
  // Item doesn't exist, so create all the items
  for ( uint8 idx = 0; idx < GAP_BONDINGS_MAX; idx++ )
  {
    gapBondRec_t bondRec; // Bond record work place
    
    // Read in NV Main Bond Record and compare public address
    if ( (osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec ) == SUCCESS) 
        && (osal_memcmp( bondRec.publicAddr, pDevAddr, B_ADDR_LEN )) )
    {
      return ( idx ); // Found it
    }
  }
  
  return ( GAP_BONDINGS_MAX );
}

/*********************************************************************
 * @fn      gapBondMgrFindEmpty
 *
 * @brief   Look through the bonding NV entries to find an empty.
 *
 * @param   none
 *
 * @return  index to empty bonding (0 - (GAP_BONDINGS_MAX-1),
 *          GAP_BONDINGS_MAX if no empty entries
 */
static uint8 gapBondMgrFindEmpty( void )
{
  // Item doesn't exist, so create all the items
  for ( uint8 idx = 0; idx < GAP_BONDINGS_MAX; idx++ )
  {
    gapBondRec_t bondRec; // Bond record work place
    
    // Look for public address of all 0xFF's
    if ( (osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec ) == SUCCESS) 
        && (osal_isbufset( bondRec.publicAddr, 0xFF, B_ADDR_LEN )) )
    {
      return ( idx ); // Found one
    }
  }
  
  return ( GAP_BONDINGS_MAX );
}

/*********************************************************************
 * @fn      gapBondMgrBondTotal
 *
 * @brief   Look through the bonding NV entries calculate the number
 *          entries.
 *
 * @param   none
 *
 * @return  total number of bonds found
 */
static uint8 gapBondMgrBondTotal( void )
{
  uint8 bonds = 0;
  
  // Item doesn't exist, so create all the items
  for ( uint8 idx = 0; idx < GAP_BONDINGS_MAX; idx++ )
  {
    gapBondRec_t bondRec; // Bond record work place
    
    // Look for public address that are not 0xFF's
    if ( (osal_snv_read( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec ) == SUCCESS) 
        && (osal_isbufset( bondRec.publicAddr, 0xFF, B_ADDR_LEN ) == FALSE) )
    {
      bonds++; // Found one
    }
  }
  
  return ( bonds );
}

/*********************************************************************
 * @fn      gapBondMgrEraseAllBondings
 *
 * @brief   Write all 0xFF's to all of the bonding entries
 *
 * @param   none
 *
 * @return  SUCCESS if successful.
 *          Otherwise, NV_OPER_FAILED for failure.
 */
static bStatus_t gapBondMgrEraseAllBondings( void )
{
  bStatus_t stat = SUCCESS;  // return value
  
  // Item doesn't exist, so create all the items
  for ( uint8 idx = 0; (idx < GAP_BONDINGS_MAX) && (stat == SUCCESS); idx++ )
  {
    // Erasing will write/create a bonding entry
    stat = gapBondMgrEraseBonding( idx );
  }
  
  // Update the GAP Privacy Flag Properties
  gapBondSetupPrivFlag();
  
  return ( stat );
}

/*********************************************************************
 * @fn      gapBondMgrEraseBonding
 *
 * @brief   Write all 0xFF's to the complete bonding record
 *
 * @param   idx - bonding index
 *
 * @return  SUCCESS if successful.
 *          Otherwise, NV_OPER_FAILED for failure.
 */
static bStatus_t gapBondMgrEraseBonding( uint8 idx )
{
  bStatus_t ret;
  gapBondRec_t bondRec;
  gapBondLTK_t ltk;
  
  VOID osal_memset( &bondRec, 0xFF, sizeof ( gapBondRec_t ) );
  VOID osal_memset( &ltk, 0xFF, sizeof ( gapBondLTK_t ) );

  // Write out FF's over the entire bond entry.  
  ret = osal_snv_write( mainRecordNvID(idx), sizeof ( gapBondRec_t ), &bondRec );
  ret |= osal_snv_write( localLTKNvID(idx), sizeof ( gapBondLTK_t ), &ltk );
  ret |= osal_snv_write( devLTKNvID(idx), sizeof ( gapBondLTK_t ), &ltk );
  ret |= osal_snv_write( devIRKNvID(idx), KEYLEN, ltk.LTK );
  ret |= osal_snv_write( devCSRKNvID(idx), KEYLEN, ltk.LTK );
  ret |= osal_snv_write( devSignCounterNvID(idx), sizeof ( uint32 ), ltk.LTK );
  
  // Update the GAP Privacy Flag Properties
  gapBondSetupPrivFlag();
  
  return ( ret );
}

/*********************************************************************
 * @brief   Task Initialization function.
 *
 * Internal function defined in gapperiphbondmgr.h.
 */
void GAPBondMgr_Init( uint8 task_id )
{
  gapBondRec_t bondRec;         // Work space for Bond Record
  gapBondMgr_TaskID = task_id;  // Save task ID
  
  // Initialize the NV needed for bonding
  if ( osal_snv_read( mainRecordNvID(0), sizeof ( gapBondRec_t ), &bondRec ) != SUCCESS )
  {
    // Can't read the first entry, assume that NV doesn't exist and erase all
    // Bond NV entries (initialize)
    VOID gapBondMgrEraseAllBondings();
  }
  
  // Take over the processing of Authentication messages
  VOID GAP_SetParamValue( TGAP_AUTH_TASK_ID, gapBondMgr_TaskID );
  
  // Check the total number of bonds
  gapBondSetupPrivFlag();
}

/*********************************************************************
 * @brief   Task Event Processor function.
 *
 * Internal function defined in gapperiphbondmgr.h.
 */
uint16 GAPBondMgr_ProcessEvent( uint8 task_id, uint16 events )
{
  VOID task_id; // OSAL required parameter that isn't used in this function
  
  if ( events & SYS_EVENT_MSG )
  {
    uint8 *pMsg;

    if ( (pMsg = osal_msg_receive( gapBondMgr_TaskID )) != NULL )
    {
      gapBondMgr_ProcessOSALMsg( (osal_event_hdr_t *)pMsg );

      // Release the OSAL message
      VOID osal_msg_deallocate( pMsg );
    }

    // return unprocessed events
    return (events ^ SYS_EVENT_MSG);
  }

  // Discard unknown events
  return 0;
}

/*********************************************************************
 * @fn      gapBondMgr_ProcessOSALMsg
 *
 * @brief   Process an incoming task message.
 *
 * @param   pMsg - message to process
 *
 * @return  none
 */
static void gapBondMgr_ProcessOSALMsg( osal_event_hdr_t *pMsg )
{
  switch ( pMsg->event )
  {
    case GAP_MSG_EVENT:
      gapBondMgr_ProcessGAPMsg( (gapEventHdr_t *)pMsg );
      break;

    case GATT_MSG_EVENT:
      gapBondMgr_ProcessGATTMsg( (gattMsgEvent_t *)pMsg );
      break;

    default:
      break;
  }
}

/*********************************************************************
 * @fn      gapRole_ProcessGAPMsg
 *
 * @brief   Process an incoming task message.
 *
 * @param   pMsg - message to process
 *
 * @return  none
 */
void gapBondMgr_ProcessGAPMsg( gapEventHdr_t *pMsg )
{
  switch ( pMsg->opcode )
  {
    case GAP_PASSKEY_NEEDED_EVENT:
      {
        gapPasskeyNeededEvent_t *pPkt = (gapPasskeyNeededEvent_t *)pMsg;
        
        if ( pGapBondCB && pGapBondCB->passcodeCB )
        {
          // Ask app for a passcode
          pGapBondCB->passcodeCB( pPkt->deviceAddr, pPkt->connectionHandle, pPkt->uiInputs, pPkt->uiOutputs );
        }
        else
        {
          // No app support, use the default passcode
          if ( GAP_PasscodeUpdate( gapBond_Passcode, pPkt->connectionHandle ) != SUCCESS )
          {
            VOID GAP_TerminateAuth( pPkt->connectionHandle, SMP_PAIRING_FAILED_PASSKEY_ENTRY_FAILED );
          }
        }        
      }
      break;
      
    case GAP_AUTHENTICATION_COMPLETE_EVENT:
      {
        gapAuthCompleteEvent_t *pPkt = (gapAuthCompleteEvent_t *)pMsg;
        
        // Should we save bonding information
        if ( (pPkt->hdr.status == SUCCESS) && (pPkt->authState & SM_AUTH_STATE_BONDING) )
        {
          gapBondRec_t bondRec;
          
          VOID osal_memset( &bondRec, 0, sizeof ( gapBondRec_t ) ) ;
          
          // Do we have a public address in the data?
          if ( pPkt->pIdentityInfo )
          {
            VOID osal_memcpy( bondRec.publicAddr, pPkt->pIdentityInfo->bd_addr, B_ADDR_LEN );
          }
          else
          {
            linkDBItem_t *pLinkItem = linkDB_Find( pPkt->connectionHandle );
            if ( pLinkItem )
            {
              VOID osal_memcpy( bondRec.publicAddr, pLinkItem->addr, B_ADDR_LEN );
            }
            else
            {
              // We don't have an address, so ignore the message.
              break;
            }
          }
          
          // Save off of the authentication state
          bondRec.stateFlags |= (pPkt->authState & SM_AUTH_STATE_AUTHENTICATED) ? GAP_BONDED_STATE_AUTHENTICATED : 0;
          
          VOID gapBondMgrAddBond( &bondRec, 
                             (gapBondLTK_t *)pPkt->pSecurityInfo, 
                             (gapBondLTK_t *)pPkt->pDevSecInfo,
                             ((uint8 *)((pPkt->pIdentityInfo) ? pPkt->pIdentityInfo->irk : NULL )),
                             ((uint8 *)((pPkt->pSigningInfo) ? pPkt->pSigningInfo->srk : NULL )),
                             ((uint32)((pPkt->pSigningInfo) ? pPkt->pSigningInfo->signCounter : GAP_INIT_SIGN_COUNTER )) );
                             
        }
        
        // Call app state callback
        if ( pGapBondCB && pGapBondCB->pairStateCB )
        {
          pGapBondCB->pairStateCB( GAPBOND_PAIRING_STATE_COMPLETE, pPkt->hdr.status );
        }
      }
      break;
        
    case GAP_BOND_COMPLETE_EVENT:
      // This message is received when the bonding is complete.  If hdr.status is SUCCESS
      // then call app state callback.  If hdr.status is NOT SUCCESS, the connection will be 
      // dropped at the LL because of a MIC failure, so again nothing to do.
      if ( pGapBondCB && pGapBondCB->pairStateCB && pMsg->hdr.status == SUCCESS )
      {
        pGapBondCB->pairStateCB( GAPBOND_PAIRING_STATE_BONDED, pMsg->hdr.status );
      }  
      break;
      
    case GAP_SIGNATURE_UPDATED_EVENT:
      {
        uint8 idx;
        gapSignUpdateEvent_t *pPkt = (gapSignUpdateEvent_t *)pMsg;
        
        idx = GAPBondMgr_ResolveAddr( pPkt->addrType, pPkt->devAddr, NULL );
        if ( idx < GAP_BONDINGS_MAX )
        {
          // Save the sign counter
          VOID osal_snv_write( devSignCounterNvID(idx), sizeof ( uint32 ), &(pPkt->signCounter) );
        }
      }
      break;
      
    case GAP_PAIRING_REQ_EVENT:
      {
        gapPairingReqEvent_t *pPkt = (gapPairingReqEvent_t *)pMsg;
        
        if ( gapBond_AutoFail != FALSE )
        {
          // Auto Fail TEST MODE (DON'T USE THIS) - Sends pre-setup reason
          VOID GAP_TerminateAuth( pPkt->connectionHandle, gapBond_AutoFailReason );
        }
        else if ( gapBond_PairingMode == GAPBOND_PAIRING_MODE_NO_PAIRING )
        {
          // No Pairing - Send error
          VOID GAP_TerminateAuth( pPkt->connectionHandle, SMP_PAIRING_FAILED_NOT_SUPPORTED );
        }
        else
        {
          gapAuthParams_t params;
          linkDBItem_t *pLinkItem = linkDB_Find( pPkt->connectionHandle );
          
          // Requesting bonding?
          if ( pPkt->pairReq.authReq & SM_AUTH_STATE_BONDING )
          {
            if ( pLinkItem ) 
            {
              if ( (pLinkItem->addrType != ADDRTYPE_PUBLIC) && (pPkt->pairReq.keyDist.mIdKey == FALSE) )
              {
                uint8 publicAddr[B_ADDR_LEN];
                
                // Check if we already have the public address in NV
                if ( GAPBondMgr_ResolveAddr(pLinkItem->addrType, pLinkItem->addr, publicAddr ) == FALSE )
                {
                  // Can't bond to a non-public address if we don't know the public address
                  VOID GAP_TerminateAuth( pPkt->connectionHandle, SMP_PAIRING_FAILED_AUTH_REQ );
                  break;
                }
              }
            }
            else
            {
              // Can't find the connection, ignore the message
              break;
            }
          }

          VOID osal_memset( &params, 0, sizeof ( gapAuthParams_t ) );
          
          // Setup the pairing parameters
          params.connectionHandle = pPkt->connectionHandle;
          params.secReqs.ioCaps = gapBond_IOCap;
          params.secReqs.oobAvailable = gapBond_OOBDataFlag;
          params.secReqs.maxEncKeySize = gapBond_KeySize;
          
          // TBD - Should we distribute keys if bonding is turned off???
          params.secReqs.keyDist.sEncKey = (gapBond_KeyDistList | GAPBOND_KEYDIST_SENCKEY) ? TRUE : FALSE;
          params.secReqs.keyDist.sIdKey = (gapBond_KeyDistList | GAPBOND_KEYDIST_SIDKEY) ? TRUE : FALSE;
          params.secReqs.keyDist.mEncKey = (gapBond_KeyDistList | GAPBOND_KEYDIST_MENCKEY) ? TRUE : FALSE;
          params.secReqs.keyDist.mIdKey = (gapBond_KeyDistList | GAPBOND_KEYDIST_MIDKEY) ? TRUE : FALSE;
          params.secReqs.keyDist.mSign = (gapBond_KeyDistList | GAPBOND_KEYDIST_MSIGN) ? TRUE : FALSE;
          params.secReqs.keyDist.sSign = (gapBond_KeyDistList | GAPBOND_KEYDIST_SSIGN) ? TRUE : FALSE;
          
          // Is bond manager setup for OOB data?
          if ( gapBond_OOBDataFlag )
          {
            VOID osal_memcpy( params.secReqs.oob, gapBond_OOBData, KEYLEN );
          }
          
          if ( (pPkt->pairReq.authReq & SM_AUTH_STATE_BONDING) && (gapBond_Bonding) )
          {
            params.secReqs.authReq |= SM_AUTH_STATE_BONDING;
            if ( pLinkItem->addrType != ADDRTYPE_PUBLIC )
            {
              // Force a master ID key 
              params.secReqs.keyDist.mIdKey = TRUE;
            }
          }
          
          // Is Bond Manager setup for passkey protection?
          if ( gapBond_MITM )
          {
            params.secReqs.authReq |= SM_AUTH_STATE_AUTHENTICATED;
          }
          
          VOID GAP_Authenticate( &params, &(pPkt->pairReq) );
        }
        
        // Call app state callback
        if ( pGapBondCB && pGapBondCB->pairStateCB )
        {
          pGapBondCB->pairStateCB( GAPBOND_PAIRING_STATE_STARTED, pPkt->hdr.status );
        }        
      }
      break;
      
    default:
      break;
  }
}

/*********************************************************************
 * @fn      GAPBondMgr_CheckNVLen
 *
 * @brief   This function will check the length of an NV Item.
 *
 * @param   id - NV ID.
 * @param   len - lengths in bytes of item.
 *
 * @return  SUCCESS or FAILURE
 */
uint8 GAPBondMgr_CheckNVLen( uint8 id, uint8 len )
{
  uint8 stat = FAILURE;
  
  // Convert to index
  switch ( (id - BLE_NVID_GAP_BOND_START) % GAP_BOND_REC_IDS )
  {
    case GAP_BOND_REC_ID_OFFSET:
      if ( len == sizeof ( gapBondRec_t ) )
      {
        stat = SUCCESS;
      }
      break;
      
    case GAP_BOND_LOCAL_LTK_OFFSET:
    case GAP_BOND_DEV_LTK_OFFSET:
      if ( len == sizeof ( gapBondLTK_t ) )
      {
        stat = SUCCESS;
      }
      break;
      
    case GAP_BOND_DEV_IRK_OFFSET:
    case GAP_BOND_DEV_CSRK_OFFSET:
      if ( len == KEYLEN )
      {
        stat = SUCCESS;
      }
      break;
      
    case GAP_BOND_DEV_SIGN_COUNTER_OFFSET:
      if ( len == sizeof ( uint32 ) )
      {
        stat = SUCCESS;
      }
      break;
  }
  
  return ( stat );
}

/*********************************************************************
 * @fn          gapBondMgr_ProcessGATTMsg
 *
 * @brief       Process an incoming GATT message.
 *
 * @param       pMsg - pointer to received message
 *
 * @return      none
 */
static void gapBondMgr_ProcessGATTMsg( gattMsgEvent_t *pMsg )
{
  // Process the GATT message
  switch ( pMsg->method )
  {
    case ATT_HANDLE_VALUE_CFM:
      // Clear Service Changed flag for this client
      VOID GAPBondMgr_ServiceChangeInd( pMsg->connHandle, 0x00 );
      break;

    default:  
      // Unknown message
      break;
  }
}

/*********************************************************************
 * @fn      gapBondMgrSendServiceChange
 *
 * @brief   Tell the GATT that a service change is needed.
 *
 * @param   pLinkItem - pointer to connection information
 *
 * @return  none
 */
static void gapBondMgrSendServiceChange( linkDBItem_t *pLinkItem )
{
  VOID GATT_ServiceChangedInd( pLinkItem->connectionHandle, gapBondMgr_TaskID );
}

/*********************************************************************
 * @fn      gapBondSetupPrivFlag
 *
 * @brief   Setup the GAP Privacy Flag properties.
 *
 * @param   none
 *
 * @return  none
 */
static void gapBondSetupPrivFlag( void )
{
  uint8 privFlagProp;
  
  if ( gapBondMgrBondTotal() > 1 )
  {
    privFlagProp = GATT_PROP_READ;
  }
  else
  {
    privFlagProp = GATT_PROP_READ | GATT_PROP_WRITE;
  }
  
  // Setup the 
  GGS_SetParameter( GGS_PERI_PRIVACY_FLAG_PROPS, sizeof ( uint8 ), &privFlagProp );
}

/*********************************************************************
*********************************************************************/
