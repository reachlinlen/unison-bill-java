package com.unison.billgeneration.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
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
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.*;
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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Service
public class EmpService {

    public ResponseEntity<Resource> generateBill(String attention, String PONum, String projName, String costCentre, String account, String clients, MultipartFile file) throws FileNotFoundException, IOException {
        Workbook wb = new XSSFWorkbook(file.getInputStream());
        for (Sheet sheet: wb) {
            for (int i=1;i <= sheet.getLastRowNum();i++) {
                String dest = "C:\\Users\\unison\\Documents\\unison-" + i + ".pdf";
                PdfWriter pdfWriter = new PdfWriter(dest);
                Document document = new Document(new PdfDocument(pdfWriter));
                addStaticContent(document);
                addBillTo(document, sheet.getRow(i));
                billingInfo(document, attention, PONum, costCentre, account, projName);
                addTable(document, sheet.getRow(i));
                addStaticPayment(document);
                document.close();
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
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

    private void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph("\n"));
        }
    }

    private void addStaticContent(Document document) throws IOException {
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Color linkColor = new DeviceRgb(66, 135, 245);
        // Title
        Text titleText = new Text("Tax Invoice").setFontSize(18);
        Paragraph title = new Paragraph().add(titleText).setFont(bold)
                                        .add("\n").add("\n")
                                        .setTextAlignment(TextAlignment.CENTER);
        title.add("");
        document.add(title);
        // tbl with address in the left and logo on the right
        Table companyTbl = new Table(UnitValue.createPercentArray(3));
        Text unisonText = new Text("Unison Consulting Pte Ltd").setFont(bold).setFontSize(12);
        Paragraph unison = new Paragraph().add(unisonText);
        Text webAddressText = new Text("www.unisonconsulting.com.sg");
        Paragraph webAddress = new Paragraph().add(webAddressText).setUnderline()
                                            .setFontColor(linkColor)
                                            .setFontSize(10)
                                            .add("\n")
                                            .add("\n");
        // Add address
        Cell leftCell = new Cell(1,2)
                            .add(unison)
                            .add(new Paragraph("1 Changi Business Park Crescent"))
                            .add(new Paragraph("Plaza 8 Podium A ,#03-06,"))
                            .add(new Paragraph("Singapore 486025"))
                            .add(new Paragraph("Tel: (65) 6639 6594"))
                            .setFontSize(10)
                            .add(webAddress)
                            .setBorder(Border.NO_BORDER);
        companyTbl.addCell(leftCell);
        // Add logo
        ImageData logoData = ImageDataFactory.create("src/main/resources/images/logo.png");
        Image logoImage = new Image(logoData).setAutoScale(true);
        Cell rightCell = new Cell(1,1)
                            .add(logoImage)
                            .setBorder(Border.NO_BORDER);
        companyTbl.addCell(rightCell);
        document.add(companyTbl);
    }

    public void addStaticPayment(Document document) {
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("\n"));
        Table table = new Table(UnitValue.createPercentArray(1));
        Cell cell = new Cell(1, 1)
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

