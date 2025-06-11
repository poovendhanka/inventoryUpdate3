package com.inventory.service;

import com.inventory.config.CompanyInfoProperties;
import com.inventory.model.Sale;
import com.inventory.model.Dealer;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BillPdfService {
    private final CompanyInfoProperties companyInfoProperties;

    public byte[] generateBillPdf(Sale sale, Dealer dealer, String billNumber) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // Header with green background
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3, 2});
        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBackgroundColor(new Color(39, 174, 96));
        leftHeader.setBorder(Rectangle.BOX);
        leftHeader.setBorderColor(Color.BLACK);
        leftHeader.setPadding(8);
        Paragraph companyName = new Paragraph(companyInfoProperties.getName(), new Font(Font.HELVETICA, 14, Font.BOLD, Color.WHITE));
        leftHeader.addElement(companyName);
        leftHeader.addElement(new Paragraph(companyInfoProperties.getAddress(), new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE)));
        leftHeader.addElement(new Paragraph("GSTIN: " + companyInfoProperties.getGst() + "    Mobile: " + companyInfoProperties.getMobile(), new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE)));
        leftHeader.addElement(new Paragraph("Email: " + companyInfoProperties.getEmail(), new Font(Font.HELVETICA, 9, Font.NORMAL, Color.WHITE)));
        headerTable.addCell(leftHeader);

        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBorder(Rectangle.BOX);
        rightHeader.setBorderColor(Color.BLACK);
        rightHeader.setPadding(8);
        rightHeader.setBackgroundColor(Color.WHITE);
        rightHeader.addElement(new Paragraph("Bill No.: " + billNumber, new Font(Font.HELVETICA, 10, Font.BOLD)));
        rightHeader.addElement(new Paragraph("Date & Time: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(sale.getSaleDate()), new Font(Font.HELVETICA, 10)));
        headerTable.addCell(rightHeader);
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // Bill To / Ship To
        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setWidths(new float[]{1, 1});
        PdfPCell billTo = new PdfPCell();
        billTo.setBorder(Rectangle.BOX);
        billTo.setPadding(6);
        billTo.addElement(new Paragraph("Bill To", new Font(Font.HELVETICA, 10, Font.BOLD)));
        billTo.addElement(new Paragraph(dealer.getName(), new Font(Font.HELVETICA, 9)));
        billTo.addElement(new Paragraph(dealer.getAddress(), new Font(Font.HELVETICA, 9)));
        partyTable.addCell(billTo);
        PdfPCell shipTo = new PdfPCell();
        shipTo.setBorder(Rectangle.BOX);
        shipTo.setPadding(6);
        shipTo.addElement(new Paragraph("Ship To", new Font(Font.HELVETICA, 10, Font.BOLD)));
        shipTo.addElement(new Paragraph(dealer.getName(), new Font(Font.HELVETICA, 9)));
        shipTo.addElement(new Paragraph(dealer.getAddress(), new Font(Font.HELVETICA, 9)));
        partyTable.addCell(shipTo);
        document.add(partyTable);
        document.add(Chunk.NEWLINE);

        // Items Table
        PdfPTable itemsTable = new PdfPTable(7);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{0.7f, 3.5f, 1.2f, 1f, 1.2f, 1.2f, 1.5f});
        Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        addTableHeader(itemsTable, thFont, "S.NO.", "ITEMS", "HSN", "QTY.", "RATE", "TAX", "AMOUNT");
        Font tdFont = new Font(Font.HELVETICA, 10);
        DecimalFormat df = new DecimalFormat("#,##0.00");
        double amount = sale.getQuantity() * sale.getPricePerUnit();
        double cgst = 0, sgst = 0, totalTax = 0;
        String taxStr = "";
        if (sale.getProductType().name().equalsIgnoreCase("BLOCK")) {
            cgst = amount * companyInfoProperties.getCgst() / 100.0;
            sgst = amount * companyInfoProperties.getSgst() / 100.0;
            totalTax = cgst + sgst;
            taxStr = df.format(totalTax) + " (" + df.format(companyInfoProperties.getBlockTax()) + "%)";
        }
        addTableRow(itemsTable, tdFont, "1", sale.getProductType().name(), companyInfoProperties.getHsn(), df.format(sale.getQuantity()), df.format(sale.getPricePerUnit()), taxStr, df.format(amount + totalTax));
        document.add(itemsTable);

        // Totals row
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.addCell(getCell("TOTAL", thFont, Element.ALIGN_LEFT, Rectangle.BOX, Color.LIGHT_GRAY));
        totalTable.addCell(getCell(df.format(amount + totalTax), thFont, Element.ALIGN_RIGHT, Rectangle.BOX, Color.LIGHT_GRAY));
        document.add(Chunk.NEWLINE);
        document.add(totalTable);
        document.add(Chunk.NEWLINE);

        // HSN/SAC Table
        PdfPTable hsnTable = new PdfPTable(6);
        hsnTable.setWidthPercentage(100);
        addTableHeader(hsnTable, thFont, "HSN/SAC", "Taxable Value", "CGST", "SGST", "Total Tax Amount", "");
        if (sale.getProductType().name().equalsIgnoreCase("BLOCK")) {
            hsnTable.addCell(getCell(companyInfoProperties.getHsn(), tdFont));
            hsnTable.addCell(getCell(df.format(amount), tdFont));
            hsnTable.addCell(getCell(companyInfoProperties.getCgst() + "%\n" + df.format(cgst), tdFont));
            hsnTable.addCell(getCell(companyInfoProperties.getSgst() + "%\n" + df.format(sgst), tdFont));
            hsnTable.addCell(getCell(df.format(totalTax), tdFont));
            hsnTable.addCell(getCell("", tdFont));
        } else {
            hsnTable.addCell(getCell(companyInfoProperties.getHsn(), tdFont));
            hsnTable.addCell(getCell(df.format(amount), tdFont));
            hsnTable.addCell(getCell("-", tdFont));
            hsnTable.addCell(getCell("-", tdFont));
            hsnTable.addCell(getCell("-", tdFont));
            hsnTable.addCell(getCell("", tdFont));
        }
        document.add(hsnTable);
        document.add(Chunk.NEWLINE);

        // Total Amount in Words
        String amountInWords = "Total Amount (in words): " + convertNumberToWords((long) Math.round(amount + totalTax)) + " Rupees";
        document.add(new Paragraph(amountInWords, new Font(Font.HELVETICA, 10, Font.BOLD)));
        document.add(Chunk.NEWLINE);

        // Footer: Bank Details, Terms, Signature
        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{2, 3, 2});
        // Bank Details (empty)
        PdfPCell bankCell = new PdfPCell();
        bankCell.setBorder(Rectangle.BOX);
        bankCell.setPadding(6);
        bankCell.addElement(new Paragraph("Bank Details", thFont));
        bankCell.addElement(new Paragraph(" "));
        footerTable.addCell(bankCell);
        // Terms and Conditions
        PdfPCell termsCell = new PdfPCell();
        termsCell.setBorder(Rectangle.BOX);
        termsCell.setPadding(6);
        termsCell.addElement(new Paragraph("Terms and Conditions", thFont));
        termsCell.addElement(new Paragraph("1. Goods once sold will not be taken back or exchanged\n2. All disputes are subject to Coimbatore jurisdiction only\n3. Payment : 100% in Advance\n4. Breakage: 5% transit breakage is allowed as per industry norms. Corner chippings will not be considered as breakage.\n5. Validity : 10 Days from the date of Bill", new Font(Font.HELVETICA, 8)));
        footerTable.addCell(termsCell);
        // Signature
        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.BOX);
        signCell.setPadding(6);
        signCell.addElement(new Paragraph(" "));
        signCell.addElement(new Paragraph("Authorised Signatory For", new Font(Font.HELVETICA, 9)));
        signCell.addElement(new Paragraph(companyInfoProperties.getName(), new Font(Font.HELVETICA, 9, Font.BOLD)));
        footerTable.addCell(signCell);
        document.add(footerTable);

        document.close();
        writer.close();
        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(new Color(220, 230, 241));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.BOX);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableRow(PdfPTable table, Font font, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, font));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorder(Rectangle.BOX);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private PdfPCell getCell(String text, Font font) {
        return getCell(text, font, Element.ALIGN_CENTER, Rectangle.BOX, Color.WHITE);
    }

    private PdfPCell getCell(String text, Font font, int alignment, int border, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        return cell;
    }

    // Simple number to words (for rupees, up to crores)
    private static final String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static final String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
    private String convertNumberToWords(long n) {
        if (n == 0) return "Zero";
        if (n < 0) return "Minus " + convertNumberToWords(-n);
        String words = "";
        if ((n / 10000000) > 0) {
            words += convertNumberToWords(n / 10000000) + " Crore ";
            n %= 10000000;
        }
        if ((n / 100000) > 0) {
            words += convertNumberToWords(n / 100000) + " Lakh ";
            n %= 100000;
        }
        if ((n / 1000) > 0) {
            words += convertNumberToWords(n / 1000) + " Thousand ";
            n %= 1000;
        }
        if ((n / 100) > 0) {
            words += convertNumberToWords(n / 100) + " Hundred ";
            n %= 100;
        }
        if (n > 0) {
            if (!words.isEmpty()) words += "and ";
            if (n < 20) words += units[(int) n];
            else {
                words += tens[(int) n / 10];
                if ((n % 10) > 0) words += "-" + units[(int) n % 10];
            }
        }
        return words.trim();
    }
} 