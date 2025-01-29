package com.emt.dms1.testOCR;//package com.example.demo.TestOCR;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Arrays;
//import java.util.EnumSet;
//import java.util.List;
//
//@RestController
//@Slf4j
//@RequestMapping("/documents")
//public class DocumentAttributesController {
//
////    @Autowired
////    private DocumentAttributesService documentAttributesService;
////
////    // Array of all available DocumentAttribute values (extracted from enum)
////    private final String[] allAttributesArray = Arrays.stream(DocumentAttribute.values())
////            .map(Enum::name)
////            .toArray(String[]::new);
////
////
////    @PostMapping(value = "/extract", produces = "application/json")
////    public ResponseEntity<DocumentAttributes> extractAttributes(
////            @RequestParam(required = false) List<String> selectedAttributes, OcrService result) {
////        try {
////            EnumSet<DocumentAttribute> attributesSet = EnumSet.noneOf(DocumentAttribute.class);
////
////            // Validate and convert selected attributes with autocomplete support
////            if (selectedAttributes != null) {
////                for (String attribute : selectedAttributes) {
////                    if (attribute != null && !attribute.isEmpty() &&
////                            Arrays.stream(allAttributesArray).anyMatch(attr -> attr.equalsIgnoreCase(attribute))) {
////                        attributesSet.add(DocumentAttribute.valueOf(attribute.toUpperCase()));
////                    } else {
////                        // Handle invalid attribute (e.g., log warning or return bad request)
////                        log.warn("Invalid attribute provided: " + attribute);
////                        return ResponseEntity.badRequest().build();
////                    }
////                }
////            }
////
////            DocumentAttributes attributes = documentAttributesService.extractAttributes(result, attributesSet);
////            return ResponseEntity.ok(attributes);
////        } catch (IllegalArgumentException e) {
////            log.error("Invalid arguments provided for extracting document attributes:", e);
////            return ResponseEntity.badRequest().build();
////        } catch (Exception e) { // Catch broad exception for unexpected errors
////            log.error("Error extracting document attributes:", e);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
////        }
////    }
////
////    @GetMapping
////    public ResponseEntity<List<DocumentAttributes>> getAll() {
////        try {
////            List<DocumentAttributes> allAttributes = documentAttributesService.getAll();
////            return ResponseEntity.ok(allAttributes);
////        } catch (Exception e) {
////            log.error("Error retrieving all document attributes:", e);
////            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
////        }
////    }
////}
