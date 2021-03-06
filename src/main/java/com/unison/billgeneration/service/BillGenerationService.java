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
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.unison.billgeneration.model.Invoice;
import com.unison.billgeneration.repository.BillGenerationRepository;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class BillGenerationService {

    private final String configFileLoc;
    private final Float GST;
    private final String configFile;
    private final String FILE_LOC = "File Location=";

    @Autowired
    public BillGenerationService(@Value("${config.file.location}") String configFileLoc,
                                 @Value("${GST}") Float GST,
                                 @Value("${config.file}") String configFile) {
        this.configFileLoc = configFileLoc;
        this.GST = GST;
        this.configFile = configFile;
    }

    @Autowired
    private BillGenerationRepository billGenerationRepository;

    @Autowired
    private ClientInvoiceService clientInvoiceService;

    public ResponseEntity<Resource> generateBill(String projName, String client, MultipartFile file) throws IOException {
        Workbook wb = new XSSFWorkbook(file.getInputStream());
        List<String> invNumbers = new ArrayList<String>();
        String home = System.getProperty("user.home");
        BufferedReader reader;
        String fileLoc = "";
        try {
            reader = new BufferedReader(new FileReader(home+configFileLoc+configFile));
            String line1 = reader.readLine();
            String line2 = reader.readLine();
            if (line1.toLowerCase().contains(FILE_LOC.toLowerCase())) {
                fileLoc = home + line1.substring(line1.indexOf('=')+1).trim();
            } else if (line2.toLowerCase().contains(FILE_LOC.toLowerCase())) {
                fileLoc = home + line2.substring(line2.indexOf('=')+1).trim();
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        for (Sheet sheet: wb) {
            for (int i=1;i <= sheet.getLastRowNum();i++) {
                int newInvNum = clientInvoiceService.getLatestInvNum(client);
                String latestInv = sheet.getRow(i).getCell(6).getStringCellValue().concat(Integer.toString(newInvNum));
                saveInvoice(sheet.getRow(i), client, latestInv);
                String fileName = client + "-" + latestInv + ".pdf";
                fileName = fileName.replaceAll(" ", "_").replaceAll("/","-");
                String dest = fileLoc + fileName;
                invNumbers.add(dest);
                PdfWriter pdfWriter = new PdfWriter(dest);
                Document document = new Document(new PdfDocument(pdfWriter));
                addStaticContent(document);
                addBillTo(document, sheet.getRow(i), latestInv);
                billingInfo(document, projName, sheet.getRow(i));
                addTable(document, sheet.getRow(i));
                addStaticPayment(document);
                addFooter(document);
                document.close();
                clientInvoiceService.incrementInvNum(client, ++newInvNum);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            for (String pdfFile:invNumbers) {
                File fileToZip = new File(pdfFile);
                FileInputStream inputStream = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zos.putNextEntry(zipEntry);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    zos.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            }
            zos.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment: filename=\""+ "unison.zip" + "\"")
                .contentLength(baos.size())
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(new InputStreamResource(new ByteArrayInputStream(baos.toByteArray())));
    }

    private void saveInvoice(Row row, String client, String latestInvNum) {
        DecimalFormat df = new DecimalFormat("#.00");
        Double invoiceAmt = row.getCell(3).getNumericCellValue() * (int)row.getCell(4).getNumericCellValue();
        Invoice invoice = new Invoice();
        invoice.setInvoice_num(latestInvNum);
        invoice.setClient_name(client);
        LocalDate date = row.getCell(5).getLocalDateTimeCellValue().toLocalDate();
        invoice.setInvoice_date(date);
        invoice.setInvoice_duedate(date.plusDays(45));
        invoice.setInvoice_datetime(LocalDateTime.now());
        invoice.setInvoice_amt(df.format(invoiceAmt));
        invoice.setInvoice_gst(df.format(invoiceAmt*GST));
        invoice.setInvoice_total(df.format(invoiceAmt*(1+GST)));
        invoice.setCost_centre(getData(row, 11));
        invoice.setResource(row.getCell(1).getStringCellValue());
        invoice.setInvoice_status("PENDING");
        billGenerationRepository.save(invoice);
    }

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

    private void addBillTo(Document document, Row row, String latestInvNum) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Text billToText = new Text("Bill To:").setFontSize(12);
        Paragraph billTo = new Paragraph().add(billToText).setFont(bold);
        document.add(billTo);
        Table table = new Table(UnitValue.createPercentArray(16));
        Cell cell1 = new Cell(1, 12)
                .add(new Paragraph(row.getCell(7).getStringCellValue())).add(new Paragraph("\n"))
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
        Cell cell3 = new Cell(1,1).add(new Paragraph(date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .add(new Paragraph(date.plusDays(45).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))))
                .add(new Paragraph(latestInvNum))
                .setTextAlignment(TextAlignment.LEFT)
                .setBorder(Border.NO_BORDER).setFontSize(10)
                .setVerticalAlignment(VerticalAlignment.TOP);
        table.addCell(cell3);
        document.add(table);
    }

    private void billingInfo(Document document, String projName, Row row) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Text costCentreText = new Text("Cost Centre:").setFontSize(11).setFont(bold);
        String PONum = getData(row, 9);
        String account = getData(row, 10);
        String costCentre = getData(row, 11);
        Paragraph costCentrePara = new Paragraph().add(costCentreText)
                .add(" " + costCentre).setFontSize(10)
                .add("\n");
        Text accountText = new Text("Account      :").setFontSize(11).setFont(bold);
        costCentrePara.add(accountText).add(" " + account).setFontSize(10);
        document.add(costCentrePara);
        String attn = "Attn         : " + row.getCell(8).getStringCellValue() + "\n" + "PO No     : " + PONum + "\n" + "\n";
        if (!projName.isEmpty()) {
            attn = "Attn         : " + row.getCell(8).getStringCellValue() + "\n" + "PO No    : " + PONum + "\n" + "Project Name: " + projName + "\n" + "\n";
        }
        Paragraph attnPara = new Paragraph().add(attn).setFontSize(10);
        document.add(attnPara);
    }

    private void addTable(Document document, Row row) {
        DecimalFormat df = new DecimalFormat("#,###,###.00");
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
        Double gstAmt = unitPrice*GST;
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
        Double tot = unitPrice * (1 + GST);
        Cell totalCell2 = new Cell(4, 2)
                .add(new Paragraph(df.format(tot)))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(totalCell2);
        document.add(table);
    }

    private void addFooter(Document document) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Text footerText = new Text("TAX REGISTRATION NUMBER: 201225652C").setFontSize(11).setFont(bold);
        Paragraph footer = new Paragraph().add("\n").add("\n").add(footerText)
                                            .setFontColor(DeviceGray.makeLighter(DeviceGray.BLACK))
                                            .setTextAlignment(TextAlignment.CENTER);
        document.add(footer);
    }

    private String getData(Row row, int cellNum) {
        if (row.getCell(cellNum).getCellType().equals(CellType.NUMERIC)) {
            return Integer.toString((int)row.getCell(cellNum).getNumericCellValue());
        } else {
            return row.getCell(cellNum).getStringCellValue();
        }
    }
}
