package com.emt.dms1.testOCR;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service

public class PatternService {

    @Autowired
  PatternRepository repository;

    public Pattern generatePatternFromName(String name) {

        // Add regex boundary markers and match any sequence of characters after the attribute name
        String formattedName = name.replaceAll("[^a-zA-Z0-9]", ""); // Keep only letters and digits
// Add regex boundary markers and match any sequence of characters after the attribute name
        String pattern = "\\b(" + formattedName + ")?\\s*:?\\s*(.*?)\\b";
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

    }

    public void saveAttribute(String name) {
        Pattern pattern = generatePatternFromName(name);
        PatternsRegex attribute = new PatternsRegex();
        attribute.setName(name);
        attribute.setPattern(pattern.pattern()); // Save the pattern string
       repository.save(attribute);
    }


}

