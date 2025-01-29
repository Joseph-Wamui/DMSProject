package com.emt.dms1.testOCR;


import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.util.LoadLibs;
import nu.pattern.OpenCV;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.opencv.imgcodecs.Imgcodecs.imdecode;
import static org.opencv.imgproc.Imgproc.*;


@Service
@Slf4j
public class OcrService {
    static {
        OpenCV.loadLocally();
    }

    private static String tessDataPath = LoadLibs.extractTessResources("tessdata").getPath();

    @Autowired
    private DocumentRepository documentRepository;

    private DocumentModel documentModel;

    private PatternsRegex pattern;

    private PatternRepository patternRepository;

    private KeyValuePairsRepository keyValuePairsRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private DocumentAttributesRepository documentAttributesRepository;
    private static final Pattern TAXPAYER_NAME_PATTERN = Pattern.compile("\\b(Taxpayer Name|taxpayer name|TAXPAYER NAME)\\\\s?(\\\\b[A-Z][A-Za-z]+(?:\\\\s?[A-Z][A-Za-z]+){1,2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern ID_NUMBER_PATTERN = Pattern.compile("\\b(ID NUMBER|id number)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERSONAL_ID_PATTERN = Pattern.compile("\\b(PersonalIdentificationNumber|PERSONAL IDENTFICATION NUMBER|personal identification number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile("\\b(Serial Number|serial number):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern DEGREE_NAME_PATTERN = Pattern.compile("\\b(Degree Name|degree name):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISSUING_INSTITUTION_PATTERN = Pattern.compile("\\b(Issuing Institution|issuing institution):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISSUE_DATE_PATTERN = Pattern.compile("\\b(Issue Date|issue date|Certificate Issue Date|Date of Issue):?\\s*(.*?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b(Email Address|email address|EMAIL ADDRESS|emailmailto):?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern P_O_BOX_PATTERN = Pattern.compile("\\b(P(ost)?\\.? O(ffice)?\\.? Box|P\\.?O\\.? Box)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NAME_PATTERN = Pattern.compile("\\b(Name|name|NAME)\\s?(\\b[A-Z][A-Za-z]+(?:\\s?[A-Z][A-Za-z]+){1,2})", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("\\b(Postal Code|ZIP Code|ZIP\\s?code)\\s*:?\\s*([^\\s]+)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVOICE_NUMBER_PATTERN = Pattern.compile("Invoice No(?:.|\\s*):\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern INVOICE_DATE_PATTERN = Pattern.compile("Invoice Date(?:.|\\s*):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOTAL_AMOUNT_PATTERN = Pattern.compile("(?:Total Due|Total Amount)(?:.|\\s*):\\s*\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERMIT_NUMBER_PATTERN = Pattern.compile("Permit No(?:.|\\s*):\\s*(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPIRY_DATE_PATTERN = Pattern.compile("Expiry Date(?:.|\\s*):\\s*(\\d{2}/\\d{2}/\\d{4})", Pattern.CASE_INSENSITIVE);

    public OcrService(PatternRepository patternRepository, KeyValuePairsRepository keyValuePairsRepository) {
        this.patternRepository = patternRepository;
        this.keyValuePairsRepository = keyValuePairsRepository;
    }

    //performing Ocr on a preprocessed image
    public ResponseEntity<String> performOCRPDF(MultipartFile file) {
        try {
            // Check if the uploaded file is a PDF
            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are supported.");
            }

            // Load the PDF document
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                StringBuilder resultBuilder = new StringBuilder();

                // Iterate through each page
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    // Render the page to an image
                    BufferedImage image = renderer.renderImageWithDPI(i, 300);

                    // Preprocess the image

                    Mat preprocessedImage = preprocessImage(image);

                    // Perform OCR on the preprocessed image (you need to implement this method)
                    String result = capture(preprocessedImage);

                    // Append OCR result
                    resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
                }


                return ResponseEntity.ok(resultBuilder.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF file.");
        }
    }


     //method to capture deails using tesseract( on preprocessed images)
    public String capture(Mat image) throws IOException {
        try {
            File tempFile = File.createTempFile("tempImage", ".png");
            Imgcodecs.imwrite(tempFile.getAbsolutePath(), image);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz/' [ ]'");
            String result = tesseract.doOCR(tempFile);

            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error performing OCR: " + e.getMessage();
        }
    }



 //methods to preprocess images
    public Mat preprocessImage(BufferedImage image) {
        // Convert BufferedImage to Mat
        Mat matImage = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        BufferedImageToMat(image, matImage);

        // Perform preprocessing steps (e.g., convert to grayscale)
        Mat grayscaleImage = new Mat();
        cvtColor(matImage, grayscaleImage, COLOR_BGR2GRAY);
        return grayscaleImage;
    }

    public void BufferedImageToMat(BufferedImage src, Mat dst) {
        int[] data = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        int width = src.getWidth();
        int height = src.getHeight();

        dst.create(height, width, CvType.CV_8UC3);

        int r, g, b;
        byte[] pixels = new byte[width * height * (int) dst.elemSize()];
        int pixelIndex = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = data[y * width + x];
                r = (argb >> 16) & 0xFF;
                g = (argb >> 8) & 0xFF;
                b = (argb) & 0xFF;

                pixels[pixelIndex++] = (byte) b;
                pixels[pixelIndex++] = (byte) g;
                pixels[pixelIndex++] = (byte) r;
            }
        }

        dst.put(0, 0, pixels);
    }

   // performing ocr on normal images without extraction
    public ResponseEntity<String> performOCRPDFex(MultipartFile file) {
        try {
            // Check if the uploaded file is a PDF
//            if (!file.getContentType().equals("application/pdf")) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are supported.");
//            }

            // Create a temporary directory to store images
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "ocr_images");
            tempDir.mkdirs();

            // Load the PDF document
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                StringBuilder resultBuilder = new StringBuilder();


                // Iterate through each page and convert it to an image
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 300);

                    // Save the image to a temporary file
                    File tempImageFile = new File(tempDir, "page_" + (i + 1) + ".jpg");
                    javax.imageio.ImageIO.write(image, "jpg", tempImageFile);

                    // Perform OCR on the image
                    String result = capture2(tempImageFile);

                    // Append OCR result
                    resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
                }
                return  ResponseEntity.ok(resultBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF file.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveResult(String result, Long id) {
        DocumentModel documentModel = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
        if (documentModel != null) {
            documentModel.setResult(result);
            documentRepository.save(documentModel);
        }

    }

    //ocr on normal images
    public String capture2(File file) throws IOException {
        try {
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@.,/' [ ]'");
            String result = tesseract.doOCR(file);

            return result;
        } catch (TesseractException e) {
            e.printStackTrace();
            return "Error performing OCR: " + e.getMessage();
        }
    }

   //Defining the key value mapping in the system
    private static final Map<DocumentAttribute, Pattern> attributePatterns = new HashMap<>();

    static {
        attributePatterns.put(DocumentAttribute.TAXPAYER_NAME, TAXPAYER_NAME_PATTERN);
        attributePatterns.put(DocumentAttribute.ID_NUMBER, ID_NUMBER_PATTERN);
        attributePatterns.put(DocumentAttribute.PIN, PERSONAL_ID_PATTERN);
        attributePatterns.put(DocumentAttribute.SERIAL_NUMBER, SERIAL_NUMBER_PATTERN);
        attributePatterns.put(DocumentAttribute.DEGREE_NAME, DEGREE_NAME_PATTERN);
        attributePatterns.put(DocumentAttribute.ISSUING_INSTITUTION, ISSUING_INSTITUTION_PATTERN);
        attributePatterns.put(DocumentAttribute.ISSUE_DATE, ISSUE_DATE_PATTERN);
        attributePatterns.put(DocumentAttribute.EMAIL, EMAIL_PATTERN);
        attributePatterns.put(DocumentAttribute.DATE, ISSUE_DATE_PATTERN);
        attributePatterns.put(DocumentAttribute.PO_BOX, P_O_BOX_PATTERN);
        attributePatterns.put(DocumentAttribute.POSTAL_CODE, POSTAL_CODE_PATTERN);
        attributePatterns.put(DocumentAttribute.NAME, NAME_PATTERN);
        attributePatterns.put(DocumentAttribute.INVOICE_NUMBER,INVOICE_NUMBER_PATTERN);
        attributePatterns.put(DocumentAttribute.INVOICE_DATE,INVOICE_DATE_PATTERN);
        attributePatterns.put(DocumentAttribute.TOTAL_AMOUNT,TOTAL_AMOUNT_PATTERN);
        attributePatterns.put(DocumentAttribute.PERMIT_NUMBER,PERMIT_NUMBER_PATTERN);
        attributePatterns.put(DocumentAttribute.EXPIRY_DATE,EXPIRY_DATE_PATTERN);

    }

    private final String[] allAttributesArray = Arrays.stream(DocumentAttribute.values())
            .map(Enum::name)
            .toArray(String[]::new);

//
    public EntityResponse extractAttributestry(MultipartFile file, Long id, List<String> keys) {
        EntityResponse entityResponse = new EntityResponse<>();
        EnumSet<DocumentAttribute> attributesSet = EnumSet.noneOf(DocumentAttribute.class);

        try {
            // Convert keys to uppercase
            List<String> uppercaseKeys = keys.stream()
                    .filter(Objects::nonNull)
                    .map(String::toUpperCase)
                    .toList();
            // Validate and convert selected attributes with autocomplete support
            for (String attribute : uppercaseKeys) {
                if (attribute != null && !attribute.isEmpty() &&
                        Arrays.stream(allAttributesArray).anyMatch(attr -> attr.equalsIgnoreCase(attribute))) {
                    attributesSet.add(DocumentAttribute.valueOf(attribute.toUpperCase()));
                } else {
                    log.warn("Invalid attribute provided: " + attribute);
                    entityResponse.setMessage("badrequest");
                    entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                    return entityResponse;
                }
            }

            if (attributesSet.isEmpty()) {
                throw new IllegalArgumentException("selectedAttributes cannot be null or empty");
            }

            // Perform OCR and extract document content
            String documentContent = Ocrextract(file, id);
            DocumentAttributes attributes = new DocumentAttributes();

            for (DocumentAttribute attribute : attributesSet) {
                Pattern pattern = attributePatterns.get(attribute);
                String value = extractValue(documentContent, pattern);

                switch (attribute) {
                    case ISSUE_DATE:
                        attributes.setIssueDate(parseDate(value));
                        break;
                    case ID_NUMBER:
                        attributes.setIdNumber(value);
                        break;
                    case TAXPAYER_NAME:
                        attributes.setTaxpayerName(value);
                        break;
                    case DEGREE_NAME:
                        attributes.setDegreeName(value);
                        break;
                    case EMAIL:
                        attributes.setEmail(value);
                        break;
                    case SERIAL_NUMBER:
                        attributes.setSerialNumber(value);
                        break;
                    case PIN:
                        attributes.setPin(value);
                        break;
                    case ISSUING_INSTITUTION:
                        attributes.setIssuingInstitution(value);
                        break;
                    case POSTAL_CODE:
                        attributes.setPostalCode(value);
                        break;
                    case PO_BOX:
                        attributes.setPoBox(value);
                        break;
                    case DATE:
                        attributes.setDate(value);
                        break;
                    case NAME:
                        attributes.setName(value);
                        break;
                    case INVOICE_NUMBER:
                        attributes.setInvoiceNumber(value);
                        break;
                    case INVOICE_DATE:
                        attributes.setInvoiceDate(value);
                        break;
                    case TOTAL_AMOUNT:
                        attributes.setTotalAmount(value);
                        break;
                    case PERMIT_NUMBER:
                        attributes.setPermitNumber(value);
                        break;
                    case EXPIRY_DATE:
                        attributes.setExpiryDate(value);
                        break;
                    default:
                        log.warn("Unhandled attribute: " + attribute);
                        break;
                }
            }

            // Filter out null attributes before returning
            Map<String, String> nonNullAttributes = filterNullAttributes(attributes);
            log.info("Attributes extracted from document: {}", nonNullAttributes);

            Optional<DocumentModel> model = documentRepository.findById(id);
            DocumentModel foundModel = model.get();

            KeyValuePairs keyValuePairsEntity = new KeyValuePairs();
            keyValuePairsEntity.setAttributes(nonNullAttributes);
            keyValuePairsEntity.setDocument(foundModel);
            keyValuePairsRepository.save(keyValuePairsEntity);

            entityResponse.setEntity(nonNullAttributes);
            entityResponse.setMessage("Document attributes extracted successfully.");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            return entityResponse;

        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments provided for extracting document attributes:", e);
            entityResponse.setMessage("selectedAttributes cannot be null or empty");
            entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
            return entityResponse;
        } catch (Exception e) {
            log.error("Error extracting and saving document attributes:", e);
            entityResponse.setMessage("Error extracting");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return entityResponse;
        }
    }

    private Map<String, String> filterNullAttributes(DocumentAttributes attributes) {
        Map<String, String> nonNullAttributes = new HashMap<>();
        Field[] fields = attributes.getClass().getDeclaredFields();

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(attributes);

                if (value != null && !isExcludedField(field.getName())) {
                    nonNullAttributes.put(field.getName(), value.toString());
                }
            } catch (IllegalAccessException e) {
                // Handle exception
                e.printStackTrace();
            }
        }
        return nonNullAttributes;
    }

    private boolean isExcludedField(String fieldName) {
        return fieldName.equalsIgnoreCase("id") || fieldName.equalsIgnoreCase("documentId");
    }
    //defining a new attribute service
//    public Map<DocumentAttribute, Pattern> initializePatterns() {
//        Map<DocumentAttribute, Pattern> attributePatterns = new HashMap<>();
//
//        // Fetch patterns from the repository
//        List<PatternsRegex> patterns = patternRepository.findAll();
//
//        // Convert PatternsRegex objects to Pattern objects and add them to the map
//        for (PatternsRegex pattern : patterns) {
//            String attribute = pattern.getName(); // Assuming attribute is part of PatternsRegex
//            String patternString = pattern.getPattern();
//            Pattern compiledPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
//            attributePatterns.put(attribute, compiledPattern);
//        }
//
//        return attributePatterns;
//    }

    public EntityResponse extractAttributes(MultipartFile file, Long id, List<String> keys) throws IOException {
        EntityResponse entityResponse = new EntityResponse();

        Optional<DocumentModel> model = documentRepository.findById(id);
        if (model.isPresent()) {
            DocumentModel foundModel = model.get();

            // Extract the text from the file
            log.info("Selected attributes: {}", keys);
            String documentContent = paddlePerformOCR(file);

            Map<String, String> keyValuePairs;
//            log.info("Extracted document content: \n{}", documentContent);
            keyValuePairs = extractkeyvaluepairs2(documentContent, keys);
            entityResponse.setEntity(keyValuePairs);

            // Create a new KeyValuePairs entity and save it to the database
            KeyValuePairs keyValuePairsEntity = new KeyValuePairs();
            keyValuePairsEntity.setAttributes(keyValuePairs);
            keyValuePairsEntity.setDocument(foundModel);
            keyValuePairsRepository.save(keyValuePairsEntity);

            log.info("Extracted key-value pairs: {}", keyValuePairs);
        } else {
            entityResponse.setMessage("Document cannot be found");
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        }
        return entityResponse;
    }

    public EntityResponse getKeyValuePairsByDocumentId (Long documentId){
        EntityResponse entityResponse = new EntityResponse<>();
        List<KeyValuePairs> keyValuePairsList = keyValuePairsRepository.findByDocumentId(documentId);

        if(!keyValuePairsList.isEmpty()){
            List<Map<String, String>> attributesList = new ArrayList<>();

            for(KeyValuePairs kvp: keyValuePairsList){
                attributesList.add(kvp.getAttributes());
            }
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Kvps found");
            entityResponse.setEntity(attributesList);
        }else{
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            entityResponse.setMessage("Kvps not found");
        }
        return entityResponse;
    }

    public Map<String, String> extractkeyvaluepairs2(String documentContent, List<String> keys) {

        Map<String, String> keyValuePairs = new HashMap<>();

        // If the key "All" is present, return the entire document content as a single entry
        if (keys.isEmpty() || keys.contains("All")) {
            keyValuePairs.put("All", documentContent);
            return keyValuePairs;
        }else {
            for (String key : keys) {
                String normalizedKey = key.trim();
                Pattern pattern = Pattern.compile(Pattern.quote(normalizedKey) + "\\s*:?\\s*(.*?)(\\n|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                Matcher matcher = pattern.matcher(documentContent);
                if (matcher.find()) {
                    String value = matcher.group(1).trim();
                    keyValuePairs.put(key, value);
                } else {
                    log.warn("No match found for key: {}", key);
                }


            }
            return keyValuePairs;
        }
    }


    private Map<String, String> extractKeyValuePairs(String documentContent) {
        Map<String, String> keyValuePairs = new HashMap<>();

        // Split the document content into lines
        String[] lines = documentContent.split("\\n");

        // Iterate over the lines with the correct indexing to handle pairs
        for (int i = 0; i < lines.length; i += 4) {
            if (i + 2 < lines.length) { // Ensure there are enough lines left for the first pair
                String key1 = lines[i].trim();
                String value1 = lines[i + 2].trim();
                keyValuePairs.put(key1, value1);
            }
            if (i + 3 < lines.length) { // Ensure there are enough lines left for the second pair
                String key2 = lines[i + 1].trim();
                String value2 = lines[i + 3].trim();
                keyValuePairs.put(key2, value2);
            }
        }

        return keyValuePairs;
    }
    public EntityResponse extractAttributes4(MultipartFile file, Long id, Set<String> selectedAttributeNames) throws ParseException {
        EntityResponse entityResponse = new EntityResponse<>();

        if (selectedAttributeNames == null || selectedAttributeNames.isEmpty()) {
            throw new IllegalArgumentException("selectedAttributeNames cannot be null or empty");
        }

        DocumentAttributes attributes = new DocumentAttributes();
        String documentContent = Ocrextract(file, id); // Extract the document content using OCR
        log.info("Extracted document content: " + documentContent);

        try {
            Map<String, PatternsRegex> attributePatternsMap = new HashMap<>();

            // Fetch patterns from the database and store them in the map
            for (String attributeName : selectedAttributeNames) {
                PatternsRegex patternRegex = patternRepository.findByName(attributeName);
                if (patternRegex != null) {
                    attributePatternsMap.put(attributeName, patternRegex);
                    log.info("Pattern for " + attributeName + ": " + patternRegex.getPattern());
                } else {
                    log.warn("Pattern not found for attribute: " + attributeName);
                }
            }

            // Create a new DocumentAttributes object
            for (String attributeName : selectedAttributeNames) {
                PatternsRegex patternRegex = attributePatternsMap.get(attributeName);
                if (patternRegex != null) {
                    Pattern pattern = Pattern.compile(patternRegex.getPattern(), Pattern.CASE_INSENSITIVE);
                    log.info("Using pattern: " + pattern.pattern());
                    String value = extractValue(documentContent, pattern);
                    log.info("Extracted value for " + attributeName + ": " + value);


                }
            }

            // Save the DocumentAttributes object
            save(attributes);

            // Filter out null attributes before returning
            Map<String, String> nonNullAttributes = filterNullAttributes(attributes);
            log.info("Attributes extracted from document: " + nonNullAttributes);
            entityResponse.setEntity(nonNullAttributes);
            entityResponse.setMessage("Document attributes extracted successfully.");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            return entityResponse;
        } catch (Exception e) {
            log.error("Error extracting and saving document attributes:", e);
            throw new RuntimeException("Failed to extract and save document attributes", e);
        }
    }



    public String Ocrextract(MultipartFile file, Long id) {
        try {
            // Check if the uploaded file is a PDF
            if (!file.getContentType().equals("application/pdf")) {
                return "Error: Only PDF files are supported.";
            }

            // Create a temporary directory to store images
            File tempDir = new File(System.getProperty("java.io.tmpdir"), "ocr_images");
            tempDir.mkdirs();

            // Load the PDF document
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                StringBuilder resultBuilder = new StringBuilder();

                // Iterate through each page and convert it to an image
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 300);

                    // Save the image to a temporary file
                    File tempImageFile = new File(tempDir, "page_" + (i + 1) + ".jpg");
                    javax.imageio.ImageIO.write(image, "jpg", tempImageFile);

                    // Perform OCR on the image
                    String result = capture2(tempImageFile);

                    // Append OCR result
                    resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
                }
                saveResult(resultBuilder.toString(), id);
                // Assuming saveResult returns a string
                return resultBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: Error processing PDF file.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: An unexpected error occurred.";
        }
    }

    public String extractContent(MultipartFile file, Long id) {
        try {
            // Check if the uploaded file is a PDF
            if (!file.getContentType().equals("application/pdf")) {
                return "Error: Only PDF files are supported.";
            }

            // Load the PDF document
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                StringBuilder resultBuilder = new StringBuilder();
                Tesseract tesseract = new Tesseract();

                // Iterate through each page and extract text
                IntStream.range(0, document.getNumberOfPages()).parallel().forEach(i -> {
                    try {
                        BufferedImage image = renderer.renderImageWithDPI(i, 300);

                        // Preprocess the image
                        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
                        imdecode(new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3), 1).copyTo(mat);

                        // Convert to grayscale
                        cvtColor(mat, mat, COLOR_BGR2GRAY);

                        // Remove blur
                        GaussianBlur(mat, mat, new Size(5, 5), 0);

                        // Reduce noise with adaptive filtering
                        Mat dest = new Mat();
                        adaptiveThreshold(mat, dest, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);

                        // Convert the processed Mat back to BufferedImage
                        byte[] imageData = new byte[(int) (dest.total() * dest.elemSize())];
                        dest.get(0, 0, imageData);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
                        BufferedImage processedImage = ImageIO.read(byteArrayInputStream);


                        // Perform OCR on the processed image
                        String result = tesseract.doOCR(processedImage);

                        // Append OCR result
                        resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
                    } catch (IOException | TesseractException e) {
                        e.printStackTrace();
                    }
                });

                // Save the result
                saveResult(resultBuilder.toString(), id);
                return resultBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: Error processing PDF file.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: An unexpected error occurred.";
        }
    }

    public String extractContent3(MultipartFile file, Long id) {
        try {
            // Check if the uploaded file is a PDF
            if (!file.getContentType().equals("application/pdf")) {
                return "Error: Only PDF files are supported.";
            }

            // Load the PDF document
            try (PDDocument document = PDDocument.load(file.getInputStream())) {
                PDFRenderer renderer = new PDFRenderer(document);
                StringBuilder resultBuilder = new StringBuilder();
                Tesseract tesseract = new Tesseract();

                // Iterate through each page and extract text
                IntStream.range(0, document.getNumberOfPages()).parallel().forEach(i -> {
                    try {
                        BufferedImage image = renderer.renderImageWithDPI(i, 300);

                        // Preprocess the image
                        Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
                        imdecode(new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3), 1).copyTo(mat);

                        // Convert to grayscale
                        cvtColor(mat, mat, COLOR_BGR2GRAY);

                        // Remove blur
                        GaussianBlur(mat, mat, new Size(5, 5), 0);

                        // Reduce noise with adaptive filtering
                        Mat dest = new Mat();
                        adaptiveThreshold(mat, dest, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 11, 2);

                        // Convert the processed Mat back to BufferedImage
                        byte[] imageData = new byte[(int) (dest.total() * dest.elemSize())];
                        dest.get(0, 0, imageData);
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);
                        BufferedImage processedImage = ImageIO.read(byteArrayInputStream);

                        // Save the processed image
                        String imagePath = saveProcessedImage(processedImage, id, i);

                        // Perform OCR on the processed image
                        String result = tesseract.doOCR(processedImage);

                        // Append OCR result
                        resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
                    } catch (IOException | TesseractException e) {
                        e.printStackTrace();
                    }
                });

                // Save the result
                saveResult(resultBuilder.toString(), id);
                return resultBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: Error processing PDF file.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: An unexpected error occurred.";
        }
    }

