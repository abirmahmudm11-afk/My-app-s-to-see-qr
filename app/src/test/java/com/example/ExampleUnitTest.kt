package com.example

import com.example.domain.BarcodeValidator
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun gs1CheckDigitSamplePasses() {
    assertTrue(BarcodeValidator.verifyGs1CheckDigit("5449000000996"))
  }
}
