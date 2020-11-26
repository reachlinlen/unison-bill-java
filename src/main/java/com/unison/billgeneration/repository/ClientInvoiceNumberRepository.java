package com.unison.billgeneration.repository;

import com.unison.billgeneration.model.ClientInvoiceNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientInvoiceNumberRepository extends JpaRepository<ClientInvoiceNumber, String> {
}
