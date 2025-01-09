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
public class SwapSort_ESTest_1 extends SwapSort_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      SwapSort swapSort0 = new SwapSort();
      Integer[] integerArray0 = new Integer[9];
      Integer integer0 = new Integer((-216));
      integerArray0[0] = integer0;
      Integer integer1 = new Integer(0);
      integerArray0[1] = integer1;
      integerArray0[2] = integer0;
      integerArray0[3] = integer1;
      integerArray0[4] = integer1;
      integerArray0[5] = integerArray0[0];
      integerArray0[6] = integer0;
      integerArray0[7] = integerArray0[5];
      integerArray0[8] = integerArray0[2];
      // Undeclared exception!
      swapSort0.sort(integerArray0);
  }
}