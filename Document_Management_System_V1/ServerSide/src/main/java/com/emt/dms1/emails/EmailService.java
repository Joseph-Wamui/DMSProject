package com.emt.dms1.emails;

import com.emt.dms1.user.UserRepository;
import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.user.User;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;

@Service
public class EmailService {
    @Value("${sender.email}")
    private String senderEmail;
    @Value("${sender.password}")
    private String senderPassword;
    @Value("${mail.imap.host}")
    private String imapHost;

    @Autowired
    private final DocumentRepository document1Repository;

    @Autowired
    private UserRepository userRepository;


    private List<String> downloadedDocuments = new ArrayList<>(); // Instance variable for downloaded documents


    public EmailService(DocumentRepository documentRepository) {
        this.document1Repository = documentRepository;
    }

    private Folder connectToInbox() throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.imaps.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Store store = session.getStore();
        store.connect(imapHost, senderEmail, senderPassword);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    public List<String> fetchUnreadEmails() throws MessagingException {
        List<String> unreadEmails = new ArrayList<>();

        // Get system properties and set properties for session
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps"); // Assuming IMAP protocol

        // Get session and connect to store
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
        Store store = session.getStore();
        store.connect(imapHost, senderEmail, senderPassword);

        // Open the inbox folder
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        // Search for unread messages
        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        // Process each unread message
        for (Message message : messages) {
            String subject = message.getSubject();
            String fromAddress = ((InternetAddress) message.getFrom()[0]).getAddress();  // Get the from address (cast to InternetAddress)
            String contentType = message.getContentType();

            String attachmentStatus;
            if (message.isMimeType("multipart/*")) {
                attachmentStatus = "Has Attachments";
            } else {
                attachmentStatus = "No Attachments";
            }

            // Build a single string for each email
            String emailInfo = "From: " + fromAddress + "\n" +
                    "Subject: " + subject + "\n" +
                    "Content Type: " + contentType + "\n" +
                    "Attachments: " + attachmentStatus + "\n";
            // Add the email info string to the list
            unreadEmails.add(emailInfo);
        }
        // Close resources
        inbox.close(false);
        store.close();
        return unreadEmails;
    }

    public List<String> downloadAllAttachments() throws MessagingException, IOException {
        downloadedDocuments.clear(); // Clear list before each download

        try (Folder inbox = connectToInbox()) {  // Wrap inbox in a try-with-resources block
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : messages) {
                if (message.getContent() instanceof Multipart) {
                    processAndSaveMultipart((Multipart) message.getContent(), message.getFileName());
                }
            }
        }  // Store will be closed automatically

        return downloadedDocuments; // Return the list of downloaded documents
    }

//    public List<String> downloadAllAttachments() throws MessagingException, IOException {
//        downloadedDocuments.clear(); // Clear list before each download
//
//
//        Properties props = new Properties();
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.starttls.enable", "true");
//
//        Session session = Session.getInstance(props, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(senderEmail, senderPassword);
//            }
//        });
//        Store store = session.getStore();
//        store.connect(imapHost, senderEmail, senderPassword);
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_WRITE);
//        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//        for (Message message : messages) {
//            if (message.getContent() instanceof Multipart) {
//                processAndSaveMultipart((Multipart) message.getContent(), message.getFileName());
//            }
//        }
//        inbox.close(false);
//        store.close();
//        return downloadedDocuments; // Return the list of downloaded documents
//    }

    public List<String> downloadAttachmentsFromSender(String senderEmail) throws MessagingException, IOException {
        List<String> downloadedDocuments = new ArrayList<>();

        try (Folder inbox = connectToInbox()) {  // Use try-with-resources for inbox
            Message[] unseenMessages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message message : unseenMessages) {
                if (message.getContent() instanceof Multipart && ((InternetAddress) message.getFrom()[0]).getAddress().equals(senderEmail)) {
                    processAndSaveMultipart((Multipart) message.getContent(), message.getFileName());
                    // Mark the message as read after processing
//                    message.setFlag(Flags.Flag.SEEN, false);

                }
            }
        }  // Store will be closed automatically here

        return downloadedDocuments;
    }

//    public List<String> downloadAttachmentsFromSender(String senderEmail) throws MessagingException, IOException {
//        List<String> downloadedDocuments = new ArrayList<>();
//
//        Properties props = new Properties();
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.starttls.enable", "true");
//
//        Session session = Session.getInstance(props, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(senderEmail, senderPassword);
//            }
//        });
//
//        Store store = session.getStore();
//        store.connect(imapHost, senderEmail, senderPassword);
//
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_WRITE);
//
//        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//        for (Message message : messages) {
//            if (message.getContent() instanceof Multipart && ((InternetAddress) message.getFrom()[0]).getAddress().equals(senderEmail)) {
//                processAndSaveMultipart((Multipart) message.getContent(), message.getFileName());
//                // Mark the message as read after processing
//                message.setFlag(Flags.Flag.SEEN, true);
//            }
//        }
//
//        inbox.close(false);
//        store.close();
//
//        return downloadedDocuments;
//    }

    public Map<String, Integer> getDistinctSendersWithAttachmentCount() throws MessagingException, IOException {
        Map<String, Integer> senderAttachmentCount = new HashMap<>();

        try (Folder inbox = connectToInbox()) {  // Use try-with-resources for inbox
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : messages) {
                if (message.getContent() instanceof Multipart) {
                    String sender = ((InternetAddress) message.getFrom()[0]).getAddress();
                    senderAttachmentCount.merge(sender, 1, Integer::sum);
                }
            }
        }  // Store will be closed automatically here

        return senderAttachmentCount;
    }

