package com.digitalwallet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.core.io.ByteArrayResource;
import com.digitalwallet.model.TransactionRecord;
import org.springframework.mail.javamail.MimeMessageHelper;


import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendActivationMail(String to, String token) {
        String activationLink = "http://localhost:8080/api/user/activate?token=" + token;

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Activate Your Digital Wallet Account");
        msg.setText(
                "Welcome!\n\n" +
                "Your account has been created.\n" +
                "Click below to activate it:\n\n" +
                activationLink +
                "\n\nIf you did not request this, ignore this email."
        );

        mailSender.send(msg);
    }
    
    public void sendEmail(String to, String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
    
    
    
    
    
  

    private byte[] buildTransactionsPdf(String username, List<TransactionRecord> txns) {

        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph title = new Paragraph("Digital Wallet Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("User: " + username));
            document.add(new Paragraph(" "));

            // Table
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);

            table.addCell("Txn ID");
            table.addCell("Date Time");
            table.addCell("Amount");
            table.addCell("Status");
            table.addCell("Merchant");
            table.addCell("Masked PAN");
            table.addCell("Provider");
            table.addCell("Token");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (TransactionRecord tx : txns) {
                table.addCell(String.valueOf(tx.getId()));
                table.addCell(tx.getTimestamp() != null ? sdf.format(tx.getTimestamp()) : "");
                table.addCell(String.valueOf(tx.getAmount()));
                table.addCell(tx.getStatus());
                table.addCell(tx.getMerchant());
                table.addCell(tx.getMaskedPan());
                table.addCell(tx.getProvider());
                table.addCell(tx.getToken());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
	/*
	 * // send payment receipt with CSV statement attached public void
	 * sendPaymentReceiptWithCsv(String to, String username, String bodyText,
	 * List<TransactionRecord> transactions) {
	 * 
	 * try { MimeMessage mimeMessage = mailSender.createMimeMessage();
	 * MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
	 * 
	 * helper.setTo(to); helper.setSubject("Payment Successful - Digital Wallet");
	 * helper.setText(bodyText);
	 * 
	 * // Build CSV content in memory String csv = buildTransactionsCsv(username,
	 * transactions);
	 * 
	 * ByteArrayResource attachment = new
	 * ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
	 * 
	 * helper.addAttachment("statement-" + username + ".csv", attachment);
	 * 
	 * mailSender.send(mimeMessage); } catch (MessagingException e) {
	 * 
	 * e.printStackTrace(); } }
	 */
    
    
    public void sendPaymentReceiptWithCsvAndPdf(String to,
            String username,
            String bodyText,
            List<TransactionRecord> transactions) {

try {
MimeMessage mimeMessage = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

helper.setTo(to);
helper.setSubject("Payment Successful - Digital Wallet");
helper.setText(bodyText);

// ✅ CSV
String csv = buildTransactionsCsv(username, transactions);
ByteArrayResource csvAttachment =
new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

helper.addAttachment("statement-" + username + ".csv", csvAttachment);

// ✅ PDF
byte[] pdfBytes = buildTransactionsPdf(username, transactions);
ByteArrayResource pdfAttachment =
new ByteArrayResource(pdfBytes);

helper.addAttachment("statement-" + username + ".pdf", pdfAttachment);

mailSender.send(mimeMessage);

} catch (MessagingException e) {
e.printStackTrace();
}
}

    private String buildTransactionsCsv(String username, List<TransactionRecord> txns) {
        StringBuilder sb = new StringBuilder();

        sb.append("Txn ID,Date Time,Amount,Status,Merchant,Masked PAN,Provider,Token\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (TransactionRecord tx : txns) {
            String id        = String.valueOf(tx.getId());
            String dateTime  = tx.getTimestamp() != null ? sdf.format(tx.getTimestamp()) : "";
            String amount    = String.valueOf(tx.getAmount());
            String status    = safeCsv(tx.getStatus());
            String merchant  = safeCsv(tx.getMerchant());
            String maskedPan = safeCsv(tx.getMaskedPan());
            String provider  = safeCsv(tx.getProvider());
            String token     = safeCsv(tx.getToken());

            sb.append(String.join(",", id, dateTime, amount, status, merchant, maskedPan, provider, token))
              .append("\n");
        }

        return sb.toString();
    }

    private String safeCsv(String value) {
        if (value == null) return "\"\"";
        String v = value.replace("\"", "\"\"");
        return "\"" + v + "\"";
    }
}


