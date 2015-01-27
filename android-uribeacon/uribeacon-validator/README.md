# UriBeacon Validation App #

## Overview ##
The purpose of the UriBeacon Validation app is to allow developers to test their implementations of UriBeacon.
The app consists of two sets of tests: Core UriBeacon Tests and Spec UriBeacon Tests.

### Core UriBeacon Tests ###
The Core UriBeacon Tests are design to make sure the app works seamlessly with the Physical Web App 
and the UriBeacon Sample app. The following table shows the expected results for each of the tests. 

| Test Name  | Value Written | Expected value read | Expected Return Code
| ------------- | :-------------: | :----------: | -------------------- |
| Connect to UriBeacon  | N/A  | N/A | 0 |
| Read Lock State | N/A | [0] | 0 |
| Lock Beacon** | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] | N/A | 0 |
| Unlock Beacon** | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] | N/A | 0 |
| Locking...** | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] | N/A | 0 |
| Try Unlock with Wrong Key** | [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1] | N/A | 8 |
| Unlocking...** | [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] | N/A | 0 |
| Write Reset  | [1]  | N/A | 0 |
| Write and Read Data 1| [2] |  [2] | 0 |
| Write and Read Data 2| [1] |  [1] | 0 |
| Write and Read Tx Power Levels 1| [1, 1, 1, 1] | [1, 1, 1, 1]  | 0 |
| Write and Read Tx Power Levels 2| [0, 0, 0, 0] | [0, 0, 0, 0]  | 0 |
| Write and Read Tx Power Mode 1| [2] | [2] | 0 |
| Write and Read Tx Power Mode 2| [1] | [1] | 0 |
| Write and Read Period 1| [233, 3] |  [233, 3] | 0 |
| Write and Read Period 2| [231, 3] |  [231, 3] | 0 |
| Disconnecting | N/A | N/A | 0 |
| Has Valid Advertisement Packet | N/A | Looking for any valid Advertisement Packet | N/A |
| Flags Written are Broadcasted | N/A | [1] | N/A |
| Tx Power Written is Broadcasted | N/A | [0] | N/A |
| Uri Written is Broadcasted | N/A | [1] | N/A |

** Only if Lock/Unlock is implemented
