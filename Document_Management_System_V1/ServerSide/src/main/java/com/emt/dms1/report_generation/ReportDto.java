package com.emt.dms1.report_generation;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table
public class ReportDto {

    private  String filename;
    @JsonSerialize(using = MediaTypeSerializer.class)
    @JsonDeserialize(using =MediaTypeDeserializer.class)
    private MediaType filetype;
    private  byte[]   fileData;




}
