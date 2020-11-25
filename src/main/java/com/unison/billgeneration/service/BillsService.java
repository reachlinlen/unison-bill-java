package com.unison.billgeneration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.unison.billgeneration.repository.BillsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;

@Service
public class BillsService {

    @Autowired
    private BillsRepository billsRepository;

    public List getBills() {
        return billsRepository.findAll();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRES_NEW)
    public void updateBillStatus(Iterator<JsonNode> recInvoices) {
        while (recInvoices.hasNext()) {
            JsonNode invoiceNode = recInvoices.next();
            String invoiceNum = invoiceNode.get("invoice_num").asText();
            String status = invoiceNode.get("invoice_status").asText();
            billsRepository.updateBillStatus(invoiceNum, status);
        }
    }
}
