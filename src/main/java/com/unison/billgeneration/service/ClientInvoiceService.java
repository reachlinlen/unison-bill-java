package com.unison.billgeneration.service;

import com.unison.billgeneration.model.ClientInvoice;
import com.unison.billgeneration.repository.ClientInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

@Service
public class ClientInvoiceService {

    private final String configFileLoc;
    private final String configFile;
    private final String defaultInvStartNum;
    private final String INV_START_NUM = "Invoice Start Number=";

    @Autowired
    public ClientInvoiceService(@Value("${config.file.location}") String configFileLoc,
                                @Value("${config.file}") String configFile,
                                @Value("${invoice.start.number}") String defaultInvStartNum) {
        this.configFileLoc = configFileLoc;
        this.configFile = configFile;
        this.defaultInvStartNum = defaultInvStartNum;
    }

    @Autowired
    private ClientInvoiceRepository clientInvoiceRepository;

    public int getLatestInvNum(String client) {
        Optional<ClientInvoice> clientInfo = clientInvoiceRepository.findById(client);
        if (clientInfo.isPresent()) {
            return clientInfo.get().getLatest_invoice_number();
        } else {
            String home = System.getProperty("user.home");
            BufferedReader reader;
            String invoiceStartNum = defaultInvStartNum;
            try {
                reader = new BufferedReader(new FileReader(home+configFileLoc+configFile));
                String line1 = reader.readLine();
                String line2 = reader.readLine();
                if (line1.toLowerCase().contains(INV_START_NUM.toLowerCase())) {
                    invoiceStartNum = line1.substring(line1.indexOf('=')+1).trim();
                } else if (line2.toLowerCase().contains(INV_START_NUM.toLowerCase())) {
                    invoiceStartNum = line2.substring(line2.indexOf('=')+1).trim();
                }
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            return Integer.parseInt(invoiceStartNum);
        }
    }

    public void incrementInvNum(String client, int count) {
        ClientInvoice clientInvoice = new ClientInvoice();
        clientInvoice.setClient_name(client);
        clientInvoice.setLatest_invoice_number(count);
        clientInvoiceRepository.save(clientInvoice);
    }
}
