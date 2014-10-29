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

#import "NSString+UB.h"

@implementation NSString (PW)

struct expansion {
  char code;
  const char *value;
  int length;
};

static struct expansion expansionsList[] = {
    {0, "http://www."},
    {1, "https://www."},
    {2, "http://"},
    {3, "https://"},
    {4, "tel:"},
    {5, "mailto:"},
    {6, "geo:"},
    {7, ".com"},
    {8, ".org"},
    {9, ".edu"},
};

static const char *expansionsMapping[255];

+ (NSString *)ub_decodedBeaconURIString:(NSData *)data {
  const char *bytes = [data bytes];
  NSUInteger length = [data length];
  NSMutableData *resultData = [NSMutableData data];
  for (unsigned i = 0; i < length; i++) {
    const char *expansionValue = expansionsMapping[(unsigned char)bytes[i]];
    if (expansionValue == NULL) {
      // TODO(dvh): There's probably room for optimization: several non-encoded
      // bytes
      // could be appended in one -appendBytes:length: call.
      [resultData appendBytes:&bytes[i] length:1];
    } else {
      [resultData appendBytes:expansionValue length:strlen(expansionValue)];
    }
  }

  return
      [[NSString alloc] initWithData:resultData encoding:NSUTF8StringEncoding];
}

- (NSData *)ub_encodedBeaconURIString {
  // TODO(dvh): There's room for optimization: implementing a trie would help.
  NSData *data = [self dataUsingEncoding:NSUTF8StringEncoding];
  NSMutableData *encodedData = [NSMutableData data];
  unsigned int i = 0;
  const char *bytes = [data bytes];
  while (i < [data length]) {
    int found = -1;
    int foundLength = -1;
    for (unsigned int k = 0;
         k < sizeof(expansionsList) / sizeof(expansionsList[0]); k++) {
      const char *value = expansionsList[k].value;
      if (strncmp(bytes + i, value, expansionsList[k].length) == 0 && expansionsList[k].length > foundLength) {
        found = k;
        foundLength = expansionsList[k].length;
      }
    }
    if (found != -1) {
      char b = (char)found;
      [encodedData appendBytes:&b length:1];
      i += expansionsList[found].length;
    } else {
      [encodedData appendBytes:bytes + i length:1];
      i++;
    }
  }
  return encodedData;
}

@end

__attribute__((constructor)) static void initialize() {
  // Build the mapping for text expansions.
  for (unsigned int i = 0;
       i < sizeof(expansionsList) / sizeof(expansionsList[0]); i++) {
    expansionsMapping[expansionsList[i].code] = expansionsList[i].value;
    expansionsList[i].length = (int) strlen(expansionsList[i].value);
  }
}
