package com.unison.billgeneration.service;

import com.unison.billgeneration.model.ClientInvoice;
import com.unison.billgeneration.repository.ClientInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ClientInvoiceService {

    private final int invoiceStartNum;

    @Autowired
    public ClientInvoiceService(@Value("${invoice.start.number}") int invoiceStartNum) {
        this.invoiceStartNum = invoiceStartNum;
    }

    @Autowired
    private ClientInvoiceRepository clientInvoiceRepository;

    public int getLatestInvNum(String client) {
        Optional<ClientInvoice> clientInfo = clientInvoiceRepository.findById(client);
        if (clientInfo.isPresent()) {
            return clientInfo.get().getLatest_invoice_number();
        } else {
            return invoiceStartNum;
        }
    }

    public void incrementInvNum(String client, int count) {
        ClientInvoice clientInvoice = new ClientInvoice();
        clientInvoice.setClient_name(client);
        clientInvoice.setLatest_invoice_number(count);
        clientInvoiceRepository.save(clientInvoice);
    }
}