    private String saveProcessedImage(BufferedImage image, Long id, int pageIndex) throws IOException {
        String fileName = id + "_page_" + (pageIndex + 1) + "_processed.png";
        String directoryPath = "C:\\Users\\marti\\Desktop\\Preprocesed images";
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        Path imagePath = Paths.get(directoryPath, fileName);
        ImageIO.write(image, "png", imagePath.toFile());
        return imagePath.toString();
    }

    private String extractValue(String documentContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(documentContent);
        return matcher.find() && matcher.groupCount() >= 2 ? matcher.group(2) : "";
    }


    private String parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null; // Handle empty or null input
        }

        // Assuming the expected date format is "MM/dd/yyyy"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        try {
            LocalDate parsedDate = LocalDate.parse(dateStr, formatter);
            return String.valueOf(parsedDate);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date '{}' using format '{}'", dateStr, formatter.toString());
            return null;
        }
    }

    public DocumentAttributes save(DocumentAttributes attributes) {
        try {
            return documentAttributesRepository.save(attributes);
        } catch (DataAccessException e) {
            log.error("Error saving document attributes:", e);
            throw new RuntimeException("Failed to save document attributes", e);
        }
    }

    public List<DocumentAttributes> getAll() {
        return (List<DocumentAttributes>) documentAttributesRepository.findAll();
    }

    public EntityResponse<List<Map<String, Object>>> findByDocumentId(Long documentId) {
        EntityResponse<List<Map<String, Object>>> entityResponse = new EntityResponse<>();

        List<DocumentAttributes> documentAttributes = documentAttributesRepository.findByDocumentId(documentId);

        if (documentAttributes.isEmpty()) {
            entityResponse.setMessage("No document attributes found for document ID: " + documentId);
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        } else {
            entityResponse.setMessage("Document attributes found for document ID: " + documentId);

            // Filter attributes and construct list of maps
            List<Map<String, Object>> filteredAttributesList = new ArrayList<>();
            for (DocumentAttributes attributes : documentAttributes) {
                Map<String, Object> filteredAttributes = filterNonNullAttributes(attributes);
                filteredAttributesList.add(filteredAttributes);
            }
            entityResponse.setEntity(filteredAttributesList);
            entityResponse.setStatusCode(HttpStatus.OK.value());
        }
        return entityResponse;
    }

    private String formatAttributeName(String attributeName) {
        StringBuilder formattedName = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : attributeName.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    formattedName.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    formattedName.append(Character.toLowerCase(c));
                }
            }
        }
        return formattedName.toString();
    }

    private Map<String, Object> filterNonNullAttributes(DocumentAttributes attributes) {
        Map<String, Object> filteredAttributes = new HashMap<>();

        // Iterate through the enum values
        for (DocumentAttribute attribute : DocumentAttribute.values()) {
            // Get the formatted representation of the enum value
            String attributeName = formatAttributeName(attribute.name());
            try {
                // Get the field corresponding to the formatted enum value
                Field field = DocumentAttributes.class.getDeclaredField(attributeName);
                field.setAccessible(true);
                Object value = field.get(attributes);
                if (value != null) {
                    filteredAttributes.put(attributeName, value);
                }
            } catch (NoSuchFieldException e) {
                // Handle the exception, possibly log it
                System.err.println("No such field: " + attributeName);
            } catch (IllegalAccessException e) {
                // Handle the exception, possibly log it
                e.printStackTrace();
            }
        }
        return filteredAttributes;
    }

