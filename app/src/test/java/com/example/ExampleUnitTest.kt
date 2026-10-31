package com.example

import com.example.ui.components.formatRupiah
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFinancialFormulas() {
    // Formula Penjualan, Modal, dan Laba Bersih
    val totalPenjualan = 5000000.0
    val modalBarangTerjual = 3000000.0
    val biayaOperasional = 500000.0

    val labaKotor = totalPenjualan - modalBarangTerjual
    assertEquals(2000000.0, labaKotor, 0.001)

    val labaBersih = labaKotor - biayaOperasional
    assertEquals(1500000.0, labaBersih, 0.001)
  }

  @Test
  fun testProductionCostCalculation() {
    // Usaha Produksi: 2kg tepung (20k) + 20 butir telur (30k) + 1kg gula (15k) = 65k total
    // Menghasilkan 13 kotak kue
    val totalBahan = 65000.0
    val outputQty = 13.0
    val modalPerUnit = totalBahan / outputQty
    assertEquals(5000.0, modalPerUnit, 0.001)
  }

  @Test
  fun testRupiahFormat() {
    val formatted = formatRupiah(10000.0)
    assert(formatted.contains("10.000"))
  }
}
