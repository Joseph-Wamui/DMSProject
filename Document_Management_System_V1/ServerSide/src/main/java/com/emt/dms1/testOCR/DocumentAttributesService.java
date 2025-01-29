package com.emt.dms1.testOCR;//package com.example.demo.TestOCR;
//
//import com.example.demo.Document.DocumentRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.DataAccessException;
//import org.springframework.stereotype.Service;
//
//import java.text.ParseException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//import java.util.EnumSet;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//
//
//
//@Slf4j
//@Service
//public class DocumentAttributesService {
//
//    @Autowired
//    private DocumentAttributesRepository documentRepository;
//
//
//
//    private static final Pattern TAXPAYER_NAME_PATTERN = Pattern.compile("\\b(Taxpayer Name|taxpayername|TAXPAYER NAME):?\\s*([^,]+)", Pattern.CASE_INSENSITIVE);
//    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("\\b(ID NUMBER|id number)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern PERSONAL_ID_PATTERN = Pattern.compile("\\b(PersonalIdentificationNumber|PERSONAL IDENTFICATION NUMBER|personal identification number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//
//    private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile("\\b(Serial Number|serial number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern DEGREE_NAME_PATTERN = Pattern.compile("\\b(Degree Name|degree name):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern ISSUING_INSTITUTION_PATTERN = Pattern.compile("\\b(Issuing Institution|issuing institution):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern ISSUE_DATE_PATTERN = Pattern.compile("\\b(Issue Date|issue date):?\\s*(.*?)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b(Email Address|email address|EMAIL ADDRESS|emailmailto):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern P_O_BOX_PATTERN = Pattern.compile("\\b(P(ost)?\\.? O(ffice)?\\.? Box|P\\.?O\\.? Box)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//
//    private static final Pattern NAME_PATTERN = Pattern.compile("\\b(Name|name|NAME):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("\\b(Postal Code|ZIP Code|ZIP\\s?code)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
//
//
//    private static final Map<DocumentAttribute, Pattern> attributePatterns = new HashMap<>();
//
//    static {
//        attributePatterns.put(DocumentAttribute.TAXPAYER_NAME, TAXPAYER_NAME_PATTERN);
//        attributePatterns.put(DocumentAttribute.ID_NUMBER, ID_NUMBER_PATTERN);
//        attributePatterns.put(DocumentAttribute.PERSONAL_ID_NUMBER, PERSONAL_ID_PATTERN);
//        attributePatterns.put(DocumentAttribute.SERIAL_NUMBER, SERIAL_NUMBER_PATTERN);
//        attributePatterns.put(DocumentAttribute.DEGREE_NAME, DEGREE_NAME_PATTERN);
//        attributePatterns.put(DocumentAttribute.ISSUING_INSTITUTION, ISSUING_INSTITUTION_PATTERN);
//        attributePatterns.put(DocumentAttribute.ISSUE_DATE, ISSUE_DATE_PATTERN);
//        attributePatterns.put(DocumentAttribute.EMAIL_ADDRESS, EMAIL_PATTERN);
//        attributePatterns.put(DocumentAttribute.DATE, ISSUE_DATE_PATTERN);
//        attributePatterns.put(DocumentAttribute.P_O_BOX, P_O_BOX_PATTERN);
//        attributePatterns.put(DocumentAttribute.POSTAL_CODE, POSTAL_CODE_PATTERN);
//        attributePatterns.put(DocumentAttribute.NAME, NAME_PATTERN);
//
//    }
//
//    public DocumentAttributes extractAttributes(OcrService result, EnumSet<DocumentAttribute> selectedAttributes) throws ParseException {
//        if (selectedAttributes == null || selectedAttributes.isEmpty()) {
//            throw new IllegalArgumentException("selectedAttributes cannot be null or empty");
//        }
//
//        DocumentAttributes attributes = new DocumentAttributes();
//
//        try {
//            for (DocumentAttribute attribute : selectedAttributes) {
//                Pattern pattern = attributePatterns.get(attribute);
//                String value = extractValue(result, pattern);
//                switch (attribute) {
//                    case DocumentAttribute.ISSUE_DATE:
//                        attributes.setIssueDate(parseDate(value));
//                        break;
//                    case DocumentAttribute.ID_NUMBER:
//                        attributes.setIdNumber(value);
//                        break;
//                    case DocumentAttribute.TAXPAYER_NAME:
//                        attributes.setTaxpayerName(value);
//                        break;
//                    case DocumentAttribute.DEGREE_NAME:
//                        attributes.setDegreeName(value);
//                        break;
//                    case EMAIL_ADDRESS:
//                        attributes.setEmailAddress(value);
//                        break;
//                    case SERIAL_NUMBER:
//                        attributes.setSerialNumber(value);
//                        break;
//                    case DocumentAttribute.PERSONAL_ID_NUMBER:
//                        attributes.setPersonalIdentificationNumber(value);
//                        break;
//                    case DocumentAttribute.ISSUING_INSTITUTION:
//                        attributes.setIssuingInstitution(value);
//                        break;
////                    case DATE:
////                        attributes.setIssueDate(parseDate(value));
////                        break;
//
//                    case P_O_BOX:
//                        attributes.setP_O_Box(value);
//                        break;
//                    case POSTAL_CODE:
//                        attributes.setPostalCode(value);
//                        break;
//                    case NAME:
//                        attributes.setName(value);
//                        break;
//
////                    default:
////                        attributes.setAttribute(attribute, value);
//                }
//            }
//            save(attributes);
//            return attributes;
//        } catch (Exception e) {
//            log.error("Error extracting and saving document attributes:", e);
//            throw new RuntimeException("Failed to extract and save document attributes", e);
//        }
//    }
//
//    private String extractValue(String documentContent, Pattern pattern) {
//        Matcher matcher = pattern.matcher(documentContent);
//        return matcher.find() && matcher.groupCount() >= 2 ? matcher.group(2) : null;
//    }
//
//
//    private LocalDate parseDate(String dateStr) {
//        if (dateStr == null || dateStr.isEmpty()) {
//            return null; // Handle empty or null input
//        }
//
//        // Assuming the expected date format is "MM/dd/yyyy"
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//        try {
//            LocalDate parsedDate = LocalDate.parse(dateStr, formatter);
//            return parsedDate;
//        } catch (DateTimeParseException e) {
//            log.warn("Failed to parse date '{}' using format '{}'", dateStr, formatter.toString());
//            return null;
//        }
//    }
//
//    public DocumentAttributes save(DocumentAttributes attributes) {
//        try {
//            return documentRepository.save(attributes);
//        } catch (DataAccessException e) {
//            log.error("Error saving document attributes:", e);
//            throw new RuntimeException("Failed to save document attributes", e);
//        }
//    }
//
//    public List<DocumentAttributes> getAll() {
//        List<DocumentAttributes> allAttributes = documentRepository.findAll();
//        return allAttributes;
//    }
//
//}
//
//
