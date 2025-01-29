package com.emt.dms1.organizationInfo;

import com.emt.dms1.utils.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/v1/company/", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyDetailsController {
    @Autowired
    private CompanyDetailsService companyDetailsService;
    //@PreAuthorize("hasRole('ADMIN')
    @PostMapping(value="/details", consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse<List<CompanyDetails>> createCompanyDetails(
            @RequestParam(value="organizationName",required = true) String organizationName,
            @RequestParam(value ="postalAddress", required=true) String postalAddress,
            @RequestParam(value = "organizationId",required = false)Long organizationId,
            @RequestParam("logo") MultipartFile logo,
            @RequestParam(value ="phoneNumber", required=true) String phoneNumber,
            @RequestParam(value ="officeLocation", required=true) String officeLocation,
            @RequestParam(value ="country", required=true) String country,
            @RequestParam(value ="emailAddress", required=true) String emailAddress,
            @RequestParam(value ="website_URl", required=true) String website_URL
    )throws Exception{
        return companyDetailsService.uploadDetails(organizationName,postalAddress,organizationId, logo,phoneNumber,officeLocation,country
                ,emailAddress,website_URL);
    }
    @PutMapping(value = "/updating",consumes =MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse<CompanyDetails> updateCompanyDetails(
            @RequestParam(value = "organizationName", required = false) String organizationName,
            @RequestParam(value = "postalAddress", required = false) String postalAddress,
            @RequestParam(value = "organizationId", required = true) String organizationId,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "officeLocation", required = false) String officeLocation,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "emailAddress", required = false) String emailAddress,
            @RequestParam(value = "website_URL", required = false) String website_URL
    ) throws Exception {
        Long parsedLong = Long.parseLong(organizationId);

        return companyDetailsService.updateDetails(parsedLong, organizationName, postalAddress, logo, phoneNumber, officeLocation, country, emailAddress, website_URL);
    }
    @GetMapping("/getCompanyDetails")
    public EntityResponse getComapanyDetails(){
        return companyDetailsService.findComanyDetails();
    }

}

