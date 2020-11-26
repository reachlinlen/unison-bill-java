package com.unison.billgeneration.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ClientInvoiceNumber {

    @Id
    private String client;
    private int latestInvoiceNumber;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public int getLatestInvoiceNumber() {
        return latestInvoiceNumber;
    }

    public void setLatestInvoiceNumber(int latestInvoiceNumber) {
        this.latestInvoiceNumber = latestInvoiceNumber;
    }
}
