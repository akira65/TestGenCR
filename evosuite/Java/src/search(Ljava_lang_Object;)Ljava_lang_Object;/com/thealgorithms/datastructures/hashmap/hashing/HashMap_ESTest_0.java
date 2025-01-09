/*
 * This file was automatically generated by EvoSuite
 * Tue Jan 07 10:24:31 GMT 2025
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
public class HashMap_ESTest_0 extends HashMap_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      Integer integer0 = new Integer((-64));
      assertEquals((-64), (int)integer0);
      assertNotNull(integer0);
      
      HashMap<Integer, Integer> hashMap0 = new HashMap<Integer, Integer>(1584);
      assertNotNull(hashMap0);
      assertEquals(0, hashMap0.size());
      
      Integer integer1 = hashMap0.search(integer0);
      assertNull(integer1);
      assertEquals(0, hashMap0.size());
  }
}
