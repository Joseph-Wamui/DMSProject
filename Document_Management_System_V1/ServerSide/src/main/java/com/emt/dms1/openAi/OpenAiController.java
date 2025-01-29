package com.emt.dms1.openAi;

import com.emt.dms1.utils.EntityResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/OpenAi" ,produces = MediaType.APPLICATION_JSON_VALUE)
public class OpenAiController {

    private final OpenAiService openAiService;

    public OpenAiController(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    @PostMapping(value = "/OcrExtract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse extractAttributes(
            @RequestParam(required = false) List<String> keys,
            @RequestParam(required = false) Long id,
            @RequestPart("file") MultipartFile file) throws IOException {

        // Call the OCR service to extract attributes
        return openAiService.extractAttributes(file, id, keys);
//        return openAiService.performOcr(file, keys);

    }


}