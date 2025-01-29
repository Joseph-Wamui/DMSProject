package com.emt.dms1.organizationInfo;

import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CompanyDetailsService {
    @Autowired
    private CompanyDetailsRepo companyDetailsRepo;

    public EntityResponse<List<CompanyDetails>> uploadDetails(String organizationName, String physicalAddress, Long organizationId, MultipartFile logo,
                                                              String phoneNumber, String officeLocation, String country,
                                                              String emailAddress, String websiteURL) throws Exception {

        EntityResponse entityResponse = new EntityResponse<>();

        try {
            List<CompanyDetails> existingCompany = companyDetailsRepo.findByOrganizationName(organizationName);
            if (!existingCompany.isEmpty()) {
                entityResponse.setMessage("Organization with this name exist");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            // Check if the logo file is empty
            if (logo == null || logo.isEmpty()) {
                entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
                entityResponse.setMessage("Logo file is required and was not uploaded.");
                entityResponse.setEntity(null);
                return entityResponse;
            }

            // Proceed with the creation of CompanyDetails
            CompanyDetails companyDetails = new CompanyDetails();

            companyDetails.setOrganizationName(organizationName);
            companyDetails.setPostalAddress(physicalAddress);
            companyDetails.setLogoData(logo.getBytes());
            companyDetails.setLogoType(logo.getContentType());
            companyDetails.setLogoName(logo.getOriginalFilename());
            companyDetails.setPhoneNumber(phoneNumber);
            companyDetails.setOfficeLocation(officeLocation);
            companyDetails.setCountry(country);
            companyDetails.setEmailAddress(emailAddress);
            companyDetails.setWebsite_URL(websiteURL);
           // companyDetails.setPostalCode(postalCode);
            companyDetails.setCreateDate(LocalDate.now());
            companyDetailsRepo.save(companyDetails);

            List<CompanyDetails> companyDetailsList = new ArrayList<>();
            companyDetailsList.add(companyDetails);

            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Company details uploaded successfully.");
            // entityResponse.setEntity(exists);
            entityResponse.setEntity(companyDetailsList);


        } catch (Exception e) {
            entityResponse.setMessage("Error occurred while  saving Company Details");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(e.getMessage());

            e.printStackTrace();
        }
        return entityResponse;
    }


    public EntityResponse<CompanyDetails> updateDetails(Long organizationId, String organizationName, String postalAddress, MultipartFile logo, String phoneNumber, String officeLocation, String country, String emailAddress, String websiteUrl) {
        EntityResponse<CompanyDetails> entityResponse = new EntityResponse<>();
        try {
            // Check if organization ID exists
            Long parsedLong = Long.parseLong(String.valueOf(organizationId));
            Optional<CompanyDetails> optionalCompany = companyDetailsRepo.findById(parsedLong);

            if (optionalCompany.isPresent()) {
                // Get the existing company details
                CompanyDetails companyDetails = optionalCompany.get();

                // Update the company details with the provided values
                if (organizationName != null) companyDetails.setOrganizationName(organizationName);
                if (postalAddress != null) companyDetails.setPostalAddress(postalAddress);
                if (logo != null && !logo.isEmpty()) companyDetails.setLogoData(logo.getBytes());
                if (logo != null && !logo.isEmpty()) companyDetails.setLogoType(logo.getContentType());
                if (logo != null && !logo.isEmpty()) companyDetails.setLogoName(logo.getOriginalFilename());
                if (phoneNumber != null) companyDetails.setPhoneNumber(phoneNumber);
                if (officeLocation != null) companyDetails.setOfficeLocation(officeLocation);
                if (country != null) companyDetails.setCountry(country);
                if (emailAddress != null) companyDetails.setEmailAddress(emailAddress);
                if (websiteUrl != null) companyDetails.setWebsite_URL(websiteUrl);

                // Save the updated company details to the database
                companyDetailsRepo.save(companyDetails);

                // Set response for successful update
                entityResponse.setMessage("Company details updated successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(companyDetails);
            } else {
                // Set response if the company is not found
                entityResponse.setMessage("Company Details not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set response for any errors encountered during the update
            entityResponse.setMessage("Error occurred while updating Company Details");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }


    public EntityResponse findComanyDetails() {
        EntityResponse entityResponse=new EntityResponse<>();
        try {
            List<CompanyDetails> companyDetailsList = companyDetailsRepo.findAll();
            if (!companyDetailsList.isEmpty()) {
                entityResponse.setMessage("Company Details Retrieved Successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(companyDetailsList);
            } else {
                entityResponse.setMessage("Company Details Not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        }catch (Exception e){
            e.printStackTrace();
            entityResponse.setMessage("Error Occurred while retrieving company details");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }
}
