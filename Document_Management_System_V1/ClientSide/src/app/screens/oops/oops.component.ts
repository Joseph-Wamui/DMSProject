import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { backupService } from 'src/app/services/backupService';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSelectChange } from '@angular/material/select';

@Component({
  selector: 'app-oops',
  templateUrl: './oops.component.html',
  styleUrls: ['./oops.component.css']
})
export class OopsComponent implements OnInit {


showRetentionPeriod: boolean=false;



  selectedBackupLocation: string = '';
  selectedArchiveLocation: string = '';
  isFileUploadedForBackup: any;
  backupFrequencyUnit: any;
  archivingRetentionPolicy: any;
  archivingFrequencyValue: any;
  archivingFrequencyUnit: any;
  backupFrequencyValue: any;
  backupRetentionPolicy: any;
  backupRetentionPeriod: any;
  retentionPeriodValue: number = 1;
  loadingArchive = false;
  loadingBackup = false;
  showBackedUpTable: boolean = false;
  showArchivedTable: boolean = false;
  backupForm!: FormGroup;
  archivingForm!: FormGroup;
  backupValue: boolean = false;

  // Predefined local locations
  localBackupLocations: string[] = ['C:', 'D:', 'E:'];

  constructor(
    private router: Router,
    private service: backupService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.backupForm = this.fb.group({
      location: ['', Validators.required],
      password: [''],
      userName: [''],
      ipAddress: [''],
      portNo: [],
      dbName: [''],
      isRemote: [null],
    });

    this.archivingForm = this.fb.group({
      archiveLocation: ['', Validators.required],
      archivePassword: [''],
      archiveUserName: [''],
      archiveIpAddress: [''],
      archivePortNo: [],
      archiveDbName: [''],
      isRemoteArchive: [null],
    });

    // Subscribe to value changes of 'isRemote' to reset other form controls when backup type changes
    this.backupForm.get('isRemote')?.valueChanges.subscribe(value => {
      this.resetFormFields('backup');
    });

    this.archivingForm.get('isRemoteArchive')?.valueChanges.subscribe(value => {
      this.resetFormFields('archive');
    });
  }

  resetFormFields(formType: string): void {
    if (formType === 'backup') {
      if (this.backupForm.get('isRemote')?.value === true) {
        this.backupForm.patchValue({
          location: '',
          ipAddress: '192.168.89.245',
          portNo: '3307',
          dbName: 'dmsproject',
          userName: 'root',
          password: 'example_pass'
        });
      } else if (this.backupForm.get('isRemote')?.value === false) {
        this.backupForm.patchValue({
          location: this.localBackupLocations[2], // Default to 'C:'
          ipAddress: '',
          portNo: '',
          dbName: '',
          userName: '',
          password: ''
        });
      } else {
        this.backupForm.reset();
      }
    } else if (formType === 'archive') {
      if (this.archivingForm.get('isRemoteArchive')?.value === true) {
        this.archivingForm.patchValue({
          archiveLocation: '',
          archiveIpAddress: '192.168.89.4',
          archivePortNo: '3307',
          archiveDbName: 'dmsproject',
          archiveUserName: 'Root',
          archivePassword: 'example_pass'
        });
      } else if (this.archivingForm.get('isRemoteArchive')?.value === false) {
        this.archivingForm.patchValue({
          archiveLocation: this.localBackupLocations[0], // Default to 'C:'
          archiveIpAddress: '',
          archivePortNo: '',
          archiveDbName: '',
          archiveUserName: '',
          archivePassword: ''
        });
      } else {
        this.archivingForm.reset();
      }
    }
  }
  showretentionPeriod(){
 
      this.showRetentionPeriod=true;
    
  }

  backupDocuments(): void {
    console.log(this.backupForm.value);
    this.loadingBackup = true;
    const backupData: FormData = new FormData();
    backupData.append('backupLocation', this.backupForm.get('location')?.value);
    backupData.append('ipAddress', this.backupForm.get('ipAddress')?.value);
    backupData.append('port', this.backupForm.get('portNo')?.value);
    backupData.append('dataBaseName', this.backupForm.get('dbName')?.value);
    backupData.append('databaseUsername', this.backupForm.get('userName')?.value);
    backupData.append('databasePassword', this.backupForm.get('password')?.value);
    backupData.append('isRemote', this.backupForm.get('isRemote')?.value);
    this.service.backupNow(backupData).subscribe({
      next: (res) => {
        console.log("Backup Server Response", res);
        const message = res.message;
        const status = res.statusCode;
        this.openSnackBar(message, status === 200);
        this.loadingBackup = false;
      },
      error: () => {
        this.openSnackBar('Error backing up documents', false);
        this.loadingBackup = false;
      }
    });
  }

