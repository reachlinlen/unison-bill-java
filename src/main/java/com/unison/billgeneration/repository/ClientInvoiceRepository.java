package com.unison.billgeneration.repository;

import com.unison.billgeneration.model.ClientInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientInvoiceRepository extends JpaRepository<ClientInvoice, String> {
}
