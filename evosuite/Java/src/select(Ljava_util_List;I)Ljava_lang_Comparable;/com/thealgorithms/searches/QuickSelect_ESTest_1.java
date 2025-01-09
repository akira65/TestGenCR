/*
 * This file was automatically generated by EvoSuite
 * Tue Jan 07 07:31:28 GMT 2025
 */

package com.thealgorithms.searches;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import com.thealgorithms.searches.QuickSelect;
import java.util.LinkedList;
import java.util.List;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, resetStaticState = true) 
public class QuickSelect_ESTest_1 extends QuickSelect_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      LinkedList<Integer> linkedList0 = new LinkedList<Integer>();
      // Undeclared exception!
      try { 
        QuickSelect.select((List<Integer>) linkedList0, (-1));
        fail("Expecting exception: IllegalArgumentException");
      
      } catch(IllegalArgumentException e) {
         //
         // The list of elements must not be empty.
         //
         verifyException("com.thealgorithms.searches.QuickSelect", e);
      }
  }
}
