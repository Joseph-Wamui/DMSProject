package com.emt.dms1.report_generation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.http.MediaType;

import java.io.IOException;

public class MediaTypeDeserializer extends JsonDeserializer<MediaType> {
    @Override
    public MediaType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        return MediaType.parseMediaType(jsonParser.getText());
    }
}
