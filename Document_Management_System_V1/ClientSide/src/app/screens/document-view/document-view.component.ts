import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { RetentionDialogComponent } from 'src/app/shared/retention-dialog/retention-dialog.component';

import { ShareDialogComponent } from 'src/app/shared/share-dialog/share-dialog.component';
import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import 'jspdf-autotable';
import { ShareService } from 'src/app/services/share.service';

interface AuditLog {
  timestamp: string;
  userEmail: string;
  documentLogType: string;
}

interface OCRData {}

interface DocumentVersion {
  versionNumber: number;
  documentName: string;
  createdBy: string;
  dateUploaded: Date;

}

@Component({
  selector: 'app-document-view',
  templateUrl: './document-view.component.html',
  styleUrls: ['./document-view.component.css'],
})
export class DocumentViewComponent implements OnInit {
  fileName: string = '';
  filepath: string = '';
  showDocumentViewer: boolean = false;
  fileSize: string = '';
  fileType: string = '';
  fileData: string = '';
  notes: string = '';
  loading = false;
  tags: any;
  createDate: Date | undefined;
  dueDate: Date | undefined;  
  retrievedDoc: any;
  documentId: number = 0;
  logs: any[] = [];
  log: any[] = [];
  OCRData: any[] = [];
  ocrAttribute: any[] = [];
  versionDocuments: any[]=[];
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 10;
  pageIndex = 0;
  ocrDisplayedColumns: string[] = ['id', 'key', 'value'];
  logsDisplayedColumns: string[] = ['id', 'time', 'user', 'action'];
  versionsDisplayedColumns: string[]= ['version_No', 'documentName', 'uploadedBy', 'date'];
  logsDataSource = new MatTableDataSource<AuditLog>([]);
  OcrDataSource = new MatTableDataSource<OCRData>([]);
  versionsDataSource = new MatTableDataSource<DocumentVersion>([])
  zeroData: boolean = false;
  displayAllLogs: boolean = false;
  initialLogCount: number = 4;
  file!: File;
  versionZeroData: boolean= false;
  retrievedVersion:any;
  versionName: string = '';
  QRCode: any;
  QRCode_data: string='';
  QRCode_type: string='';
  QRCodeGenerated: boolean= false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  constructor(
    private dialog: MatDialog,
    private documentService: GetDocumentsService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private shareService:ShareService,
  ) {}

