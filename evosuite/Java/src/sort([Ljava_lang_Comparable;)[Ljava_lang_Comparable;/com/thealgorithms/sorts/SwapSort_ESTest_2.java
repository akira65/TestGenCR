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
public class SwapSort_ESTest_2 extends SwapSort_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      SwapSort swapSort0 = new SwapSort();
      // Undeclared exception!
      try { 
        swapSort0.sort((Integer[]) null);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("com.thealgorithms.sorts.SwapSort", e);
      }
  }
}
