import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DocumentService } from 'src/app/services/upload-service.service';
import * as levenshtein from 'fast-levenshtein';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { MatDialog } from '@angular/material/dialog';
import { ShareDialogComponent } from 'src/app/shared/share-dialog/share-dialog.component';
import { RetentionDialogComponent } from 'src/app/shared/retention-dialog/retention-dialog.component';
import { Observable } from 'rxjs';
import { ShareService } from 'src/app/services/share.service';
import { SaveWorkflowsService } from 'src/app/services/save-workflows.service';
import { StartApprovalWorkflowComponent } from '../start-approval-workflow/start-approval-workflow.component';

@Component({
  selector: 'app-edit-metadata',
  templateUrl: './edit-metadata.component.html',
  styleUrls: ['./edit-metadata.component.css'],
})
export class EditMetadataComponent implements OnInit {
  details: string = '';
  keys: string[] = [];
  value: any[] = [];

  detailsList: { id: number; key: string; value: string }[] = [];
  //[x: string]: any;
  fileName: string = '';
  fileSize: number = 0;
  fileType: string = '';
  file: File | null = null;
  notes: string = '';
  dueDate: string = '';
  document2Name: string;
  todayDate: string;
  tagsInput: string = '';
  locationInput: string = '';
  message!: string;
  comments: string = '';
  fileURL: string = '';
  showDocumentViewer: boolean = false;
  savedDocumentId: any;
  updatedValues: any[] = [];
  loading = false;
  showCard: boolean =false;
  showOcrResults:boolean=false
  uploadSuccess:boolean= false;
  saveOnly:boolean=false;
  results: any = {};
  transformedData: any[] = [];
  QRCode: any;
  QRCode_data: string='';
  QRCode_type: string='';
  QRCodeGenerated: boolean= false;

  constructor(
    private sharedService: DocumentService,
    private snackBar: MatSnackBar,
    private service:GetDocumentsService,
    private dialog: MatDialog, 
    public modalService: SaveWorkflowsService
  ) {
    this.document2Name = '';
    const currentDate = new Date();
    this.todayDate = currentDate.toISOString().substring(0, 10);
  }

  ngOnInit() {
    const { file, fileName, fileSize, fileType } =
      this.sharedService.getFileData();
    const { fileURL } = this.sharedService.getFileURL();
    console.log('File Data:', { file, fileName, fileSize, fileType, fileURL });
    this.fileName = fileName;
    this.file = file!;
    this.fileSize = fileSize;
    this.fileType = file!.type;
    this.fileURL = fileURL;
  }

  getFileNameWithoutExtension(fileName: string): string {
    return fileName.split('.').slice(0, -1).join('.');
  }
  
  openShareDialog() {
    this.dialog.open(ShareDialogComponent, {
      maxHeight: '600px',
      maxWidth: '600px'
    } );
  }

 
  
  openWorkflowDialog(){
    this.dialog.open(StartApprovalWorkflowComponent, {
      maxHeight: '80vh', // 80% of the viewport height
      maxWidth: '45vw',  // 45% of the viewport width
    } );
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

  toggleDocumentViewer() {
    this.showDocumentViewer = true;
  }
  public getIconPath(fileName: string): string {
      const extension = fileName.split('.').pop()?.toLowerCase();
      switch (extension) {
         case 'pdf':
           return '../assets/images/icons/pdf-icon.png';
         case 'doc':
         case 'docx':
           return '../assets/images/icons/word-icon.png';
           case 'xls':
         case 'xlsx':
           return '../assets/images/icons/excel-icon.png';
         case 'ppt':
         case 'pptx':
           return '../assets/images/icons/ppt.png';
         case 'jpg':
         case 'jpeg':
         case 'png':
         case 'gif':
           return '../assets/images/icons/img.png';
         default:
           return '../assets/images/icons/default-icon.png';
      }
     }


  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.document2Name = file.name;
    }
  }
  addDetails(event: any) {
    event.preventDefault(); // Prevent form submission
    const inputValue = this.details.trim();
    if (inputValue) {
      // Split the input value by commas, trim each key, and append to the keys array
      const newKeys = inputValue.split(',').map((key) => key.trim());
      this.keys = [...this.keys, ...newKeys];
      this.details = '';
    }
  }

  removeKey(index: number) {
    this.keys.splice(index, 1);
  }

  deleteRow(detail: { id: number; key: string; value: string }) {
    const index = this.detailsList.indexOf(detail);
    if (index > -1) {
      this.detailsList.splice(index, 1);
      // Update IDs after deletion
      this.detailsList = this.detailsList.map((item, newIndex) => ({
        ...item,
        id: newIndex + 1, // Assuming ID starts from 1
      }));
    }
  }

  onDueDateChange(event: any) {
        // Check if the selected date is empty
        if (!event.value) {
          this.dueDate = "";
          return;
      }

    // Format the selected date into YYYY-MM-DD format
    const selectedDate = event.value;
    const year = selectedDate.getFullYear();
    let month = selectedDate.getMonth() + 1; // Months are 0-based in JavaScript
    let day = selectedDate.getDate();

    // Pad the month and day with a leading zero if they are single digits
    month = month < 10 ? '0' + month : month;
    day = day < 10 ? '0' + day : day;

    // Update the dueDate variable
    this.dueDate = `${year}-${month}-${day}`;
    console.log('NewDate:',this.dueDate);
 }

 openPerformOCR() {
  this.showCard =!this.showCard;
  this.saveOnly = !this.saveOnly;
}
closePerformOCR() {
  this.saveOnly= false;
  this.showCard = false; // Show the card when the "Yes" button is clicked
}

