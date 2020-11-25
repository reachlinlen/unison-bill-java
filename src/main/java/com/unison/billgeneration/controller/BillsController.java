package com.unison.billgeneration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unison.billgeneration.model.Invoice;
import com.unison.billgeneration.service.BillsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@RestController
public class BillsController {

    @Autowired
    private BillsService billsService;

    @GetMapping("/bills")
    public List getBills() {
        return billsService.getBills();
    }

    @PostMapping("/billstatus")
    public void updateBillStatus(@RequestBody ObjectNode node) {
        Iterator<JsonNode> recInvoices = node.get("invoices").elements();
        billsService.updateBillStatus(recInvoices);
    }
}
