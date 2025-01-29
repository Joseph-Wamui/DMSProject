package com.emt.dms1.openAi;

import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.testOCR.KeyValuePairs;
import com.emt.dms1.testOCR.KeyValuePairsRepository;
import com.emt.dms1.utils.EntityResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OpenAiService {
    private final KeyValuePairsRepository keyValuePairsRepository;

    private final DocumentRepository documentRepository;

    public OpenAiService(KeyValuePairsRepository keyValuePairsRepository, DocumentRepository documentRepository) {
        this.keyValuePairsRepository = keyValuePairsRepository;
        this.documentRepository = documentRepository;
    }

    public EntityResponse performOcr(MultipartFile file, List<String> keys) throws IOException {
        EntityResponse entityResponse = new EntityResponse<>();

        String fileType = file.getContentType();
        Map<String, String> kvps;

        if (fileType != null && fileType.equals("application/pdf")){
            List<File> imageFiles = new ArrayList<>();

            try (PDDocument pdDocument = PDDocument.load(file.getInputStream())) {
                PDFRenderer pdfRenderer = new PDFRenderer(pdDocument);

                for (int page = 0; page < pdDocument.getNumberOfPages(); page++) {
                    BufferedImage bufferedImage = pdfRenderer.renderImage(page);
                    File tempPdfFile = File.createTempFile("temporary_pdf_" + page, ".png");
                    ImageIO.write(bufferedImage, "png", tempPdfFile);
                    imageFiles.add(tempPdfFile);
                }

                // Collect JSON parts
                List<String> jsonParts = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();

                for (File imageFile : imageFiles) {
                    String jsonResponse = extractContent(imageFile, keys);

                    imageFile.delete();

                    JsonNode rootNode = mapper.readTree(jsonResponse);
                    JsonNode choicesNode = rootNode.path("choices").get(0).path("message").path("content");

                    String jsonContent = choicesNode.asText();
                    jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();
                    String innerJsonContent = jsonContent.replaceAll("^\\{|\\}$", "");

                    jsonParts.add(innerJsonContent);
                }

                // Join parts with commas
                String finalContent = "{" + String.join(",", jsonParts) + "}";

                // Pass combined content to kvp matcher
                JsonNode jsonNode = mapper.readTree(finalContent);
                kvps = flattenJson(jsonNode, "");
                Map<String, String> uniqueKvps = removeDuplicateValues(kvps);
                log.info("Kvps: {}", uniqueKvps);

                entityResponse.setMessage("Kvps extracted successfully from pdf");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(uniqueKvps);
            }
        }
        else {
            File temporaryFile = File.createTempFile("temporary_image",".png");
            try(OutputStream outputStream = new FileOutputStream(temporaryFile)) {
                outputStream.write(file.getBytes());
            }

            //have temp image file
            String jsonResponse = extractContent(temporaryFile, keys);

            log.info("Response: {}", jsonResponse);
            temporaryFile.delete();

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(jsonResponse);
            JsonNode choicesNode = rootNode.path("choices").get(0).path("message").path("content");

            String jsonContent = choicesNode.asText();

            jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();

            // Parse the JSON content
            JsonNode jsonNode = mapper.readTree(jsonContent);

            // Flatten the JSON object
            kvps = flattenJson(jsonNode, "");

            entityResponse.setMessage("Kvps extracted successfully from image");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(kvps);

        }

        return entityResponse;
    }

    public Map<String, String> removeDuplicateValues(Map<String, String> inputMap) {
        // Create a map to store the unique values and their associated keys
        Map<String, String> uniqueMap = new LinkedHashMap<>();

        // Create a set to keep track of seen values
        Set<String> seenValues = inputMap.values().stream().collect(Collectors.toSet());

        // Iterate over the original map and add entries with unique values
        for (Map.Entry<String, String> entry : inputMap.entrySet()) {
            if (seenValues.contains(entry.getValue())) {
                // If the value is seen for the first time, add it to the unique map
                if (!uniqueMap.containsValue(entry.getValue())) {
                    uniqueMap.put(entry.getKey(), entry.getValue());
                }
                // Remove the value from the set to prevent future duplicates
                seenValues.remove(entry.getValue());
            }
        }

        return uniqueMap;
    }

    public String performOpenAIImageAnalysis(MultipartFile file, List<String> keys) throws IOException {
        String fileType = file.getContentType();
        List<File> tempFiles = new ArrayList<>();

        if (fileType != null && fileType.equals("application/pdf")) {
            // Process PDF file
            tempFiles = convertPdfToImages(file);
        } else {
            // Process image file directly
            File tempFile = File.createTempFile("uploaded_image", ".png");
            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(file.getBytes());
            }
            tempFiles.add(tempFile);
        }

        StringBuilder aggregatedResponse = new StringBuilder();

        for (File tempFile : tempFiles) {
            String jsonResponse = extractContent(tempFile, keys);
            log.info("Response: {}", jsonResponse);
            aggregatedResponse.append(jsonResponse).append(System.lineSeparator());
            tempFile.delete();
        }
        log.info("Aggregated Response: "+ aggregatedResponse);

//        StringBuilder aggregatedResponse = new StringBuilder();
//        for (File tempFile : tempFiles) {
//            String jsonResponse = callPythonScript(tempFile, keys);
//            log.info("Response: {}", jsonResponse);
//            aggregatedResponse.append(jsonResponse).append(System.lineSeparator());
//            tempFile.delete();
//        }

        return aggregatedResponse.toString().trim();
    }

    private List<File> convertPdfToImages(MultipartFile pdfFile) throws IOException {
        List<File> imageFiles = new ArrayList<>();
        PDDocument document = PDDocument.load(pdfFile.getInputStream());
        PDFRenderer pdfRenderer = new PDFRenderer(document);

        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
            File tempFile = File.createTempFile("uploaded_image_" + page, ".png");
            ImageIO.write(bufferedImage, "png", tempFile);
            imageFiles.add(tempFile);
        }

        document.close();
        return imageFiles;
    }

    private String extractContent(File tempFile, List<String> keys) throws IOException {
        File pythonFile = ResourceUtils.getFile("classpath:scripts/Ocr.py");
        String pythonScriptPath = pythonFile.getAbsolutePath();

        String[] command = new String[]{"python", pythonScriptPath, tempFile.getAbsolutePath(), String.valueOf(keys)};
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.lineSeparator());
            }
            return response.toString().trim();
        }
    }

