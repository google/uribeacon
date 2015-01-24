# Building mbed UriBeacon on Linux

## Preliminaries

You'll need to install:

1. [GNU Tools for ARM Embedded](https://launchpad.net/gcc-arm-embedded)
2. [cmake](http://www.cmake.org/) command line tools.

```
sudo apt-get install gcc-arm-none-eabi
sudo apt-get install libnewlib-arm-none-eabi 
```

There is an issue where ``<stdlib>`` is not installed as part of ``gcc-arm-none-eabi`` so the
``libnewlib-arm-none-eabi`` package also needs to be installed.

If you have issues, see
https://launchpad.net/~terry.guo/+archive/ubuntu/gcc-arm-embedded

## Introduction


## For Mac users

NOTE: Mac doesn't flash onto the nRF51822-Dongle. Stick with Linux!

I never got this to build on Macs. But these are some hints. You'll
eventually find that ``srecord`` is not functioning properly.

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

## Linux Build

Then issue the following

```
git clone --recursive http://github.com/google/uribeacon
cd uribeacon/beacon/mbed
mkdir build
cd build
cmake ..
make
```

we are using submodules, update the ``mbedmicro`` repos:
```
cd uribeacon
git pull && git submodule init && git submodule update && git submodule status
```

Install the `combined.hex` onto the target over the USB:
```
cp combined.hex /media/$USER/JLINK
```

## C++ Source formatting

We use [astyle](http://sourceforge.net/projects/astyle/files/astyle) for formatting
```
astyle --style=google main.cpp
```

