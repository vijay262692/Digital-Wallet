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
    
    
 //  send payment receipt with CSV statement attached
    public void sendPaymentReceiptWithCsv(String to,
                                          String username,
                                          String bodyText,
                                          List<TransactionRecord> transactions) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject("Payment Successful - Digital Wallet");
            helper.setText(bodyText);

            // Build CSV content in memory
            String csv = buildTransactionsCsv(username, transactions);

            ByteArrayResource attachment =
                    new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

            helper.addAttachment("statement-" + username + ".csv", attachment);

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


