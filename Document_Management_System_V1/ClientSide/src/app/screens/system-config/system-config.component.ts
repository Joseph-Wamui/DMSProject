import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Company } from 'src/app/Models/company.model';
import { CompanyService } from 'src/app/services/company.service';

@Component({
  selector: 'app-system-config',
  templateUrl: './system-config.component.html',
  styleUrls: ['./system-config.component.css']
})
export class SystemConfigComponent implements OnInit {
  companyForm!: FormGroup;
  file!: File;
  company!: Company;
  loading: boolean = true;

  constructor(
    private fb: FormBuilder,
    private companyService: CompanyService,
    private snackBar: MatSnackBar,
  ) { }

  ngOnInit(): void {
    this.getCompanyInfo();
    this.companyForm = this.fb.group({
      organizationName: ['', Validators.required],
      postalAddress: ['', Validators.required],
      phoneNumber: ['', Validators.required],
      officeLocation: ['', Validators.required],
      country: ['', Validators.required],
      emailAddress: ['', [Validators.required, Validators.email]],
      website_URL: ['', [Validators.required, Validators.pattern('(https?:\/\/)?(www\.)?[a-z0-9\-]+\.[a-z]{2,}(\.[a-z]{2,})?\/?.*')]],
      logo: [null, Validators.required],
      organizationId: []
    });
  }

  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: panelClass
    });
  }

  getCompanyInfo(): void {
    this.companyService.fetchOrganizationInfo().subscribe({
      next: (res) => {
        console.log("Server response get for company Info:", res);
        this.company = res.entity;
        this.companyService.setcompanyInfo(this.company);
        console.log("Organization Set:", this.company);

        if (this.company) {
          const result = Array.isArray(this.company) ? this.company[0] : this.company;
          const logo = 'data:' + result.logoType + ';base64,' + encodeURIComponent(result.logoData);
          const imgElement = document.getElementById('logoImage') as HTMLImageElement;

          // Set the src attribute of the image to the base64 encoded string
          if (imgElement) {
              imgElement.src = logo;
          }

          this.companyForm.patchValue({
            organizationName: result.organizationName,
            postalAddress: result.postalAddress,
            phoneNumber: result.phoneNumber,
            officeLocation: result.officeLocation,
            country: result.country,
            emailAddress: result.emailAddress,
            website_URL: result.website_URL,
            organizationId: result.organizationId,
          });

          this.companyForm.get('logo')?.clearValidators();
          this.companyForm.get('logo')?.updateValueAndValidity();

          console.log(" New Company Form:", result);
        }
      },
      error: (error) => {
        console.log("Error for get company Info:", error);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.companyForm.valid) {
      const companyData: FormData = new FormData();
      companyData.append('organizationName', this.companyForm.get('organizationName')?.value);
      companyData.append('postalAddress', this.companyForm.get('postalAddress')?.value);
      companyData.append('phoneNumber', this.companyForm.get('phoneNumber')?.value);
      companyData.append('officeLocation', this.companyForm.get('officeLocation')?.value);
      companyData.append('country', this.companyForm.get('country')?.value);
      companyData.append('emailAddress', this.companyForm.get('emailAddress')?.value);
      companyData.append('website_URL', this.companyForm.get('website_URL')?.value);

      if (this.file) {
        companyData.append('logo', this.file, this.file.name);
      }

      console.log(this.companyForm.value);
      this.companyService.postOrganizationInfo(companyData).subscribe({
        next: (res) => {
          console.log("Server response for post company Info:", res);
          this.openSnackBar(res.message, true);
        },
        error: (error) => {
          console.log("Error for post company Info:", error);
          this.openSnackBar("Error saving Company Info", false);
        },
        complete: () => {
          this.getCompanyInfo();
        }
      });
    }
  }

  updateCompanyInfo(): void {
    const organizationId = this.companyForm.get('organizationId')?.value;
    const companyData: FormData = new FormData();
    companyData.append('organizationName', this.companyForm.get('organizationName')?.value);
    companyData.append('postalAddress', this.companyForm.get('postalAddress')?.value);
    companyData.append('phoneNumber', this.companyForm.get('phoneNumber')?.value);
    companyData.append('officeLocation', this.companyForm.get('officeLocation')?.value);
    companyData.append('country', this.companyForm.get('country')?.value);
    companyData.append('emailAddress', this.companyForm.get('emailAddress')?.value);
    companyData.append('website_URL', this.companyForm.get('website_URL')?.value);
    //companyData.append('organizationId', this.companyForm.get('organizationId')?.value);


    if (this.file) {
      companyData.append('logo', this.file, this.file.name);
    }

    this.companyService.updateOrganizationInfo(companyData, organizationId).subscribe({
      next: (res) => {
        console.log("Server response for post company Info:", res);
        this.openSnackBar(res.message, true);
      },
      error: (error) => {
        console.log("Error for post company Info:", error);
        this.openSnackBar("Error saving Company Info", false);
      },
      complete: () => {
        this.getCompanyInfo();
      }
    });
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;

    if (input?.files && input.files.length > 0) {
      this.file = input.files[0];
      this.companyForm.patchValue({
        logo: this.file
      });

      // Ensure `logo` control exists before updating its value and validity
      this.companyForm.get('logo')!.updateValueAndValidity();
    }
  }
}
