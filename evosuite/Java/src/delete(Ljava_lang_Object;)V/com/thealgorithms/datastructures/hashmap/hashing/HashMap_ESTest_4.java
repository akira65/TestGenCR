/*
 * This file was automatically generated by EvoSuite
 * Tue Jan 07 10:24:05 GMT 2025
 */

package com.thealgorithms.datastructures.hashmap.hashing;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import com.thealgorithms.datastructures.hashmap.hashing.HashMap;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, resetStaticState = true) 
public class HashMap_ESTest_4 extends HashMap_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      HashMap.LinkedList<Integer, Integer> hashMap_LinkedList0 = new HashMap.LinkedList<Integer, Integer>();
      assertTrue(hashMap_LinkedList0.isEmpty());
      assertNotNull(hashMap_LinkedList0);
      
      Integer integer0 = new Integer((-1));
      assertNotNull(integer0);
      assertEquals((-1), (int)integer0);
      
      hashMap_LinkedList0.delete(integer0);
      assertTrue(hashMap_LinkedList0.isEmpty());
  }
}