    private void addBillTo(Document document, Row row) throws IOException {
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
        Table table = new Table(UnitValue.createPercentArray(16));
        Cell cell1 = new Cell(1, 12)
                        .add(new Paragraph(billing)).add(new Paragraph("\n"))
                        .setBorder(Border.NO_BORDER).setFontSize(10);
        table.addCell(cell1);
        Cell cell2 = new Cell(1,3).add(new Paragraph("Date :"))
                                                    .add(new Paragraph("Due Date :"))
                                                    .add(new Paragraph("Invoice Number :"))
                                                    .setTextAlignment(TextAlignment.RIGHT)
                                                    .setBorder(Border.NO_BORDER).setFontSize(10)
                                                    .setVerticalAlignment(VerticalAlignment.TOP);
        table.addCell(cell2);
        LocalDate date = row.getCell(5).getLocalDateTimeCellValue().toLocalDate();
        String invoiceNum = Integer.toString((int)row.getCell(6).getNumericCellValue());
        Cell cell3 = new Cell(1,1).add(new Paragraph(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .add(new Paragraph(date.plusDays(45).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .add(new Paragraph(invoiceNum))
                .setTextAlignment(TextAlignment.LEFT)
                .setBorder(Border.NO_BORDER).setFontSize(10)
                .setVerticalAlignment(VerticalAlignment.TOP);
        table.addCell(cell3);
        document.add(table);
    }

    private void billingInfo(Document document, String attention, String PONum, String costCentre, String account, String projName) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Text costCentreText = new Text("Cost Centre:").setFontSize(11).setFont(bold);
        Paragraph costCentrePara = new Paragraph().add(costCentreText)
                                                    .add(" " + costCentre).setFontSize(10)
                                                    .add("\n");
        Text accountText = new Text("Account      :").setFontSize(11).setFont(bold);
        costCentrePara.add(accountText).add(" " + account).setFontSize(10);
        document.add(costCentrePara);
        String attn = "Attn         : " + attention + "\n" + "PO No     : " + PONum + "\n" + "\n";
        if (!projName.isEmpty()) {
            attn = "Attn         : " + attention + "\n" + "PO No    : " + PONum + "\n" + "Project Name: " + projName + "\n" + "\n";
        }
        Paragraph attnPara = new Paragraph().add(attn).setFontSize(10);
        document.add(attnPara);
    }

    private void addTable(Document document, Row row) {
        DecimalFormat df = new DecimalFormat("#.00");
        Table table = new Table(UnitValue.createPercentArray(10));
        Cell cell0 = new Cell(1, 1)
                .add(new Paragraph("PO Line - "))
                .add(new Paragraph("Schedule"))
                .setFontSize(10)
                .setBackgroundColor(DeviceGray.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setHeight(30);
        table.addHeaderCell(cell0);
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
        //ROW 2
        Cell rowCell0 = new Cell(2, 1)
                .add(new Paragraph(row.getCell(0).getStringCellValue()))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(rowCell0);
        Cell rowCell1 = new Cell(2, 5)
                .add(new Paragraph(row.getCell(1).getStringCellValue()).add("\n").add("\n"))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT);
        table.addCell(rowCell1);
        String quantity = Integer.toString((int)row.getCell(2).getNumericCellValue());
        Cell rowCell2 = new Cell(2, 1)
                .add(new Paragraph(quantity))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        table.addCell(rowCell2);
        Double unitPrice = row.getCell(3).getNumericCellValue() * (int)row.getCell(4).getNumericCellValue();
        Cell rowCell3 = new Cell(2, 2)
                .add(new Paragraph(df.format(unitPrice)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(rowCell3);
        Cell rowCell4 = new Cell(2, 2)
                .add(new Paragraph(df.format(unitPrice)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(rowCell4);
        // ROW 3
        table.addCell(new Cell(3,1).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(3,5).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(3,1).setBorder(Border.NO_BORDER));
        Cell gstCell1 = new Cell(3, 2)
                .add(new Paragraph("GST (7%)"))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(gstCell1);
        Double gstAmt = unitPrice*0.07;
        Cell gstCell2 = new Cell(3, 2)
                .add(new Paragraph(df.format(gstAmt)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(gstCell2);
        // ROW 4
        table.addCell(new Cell(4,1).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(4,5).setBorder(Border.NO_BORDER));
        table.addCell(new Cell(4,1).setBorder(Border.NO_BORDER));
        Cell totalCell1 = new Cell(4, 2)
                .add(new Paragraph("Total"))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(totalCell1);
        Double tot = unitPrice*1.07;
        Cell totalCell2 = new Cell(4, 2)
                .add(new Paragraph(df.format(tot)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(totalCell2);
        document.add(table);
    }
}
