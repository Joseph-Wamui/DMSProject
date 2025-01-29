import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { StateService } from 'src/app/services/state.service';
import { DocumentService } from 'src/app/services/upload-service.service';

interface FileRow {
  fileName: string;
  fileType: string;
  fileSize: string;
  file: File | null;
}

@Component({
  selector: 'app-upload-page',
  templateUrl: './upload-page.component.html',
  styleUrls: ['./upload-page.component.css'],
})
export class UploadPageComponent implements OnInit,  OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef;

  rows: FileRow[] = [];
  allRows: number = 0;
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 5;
  pageIndex = 0;
  fileSelected: boolean = false;
  displayedRows: any[] = [];
  formatSizeUnits: any;
  multiple: boolean = false;
  file: File |undefined;
  index: number =0;
  files: File[]=[];
  isLoading: boolean= false;
  // displayedColumns: string[] = ['id', 'fileName', 'fileType', 'fileSize', 'selectFile', 'download', 'actions'];
  //  columnTitles: {[key: string]: string} = {
  //    id: 'Id',
  //    fileName: 'File Name',
  //    fileType: 'File Type',
  //    fileSize: 'File Size',
  //    selectFile: 'Select File',
  //    download: 'Download',
  //    actions: 'Actions'
  //  };
   state: any;

  constructor(
    private router: Router,
    private sharedService: DocumentService,
    private snackBar: MatSnackBar,
    private stateService: StateService,
  ) {}

  

  ngOnInit() {
    this.state = this.stateService.getState('UploadPageComponentState') || { /* initial state */ };
  }

  ngOnDestroy() {
    //this.stateService.setState('UploadPageComponentState', this.state);
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

  handlePageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updateDisplayedRows();
  }

  updateDisplayedRows() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.displayedRows = this.rows.slice(startIndex, endIndex);
  }

  triggerFileInput() {
    this.fileInput.nativeElement.click();
  }

  onMultipleFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.files= Array.from(input.files);
      console.log(this.files);
      this.files.forEach((file) => {
        this.addRow(file);
        this.fileSelected = true;
      });
    }
    this.multiple = true;
    this.stateService.setState('UploadPageComponentState', this.state);
  }

  uploadMultipleDocuments(){
    this.isLoading=true;
    console.log("Uploading Documents:", this.files); 
    if(this.files.length>0){
      this.sharedService.uploadMultipleDocuments(this.files).subscribe({
        next:(res)=>{
         console.log('ServerResponse:', res); 
         this.openSnackBar(res.message, true);
         this.isLoading= false;
        },
        error:(error)=>{
          console.log('Error Uploading Documents:', error);
          this.openSnackBar('Error Uploading Documents:', false);
          this.isLoading= false;
        },
        complete:()=>{}
      })
    }
  }

  addRow(file: File | null = null): void {
    const newRow: FileRow = {
      fileName: file ? file.name : '',
      fileType: file ? this.sharedService.determineFileType(file.name) : '',
      fileSize: file ? this.sharedService.formatSizeUnits(file.size) : '',
      file: file,
    };
    this.rows.push(newRow);
    this.allRows = this.rows.length;
    this.updateDisplayedRows();
    this.stateService.setState('UploadPageComponentState', this.state);
  }

  removeRow(index: number) {
    // Find the actual index in the 'rows' array based on the current page
    const actualIndex = this.pageIndex * this.pageSize + index;
    if (actualIndex < this.rows.length) {
      this.rows.splice(actualIndex, 1);
      this.allRows = this.rows.length; // Update the total count of rows
      this.updateDisplayedRows(); 
      this.stateService.setState('UploadPageComponentState', this.state);// Update the displayed rows based on the new total count
    }
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value.toLowerCase();
    this.displayedRows = this.rows.filter(
      (row) =>
        row.fileName.toLowerCase().includes(filterValue) ||
        row.fileType.toLowerCase().includes(filterValue)
    );

  }

  selectFile(event: any, index: number) {
    this.file = event.target.files.item(0);
    if (this.file) {
      // Update the file name and file type
      this.rows[index].fileName = this.file.name;

      // Extract the file extension and update the file type
      // const fileExtension = file.name.split('.').pop();
      this.rows[index].fileType = this.sharedService.determineFileType(
        this.file.name
      );
      this.rows[index].fileSize = this.sharedService.formatSizeUnits(this.file.size);
      // Update the file property with the File object
      this.rows[index].file = this.file;
      // Set the fileSelected flag to true
      this.fileSelected = true;
      this.stateService.setState('UploadPageComponentState', this.state);

    } else {
      // If no file is selected, set the fileSelected flag to false
      this.fileSelected = false;
    }
  }


  navigateToEditMetadata(index: number): void {
    const fileRow = this.displayedRows[index];
    if (fileRow && fileRow.file) {
      const fileURL = URL.createObjectURL(fileRow.file);
      this.sharedService.setFileURL(fileURL);
      this.sharedService.setFileData(fileRow.file);
      this.router.navigate(['/edit-metadata']);
      this.stateService.setState('UploadPageComponentState', this.state);
    } else {
      this.openSnackBar('Please select a file before proceeding', false);
    }
    
  }

  downloadFile(index: number) {
    // Implement file download logic here
  }
  
}
