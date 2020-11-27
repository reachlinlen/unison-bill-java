package com.unison.billgeneration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "client_invoice")
public class ClientInvoice {

    @Id
    private String client_name;
    private int latest_invoice_number;

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public int getLatest_invoice_number() {
        return latest_invoice_number;
    }

    public void setLatest_invoice_number(int latest_invoice_number) {
        this.latest_invoice_number = latest_invoice_number;
    }
}
