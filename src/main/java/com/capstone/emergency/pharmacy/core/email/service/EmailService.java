package com.capstone.emergency.pharmacy.core.email.service;


import com.capstone.emergency.pharmacy.core.email.model.EmailDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;


@RequiredArgsConstructor
@Service
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${email.display.name}")
    private String fromName;

    @Value("${email.sender.username}")
    private String fromEmail;


    public void sendEmail(final EmailDto emailDTO) {
        final MimeMessage message = buildMailMessage(emailDTO);
        javaMailSender.send(message);
        log.info("Finished sendEmail");
    }

    private MimeMessage buildMailMessage(final EmailDto emailDTO) {
        log.info("Started creating mail message");
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            helper = new MimeMessageHelper(message, false);
            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(emailDTO.getEmailTo());
            helper.setSubject(emailDTO.getSubject());
            helper.setText(emailDTO.getText());
            if (emailDTO.getCcRecipients() != null && !emailDTO.getCcRecipients().isEmpty()) {
                helper.setCc(emailDTO.getCcRecipients()
                        .stream().map(a -> {
                            try {
                                return new InternetAddress(a);
                            } catch (AddressException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }).toArray(InternetAddress[]::new));
            }
            log.info("Finished creating mail message");
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }
}

