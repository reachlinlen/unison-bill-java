package com.unison.billgeneration.repository;

import com.unison.billgeneration.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BillsRepository extends JpaRepository<Invoice, String> {

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE INVOICE SET INVOICE_STATUS=?2 WHERE INVOICE_NUM=?1")
    public void updateBillStatus(String invoiceNum, String invoiceStatus);

}
