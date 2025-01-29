package com.emt.dms1.organizationInfo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "organization_info")
public class CompanyDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long organizationId;

    public  String organizationName;
   // @Pattern(regexp = "\\d{5}-\\d{5},\\s*[A-Za-z]+(?:[\\sA-Za-z]+)*")
    public String postalAddress;
    public String officeLocation;
    public String country;
    public String emailAddress;
   // @Pattern(regexp ="\\d{12}")
    public String phoneNumber;
    public String website_URL;
    @Lob
    @Column(name = "logo", length = 1000000)
    private byte[]  logoData;

    public String logoType;
    public String logoName;
    @CreatedDate
    @Column(
            nullable = false,
            updatable = false)
    private LocalDate createDate;



}
