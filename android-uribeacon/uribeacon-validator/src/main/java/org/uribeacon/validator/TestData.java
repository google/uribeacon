package org.uribeacon.validator;

public class TestData {

  public static byte[] SHORT_LOCK_KEY = new byte[15];
  public static byte[] BASIC_LOCK_KEY = new byte[16];
  public static byte[] WRONG_LOCK_KEY = createWrongKey();

  private static byte[] createWrongKey() {
    byte[] wrongKey = new byte[16];
    wrongKey[0] = 1;
    return wrongKey;
  }

  public static byte[] LONG_LOCK_KEY = new byte[17];
  public static byte[] UNLOCKED_STATE = new byte[]{0};
  public static byte[] LOCKED_STATE = new byte[]{1};
  public static byte[] BASIC_GENERAL_DATA = new byte[]{1};
  public static byte[] BASIC_GENERAL_DATA_2 = new byte[]{2};
  public static byte[][] MULTIPLE_GENERAL_DATA = new byte[][]{BASIC_GENERAL_DATA_2, BASIC_GENERAL_DATA};
  public static byte[] LONG_FLAGS = new byte[2];
  public static byte[] LONG_URI = new byte[19];
  public static byte[] SHORT_TX_POWER_LEVELS = new byte[3];
  public static byte[] BASIC_TX_POWER_LEVELS = new byte[4];
  public static byte[] BASIC_TX_POWER_LEVELS_2 = new byte[]{1, 1, 1, 1};
  public static byte[][] MULTIPLE_TX_POWER_LEVELS = new byte[][]{BASIC_TX_POWER_LEVELS_2, BASIC_TX_POWER_LEVELS};
  public static byte[] LONG_TX_POWER_LEVELS = new byte[5];
  public static byte[] SHORT_POWER_MODE = new byte[0];
  public static byte[] LONG_POWER_MODE = new byte[2];
  public static byte[] SHORT_PERIOD = new byte[1];
  // period = 999
  public static byte[] BASIC_PERIOD = new byte[]{(byte) 0xE7, 0x03};
  // period = 1001
  public static byte[] BASIC_PERIOD_2 = new byte[]{(byte) 0xE9, 0x03};
  public static byte[][] MULTIPLE_BASIC_PERIOD = new byte[][]{BASIC_PERIOD_2, BASIC_PERIOD};
  public static byte[] LONG_PERIOD = new byte[3];
  public static byte[] SHORT_RESET = new byte[0];
  public static byte[] LONG_RESET = new byte[2];
}
