/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "UBUriBeacon.h"

#import <CoreBluetooth/CoreBluetooth.h>

#define URIBEACON_SERVICE @"FED8"

#define CONFIG_SERVICE @"b35d7da6-eed4-4d59-8f89-f6573edea967"
//                              ^

#define CONFIG_CHARACTERISTIC_DATA1 @"b35d7da7-eed4-4d59-8f89-f6573edea967"
//                                           ^

#define CONFIG_CHARACTERISTIC_DATA2 @"b35d7da8-eed4-4d59-8f89-f6573edea967"
//                                           ^

#define CONFIG_CHARACTERISTIC_DATASIZE @"b35d7da9-eed4-4d59-8f89-f6573edea967"
//                                              ^

@interface UBUriBeacon (Private)

- (id)initWithPeripheral:(CBPeripheral *)peripheral
       advertisementData:(NSDictionary *)advertisementData
                    RSSI:(NSNumber *)RSSI;

- (id)initWithPeripheral:(CBPeripheral *)peripheral
                    data:(NSData *)data
                    RSSI:(NSInteger)RSSI;

- (NSData *)_advertisementData;

- (void)_updateWithBeacon:(UBUriBeacon *)beacon;

@end