//    public Map<String, Integer> getDistinctSendersWithAttachmentCount() throws MessagingException, IOException {
//        Map<String, Integer> senderAttachmentCount = new HashMap<>();
//
//        Properties props = new Properties();
//        props.setProperty("mail.store.protocol", "imaps");
//        props.setProperty("mail.imaps.starttls.enable", "true");
//
//        Session session = Session.getInstance(props, new Authenticator() {
//            @Override
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(senderEmail, senderPassword);
//            }
//        });
//
//        Store store = session.getStore();
//        store.connect(imapHost, senderEmail, senderPassword);
//
//        Folder inbox = store.getFolder("INBOX");
//        inbox.open(Folder.READ_WRITE);
//
//        Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//        for (Message message : messages) {
//            if (message.getContent() instanceof Multipart) {
//                String sender = ((InternetAddress) message.getFrom()[0]).getAddress();
//                int currentCount = senderAttachmentCount.getOrDefault(sender, 0);
//                senderAttachmentCount.put(sender, currentCount + 1);
//            }
//        }
//
//        inbox.close(false);
//        store.close();
//
//        return senderAttachmentCount;
//    }

    public byte[] downloadAttachment(String sender, String fileName) throws MessagingException, IOException {
        try (Folder inbox = connectToInbox()) {  // Use try-with-resources for inbox

            Message targetEmail = null;
            for (Message message : inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))) {
                if (message.getContent() instanceof Multipart) {
                    String messageSender = ((InternetAddress) message.getFrom()[0]).getAddress();
                    if (messageSender.equals(sender)) {
                        targetEmail = message;
                        break; // Exit after finding the email from the sender
                    }
                }
            }

            if (targetEmail != null) {
                return getAttachmentData(targetEmail, fileName);
            } else {
                return null; // Attachment not found
            }
        }
    }

    private byte[] getAttachmentData(Message message, String fileName) throws MessagingException, IOException {
        Multipart mp = (Multipart) message.getContent();
        for (int i = 0; i < mp.getCount(); i++) {
            Part part = mp.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) && part.getFileName().equals(fileName)) {
                return IOUtils.toByteArray(part.getInputStream());
            }
        }
        return null; // Attachment not found within the email
    }

    private void processAndSaveMultipart(Multipart mp, String targetFilename) throws MessagingException, IOException {
        for (int i = 0; i < mp.getCount(); i++) {
            Part part = mp.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) &&
                    (targetFilename == null || targetFilename.equals(part.getFileName()))) {
                String fileName = part.getFileName();
                if (fileName == null) {
                    fileName = "attachment.txt";
                }

                try (InputStream in = new BufferedInputStream(part.getInputStream())) {
                    byte[] fileData = in.readAllBytes();

                    saveDocument(fileData, fileName);// Save to repository
//                    emailNotificationsService.sendNotification(savedDocument.getDocumentName(), "uploaded", LocalDateTime.now().toString());
                }
                return; // Exit loop after saving the matching attachment
            } else if (part.getContent() instanceof Multipart) {
                processAndSaveMultipart((Multipart) part.getContent(), targetFilename); // Handle nested multiparts
            }
        }
    }

    // Helper method to save a document to the repository (assuming no recursion)
    private void saveDocument(byte[] fileData, String documentName) throws IOException {
        DocumentModel documentModel = new DocumentModel();
        documentModel.setDocumentName(documentName);
        documentModel.setFileType(Files.probeContentType(new File(documentName).toPath()));
        documentModel.setDocumentData(fileData);
        // Get the email address of the currently logged-in user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();

        // Fetch the user from the database using the email address or unique identifier
        Optional<User> optionalUser = userRepository.findByEmailAddress(loggedInUsername); // Adjust this according to your UserRepository

        // If the user is found, set the department attribute of the document
        if (optionalUser.isPresent()) {
            User loggedInUser = optionalUser.get();
            documentModel.setDepartment(loggedInUser.getDepartment());
            // Concatenate first and last names to create full name
            String fullName = loggedInUser.getFirstName() + " " + loggedInUser.getLastName();
            documentModel.setCreatedBy(fullName);
        } else {
            // Handle the case where the user is not found or department is not available
            // You can set a default department or throw an exception
        }
        LocalDate currentDate = LocalDate.now();
        documentModel.setCreateDate(currentDate);

        document1Repository.save(documentModel);

        // Add downloaded document name to the list (assuming successful save)
        downloadedDocuments.add(documentName);
    }


    public ResponseEntity<Map<String, List<String>>> listAttachmentsBySender() {
        Map<String, List<String>> attachmentsBySender = new HashMap<>();

        try {
            // Connect to the inbox and fetch unread messages
            try (Folder inbox = connectToInbox()) {
                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                // Process each unread message
                for (Message message : messages) {
                    String sender = ((InternetAddress) message.getFrom()[0]).getAddress(); // Get sender email address
                    if (!attachmentsBySender.containsKey(sender)) {
                        attachmentsBySender.put(sender, new ArrayList<>());
                    }
                    if (message.getContent() instanceof Multipart) {
                        processAttachments((Multipart) message.getContent(), attachmentsBySender.get(sender));
                    }
                }
            } // Inbox will be closed automatically

            return ResponseEntity.ok(attachmentsBySender);
        } catch (MessagingException | IOException e) {
            // Handle exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
        }
    }

    private void processAttachments(Multipart multipart, List<String> attachments) throws MessagingException, IOException {
        // Process each part of the multipart content
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                // Add attachment name to the list
                attachments.add(bodyPart.getFileName());
            }
        }
    }
}