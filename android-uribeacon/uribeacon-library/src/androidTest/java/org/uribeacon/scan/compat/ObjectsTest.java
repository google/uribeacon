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

package org.uribeacon.scan.compat;

import junit.framework.TestCase;

import org.uribeacon.scan.compat.Objects;

import java.util.Arrays;

/**
 * Tests for our lightweight 'Objects' utility class.  This class exists so that
 * less advanced projects, which don't yet support JavaSE 7, can use the more
 * advanced classes imported from the Android 'L' release, which does.
 */
public class ObjectsTest extends TestCase {

  public void testEquals() {
    assertTrue(Objects.equals(null, null));
    assertTrue(Objects.equals("a", "a"));
    assertFalse(Objects.equals("a", "b"));
    assertFalse(Objects.equals("a", null));
    assertFalse(Objects.equals(null, "b"));
  }
  
  public void testDeepEquals() {
    assertTrue(Objects.deepEquals(null, null));
    assertTrue(Objects.deepEquals(new byte[]{ 1, 2, 3 }, new byte[]{ 1, 2, 3 }));
    assertFalse(Objects.deepEquals(new byte[]{ 1, 2, 3 }, null));
    assertFalse(Objects.deepEquals(null, new byte[]{ 1, 2, 3 }));
  }
  
  public void testHash() {
    assertEquals(Arrays.hashCode(new String[] { "a", "b", "c" }), Objects.hash("a", "b", "c"));
  }
}
