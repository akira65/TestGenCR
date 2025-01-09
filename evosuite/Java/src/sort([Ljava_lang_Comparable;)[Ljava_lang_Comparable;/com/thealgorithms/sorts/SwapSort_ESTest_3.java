/*
 * This file was automatically generated by EvoSuite
 * Tue Jan 07 07:30:14 GMT 2025
 */

package com.thealgorithms.sorts;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import com.thealgorithms.sorts.SwapSort;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, resetStaticState = true) 
public class SwapSort_ESTest_3 extends SwapSort_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      Integer[] integerArray0 = new Integer[3];
      Integer integer0 = new Integer(0);
      integerArray0[0] = integer0;
      Integer integer1 = new Integer((-1951));
      integerArray0[1] = integer1;
      integerArray0[2] = integerArray0[1];
      SwapSort swapSort0 = new SwapSort();
      Integer[] integerArray1 = swapSort0.sort(integerArray0);
      assertEquals(3, integerArray1.length);
  }
}
