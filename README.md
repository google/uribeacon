# Welcome to the UriBeacon Open Source Project

*(pronounced YUR-ee-BEE-kun)*

**A Bluetooth 4.0 advertisment format containing Web Uris for the Internet of Things.**


## Why We Created the UriBeacon Specification

The UriBeacon Specification is designed with one goal in mind: to
create the easiest, most open way to discover and use nearby smart
things.

UriBeacon is a wireless advertisement format for broadcasting Uris to
any nearby smart device. By leveraging the Open Web it enables a
*real*-World Wide Web where every person, place and thing can have its
own Web presence. Our design is:

1. Compliant with Bluetooth 4.0 Advertising messages.
2. Compatable with iOS and Android low power scanning modes.
3. Independent of any particular hardware, user-agent, service or mobile platform.
4. Openly licensed under Apache 2.0 and as such free of restrictive and proprietary terms.

In contrast, existing beacon specifications ask users to constantly
install new smart phone applications, use a specific cloud service,
and/or require restrictive terms of use.

In short, UriBeacon connects low power beacons to the family of Open
Web technologies and is one step towards making the Internet of Things as
easy to use as the World Wide Web.

## Contents

This repository contains the UriBeacon written specification
and sample code for advertisers and scanners:

* **[specification](specification) - The UriBeacon Open Specification.**
* **[android-uribeacon](android-uribeacon) - Allows Android apps to interact with UriBeacons.**
* **[ios-uribeacon](ios-uribeacon) - Allows iOS apps to interact with UriBeacons.**
* **[linux-uribeacon](linux-uribeacon) - Allows Linux users to experiment with UriBeacons.**

We will also be providing instructions on how to assemble and 
program your own standalone UriBeacon tags to make the things you
care about smarter.

## UriBeacon Devices and Tags

Various things may broadcast UriBeacons. Smart devices such as phones,
tablets, laptops, vending machines, irrigation controllers may
incorporate built in UriBeacons. Other things such as movie posters,
bus stops, and meeting rooms may use small hardware devices, or tags,
specifically designed to emit a UriBeacon advertisement. The libraries
and sample code in this repository are a resource for developers who
want to encorporate UriBeacons in existing devices and also to create
standalone UriBeacon tags.


## Short Urls

In order to allow arbitrary sized Urls in short messages the
specification finds inspiration from Twitter where all Url links
posted in Tweets are auto-shortened. Similarly the UriBeacon contains
either a naturally short Uri or one that is shortened through a
third-party service. The actual Uri shortener service used is up to
the tag writer.

UriBeacon also uses an encoding that replaces common strings like
`https://` with a single byte code. The result are Uris that can fit
within the limited size of `ADV_NONCONN_IND` packets as shown in the
table below:

|Service      | Example              | Length | Encoded Length
|------------ | -------------------- | ------ | --------------
|goo.gl       | http://goo.gl/lNhc7  | 19     | 13
|bit.ly       | http://bit.ly/a99vaw | 20     | 14
|is.gd        | http://is.gd/EstPIk  | 19     | 13
|t.co         | http://t.co/LEGhQ4i0 | 20     | 14

**Table 1: Sampling of Url Shorteners**

Note that short Urls are mostly transparent: user agents can hide
short and show long URLs, titles, etc. and configuration tools can
automatically shorten long Urls when writing beacons. Also this
specification does not preclude additional information exchange over a
GATT service following discovery.

## Disclaimer

This is not an official Google product.

