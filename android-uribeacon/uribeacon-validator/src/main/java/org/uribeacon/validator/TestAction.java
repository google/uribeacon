/*
 * Copyright 2015 Google Inc. All rights reserved.
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

package org.uribeacon.validator;

import java.util.UUID;

class TestAction {

  public final static int CONNECT = 0;
  public final static int WRITE = 1;
  public final static int ASSERT = 2;
  public final static int ASSERT_NOT_EQUALS = 3;
  public final static int DISCONNECT = 4;
  public final static int ADV_TX_POWER = 5;
  public final static int ADV_FLAGS = 6;
  public final static int ADV_URI = 7;
  public final static int ADV_PACKET = 8;
  public final static int LAST = 9;


  public final int actionType;
  public UUID characteristicUuid;
  public int expectedReturnCode;
  public byte[] transmittedValue;
  public boolean failed;
  public String reason;

  public TestAction(int type) {
    this.actionType = type;
  }

  public TestAction(int actionType, UUID characteristicUuid, int expectedReturnCode,
      byte[] transmittedValue) {
    this.actionType = actionType;
    this.characteristicUuid = characteristicUuid;
    this.expectedReturnCode = expectedReturnCode;
    this.transmittedValue = transmittedValue;
  }

  public TestAction(int actionType, byte[] transmittedValue) {
    this.actionType = actionType;
    this.transmittedValue = transmittedValue;
  }
}

