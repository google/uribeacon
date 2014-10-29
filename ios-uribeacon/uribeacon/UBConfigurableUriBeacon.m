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

#import "UBConfigurableUriBeacon.h"
#import "UBConfigurableUriBeaconPrivate.h"

#import "UBUriBeaconScanner.h"
#import "UBUriBeaconScannerPrivate.h"
#import "UBUriBeacon.h"
#import "UBUriBeaconPrivate.h"
#import "NSURL+UB.h"

@implementation UBConfigurableUriBeacon {
  UBUriBeaconScanner *_scanner;
  CBPeripheral *_peripheral;
}

- (id)initWithPeripheral:(CBPeripheral *)peripheral
       advertisementData:(NSDictionary *)advertisementData
                    RSSI:(NSNumber *)RSSI {
  self = [super init];
  if (!self) {
    return nil;
  }
  NSArray *serviceUUIDS =
      [advertisementData objectForKey:CBAdvertisementDataServiceUUIDsKey];
  if (![serviceUUIDS
          containsObject:
              [CBUUID
                  UUIDWithString:@"B35D7DA6-EED4-4D59-8F89-F6573EDEA967"]]) {
    return nil;
  }
  _peripheral = peripheral;
  [self setIdentifier:[peripheral identifier]];
  [self setRSSI:[RSSI intValue]];
  return self;
}

- (void)setScanner:(UBUriBeaconScanner *)scanner {
  _scanner = scanner;
}

- (UBUriBeaconScanner *)scanner {
  return _scanner;
}

- (BOOL)isEqual:(id)object {
  UBConfigurableUriBeacon *otherBeacon = object;
  return [[self identifier] isEqual:[otherBeacon identifier]] &&
         ([self RSSI] == [otherBeacon RSSI]);
}

- (void)connect:(void (^)(NSError *error))block {
  [[self scanner] _connectBeaconWithPeripheral:_peripheral
                               completionBlock:block];
}

- (void)disconnect:(void (^)(NSError *error))block {
  [[self scanner] _disconnectBeaconWithPeripheral:_peripheral
                                  completionBlock:block];
}

- (void)writeBeacon:(UBUriBeacon *)beacon
    completionBlock:(void (^)(NSError *error))block {
  [[self scanner] _writeBeaconWithPeripheral:_peripheral
                           advertisementData:[beacon _advertisementData]
                             completionBlock:block];
}

- (void)readBeaconWithCompletionBlock:(void (^)(NSError* error,
                                                UBUriBeacon* beacon))block {
  [[self scanner] _readBeaconWithPeripheral:_peripheral
                            completionBlock:^(NSError* error, NSData* data) {
                                UBUriBeacon* beacon = [[UBUriBeacon alloc]
                                    initWithPeripheral:_peripheral
                                                  data:data
                                                  RSSI:[self RSSI]];
                                block(error, beacon);
                            }];
}

- (void)_updateWithConfigurableBeacon:(UBConfigurableUriBeacon *)beacon {
  _peripheral = beacon->_peripheral;
  [self setRSSI:[beacon RSSI]];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"<%@: %p %@ %li>", [self class], self,
                                    [self identifier], (long)[self RSSI]];
}

@end
