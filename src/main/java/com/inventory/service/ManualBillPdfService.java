package com.inventory.service;

import com.inventory.config.CompanyInfoProperties;
import com.inventory.model.ManualBill;
import com.inventory.model.ManualBillItem;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ManualBillPdfService {
    
    private final CompanyInfoProperties companyInfoProperties;

    public byte[] generateBillPdf(ManualBill bill, List<ManualBillItem> items) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();

        // Header with green background
        addHeader(document, bill);
        document.add(Chunk.NEWLINE);

        // Bill To / Ship To Section
        addBillToShipTo(document, bill);
        document.add(Chunk.NEWLINE);

        // Items Table
        addItemsTable(document, items);
        document.add(Chunk.NEWLINE);

        // HSN/SAC Summary Table
        addHsnSummaryTable(document, bill, items);
        document.add(Chunk.NEWLINE);

        // Total Amount in Words
        addAmountInWords(document, bill.getTotalAmount());
        document.add(Chunk.NEWLINE);

        // Footer: Bank Details, Terms, Signature
        addFooter(document);

        document.close();
        writer.close();
        return out.toByteArray();
    }

    private void addHeader(Document document, ManualBill bill) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{3, 2});

        // Left Header (Company Info - no background, only company name in green)
        PdfPCell leftHeader = new PdfPCell();
        leftHeader.setBorder(Rectangle.BOX);
        leftHeader.setBorderColor(Color.BLACK);
        leftHeader.setPadding(8);
        leftHeader.setBackgroundColor(Color.WHITE);

        // Company name in green, rest in black
        Paragraph companyName = new Paragraph(companyInfoProperties.getName(), 
            new Font(Font.HELVETICA, 14, Font.BOLD, new Color(39, 174, 96)));
        leftHeader.addElement(companyName);
        leftHeader.addElement(new Paragraph(companyInfoProperties.getAddress(), 
            new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK)));
        leftHeader.addElement(new Paragraph("GSTIN: " + companyInfoProperties.getGst() + 
            "    Mobile: " + companyInfoProperties.getMobile(), 
            new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK)));
        leftHeader.addElement(new Paragraph("Email: " + companyInfoProperties.getEmail(), 
            new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK)));
        headerTable.addCell(leftHeader);

        // Right Header (Bill Info)
        PdfPCell rightHeader = new PdfPCell();
        rightHeader.setBorder(Rectangle.BOX);
        rightHeader.setBorderColor(Color.BLACK);
        rightHeader.setPadding(8);
        rightHeader.setBackgroundColor(Color.WHITE);
        
        rightHeader.addElement(new Paragraph("Bill Number: " + bill.getBillNumber(), 
            new Font(Font.HELVETICA, 10, Font.BOLD)));
        rightHeader.addElement(new Paragraph("Bill Date: " + 
            bill.getBillDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), 
            new Font(Font.HELVETICA, 10)));
        headerTable.addCell(rightHeader);
        
        document.add(headerTable);
    }

    private void addBillToShipTo(Document document, ManualBill bill) throws DocumentException {
        PdfPTable partyTable = new PdfPTable(2);
        partyTable.setWidthPercentage(100);
        partyTable.setWidths(new float[]{1, 1});

        // Bill To
        PdfPCell billTo = new PdfPCell();
        billTo.setBorder(Rectangle.BOX);
        billTo.setPadding(6);
        billTo.addElement(new Paragraph("BILL TO", new Font(Font.HELVETICA, 10, Font.BOLD)));
        billTo.addElement(new Paragraph(bill.getCustomerName(), new Font(Font.HELVETICA, 9, Font.BOLD)));
        if (bill.getCustomerAddress() != null && !bill.getCustomerAddress().trim().isEmpty()) {
            billTo.addElement(new Paragraph("Address: " + bill.getCustomerAddress(), 
                new Font(Font.HELVETICA, 9)));
        }
        if (bill.getCustomerGst() != null && !bill.getCustomerGst().trim().isEmpty()) {
            billTo.addElement(new Paragraph("GSTIN: " + bill.getCustomerGst(), 
                new Font(Font.HELVETICA, 9)));
        }
        if (bill.getCustomerMobile() != null && !bill.getCustomerMobile().trim().isEmpty()) {
            billTo.addElement(new Paragraph("Mobile: " + bill.getCustomerMobile(), 
                new Font(Font.HELVETICA, 9)));
        }
        partyTable.addCell(billTo);

        // Ship To
        PdfPCell shipTo = new PdfPCell();
        shipTo.setBorder(Rectangle.BOX);
        shipTo.setPadding(6);
        shipTo.addElement(new Paragraph("SHIP TO", new Font(Font.HELVETICA, 10, Font.BOLD)));
        shipTo.addElement(new Paragraph(bill.getCustomerName(), new Font(Font.HELVETICA, 9, Font.BOLD)));
        
        // Use shipping address if different from billing, otherwise use billing address
        String addressToUse = bill.getCustomerAddress();
        if (bill.getSameAsBilling() != null && !bill.getSameAsBilling() && 
            bill.getShippingAddress() != null && !bill.getShippingAddress().trim().isEmpty()) {
            addressToUse = bill.getShippingAddress();
        }
        
        if (addressToUse != null && !addressToUse.trim().isEmpty()) {
            shipTo.addElement(new Paragraph("Address: " + addressToUse, 
                new Font(Font.HELVETICA, 9)));
        }
        if (bill.getCustomerGst() != null && !bill.getCustomerGst().trim().isEmpty()) {
            shipTo.addElement(new Paragraph("GSTIN: " + bill.getCustomerGst(), 
                new Font(Font.HELVETICA, 9)));
        }
        if (bill.getCustomerMobile() != null && !bill.getCustomerMobile().trim().isEmpty()) {
            shipTo.addElement(new Paragraph("Mobile: " + bill.getCustomerMobile(), 
                new Font(Font.HELVETICA, 9)));
        }
        partyTable.addCell(shipTo);

        document.add(partyTable);
    }

    private void addItemsTable(Document document, List<ManualBillItem> items) throws DocumentException {
        PdfPTable itemsTable = new PdfPTable(7);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{0.7f, 3.5f, 1.2f, 1f, 1.2f, 1.2f, 1.5f});
        
        // Add top and bottom borders to the entire table
        itemsTable.getDefaultCell().setBorder(Rectangle.TOP | Rectangle.BOTTOM);

        Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font tdFont = new Font(Font.HELVETICA, 9);

        // Table Headers
        addTableHeader(itemsTable, thFont, "S.NO.", "ITEMS", "HSN", "QTY", "RATE", "TAX", "AMOUNT");

        // Add items
        for (int i = 0; i < items.size(); i++) {
            ManualBillItem item = items.get(i);
            
            // S.No
            addTableCell(itemsTable, String.valueOf(i + 1), tdFont);
            
            // Item Description
            addTableCell(itemsTable, item.getItemDescription(), tdFont);
            
            // HSN Code
            addTableCell(itemsTable, item.getHsnCode() != null ? item.getHsnCode() : "", tdFont);
            
            // Quantity with Unit
            String qtyText = formatBigDecimal(item.getQuantity());
            if (item.getUnit() != null && !item.getUnit().trim().isEmpty()) {
                qtyText += " " + item.getUnit().toUpperCase();
            }
            addTableCell(itemsTable, qtyText, tdFont);
            
            // Rate
            addTableCell(itemsTable, formatBigDecimal(item.getRate()), tdFont);
            
            // Tax - Show tax amount with percentage in brackets
            String taxText = "";
            BigDecimal taxAmount = BigDecimal.ZERO;
            if (item.getTaxRate() != null && item.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal baseAmount = item.getQuantity().multiply(item.getRate());
                taxAmount = baseAmount.multiply(item.getTaxRate()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                taxText = "₹" + formatBigDecimal(taxAmount) + " (" + formatBigDecimal(item.getTaxRate()) + "%)";
            }
            addTableCell(itemsTable, taxText, tdFont);
            
            // Amount - Use stored amount (which already includes tax)
            addTableCell(itemsTable, "₹" + formatBigDecimal(item.getAmount()), tdFont);
        }

        // Add empty rows only if there are products, and reduce count to fit single page
        int currentRows = items.size();
        if (currentRows > 0) {
            // Add empty rows to fill space and push footer down (always 15 total rows)
            int maxRows = 10;
            for (int i = currentRows; i < maxRows; i++) {
                // Add empty row
                for (int j = 0; j < 7; j++) {
                    PdfPCell emptyCell = new PdfPCell(new Phrase(" ", tdFont));
                    emptyCell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
                    emptyCell.setPadding(5);
                    emptyCell.setMinimumHeight(15);
                    itemsTable.addCell(emptyCell);
                }
            }
        }

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;
        for (ManualBillItem item : items) {
            // Use stored amount which already includes tax
            totalAmount = totalAmount.add(item.getAmount());
            
            // Calculate tax amount for this item
            if (item.getTaxRate() != null && item.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal baseAmount = item.getQuantity().multiply(item.getRate());
                BigDecimal itemTaxAmount = baseAmount.multiply(item.getTaxRate()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                totalTaxAmount = totalTaxAmount.add(itemTaxAmount);
            }
        }

        // Total row with TOTAL on left and both tax amount and total amount on right
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", new Font(Font.HELVETICA, 11, Font.BOLD)));
        totalLabelCell.setColspan(5);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setBorder(Rectangle.BOX);
        totalLabelCell.setPadding(5);
        totalLabelCell.setBackgroundColor(new Color(240, 240, 240));
        itemsTable.addCell(totalLabelCell);

        PdfPCell totalTaxCell = new PdfPCell(new Phrase("₹ " + formatBigDecimal(totalTaxAmount), new Font(Font.HELVETICA, 10, Font.NORMAL)));
        totalTaxCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalTaxCell.setBorder(Rectangle.BOX);
        totalTaxCell.setPadding(5);
        totalTaxCell.setBackgroundColor(new Color(240, 240, 240));
        itemsTable.addCell(totalTaxCell);

        PdfPCell totalAmountCell = new PdfPCell(new Phrase("₹ " + formatBigDecimal(totalAmount), new Font(Font.HELVETICA, 11, Font.BOLD)));
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalAmountCell.setBorder(Rectangle.BOX);
        totalAmountCell.setPadding(5);
        totalAmountCell.setBackgroundColor(new Color(240, 240, 240));
        itemsTable.addCell(totalAmountCell);

        document.add(itemsTable);
    }

    private void addHsnSummaryTable(Document document, ManualBill bill, List<ManualBillItem> items) 
            throws DocumentException {
        PdfPTable hsnTable = new PdfPTable(6);
        hsnTable.setWidthPercentage(100);
        hsnTable.setWidths(new float[]{1.5f, 1.5f, 1f, 1f, 1f, 2f});

        Font thFont = new Font(Font.HELVETICA, 9, Font.BOLD);
        Font tdFont = new Font(Font.HELVETICA, 9);

        // Headers
        addTableHeader(hsnTable, thFont, "HSN/SAC", "Taxable Value", "Rate", "Amount", "Rate", "Total Tax Amount");
        
        // Sub-headers for CGST and SGST
        PdfPCell cgstHeader = new PdfPCell(new Phrase("CGST", new Font(Font.HELVETICA, 8, Font.BOLD)));
        cgstHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cgstHeader.setBorder(Rectangle.BOX);
        cgstHeader.setPadding(3);
        cgstHeader.setBackgroundColor(new Color(230, 230, 230));
        
        PdfPCell sgstHeader = new PdfPCell(new Phrase("SGST", new Font(Font.HELVETICA, 8, Font.BOLD)));
        sgstHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        sgstHeader.setBorder(Rectangle.BOX);
        sgstHeader.setPadding(3);
        sgstHeader.setBackgroundColor(new Color(230, 230, 230));

        // Add sub-header row
        hsnTable.addCell(getCell("", tdFont));
        hsnTable.addCell(getCell("", tdFont));
        hsnTable.addCell(cgstHeader);
        hsnTable.addCell(getCell("", tdFont));
        hsnTable.addCell(sgstHeader);
        hsnTable.addCell(getCell("", tdFont));

        // Group items by HSN code and aggregate values
        Map<String, HsnSummary> hsnSummaries = new LinkedHashMap<>();
        for (ManualBillItem item : items) {
            String hsnCode = item.getHsnCode() != null ? item.getHsnCode() : "";
            BigDecimal baseAmount = item.getQuantity().multiply(item.getRate());
            
            HsnSummary summary = hsnSummaries.getOrDefault(hsnCode, new HsnSummary(hsnCode));
            summary.addItem(item, baseAmount);
            hsnSummaries.put(hsnCode, summary);
        }

        // Add HSN rows
        BigDecimal totalTaxableValue = BigDecimal.ZERO;
        BigDecimal totalCgstAmount = BigDecimal.ZERO;
        BigDecimal totalSgstAmount = BigDecimal.ZERO;
        BigDecimal totalTaxAmount = BigDecimal.ZERO;

        for (HsnSummary summary : hsnSummaries.values()) {
            hsnTable.addCell(getCell(summary.hsnCode, tdFont));
            hsnTable.addCell(getCell(formatBigDecimal(summary.taxableValue), tdFont));
            
            String cgstRate = summary.cgstRate.compareTo(BigDecimal.ZERO) > 0 ? 
                formatBigDecimal(summary.cgstRate) + "%" : "";
            String sgstRate = summary.sgstRate.compareTo(BigDecimal.ZERO) > 0 ? 
                formatBigDecimal(summary.sgstRate) + "%" : "";
                
            hsnTable.addCell(getCell(cgstRate, tdFont));
            hsnTable.addCell(getCell(formatBigDecimal(summary.cgstAmount), tdFont));
            hsnTable.addCell(getCell(sgstRate, tdFont));
            hsnTable.addCell(getCell(formatBigDecimal(summary.getTotalTaxAmount()), tdFont));

            totalTaxableValue = totalTaxableValue.add(summary.taxableValue);
            totalCgstAmount = totalCgstAmount.add(summary.cgstAmount);
            totalSgstAmount = totalSgstAmount.add(summary.sgstAmount);
            totalTaxAmount = totalTaxAmount.add(summary.getTotalTaxAmount());
        }

        // Total Tax row
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total Tax", thFont));
        totalLabelCell.setColspan(5);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        totalLabelCell.setBorder(Rectangle.BOX);
        totalLabelCell.setPadding(5);
        hsnTable.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("₹ " + formatBigDecimal(totalTaxAmount), thFont));
        totalValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalValueCell.setBorder(Rectangle.BOX);
        totalValueCell.setPadding(5);
        hsnTable.addCell(totalValueCell);

        document.add(hsnTable);
    }

    // Helper class for HSN summary
    private static class HsnSummary {
        String hsnCode;
        BigDecimal taxableValue = BigDecimal.ZERO;
        BigDecimal cgstRate = BigDecimal.ZERO;
        BigDecimal sgstRate = BigDecimal.ZERO;
        BigDecimal igstRate = BigDecimal.ZERO;
        BigDecimal cgstAmount = BigDecimal.ZERO;
        BigDecimal sgstAmount = BigDecimal.ZERO;
        BigDecimal igstAmount = BigDecimal.ZERO;

        public HsnSummary(String hsnCode) {
            this.hsnCode = hsnCode;
        }

        public void addItem(ManualBillItem item, BigDecimal baseAmount) {
            taxableValue = taxableValue.add(baseAmount);
            
            if (item.getTaxRate() != null && item.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
                if ("GST".equals(item.getTaxType())) {
                    cgstRate = item.getCgstRate() != null ? item.getCgstRate() : BigDecimal.ZERO;
                    sgstRate = item.getSgstRate() != null ? item.getSgstRate() : BigDecimal.ZERO;
                    
                    BigDecimal cgstAmt = baseAmount.multiply(cgstRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    BigDecimal sgstAmt = baseAmount.multiply(sgstRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    
                    cgstAmount = cgstAmount.add(cgstAmt);
                    sgstAmount = sgstAmount.add(sgstAmt);
                } else if ("IGST".equals(item.getTaxType())) {
                    igstRate = item.getIgstRate() != null ? item.getIgstRate() : BigDecimal.ZERO;
                    BigDecimal igstAmt = baseAmount.multiply(igstRate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    igstAmount = igstAmount.add(igstAmt);
                }
            }
        }

        public BigDecimal getTotalTaxAmount() {
            return cgstAmount.add(sgstAmount).add(igstAmount);
        }
    }

    private void addAmountInWords(Document document, BigDecimal totalAmount) throws DocumentException {
        String amountInWords = "Total Amount (in words): " + 
            convertNumberToWords(totalAmount.longValue()) + " Rupees Only";
        document.add(new Paragraph(amountInWords, new Font(Font.HELVETICA, 10, Font.BOLD)));
    }

    private void addFooter(Document document) throws DocumentException {
        PdfPTable footerTable = new PdfPTable(3);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{2, 3, 2});

        Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font tdFont = new Font(Font.HELVETICA, 9);

        // Bank Details
        PdfPCell bankCell = new PdfPCell();
        bankCell.setBorder(Rectangle.BOX);
        bankCell.setPadding(6);
        bankCell.addElement(new Paragraph("Bank Details", thFont));
        bankCell.addElement(new Paragraph("A/c Holder: EYARKKAI FIBERS PRIVATE LIMITED", tdFont));
        bankCell.addElement(new Paragraph("A/c No: 3335557773", tdFont));
        bankCell.addElement(new Paragraph("A/c Type: Current", tdFont));
        bankCell.addElement(new Paragraph("IFSC Code: KKBK0008664", tdFont));
        bankCell.addElement(new Paragraph("Branch: COIMBATORE - TRICHY ROAD", tdFont));
        footerTable.addCell(bankCell);

        // Terms and Conditions
        PdfPCell termsCell = new PdfPCell();
        termsCell.setBorder(Rectangle.BOX);
        termsCell.setPadding(6);
        termsCell.addElement(new Paragraph("Terms and Conditions", thFont));
        termsCell.addElement(new Paragraph(
            "1. Goods once sold will not be taken back or exchanged\n" +
            "2. All disputes are subject to Coimbatore jurisdiction only\n" +
            "3. Payment: Payment : 100% in Advance\n" +
            "4. Breakage: 5% transit breakage is allowed\n" +
            "5. Validity: 10 Days from the date of\nBill", 
            new Font(Font.HELVETICA, 8)));
        footerTable.addCell(termsCell);

        // Signature
        PdfPCell signCell = new PdfPCell();
        signCell.setBorder(Rectangle.BOX);
        signCell.setPadding(6);
        signCell.setVerticalAlignment(Element.ALIGN_TOP);
        signCell.addElement(new Paragraph(" ", tdFont));
        signCell.addElement(new Paragraph(" ", tdFont));
        signCell.addElement(new Paragraph(" ", tdFont));
        signCell.addElement(new Paragraph("Authorised Signatory For", tdFont));
        signCell.addElement(new Paragraph(companyInfoProperties.getName(), 
            new Font(Font.HELVETICA, 9, Font.BOLD)));
        footerTable.addCell(signCell);

        document.add(footerTable);
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

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        cell.setPadding(5);
        table.addCell(cell);
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

    private String formatBigDecimal(BigDecimal value) {
        if (value == null) return "0.00";
        return String.format("%.2f", value);
    }

    // Number to words conversion
    private String convertNumberToWords(long number) {
        if (number == 0) return "Zero";
        
        String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", 
                         "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", 
                         "Seventeen", "Eighteen", "Nineteen"};
        
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
        
        if (number < 20) {
            return units[(int)number];
        } else if (number < 100) {
            return tens[(int)number/10] + (number%10 != 0 ? " " + units[(int)number%10] : "");
        } else if (number < 1000) {
            return units[(int)number/100] + " Hundred" + (number%100 != 0 ? " " + convertNumberToWords(number%100) : "");
        } else if (number < 100000) {
            return convertNumberToWords(number/1000) + " Thousand" + (number%1000 != 0 ? " " + convertNumberToWords(number%1000) : "");
        } else if (number < 10000000) {
            return convertNumberToWords(number/100000) + " Lakh" + (number%100000 != 0 ? " " + convertNumberToWords(number%100000) : "");
        } else {
            return convertNumberToWords(number/10000000) + " Crore" + (number%10000000 != 0 ? " " + convertNumberToWords(number%10000000) : "");
        }
    }
} 