//    @Cacheable("extractedContent")
//    public String performOpenAIImageAnalysis(MultipartFile file, List<String> keys) throws IOException {
//        // Save the MultipartFile to a temporary file
//        File tempFile = File.createTempFile("uploaded_image", ".png");
//        try (OutputStream os = new FileOutputStream(tempFile)) {
//            os.write(file.getBytes());
//        }
//
//        File pythonFile = ResourceUtils.getFile("classpath:scripts/Ocr.py");
//
//        // Set the path to your Python script
//        String pythonScriptPath = pythonFile.getAbsolutePath();
//
//        // Convert the list of keys to a space-separated string
////        String keysString = String.join(" ", keys);
//
//        // Construct the command to run the Python script
//        String[] command = new String[]{"python", pythonScriptPath, tempFile.getAbsolutePath(), String.valueOf(keys)};
//
//        // Run the Python script
//        ProcessBuilder builder = new ProcessBuilder(command);
//        builder.redirectErrorStream(true);
//        Process process = builder.start();
//
//        // Read and return the response from the Python script
//        String jsonResponse;
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line).append(System.lineSeparator());
//            }
//            jsonResponse = response.toString().trim();
//            log.info("Response: {}", jsonResponse);
//        } finally {
//            // Delete the temporary file
//            tempFile.delete();
//        }
//
//        return jsonResponse;
//    }

    public EntityResponse extractAttributes(MultipartFile file, Long id, List<String> keys) throws IOException {
        EntityResponse entityResponse = new EntityResponse();

        Optional<DocumentModel> model = documentRepository.findById(id);
        if (model.isPresent()) {
            DocumentModel foundModel = model.get();

            // Extract the text from the file
            log.info("Selected keys: {}", keys);
            String documentContent = performOpenAIImageAnalysis(file, keys);

            Map<String, String> keyValuePairs;
            log.info("Extracted document content: \n{}", documentContent);
            keyValuePairs = extractKeyValuePairs(documentContent);

//            Map<String, String> selectedKeyValuePairs;
//            selectedKeyValuePairs = extractSelectedKeyValues(keyValuePairs, keys);
            entityResponse.setEntity(keyValuePairs);
            entityResponse.setMessage("Key Information Extracted Successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());

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

    public Map<String, String> extractSelectedKeyValues(Map<String,String> documentContent, List<String> keys) {

        Map<String, String> keyValuePairs = new HashMap<>();

        if (keys.isEmpty() || keys.contains("All")) {
            keyValuePairs.putAll(documentContent);
            return keyValuePairs;
        }  else {
            for (String key : keys) {
                String normalizedKey = key.trim();
                Pattern pattern = Pattern.compile(Pattern.quote(normalizedKey) + "\\s*:?\\s*(.*?)(\\n|$)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
                for (Map.Entry<String, String> entry : documentContent.entrySet()) {
                    Matcher matcher = pattern.matcher(entry.getValue());
                    if (matcher.find()) {
                        String value = matcher.group(1).trim();
                        keyValuePairs.put(entry.getKey(), value);
                        break; // Stop searching once a match is found
                    }
                }
                if (!keyValuePairs.containsKey(normalizedKey)) {
                    log.warn("No match found for key: {}", normalizedKey);
                }
            }
            return keyValuePairs;
        }
    }

    private Map<String, String> extractKeyValuePairs(String responseJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Parse the response JSON
        JsonNode rootNode = mapper.readTree(responseJson);
        JsonNode choicesNode = rootNode.path("choices").get(0).path("message").path("content");

        // Extract the JSON content from the "content" field
        String jsonContent = choicesNode.asText();

        // Remove any code block delimiters (```) and extra newlines
        jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();

        // Parse the JSON content
        JsonNode jsonNode = mapper.readTree(jsonContent);

        // Flatten the JSON object
        return flattenJson(jsonNode, "");
    }

    private Map<String, String> flattenJson(JsonNode node, String parentKey) {
        Map<String, String> flatMap = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = parentKey.isEmpty() ? field.getKey() : parentKey + "." + field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                flatMap.putAll(flattenJson(value, key));
            } else if (value.isArray()) {
                for (int i = 0; i < value.size(); i++) {
                    flatMap.putAll(flattenJson(value.get(i), key + "[" + i + "]"));
                }
            } else {
                flatMap.put(key, value.asText());
            }
        }
        // Removing unwanted entries
        flatMap.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().contains("<") || entry.getValue().contains(">") || entry.getValue().equals("{") || entry.getValue().equals("}") || entry.getValue().isEmpty());

        return flatMap;
    }

    private Set<Map.Entry<String, String>> flattenJson2(JsonNode node, String parentKey) {
        Set<Map.Entry<String, String>> flatSet = new HashSet<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = parentKey.isEmpty() ? field.getKey() : parentKey + "." + field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                flatSet.addAll(flattenJson2(value, key));
            } else if (value.isArray()) {
                for (int i = 0; i < value.size(); i++) {
                    flatSet.addAll(flattenJson2(value.get(i), key + "[" + i + "]"));
                }
            } else {
                flatSet.add(new AbstractMap.SimpleEntry<>(key, value.asText()));
            }
        }

        // Removing unwanted entries
        flatSet.removeIf(entry -> entry.getValue() == null || entry.getValue().equals("{") || entry.getValue().equals("}") || entry.getValue().isEmpty());

        return flatSet;
    }


}