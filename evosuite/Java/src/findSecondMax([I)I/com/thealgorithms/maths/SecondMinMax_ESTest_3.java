/*
 * This file was automatically generated by EvoSuite
 * Tue Jan 07 07:30:39 GMT 2025
 */

package com.thealgorithms.maths;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import com.thealgorithms.maths.SecondMinMax;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, resetStaticState = true) 
public class SecondMinMax_ESTest_3 extends SecondMinMax_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      int[] intArray0 = new int[10];
      intArray0[1] = (-1514);
      int int0 = SecondMinMax.findSecondMax(intArray0);
      assertEquals((-1514), int0);
  }
}
