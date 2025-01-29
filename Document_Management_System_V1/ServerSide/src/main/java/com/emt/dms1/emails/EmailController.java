package com.emt.dms1.emails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping( value = "/api/v1/emails" , produces = MediaType.APPLICATION_JSON_VALUE)
public class EmailController {
    @Autowired
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/AllUnreadEmailsDetails")
    public ResponseEntity<List<String>> getUnreadEmails2() throws MessagingException {
        List<String> unreadEmails = emailService.fetchUnreadEmails();
        return ResponseEntity.ok(unreadEmails);
    }

//    @GetMapping("/list")
//    public ResponseEntity<List<String>> getUploadedDocuments() throws MessagingException, IOException {
//        List<String> downloadedDocuments = emailService.downloadAllAttachments(); // Trigger download if needed
//        return ResponseEntity.ok(downloadedDocuments);
//    }

    @GetMapping("/list2")
    public ResponseEntity<Map<String, List<String>>> listAttachmentsBySenderController() {
        return emailService.listAttachmentsBySender();
    }

    // New API to get distinct senders with attachment count
    @GetMapping("/senders/attachment-count")
    public ResponseEntity<Map<String, Integer>> getDistinctSendersWithAttachmentCount() throws MessagingException, IOException {
        Map<String, Integer> senderAttachmentCount = emailService.getDistinctSendersWithAttachmentCount();
        return ResponseEntity.ok(senderAttachmentCount);
    }

    // New API to download attachments from specific sender
    @PostMapping("/download/{senderEmail}")
    public ResponseEntity<List<String>> downloadAttachmentsFromSender(@PathVariable String senderEmail) throws MessagingException, IOException {
        List<String> downloadedDocuments = emailService.downloadAttachmentsFromSender(senderEmail);
        return ResponseEntity.ok(downloadedDocuments);
    }

    @GetMapping("/download/attachment/{sender}/{fileName}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable String sender, @PathVariable String fileName) throws MessagingException, IOException {
        byte[] attachmentData = emailService.downloadAttachment(sender, fileName);

        if (attachmentData == null) {
            return ResponseEntity.notFound().build(); // Return 404 Not Found
        }

        // Set appropriate headers for the downloaded attachment
        HttpHeaders headers = new HttpHeaders();
        // Use a default content type instead of relying on `part`
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        return ResponseEntity.ok().headers(headers).body(attachmentData);
    }
}
