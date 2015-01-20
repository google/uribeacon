package org.uribeacon.validator;

public class TestData {

  public static byte[] SHORT_LOCK_KEY = new byte[15];
  public static byte[] BASIC_LOCK_KEY = new byte[16];
  public static byte[] LONG_LOCK_KEY = new byte[17];
  public static byte[] UNLOCKED_STATE = new byte[]{0};
  public static byte[] LOCKED_STATE = new byte[]{1};
  public static byte[] BASIC_GENERAL_DATA = new byte[]{1};
  public static byte[] LONG_URI = new byte[]{19};
  public static byte[] BASIC_TX_POWER = new byte[]{0, 0, 0, 0};
  // period = 999
  public static byte[] BASIC_PERIOD = new byte[]{(byte) 0xE7, 0x03};
}