//Save document only without perfirming oCR
saveDocumentOnly(): void{
  this.loading=true;
    console.log('save btn clicked');
    if (this.file) {
      const documentInfo = {
        documentName: this.fileName,
        fileSize: this.fileSize,
        fileType: this.fileType,
        file: this.file,
        notes: this.notes,
        tags: this.tagsInput,
        location: this.locationInput,
        dueDate: this.dueDate,
        approverComments: this.comments,
      };
      console.log('Uploading Document........');
      this.sharedService.saveDocument(documentInfo).subscribe({
        next: (res) => {
          console.log('Upload successful:', res);
          const message = res.message;
          
          const saved_file = res.entity;
          this.service.setSelectedDocument(saved_file);
          const status = res.statusCode;
          if(status !== 200){
            this.openSnackBar(message, false);
            this.loading = false;
            
          }else{
            this.openSnackBar(message, true);
            this.loading = false;
            this.uploadSuccess=true;
            
          }
          this.savedDocumentId = saved_file.id;
        },
        error: (error) => {
          this.openSnackBar('Error Saving Document', false);
          this.loading=false
        },
        complete: () => {},
      });
  };
}

  saveDocument(): Promise<void> {
    return new Promise((resolve, reject) => {
      console.log('save btn clicked');
      if (this.file) {
        console.log('Getting doc metaData', this.dueDate);
        const documentInfo = {
          documentName: this.fileName,
          fileSize: this.fileSize,
          fileType: this.fileType,
          file: this.file,
          notes: this.notes,
          tags: this.tagsInput,
          location: this.locationInput,
          dueDate: this.dueDate,
          approverComments: this.comments,
        };
        console.log('Uploading Document........');
        this.sharedService.saveDocument(documentInfo).subscribe({
          next: (res) => {
            console.log('Upload successful:', res);
            const message = res.message;
            
            const saved_file = res.entity;
            this.service.setSelectedDocument(saved_file);
            const status = res.statusCode;
            if(status !== 200){
              this.openSnackBar(message, false);
              this.loading = false;
            
            }else{
              this.openSnackBar(message, true);
              //this.loading = false;
              
            }
            this.savedDocumentId = saved_file.id;
            //sessionStorage.setItem('fileName', saved_file.documentName);
            resolve(); // Resolve the promise when the document is successfully saved
          },
          error: (error) => {
            this.openSnackBar('Error Saving Document', false);
            reject(error); // Reject the promise if there's an error
            this.loading=false
          },
          complete: () => {},
        });
      } else {
        reject(new Error('No file to save')); // Reject the promise if there's no file to save
      }
    });
  }

  async ocrCapture() {
    this.loading=true;
    console.log('capture button clicked');
    console.log('Getting doc OCRData....');

    // Wait for saveDocument to complete
    await this.saveDocument();

    const OcrData = new FormData();
    OcrData.append('file', this.file!);
    OcrData.append('id', this.savedDocumentId);
    OcrData.append(
      'keys',
      this.keys.join(',')
    );
     console.log(OcrData);
    console.log('Starting OCR process');
    this.sharedService.ocrCapture(OcrData).subscribe({
      // Importing Levenshtein distance function

      next: (res) => {
        console.log('OCR Complete:', res);

        if (typeof res.entity !== 'object' || res.entity === null) {
          this.openSnackBar('Invalid Sever Response', false);
          this.loading=false;
          return; // Exit early if the response entity is not valid
        }

        this.results = res.entity;
        this.transformedData = this.transformOCRData(this.results);

        this.openSnackBar(res.message, true);
        this.showOcrResults=true;
        this.loading =false;
        this.uploadSuccess=true;
      },

      error: (error) => {
        this.openSnackBar('OCR failed', false);
        this.loading =false;
      },
      complete: () => {},
    });
  }

  transformOCRData(data: any): any[] {
    const result: any[] = [];
    for (const key in data) {
      if (data.hasOwnProperty(key)) {
        result.push({ key: key, value: data[key] });
      }
    }
    return result;
  }
  generateQRCode(){
    this.service.generateQRCode(this.savedDocumentId).subscribe({
            next: (res) => {
        console.log('Server Response for QR Code:', res);
        const result = res.entity;
        this.QRCode_data= result.qrData;
        this.QRCode_type =result.contentType;
        this.QRCode = 'data:' + this.QRCode_type+ ';base64,' + encodeURIComponent(this.QRCode_data);
        this.openSnackBar(res.message, true);
        this.QRCodeGenerated = true;
       
      },
      error: (error: any) => {
        console.error('Error generating QR Code:', error);
        this.openSnackBar('QR Code failed to generate', false);
      },
      complete:()=>{}
    });
  }
}
