# Welcome to the UriBeacon<br> Open Source Project

[![Build Status](https://travis-ci.org/google/uribeacon.png?branch=master)](https://travis-ci.org/google/uribeacon)

**The Web Uri Open Beacon specification for the Internet of Things**

*(pronounced YUR-ee-BEE-kun)*

**Are you a UriBeacon Developer?** Open an [issue](https://github.com/google/uribeacon/issues) and we will point you at resources to help development and testing.

**Are you looking for UriBeacon Products?** You can get UriBeacons from any of the following vendors:

* [Blesh](https://www.blesh.com/physical-web)
* [Twocanoes](http://twocanoes.com/products/hardware/physical-web-beacon)
* [KST](https://kstechnologies.com/shop/particle/)
* [iBLio](http://www.iblio.net)
* [BKON](http://www.bkon.com/uribeacon)
* [GoPlus](http://iffu.co.kr/eng/product/go-plus_uri.html)
* [BlueUp](http://www.blueupbeacons.com/index.php?page=products)

and also see the flashable implementations on the [GitHub repo](https://github.com/google/uribeacon/blob/master/beacons/README.md).

**Interested in the big picture?** Check out how UriBeacons are used in the [Physical Web](http://google.github.io/physical-web/) project.

## Disclaimer

This is not an official Google product. This is an early-stage experiment that we are developing in the open as we do all things related to the web. This should only be of interest to developers looking to provide feedback and [contribute](CONTRIBUTING.md).

## Why We Created the UriBeacon Specification

The UriBeacon Specification is designed with one goal in mind: to
create the easiest, most open way to discover and use nearby smart
things.

UriBeacon is a wireless advertisement format for broadcasting Uris to
any nearby smart device. By leveraging the Open Web it enables a
*real*-World Wide Web where every person, place and thing can have its
own Web presence. Our design is:

1. Compliant with Bluetooth 4.0 Advertising messages.
2. Compatible with iOS and Android low power scanning modes.
3. Independent of any particular hardware, user-agent, service or mobile platform.
4. Openly licensed under Apache 2.0 and as such free of restrictive and proprietary terms.

In short, UriBeacon connects low power beacons to the family of Open
Web technologies and is one step towards making the Internet of Things as
easy to use as the World Wide Web.

## Contents

This repository contains the UriBeacon written specification
and sample code for advertisers and scanners:

* **[specification](specification) - The UriBeacon Open Specification.**
* **[android-uribeacon](android-uribeacon) - Android client to interact with UriBeacons.**
* **[ios-uribeacon](ios-uribeacon) - iOS client to interact with UriBeacons.**
* **[uribeacon-validator](android-uribeacon/uribeacon-validator) - Android app for testing UriBeacon implementations over BLE.**
* **[beacons](beacons) - Implementations for UriBeacon on a number of embedded BLE devices.**

We will also be providing instructions on how to assemble and
program standalone UriBeacon tags.

The releases tab contains the [UriBeacon Developer Tools](https://github.com/google/uribeacon/releases).

## What is a Uri?

One can classify URIs as Locators (URLs), or Names (URNs), or both.

A Name functions like a person’s name (or Government ID number), while a Locator resembles that person’s postal address.

In other words: URN is an item’s identity, while the URL is a method for finding it.

* `urn:uuid:f81d4fae-7dec-11d0-a765-00a0c91e6bf6` [RFC4122](http://www.ietf.org/rfc/rfc4122.txt)
* `https://www.ietf.org/rfc/rfc2141.txt` [RFC1738](http://www.ietf.org/rfc/rfc1738.txt)

UriBeacons can broadcast familiar HTTP URLs (`http` and `https`), UUID URNs (128-bit universally unique identifiers) as well as other URIs.

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

For example, let's say we have a truncated URL of the form "https://www..com". The initial "https://www." is encoded in the scheme prefix and ".com" is encoded as a single byte. This leaves 16 bytes for the remainder of the URL, which leads 16 characters to play with in most cases.

Note that short Urls are mostly transparent: user agents can hide
short and show long URLs, titles, etc. and configuration tools can
automatically shorten long Urls when writing beacons. Also this
specification does not preclude additional information exchange over a
GATT service following discovery.
