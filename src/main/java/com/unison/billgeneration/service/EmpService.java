package com.unison.billgeneration.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TabAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.print.Doc;
import javax.swing.text.Element;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;

@Service
public class EmpService {

    public ResponseEntity<Resource> generateBill(String empName, LocalDate startDate, LocalDate endDate, String clientName, String fileName) throws FileNotFoundException, IOException {
        String dest = "C:\\Users\\unison\\Documents\\new.pdf";
        PdfWriter pdfWriter = new PdfWriter(dest);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(new PdfDocument(pdfWriter));
        addStaticContent(document);
        addBillTo(document);
        addHeader(document);
        addStaticPayment(document);
        document.close();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment: filename=\""+ "unison" + "\"")
                .contentLength(out.size())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
    }

//    public ResponseEntity<Resource> genBillFromExcel(MultipartFile file) throws IOException {
//        Workbook wb = new XSSFWorkbook(file.getInputStream());
//        Sheet sheet = wb.getSheetAt(0);
//        int i = 1;
//        for (i = 1;i < sheet.getLastRowNum();i++) {
//            Row row = sheet.getRow(i);
//            row.getCell(0);
//        }
//    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph("\n"));
        }
    }

    private static void addStaticContent(Document document) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Color linkColor = new DeviceRgb(66, 135, 245);
        Text titleText = new Text("Tax Invoice").setFontSize(18);
        Paragraph title = new Paragraph().add(titleText).setFont(bold).setTextAlignment(TextAlignment.CENTER);
        title.add("");
        document.add(title);
        Text unisonText = new Text("Unison Consulting Pte Ltd").setFont(bold).setFontSize(12);
        Paragraph unison = new Paragraph().add(unisonText);
        unison.add("");
        document.add(unison);
        Text addressText = new Text("1 Changi Business Park Crescent");
        //Address to the left
        Paragraph address = new Paragraph().add(addressText);
        address.add(new Tab());
        address.addTabStops(new TabStop(1000, TabAlignment.RIGHT));
        //Logo to the right
        address.add("Text to the right");
        address.add("\n")
                .add("Plaza 8 Podium A ,#03-06,").add("\n")
                .add("Singapore 486025").add("\n")
                .add("Tel: (65) 6639 6594")
                .setFontSize(10);
        document.add(address);
        Text webAddressText = new Text("www.unisonconsulting.com.sg");
        Paragraph webAddress = new Paragraph()
                                    .add(webAddressText).setUnderline()
                                    .setFontColor(linkColor)
                                    .setFontSize(9)
                                    .add("\n")
                                    .add("\n");
        document.add(webAddress);
    }

    private static void addHeader(Document document) {
        Table table = new Table(UnitValue.createPercentArray(10));
        Cell cell = new Cell(1, 1)
                        .add(new Paragraph("PO Line - "))
                        .add(new Paragraph("Schedule"))
                        .setFontSize(10)
                        .setBackgroundColor(DeviceGray.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setHeight(30);
        table.addHeaderCell(cell);
        Cell cell1 = new Cell(1, 5)
                .add(new Paragraph("Item Description"))
                .setFontSize(10)
                .setBackgroundColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell1);
        Cell cell2 = new Cell(1, 1)
                .add(new Paragraph("Quantity"))
                .setFontSize(10)
                .setBackgroundColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell2);
        Cell cell3 = new Cell(1, 2)
                .add(new Paragraph("Unit Price"))
                .setFontSize(10)
                .setBackgroundColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell3);
        Cell cell4 = new Cell(1, 2)
                .add(new Paragraph("Sub Total"))
                .setFontSize(10)
                .setBackgroundColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(cell4);
        document.add(table);
    }

    public static void addStaticPayment(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        Table table = new Table(UnitValue.createPercentArray(1));
        Cell cell = new Cell(1, 10)
                .add(new Paragraph("\n"))
                .add(new Paragraph("EFT Payment is preferred and should be made to        "))
                .add(new Paragraph("\n"))
                .add(new Paragraph("Name     : Unison Consulting Pte Ltd"))
                .add(new Paragraph("Bank      : Maybank, Singapore"))
                .add(new Paragraph("Account : 04131006981"))
                .setFontSize(10);
        table.addHeaderCell(cell);
        document.add(table);
    }

    private static void addBillTo(Document document) throws IOException {
        String billing = "Standard Chartered Bank," + "\n" +
                "Registration No.S16FC0027L" + "\n" +
                "GST Group Registration No. MR-8500053-0" + "\n" +
                "8 Marina Boulevard #27-01" + "\n" +
                "Marina Bay Financial Centre" + "\n" +
                "Singapore 018981";
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Text billToText = new Text("Bill To:").setFontSize(12);
        Paragraph billTo = new Paragraph().add(billToText).setFont(bold);
        document.add(billTo);
        Table table = new Table(UnitValue.createPercentArray(2));
        Cell cell1 = new Cell(1, 1)
                        .add(new Paragraph(billing)).add(new Paragraph("\n"))
                        .setBorder(Border.NO_BORDER).setFontSize(10);
        table.addCell(cell1);
        Cell cell2 = new Cell(1,1).add(new Paragraph("\n"))
                                                    .add(new Paragraph("Date :"))
                                                    .add(new Paragraph("Due Date :"))
                                                    .add(new Paragraph("Invoice Number :"))
                                                    .setTextAlignment(TextAlignment.RIGHT)
                                                    .setBorder(Border.NO_BORDER).setFontSize(10);
        table.addCell(cell2);
        document.add(table);
    }
}
