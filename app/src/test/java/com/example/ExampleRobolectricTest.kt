package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.MemoryPressure
import com.example.model.RamInfo
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tab Cleaner", appName)
  }

  @Test
  fun `format bytes correctly in megabytes and gigabytes`() {
    val bytes100MB = 100L * 1024L * 1024L
    val formatted = FormatUtils.formatBytes(bytes100MB)
    assertTrue(formatted.contains("100") || formatted.contains("ميجابايت"))

    val bytes4GB = 4L * 1024L * 1024L * 1024L
    val formattedGB = FormatUtils.formatGigabytesOnly(bytes4GB)
    assertTrue(formattedGB.contains("4.00") || formattedGB.contains("جيجابايت"))
  }

  @Test
  fun `ram info model values`() {
    val info = RamInfo(
      totalBytes = 4294967296L,
      availableBytes = 2147483648L,
      usedBytes = 2147483648L,
      usagePercentage = 50,
      pressureLevel = MemoryPressure.OPTIMAL
    )
    assertEquals(50, info.usagePercentage)
    assertEquals(MemoryPressure.OPTIMAL, info.pressureLevel)
  }
}
