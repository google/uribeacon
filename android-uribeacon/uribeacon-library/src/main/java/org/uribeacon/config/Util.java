package org.uribeacon.config;

public class Util {

  /**
   * Convert a byte array into a printable form.
   *
   * @param a the byte array to convert to a string.
   * @return the string format of byte array
   */
  public static String byteArrayToHexString(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    for(byte b: a)
      sb.append(String.format("%02x", b & 0xff));
    return sb.toString();
  }

  /**
   * Concatenates two byte arrays.
   *
   * @param a the first array.
   * @param b the second array.
   * @return the concatenated array.
   */
  public static byte[] concatenate(byte[] a, byte[] b) {
    byte[] result = new byte[a.length + b.length];
    System.arraycopy(a, 0, result, 0, a.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }
}
