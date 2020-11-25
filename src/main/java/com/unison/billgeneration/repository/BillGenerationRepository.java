package com.unison.billgeneration.repository;

import com.unison.billgeneration.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BillGenerationRepository extends JpaRepository<Invoice, String> {

    @Query(nativeQuery = true, value = "SELECT INVOICE_NUM FROM INVOICE WHERE CLIENT_NAME=?1 AND " +
            "INVOICE_DATETIME = ( SELECT MAX(INVOICE_DATETIME) FROM INVOICE)")
    public String getClientInvoice(String clientName);
}