  archiveDocuments() {
    console.log(this.archivingForm.value);
    this.loadingArchive = true;
    const archiveData: FormData = new FormData();
    archiveData.append('archiveLocation', this.archivingForm.get('archiveLocation')?.value);
    archiveData.append('ipAddress', this.archivingForm.get('archiveIpAddress')?.value);
    archiveData.append('port', this.archivingForm.get('archivePortNo')?.value);
    archiveData.append('dataBaseName', this.archivingForm.get('archiveDbName')?.value);
    archiveData.append('databaseUsername', this.archivingForm.get('archiveUserName')?.value);
    archiveData.append('databasePassword', this.archivingForm.get('archivePassword')?.value);
    archiveData.append('isRemote', this.archivingForm.get('isRemoteArchive')?.value);
    this.service.archiveNow(archiveData).subscribe({
      next: (res) => {
        console.log("archive Server Request", archiveData);
        console.log("archive Server Response", res);
        const message = res.message;
        const status = res.statusCode;
        this.openSnackBar(message, status === 200);
        this.loadingArchive = false;
      },
      error: () => {
        this.openSnackBar('Error arvhiving documents', false);
        this.loadingArchive = false;
      }
    });
  }
  
  restore() {
   this.service.restore().subscribe({
    next: (res) => {
      const message = res.message;
      const status = res.statusCode;
      if (status !== 200) {
        this.openSnackBar(message, false);
      } else {
        this.openSnackBar(message, true);
      }
      this.loadingBackup = false;
    },
    error: () => {
      this.openSnackBar('Error restoring backups ', false);
      this.loadingBackup = false;
    }
  });

   
  }
  submitBackupSettings(): void {
    const frequencyInHours = this.convertToHours(this.backupFrequencyUnit, this.backupFrequencyValue);
    const backupLocation= this.backupForm.get('location')?.value;
    console.log(frequencyInHours, backupLocation);

    this.loadingBackup = true;
    this.service.backupFrequency(backupLocation, frequencyInHours).subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
        } else {
          this.openSnackBar(message, true);
        }
        this.loadingBackup = false;
      },
      error: () => {
        this.openSnackBar('Error creating backup policy', false);
        this.loadingBackup = false;
      }
    });
  }

  submitArchivingSettings(): void {
    const frequencyInDays = this.convertToDays(this.archivingFrequencyUnit, this.archivingFrequencyValue);
    const retentionInDays = this.convertRetentionPolicyToDays(this.archivingRetentionPolicy);
    console.log( frequencyInDays,retentionInDays);

    this.loadingArchive = true;
    this.service.archiving(frequencyInDays).subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
        } else {
          this.openSnackBar(message, true);
        }
        this.loadingArchive = false;
      },
      error: () => {
        this.openSnackBar('Error creating archiving policy', false);
        this.loadingArchive = false;
      }
    });
    this.service.setRetentionPeriod(retentionInDays).subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
        } else {
          this.openSnackBar(message, true);
        }
        this.loadingArchive = false;
      },
      error: () => {
        this.openSnackBar('Error creating archiving policy', false);
        this.loadingArchive = false;
      }
    });
  }

  showArchivedDocuments() {
    this.router.navigate(['/archivedDoc']).catch(err => {
      console.error("Navigation error:", err);
    });
  }

  showBackedUpDocuments() {
    this.router.navigate(['/backuptable']).catch(err => {
      console.error("Navigation error:", err);
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

  checkBackupLocation(value: string): void {
    this.isFileUploadedForBackup = value.trim().length > 0;
  }


  uploadfile(): void {
    console.log('Backup button clicked');
    const location = this.selectedBackupLocation;
    console.log(location);
    this.loadingBackup = true;
    this.service.backupNow(location).subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
        } else {
          this.openSnackBar(message, true);
        }
        this.loadingBackup = false;
      },
      error: () => {
        this.openSnackBar('Error backing up documents', false);
        this.loadingBackup = false;
      }
    });
  }

  convertToDays(unit: string, value: number): number {
    switch (unit) {
      case 'days':
        return value;
      case 'months':
        return value * 30; // Approximation: 1 month = 30 days
      default:
        return value / 24;
    }
  }

  convertToHours(unit: string, value: number): number {
    switch (unit) {
      case 'days':
        return value * 24 * 60;
      case 'months':
        return value * 30 * 24 * 60; // Approximation: 1 month = 30 days
      case 'hours':
        return value * 60;
      default:
        return value;
    }
  }

  convertRetentionPolicyToDays(policy: string): number {
    switch (policy) {
      case '1 Week':
        return 7;
      case '1 Month':
        return 31;
      case '3 Months':
        return 93;
      case '6 Months':
        return 186;
      case '1 Year':
        return 372 ;
      default:
        return 0;
    }
  }
}
