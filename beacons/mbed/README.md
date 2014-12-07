# Building mbed UriBeacon

## Introduction

You'll need to install three components:

1. [GNU Tools for ARM Embedded](https://launchpad.net/gcc-arm-embedded)
2. [cmake](http://www.cmake.org/) command line tools.
3. [srecord](http://srecord.sourceforge.net/)

## For Mac users

Download and install the Macintosh version of the latest [GNU Tools
for ARM Embedded](https://launchpad.net/gcc-arm-embedded)

Add ``gcc-arm-none-eabi-4_8-2014q3/bin`` to your ``PATH``

Download and install [homebrew](http://brew.sh/)

```
ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
```

Install [srecord](http://srecord.sourceforge.net/) and
[cmake](http://www.cmake.org/) command line tools.

```
brew install srecord
brew install cmake
```

The issue the following

```
git clone --recursive http://github.com/google/uribeacon
cd uribeacon/beacon/mbed
mkdir build
cd build
cmake ..
make
```

Install the `combined.hex` onto the target over the USB.

NOTE: Mac doesn't flash onto the nRF51822-Dongle. Stick with Linux!