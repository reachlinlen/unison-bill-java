package com.unison.billgeneration.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoice")
public class Invoice {

    @Id
    private String invoice_num;
    private String invoice_amt;
    private String invoice_gst;
    private String invoice_total;
    private LocalDate invoice_date;
    private LocalDate invoice_duedate;
    private LocalDateTime invoice_datetime;
    private String client_name;
    private String cost_centre;
    private String resource;
    private String invoice_status;

    public String getInvoice_num() {
        return invoice_num;
    }

    public void setInvoice_num(String invoice_num) {
        this.invoice_num = invoice_num;
    }

    public String getInvoice_amt() {
        return invoice_amt;
    }

    public void setInvoice_amt(String invoice_amt) {
        this.invoice_amt = invoice_amt;
    }

    public String getInvoice_gst() {
        return invoice_gst;
    }

    public void setInvoice_gst(String invoice_gst) {
        this.invoice_gst = invoice_gst;
    }

    public String getInvoice_total() {
        return invoice_total;
    }

    public void setInvoice_total(String invoice_total) {
        this.invoice_total = invoice_total;
    }

    public LocalDate getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(LocalDate invoice_date) {
        this.invoice_date = invoice_date;
    }

    public LocalDate getInvoice_duedate() {
        return invoice_duedate;
    }

    public void setInvoice_duedate(LocalDate invoice_duedate) {
        this.invoice_duedate = invoice_duedate;
    }

    public LocalDateTime getInvoice_datetime() {
        return invoice_datetime;
    }

    public void setInvoice_datetime(LocalDateTime invoice_datetime) {
        this.invoice_datetime = invoice_datetime;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getCost_centre() {
        return cost_centre;
    }

    public void setCost_centre(String cost_centre) {
        this.cost_centre = cost_centre;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getInvoice_status() {
        return invoice_status;
    }

    public void setInvoice_status(String invoice_status) {
        this.invoice_status = invoice_status;
    }
}
