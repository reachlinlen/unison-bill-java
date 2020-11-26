package com.unison.billgeneration.service;

import com.unison.billgeneration.model.ClientInvoiceNumber;
import com.unison.billgeneration.repository.ClientInvoiceNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientInvoiceNumberService {

    private final int invoiceStartNum;

    @Autowired
    public ClientInvoiceNumberService(@Value("${invoice.start.number}") int invoiceStartNum) {
        this.invoiceStartNum = invoiceStartNum;
    }

    @Autowired
    private ClientInvoiceNumberRepository clientInvoiceNumberRepository;

    public int getLatestInvNum(String client) {
        Optional<ClientInvoiceNumber> clientInfo = clientInvoiceNumberRepository.findById(client);
        if (clientInfo.isPresent()) {
            return clientInfo.get().getLatestInvoiceNumber();
        } else {
            return invoiceStartNum;
        }
    }

    public void incrementInvNum(String client, int count) {
        ClientInvoiceNumber clientInvoiceNumber = new ClientInvoiceNumber();
        clientInvoiceNumber.setClient(client);
        clientInvoiceNumber.setLatestInvoiceNumber(count);
        clientInvoiceNumberRepository.save(clientInvoiceNumber);
    }
}
