package com.emt.dms1.testOCR;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public enum DocumentAttribute {

    TAXPAYER_NAME,
    ID_NUMBER,
    PIN,
    SERIAL_NUMBER,
    DEGREE_NAME,
    ISSUING_INSTITUTION,
    ISSUE_DATE,
    EMAIL,
    DATE,
    NAME,
    PO_BOX,
    INVOICE_NUMBER,
    INVOICE_DATE,
    TOTAL_AMOUNT,
    PERMIT_NUMBER,
    EXPIRY_DATE,
    POSTAL_CODE;

    private static final Map<DocumentAttribute, Pattern> attributePatterns = new HashMap<>();

    static {
        // Initialize existing attribute patterns
        attributePatterns.put(TAXPAYER_NAME, Pattern.compile("\\b(Taxpayer Name|taxpayer name|TAXPAYER NAME)\\\\s?(\\\\b[A-Z][A-Za-z]+(?:\\\\s?[A-Z][A-Za-z]+){1,2})", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(ID_NUMBER, Pattern.compile("\\b(ID NUMBER|id number)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(SERIAL_NUMBER, Pattern.compile("\\b(Serial Number|serial number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(EMAIL, Pattern.compile("\\b(Email Address|email address|EMAIL ADDRESS|email):?\\s*([^\\s]+)(?:\\s{2,8})([^\\s]+)\\b",Pattern.CASE_INSENSITIVE));
        attributePatterns.put(NAME, Pattern.compile("\\b(Name|name|NAME)\\s?(\\b[A-Z][A-Za-z]+(?:\\s?[A-Z][A-Za-z]+){1,2})", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(PIN, Pattern.compile("\\b(PersonalIdentificationNumber|PERSONAL IDENTFICATION NUMBER|personal identification number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(DEGREE_NAME, Pattern.compile("\\b(Degree Name|degree name):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(POSTAL_CODE, Pattern.compile("\\b(Postal Code|ZIP Code|ZIP\\s?code)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(TOTAL_AMOUNT, Pattern.compile("(?:Total Due|Total Amount)(?:.|\\s*):\\s*\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(PERMIT_NUMBER, Pattern.compile("Permit No(?:.|\\s*):\\s*(\\w+)", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(EXPIRY_DATE, Pattern.compile("Expiry Date(?:.|\\s*):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(INVOICE_NUMBER, Pattern.compile("Invoice No(?:.|\\s*):\\s*(\\w+)", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(INVOICE_DATE, Pattern.compile("Invoice Date(?:.|\\s*):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE));
        attributePatterns.put(ISSUING_INSTITUTION, Pattern.compile("\\b(Issuing Institution|issuing institution):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE));
    }

    public Pattern getPattern() {
        return attributePatterns.get(this);
    }



    public <K, V> Map<K, V> toLowerCase() {
        Map<K, V> result = new HashMap<>();
        for (DocumentAttribute attribute : DocumentAttribute.values()) {
            result.put((K) attribute.name().toLowerCase(), (V) attribute.name().toLowerCase());
        }
        return result;
    }
}
