package com.example

import com.example.data.local.entity.ChangeRecordEntity
import com.example.data.local.entity.CustomerEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.CustomerWithStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomerTest {

    @Test
    fun `test customer entity creation and defaults`() {
        val customer = CustomerEntity(
            id = 1L,
            name = "Budi Santoso",
            phone = "08123456789",
            notes = "Pelanggan setia"
        )

        assertEquals(1L, customer.id)
        assertEquals("Budi Santoso", customer.name)
        assertEquals("08123456789", customer.phone)
        assertEquals("Pelanggan setia", customer.notes)
        assertTrue(customer.createdAt > 0L)
        assertTrue(customer.updatedAt >= customer.createdAt)
    }

    @Test
    fun `test CustomerWithStats computation logic`() {
        val customer1 = CustomerEntity(id = 1L, name = "Ahmad")
        val customer2 = CustomerEntity(id = 2L, name = "Budi")

        // Transactions
        val transactions = listOf(
            TransactionEntity(id = 101L, transactionNumber = "TX01", subtotal = 50000L, total = 50000L, cashReceived = 30000L, customerId = 1L, status = "COMPLETED"),
            TransactionEntity(id = 102L, transactionNumber = "TX02", subtotal = 25000L, total = 25000L, cashReceived = 25000L, customerId = 1L, status = "COMPLETED"),
            TransactionEntity(id = 103L, transactionNumber = "TX03", subtotal = 100000L, total = 100000L, cashReceived = 100000L, customerId = 1L, status = "CANCELLED"), // should be ignored
            TransactionEntity(id = 104L, transactionNumber = "TX04", subtotal = 75000L, total = 75000L, cashReceived = 75000L, customerId = 2L, status = "COMPLETED"),
            TransactionEntity(id = 105L, transactionNumber = "TX05", subtotal = 40000L, total = 40000L, cashReceived = 40000L, customerId = null, customerName = "Ahmad", status = "COMPLETED") // manual, no customerId
        )

        // Debts
        val debts = listOf(
            DebtEntity(id = 201L, transactionId = 101L, customerName = "Ahmad", amount = 50000L, remainingAmount = 20000L, customerId = 1L, status = "UNPAID"),
            DebtEntity(id = 202L, transactionId = 999L, customerName = "Ahmad", amount = 10000L, remainingAmount = 0L, customerId = 1L, status = "PAID"), // paid debt
            DebtEntity(id = 203L, transactionId = 104L, customerName = "Budi", amount = 30000L, remainingAmount = 30000L, customerId = 2L, status = "UNPAID")
        )

        // Change Records
        val changeRecords = listOf(
            ChangeRecordEntity(id = 301L, transactionId = 102L, buyerName = "Ahmad", amount = 5000L, customerId = 1L, status = "PENDING"),
            ChangeRecordEntity(id = 302L, transactionId = 998L, buyerName = "Ahmad", amount = 2000L, customerId = 1L, status = "PAID"), // already given
            ChangeRecordEntity(id = 303L, transactionId = 104L, buyerName = "Budi", amount = 10000L, customerId = 2L, status = "PENDING")
        )

        // Compute stats for Ahmad (customerId = 1)
        val ahmadTxCount = transactions.count { it.customerId == customer1.id && it.status != "CANCELLED" }
        val ahmadUnpaid = debts.filter { it.customerId == customer1.id && it.status != "PAID" }.sumOf { it.remainingAmount }
        val ahmadPendingChange = changeRecords.filter { it.customerId == customer1.id && it.status == "PENDING" }.sumOf { it.amount }

        val ahmadCompletedTxIds = transactions.filter { it.customerId == customer1.id && it.status == "COMPLETED" }.map { it.id }.toSet()
        val ahmadDirectPaid = transactions.filter { it.customerId == customer1.id && it.status == "COMPLETED" }.sumOf { it.total }
        val ahmadDebtPaid = debts.filter { it.customerId == customer1.id && !ahmadCompletedTxIds.contains(it.transactionId) }.sumOf { (it.amount - it.remainingAmount).coerceAtLeast(0L) }
        val ahmadTotalPaid = ahmadDirectPaid + ahmadDebtPaid

        val ahmadStats = CustomerWithStats(
            customer = customer1,
            totalPurchases = ahmadTxCount,
            totalUnpaid = ahmadUnpaid,
            totalPendingChange = ahmadPendingChange,
            totalPaid = ahmadTotalPaid
        )

        assertEquals("Ahmad should have 2 valid purchases (ignoring cancelled & unlinked manual)", 2, ahmadStats.totalPurchases)
        assertEquals("Ahmad should have 20000 unpaid debt", 20000L, ahmadStats.totalUnpaid)
        assertEquals("Ahmad should have 5000 pending change", 5000L, ahmadStats.totalPendingChange)
        assertEquals("Ahmad should have 85000 total paid", 85000L, ahmadStats.totalPaid)

        // Compute stats for Budi (customerId = 2)
        val budiTxCount = transactions.count { it.customerId == customer2.id && it.status != "CANCELLED" }
        val budiUnpaid = debts.filter { it.customerId == customer2.id && it.status != "PAID" }.sumOf { it.remainingAmount }
        val budiPendingChange = changeRecords.filter { it.customerId == customer2.id && it.status == "PENDING" }.sumOf { it.amount }

        val budiCompletedTxIds = transactions.filter { it.customerId == customer2.id && it.status == "COMPLETED" }.map { it.id }.toSet()
        val budiDirectPaid = transactions.filter { it.customerId == customer2.id && it.status == "COMPLETED" }.sumOf { it.total }
        val budiDebtPaid = debts.filter { it.customerId == customer2.id && !budiCompletedTxIds.contains(it.transactionId) }.sumOf { (it.amount - it.remainingAmount).coerceAtLeast(0L) }
        val budiTotalPaid = budiDirectPaid + budiDebtPaid

        val budiStats = CustomerWithStats(
            customer = customer2,
            totalPurchases = budiTxCount,
            totalUnpaid = budiUnpaid,
            totalPendingChange = budiPendingChange,
            totalPaid = budiTotalPaid
        )

        assertEquals(1, budiStats.totalPurchases)
        assertEquals(30000L, budiStats.totalUnpaid)
        assertEquals(10000L, budiStats.totalPendingChange)
        assertEquals(75000L, budiStats.totalPaid)
    }

    @Test
    fun `test CustomerWithStats totalPaid calculation with partial debt payments`() {
        val customer = CustomerEntity(id = 10L, name = "Dewi")

        val txList = listOf(
            TransactionEntity(id = 1L, transactionNumber = "TX-01", subtotal = 100000L, total = 100000L, cashReceived = 100000L, customerId = 10L, status = "COMPLETED"),
            TransactionEntity(id = 2L, transactionNumber = "TX-02", subtotal = 50000L, total = 50000L, cashReceived = 0L, customerId = 10L, status = "UNPAID"),
            TransactionEntity(id = 3L, transactionNumber = "TX-03", subtotal = 200000L, total = 200000L, cashReceived = 0L, customerId = 10L, status = "CANCELLED")
        )

        val debtList = listOf(
            // Sisa 20k dari total 50k -> berarti sudah bayar 30k
            DebtEntity(id = 5L, transactionId = 2L, customerName = "Dewi", customerId = 10L, amount = 50000L, remainingAmount = 20000L, status = "PARTIALLY_PAID")
        )

        val completedTxIds = txList.filter { it.customerId == customer.id && it.status == "COMPLETED" }.map { it.id }.toSet()
        val directPaid = txList.filter { it.customerId == customer.id && it.status == "COMPLETED" }.sumOf { it.total }
        val debtPaid = debtList.filter { it.customerId == customer.id && !completedTxIds.contains(it.transactionId) }.sumOf { (it.amount - it.remainingAmount).coerceAtLeast(0L) }
        val totalPaid = directPaid + debtPaid

        val stats = CustomerWithStats(
            customer = customer,
            totalPurchases = txList.count { it.customerId == customer.id && it.status != "CANCELLED" },
            totalUnpaid = debtList.filter { it.customerId == customer.id && it.status != "PAID" }.sumOf { it.remainingAmount },
            totalPaid = totalPaid
        )

        assertEquals("Should have 2 valid purchases", 2, stats.totalPurchases)
        assertEquals("Should have 20000 unpaid debt", 20000L, stats.totalUnpaid)
        assertEquals("Total money received should be 100k direct + 30k partial debt = 130k", 130000L, stats.totalPaid)
    }

    @Test
    fun `test settling debt updates unpaid debt to zero`() {
        val customer = CustomerEntity(id = 1L, name = "Siti")
        var debt = DebtEntity(id = 201L, transactionId = 101L, customerName = "Siti", amount = 50000L, remainingAmount = 50000L, customerId = 1L, status = "UNPAID")

        var debts = listOf(debt)
        var unpaid = debts.filter { it.customerId == customer.id && it.status != "PAID" }.sumOf { it.remainingAmount }
        assertEquals(50000L, unpaid)

        // Settle debt
        debt = debt.copy(remainingAmount = 0L, status = "PAID")
        debts = listOf(debt)
        unpaid = debts.filter { it.customerId == customer.id && it.status != "PAID" }.sumOf { it.remainingAmount }
        assertEquals(0L, unpaid)
    }

    @Test
    fun `test marking pending change as paid updates pending change to zero`() {
        val customer = CustomerEntity(id = 1L, name = "Budi")
        var changeRecord = ChangeRecordEntity(id = 301L, transactionId = 101L, buyerName = "Budi", amount = 15000L, customerId = 1L, status = "PENDING")

        var changeRecords = listOf(changeRecord)
        var pendingChange = changeRecords.filter { it.customerId == customer.id && it.status == "PENDING" }.sumOf { it.amount }
        assertEquals(15000L, pendingChange)

        // Mark as paid
        changeRecord = changeRecord.copy(status = "PAID")
        changeRecords = listOf(changeRecord)
        pendingChange = changeRecords.filter { it.customerId == customer.id && it.status == "PENDING" }.sumOf { it.amount }
        assertEquals(0L, pendingChange)
    }

    @Test
    fun `test customer filter and search logic`() {
        val list = listOf(
            CustomerWithStats(CustomerEntity(id = 1L, name = "Charlie"), totalPurchases = 3, totalUnpaid = 20000L, totalPendingChange = 0L),
            CustomerWithStats(CustomerEntity(id = 2L, name = "Alice"), totalPurchases = 1, totalUnpaid = 0L, totalPendingChange = 5000L),
            CustomerWithStats(CustomerEntity(id = 3L, name = "Bob"), totalPurchases = 5, totalUnpaid = 10000L, totalPendingChange = 2000L)
        )

        // Filter: Ada Kasbon (totalUnpaid > 0)
        val withDebts = list.filter { it.totalUnpaid > 0 }
        assertEquals(2, withDebts.size)
        assertTrue(withDebts.any { it.customer.name == "Charlie" })
        assertTrue(withDebts.any { it.customer.name == "Bob" })

        // Filter: Ada Kembalian (totalPendingChange > 0)
        val withChange = list.filter { it.totalPendingChange > 0 }
        assertEquals(2, withChange.size)
        assertTrue(withChange.any { it.customer.name == "Alice" })
        assertTrue(withChange.any { it.customer.name == "Bob" })

        // Sort: Nama A-Z
        val sorted = list.sortedBy { it.customer.name.lowercase() }
        assertEquals("Alice", sorted[0].customer.name)
        assertEquals("Bob", sorted[1].customer.name)
        assertEquals("Charlie", sorted[2].customer.name)

        // Search: "li" matches Charlie and Alice
        val searchResults = list.filter { it.customer.name.contains("li", ignoreCase = true) }
        assertEquals(2, searchResults.size)
        assertTrue(searchResults.any { it.customer.name == "Charlie" })
        assertTrue(searchResults.any { it.customer.name == "Alice" })
    }

    @Test
    fun `test backward compatibility when transaction has no customerId`() {
        val legacyTx = TransactionEntity(
            id = 50L,
            transactionNumber = "TX-LEGACY-001",
            subtotal = 20000L,
            total = 20000L,
            cashReceived = 20000L,
            customerId = null,
            customerName = "Pak Joko",
            status = "COMPLETED"
        )

        assertEquals(null, legacyTx.customerId)
        assertEquals("Pak Joko", legacyTx.customerName)
    }
}