//    public File getPythonScript() throws IOException {
//        Resource resource = resourceLoader.getResource("classpath:scripts/perform_ocr_on_yolo_detections_from_file.py");
//        return resource.getFile();
//    }
//
    public EntityResponse ocrSelector(MultipartFile file, Long id, List<String> keys) throws IOException {
        EntityResponse entityResponse = new EntityResponse<>();

        String docContent = String.valueOf(performOCR2(file));

        String firstLine = extractFirstLine(docContent);
        log.info("firstline: {}", firstLine);

        if(firstLine.equals("<200 OK OK,For General Tax Questions")){
            return extractAttributestry(file, id, keys);
        }else{
            return extractAttributes(file, id, keys);
        }
    }

    public String paddlePerformOCR(MultipartFile file) throws IOException {
        // Convert MultipartFile to byte array
        byte[] imageBytes = file.getBytes();

        // Set the path to your Python script
        String pythonScriptPath = "D:\\paddle_ocr_code\\perform_ocr_on_yolo_detections_from_file.py"; // Replace with the actual path

        // Construct the command to run the Python script
        String pythonExecutablePath = "D:\\paddleocr_env\\Scripts\\python.exe";
        String[] command = new String[]{pythonExecutablePath, pythonScriptPath};

        // Run the Python script and pass image bytes as input
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        OutputStream outputStream = process.getOutputStream();
        outputStream.write(imageBytes);
        outputStream.close();

        // Read and return the recognized text
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder recognizedText = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            recognizedText.append(line).append(" ").append(System.lineSeparator());
        }
        return recognizedText.toString().lines().skip(3).collect(Collectors.joining("\n"));
    }

    public ResponseEntity<String> performOCR2(MultipartFile file) {
        try {
            BufferedImage image;
            String contentType = file.getContentType();

            if (contentType != null) {
                if (contentType.equals("application/pdf")) {
                    PDDocument document = PDDocument.load(file.getInputStream());
                    PDFRenderer renderer = new PDFRenderer(document);
                    StringBuilder resultBuilder = new StringBuilder();

                    for (int i = 0; i < document.getNumberOfPages(); i++) {
                        image = renderer.renderImageWithDPI(i, 300);
                        String result = performOCR(image);
                        resultBuilder.append(result).append("\n");
                    }

                    document.close();
                    return ResponseEntity.ok(resultBuilder.toString());
                } else if (contentType.startsWith("image")) {
                    image = ImageIO.read(file.getInputStream());
                    String result = performOCR(image);
                    return ResponseEntity.ok(result);
                }
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported file format.");
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
    }

    private String performOCR(BufferedImage image) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
        tesseract.setLanguage("eng");
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz@.,/' [ ]'");
        return tesseract.doOCR(image);
    }

    private String extractFirstLine(String text) {
        if (text == null) {
            return "";
        }
        int indexOfNewLine = text.indexOf('\n');
        return indexOfNewLine != -1 ? text.substring(0, indexOfNewLine) : text;
    }

//    public File getPythonScript() throws IOException {
//        Resource resource = resourceLoader.getResource("classpath:scripts/perform_ocr_on_yolo_detections_from_file.py");
//        return resource.getFile();
//    }
//
//    public String paddlePerformOCR(MultipartFile file) throws IOException {
//        // Convert MultipartFile to byte array
//        byte[] imageBytes = file.getBytes();
//
//        // Get the path to your Python script from the resources folder
//        File pythonScriptFile = getPythonScript();
//        String pythonScriptPath = pythonScriptFile.getAbsolutePath();
//
//        // Construct the command to run the Python script
//        String pythonExecutablePath = "D:\\paddleocr_env\\Scripts\\python.exe";
//        String[] command = new String[]{pythonExecutablePath, pythonScriptPath};
//
//        // Run the Python script and pass image bytes as input
//        ProcessBuilder builder = new ProcessBuilder(command);
//        builder.redirectErrorStream(true);
//        Process process = builder.start();
//
//        try (OutputStream outputStream = process.getOutputStream()) {
//            outputStream.write(imageBytes);
//        }
//
//        // Read and return the recognized text
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            StringBuilder recognizedText = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                recognizedText.append(line).append(System.lineSeparator());
//            }
//            return recognizedText.toString().trim();  // Trim to remove extra newline at the end
//        }
//    }

}

