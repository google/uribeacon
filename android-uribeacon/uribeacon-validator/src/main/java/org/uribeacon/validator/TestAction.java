package org.uribeacon.validator;

import java.util.UUID;

public class TestAction {
  public final static int CONNECT = 0;
  public final static int WRITE = 1;
  public final static int ASSERT = 2;
  public final static int DISCONNECT = 3;
  public final static int LAST = 4;


  public int actionType;
  public UUID characteristicUuid;
  public int expectedReturnCode;
  public byte[] transmittedValue;
  public boolean failed;
  public String reason;

  public TestAction(int type) {
    this.actionType = type;
  }

  public TestAction(int actionType, UUID characteristicUuid, int expectedReturnCode, byte[] transmittedValue) {
    this.actionType = actionType;
    this.characteristicUuid = characteristicUuid;
    this.expectedReturnCode = expectedReturnCode;
    this.transmittedValue = transmittedValue;
  }
}

