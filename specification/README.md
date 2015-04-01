# UriBeacon Specification

***The Web Uri Open Beacon specification for the Internet of Things***

*(pronounced YUR-ee-BEE-kun)*

Note: this is a preview-release intended for comments and experimentation.

## Overview

The UriBeacon design allows a Bluetooth Low Energy (BLE) device to periodically transmit an ADV packet that contains a URI.  This technology enables any device, place or thing to become a beacon through physical association. As a consequence UriBeacon enables any object in the physical world to transmit a URI over a short distance (typically 20-30 feet), and is an enabling technology to support the [Physical Web](https://github.com/google/physical-web) paradigm. By providing discovery, search, and control (using web pages) based on situated proximity, the Physical Web can be thought of as an on-ramp to the Internet-of-things(IoT).

A UriBeacon can exist in two states:

* [Advertising Mode](AdvertisingMode.md) (non-connectable)

* [Configuration Mode](ConfigService.md) (connectable)

In the URI Advertising mode a beacon will periodically transmit (typically at 1 second intervals) an ADV packet containing: flags, transmit power, and an encoded URI. In this state the beacon content cannot be modified, and the device does not support a connection. See [UriBeacon ADV packet](AdvertisingMode.md) for a full description.

In the Configuration Mode the UriBeacon is connectable and supports the UriBeacon configuration service. Using this service, the ADV packet payload can be modified, parameters can be locked/unlocked with a passcode, and operational characteristics modified.  See [Uribeacon Configuration Mode](ConfigService.md) for a full description.