//    public DocumentAttributes extractAttributes(EnumSet<DocumentAttribute> selectedAttributes, Long id) throws ParseException {
////        if (selectedAttributes == null || selectedAttributes.isEmpty()) {
////            throw new IllegalArgumentException("selectedAttributes cannot be null or empty");
////        }
//        DocumentAttributes attributes = new DocumentAttributes();
//
//        try {
//            for (DocumentAttribute attribute : selectedAttributes) {
//                Pattern pattern = attributePatterns.get(attribute);
//                var result=  documentModel.getResult();
//                String value = extractValue( result , pattern);
//                switch (attribute) {
//                    case DocumentAttribute.ISSUE_DATE:
//                        attributes.setIssueDate(parseDate(value));
//                            break;
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

//    private DocumentAttributes filterNullAttributes(DocumentAttributes attributes, EnumSet<DocumentAttribute> selectedAttributes) {
//        DocumentAttributes nonNullAttributes = new DocumentAttributes();
//        Class<?> clazz = attributes.getClass();
//        while (clazz != null) {
//            Field[] fields = clazz.getDeclaredFields();
//            for (Field field : fields) {
//                try {
//                    field.setAccessible(true);
//                    Object value = field.get(attributes);
//                    System.out.println("Filter ;" + attributes);
//                    // Check if the field corresponds to a selected attribute
//                    DocumentAttribute attribute = DocumentAttribute.valueOf(field.getName().toUpperCase());
//                    if (selectedAttributes.contains(attribute)) {
//                        if (value == null || (value instanceof String && ((String) value).isEmpty())) {
//                            // Set null for selected attributes
//                            field.set(nonNullAttributes, null);
//                        } else {
//                            // Set non-null value for selected attributes
//                            field.set(nonNullAttributes, value);
//                        }
//                    }
//                } catch (IllegalAccessException e) {
//                    // Handle exception
//                    e.printStackTrace();
//                }
//            }
//            clazz = clazz.getSuperclass();
//        }
//        return nonNullAttributes;
//    }



//    private DocumentAttributes filterNullAttributes(DocumentAttributes attributes) {
//        DocumentAttributes nonNullAttributes = new DocumentAttributes();
//        Field[] fields = attributes.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            try {
//                field.setAccessible(true);
//                Object value = field.get(attributes);
//                if (value != null) {
//                    // Set non-null value to the corresponding field in nonNullAttributes
//                    System.out.println("Filter ;" + value);
//                    field.set(nonNullAttributes, value);
//                }
//                 else {
//                    // If the value is null, ensure it's set to null in the nonNullAttributes object
//                    field.set(nonNullAttributes, null);
//                }
//
//            } catch (IllegalAccessException e) {
//                // Handle exception
//                e.printStackTrace();
//            }
//        }
//        System.out.println("NoNull :" + nonNullAttributes);
//        return nonNullAttributes;
//    }















