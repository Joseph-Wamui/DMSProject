package com.emt.dms1.report_generation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.MediaType;

import java.io.IOException;

public class MediaTypeSerializer extends JsonSerializer<MediaType> {
    @Override
    public void serialize(MediaType mediaType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(mediaType.toString());
    }
}