  ngOnInit(): void {
    this.logsDataSource.paginator = this.paginator;
    this.logsDataSource.sort = this.sort;
     this.gedSelectedDocumentById();
     this.getAuditLogs(this.documentId);
     this.getOcrData(this.documentId);
     this.getDocumentVersion();
  }


  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      this.file = input.files[0];
      console.log('Selected file:', this.file);
    }
    this.addNewVersion();
  }
  
  addNewVersion() {
    const versionData: FormData = new FormData();
    versionData.append('file', this.file, this.file.name);
    versionData.append('documentName', this.file.name);
    this.documentService.updateDocumentVersion(this.documentId, versionData).subscribe({
      next:(res)=>{
        console.log("Server Response for Versions:", res);
        this.openSnackBar(res.message, true);
      },
      error:(error)=>{
        console.log("Server Error for Versions", error);
        this.openSnackBar("Error uploading new version", false);
      },
      complete:()=>{
        this.getDocumentVersion();
      }
    });
  }

  getDocumentVersion() {
    this.documentService.getDocumentVersions(this.documentId).subscribe({
      next:(res)=>{
        this.openSnackBar(res.entity.length + " Versions of the Document Found", true);
        if(res.entity.length>0){
          this.versionDocuments = res.entity;
          this.versionsDataSource.data = this.versionDocuments;
          this.versionZeroData = true;
        }else{
          this.versionZeroData = false;
        }
        console.log("Server Response for  Get Version API:", res);
        
      },
      error:(error)=>{
        console.log("Server Error for Versions", error);
        this.openSnackBar("Error Getting Document Versions", true);
      },
      complete:()=>{}
    });
  }

  fetchVersionData(versionNumber: number){
    this.documentService.getDocumentVersionData(this.documentId, versionNumber).subscribe({
      next:(res)=>{
        this.openSnackBar(res.message, true);
        console.log("Server Response for  Get Version DATA API:", res);
        this.versionName = res.entity.documentName;
        this.retrievedVersion= 'data:' + res.entity.fileType + ';base64,' + encodeURIComponent(res.entity.documentData);
        this.toggleDocumentViewer();
      },
      error:(error)=>{
        console.log("Error Getting Version Data", error);
        this.openSnackBar("Error Getting Version Data:", false);
      },
      complete:()=>{ 
      }
    })
  }

  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: panelClass,
    });
  }
  handlePageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updateDisplayedDocuments();
  }

  updateDisplayedDocuments() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    // this.documents = this.allDocuments.slice(startIndex, endIndex);
    this.logs = this.logsDataSource.data.slice(startIndex, endIndex);
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

  public gedSelectedDocumentById() {
    const {
      id,
      documentData,
      documentName,
      fileSize,
      fileType,
      filepath,
      notes,
      tags,
      dueDate,
      createDate,
    } = this.documentService.getSelectedDocument();
    this.documentId = id;
    this.fileName = documentName;
    this.filepath = filepath;
    this.fileSize = fileSize;
    this.fileType = fileType;
    this.fileData = documentData;
    this.notes = notes;
    this.tags = tags;
    this.createDate = createDate;
    this.dueDate = dueDate;
    // Assuming fileData is already a base64 string, no further encoding needed
    this.retrievedDoc =
      'data:' + this.fileType + ';base64,' + encodeURIComponent(this.fileData);
  }

  getAuditLogs(documentId: number) {
    // console.log("Document ID:", documentId);
    this.documentService.getAuditLogs(documentId).subscribe({
      next: (res) => {
        console.log('Server Response for audit Logs:', res);
        if (res.statusCode === 200) {
          console.log('Server Response for audit Logs:', res);
          this.openSnackBar(res.message, true);
          this.logs = res.entity.map((log: { timestamp: any }) => {
            return {
              ...log,
              formattedTimestamp: this.formatTimestamp(log.timestamp),
            };
          });
          this.logs.sort(
            (a, b) =>
              new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
          );
          this.updateDisplayedLogs();
        } else {
          this.openSnackBar('Logs Loading Failed', false);
        }
      },
      error: (error) => {
        console.error('Error loading logs:', error);
        this.openSnackBar('Logs Loading Failed', false);
      },
    });
  }

  applyFilter(filterValue: string) {
    this.logsDataSource.filter = filterValue.trim().toLowerCase();
  }

  formatTimestamp(timestamp: any): string {
    // Convert timestamp to string if it's not already
    if (typeof timestamp !== 'string') {
      timestamp = String(timestamp);
    }

    const parts = timestamp
      .split(',')
      .map((part: string) => parseInt(part, 10));

    // Handle potential invalid timestamp format
    if (parts.length < 7) {
      console.error('Invalid timestamp format:', timestamp);
      return timestamp;
    }

    const date = new Date(
      parts[0],
      parts[1] - 1,
      parts[2],
      parts[3],
      parts[4],
      parts[5],
      Math.floor(parts[6] / 1000000)
    );

    const options: Intl.DateTimeFormatOptions = {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    };

    return new Intl.DateTimeFormat('en-KE', options).format(date);
  }

  updateDisplayedLogs() {
    if (this.displayAllLogs) {
      this.logsDataSource.data = this.logs;
    } else {
      this.logsDataSource.data = this.logs.slice(0, this.initialLogCount);
    }
  }

  toggleDisplayAllLogs() {
    this.displayAllLogs = !this.displayAllLogs;
    this.updateDisplayedLogs();
  }

  getOcrData(documentId: number) {
    //console.log("Document ID:", documentId);
    this.documentService.getOCRData(documentId).subscribe({
      next: (res) => {
        console.log('Server Response for Ocr data:', res);
        if (res.statusCode === 200) {
          console.log('Server Response for OCRData:', res);
          this.openSnackBar('OCR Data Found', true);
          this.OCRData = res.entity;
          this.OcrDataSource.data = this.transformOCRData(this.OCRData);
          this.zeroData = true;
        } else if (res.statusCode === 404) {
          this.openSnackBar('No OCR Data Found', true);
          this.zeroData = false;
        }
      },
      error: (error: any) => {
        console.error('Error loading logs:', error);
        this.openSnackBar('No OCR Data Found', false);
      },
      complete:()=>{}
    });
  }

  transformOCRData(data: any[]): any[] {
    const result: any[] = [];
    data.forEach((item, index) => {
      for (const key in item) {
        if (item.hasOwnProperty(key)) {
          result.push({ key: key, value: item[key] });
        }
      }
    });
    return result;
  }

  openShareDialog() {
    this.dialog.open(ShareDialogComponent);
  }

  openRetentionDialog() {
    this.dialog.open(RetentionDialogComponent);
  }
  SetDoc() {
    const document = this.documentService.getSelectedDocument();
    this.documentService.setSelectedDocument(document);
    console.log(document);
  }
  // Export to Excel
  exportToExcel(): void {
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.logs);
    const workbook: XLSX.WorkBook = {
      Sheets: { data: worksheet },
      SheetNames: ['data'],
    };
    XLSX.writeFile(workbook, 'AuditLogs.xlsx');
  }

  // Export to CSV
  exportToCSV(): void {
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.logs);
    const csvData = XLSX.utils.sheet_to_csv(worksheet);
    const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, 'AuditLogs.csv');
  }

  // Export to JSON
  exportToJSON(): void {
    const jsonData = JSON.stringify(this.logs, null, 2);
    const blob = new Blob([jsonData], {
      type: 'application/json;charset=utf-8;',
    });
    saveAs(blob, 'AuditLogs.json');
  }

  // Export to Text
  exportToText(): void {
    const textData = this.logs.map((log) => JSON.stringify(log)).join('\n');
    const blob = new Blob([textData], { type: 'text/plain;charset=utf-8;' });
    saveAs(blob, 'AuditLogs.txt');
  }

  // Export to PDF
  exportToPDF(): void {
    const doc = new jsPDF();
    const columns = ['No.', 'Time', 'User', 'Action'];
    const rows = this.logs.map((log, index) => [
      index + 1,
      log.formattedTimestamp,
      log.userEmail,
      log.documentLogType,
    ]);

    (doc as any).autoTable({
      head: [columns],
      body: rows,
    });

    doc.save('AuditLogs.pdf');
  }
  generateQRCode(){
    this.documentService.generateQRCode(this.documentId).subscribe({
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
