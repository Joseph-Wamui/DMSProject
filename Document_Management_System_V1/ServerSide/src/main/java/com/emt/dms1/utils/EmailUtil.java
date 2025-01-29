package com.emt.dms1.utils;//package  com.example.demo.utils;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import java.util.Properties;
//
//@Configuration
//public class EmailUtil {
//
//    @Autowired
//    private ProviderConfiguration providerConfiguration;
//
//    @Bean
//    public JavaMailSender mailSender()
//    {
//        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
//        javaMailSender.setHost(providerConfiguration.getHost());
//        javaMailSender.setPort(providerConfiguration.getPort());
//
//        javaMailSender.setUsername(providerConfiguration.getUsername());
//        javaMailSender.setPassword(providerConfiguration.getPassword());
//
//        Properties properties = javaMailSender.getJavaMailProperties();
//        properties.put("mail.transport.protocol", "smtp");
//        properties.put("mail.smtp.auth", "true");
//        properties.put("mail.smtp.starttls.enable", "true");
//        properties.put("mail.debug", providerConfiguration.getDebug().toString());
//
//        return javaMailSender;
//    }
//
//}
///*
//package com.example.demo.utils;
//import jakarta.mail.internet.MimeMessage;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import javax.mail.MessagingException;
//import java.util.List;
//
//@Service
//public class EmailUtil {
//
//    @Autowired
//    private JavaMailSender emailSender;
//
//    public void sendSimpleMessage(String to, String subject, String text, List<String> list){
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("timothysainnh@gmail.com");
//        message.setTo(to);
//        message.setSubject(subject);
//        message.setText(text);
//        if(list!=null && list.size()>0)
//            message.setCc(getCcArray(list));
//        emailSender.send(message);
//    }
//    private String[] getCcArray(List<String> ccList){
//        String[] cc = new String[ccList.size()];
//        for(int i=0;i<ccList.size();i++){
//            cc[i]=ccList.get(i);
//        }
//        return cc;
//    }
//
//    public void forgotMail(String to, String subject, String password) throws MessagingException, jakarta.mail.MessagingException {
//        MimeMessage message = emailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setFrom("noreplyaltech6@gmail.com");
//        helper.setTo(to);
//        helper.setSubject(subject);
//        String htmlMsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b> " + to + " <br><b>Password: </b> " + password + "<br><a href=\"http://localhost:6300/\">Click here to login</a></p>";
//        message.setContent(htmlMsg, "text/html");
//        emailSender.send(message);
//    }
//}
//*/
