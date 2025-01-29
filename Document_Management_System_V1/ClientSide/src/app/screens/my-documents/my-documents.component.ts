import { Component, OnInit, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { PageEvent } from '@angular/material/paginator';
import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';

import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';
import { DocumentService } from 'src/app/services/upload-service.service';
import { DeleteDialogComponent } from 'src/app/shared/delete-dialog/delete-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from 'src/app/core/models/user';

interface Document {
  documentName: string;
  fileType: string;
  createDate: Date;
  fileSize: number;
  status: string;
}

@Component({
  selector: 'app-my-documents',
  templateUrl: './my-documents.component.html',
  styleUrls: ['./my-documents.component.css'],
})
export class MyDocumentsComponent implements OnInit {
downloadDocument(arg0: any) {
throw new Error('Method not implemented.');
}
  allDocuments: Document[] = [];
  documents: Document[] = [];
  dataSource!: MatTableDataSource<Document>;
  displayedColumns: string[] = ['id', 'documentName', 'fileType', 'createDate', 'fileSize', 'status', 'action'];
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 10;
  pageIndex = 0;
  document: any[] = [];
  documentId: number = 0;
  page: any;
  filteredDocuments: any[] = [];
  isLoading: boolean = false;
  fileSelected: boolean = false;
  searchTerm: string = '';
  user: User = new User();
  email: string = '';
  zeroDocuments: boolean = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private sharedService: DocumentService,
    private userService: UserAuthService,
    private router: Router,
    private route: ActivatedRoute,
    private documentsService: GetDocumentsService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.getCurrentUserEmail();
    this.fetchDocuments();
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

  fetchDocuments(): void {
    this.isLoading = true;
    console.log('fetching documents');
    this.documentsService.getDocuments().subscribe({
      next: (res) => {
        console.log(res);
        if (res.statusCode === 200) {
          this.allDocuments = res.entity.map((doc: { documentName: string; fileSize: number; }) => ({
            ...doc,
            formattedFileType: this.sharedService.determineFileType(doc.documentName),
            formattedFileSize: this.sharedService.formatSizeUnits(doc.fileSize)
          }));
          this.dataSource = new MatTableDataSource<Document>(this.allDocuments);
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
          this.updateDisplayedDocuments();
          this.openSnackBar(res.entity.length + ' Documents Found', true);
        } else {
          this.zeroDocuments = true;
          //this.openSnackBar('The Server Responded with Status Code: ' + res.statusCode, false);
        }
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error fetching documents:', error);
        this.openSnackBar(' An Error occurred while Fetch Documents', false);
        this.isLoading = false;
      },
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
    this.documents = this.dataSource.data.slice(startIndex, endIndex);
  }

  getCurrentUserEmail() {
    this.user = this.userService.getLoggedInUser();
    this.email = this.user.emailAddress;
  }

 public viewDocument(documentId: number){
  console.log('Eye clicked');
  this.isLoading=true;
  const id = documentId;
  console.log('Document ID:', id);
  this.documentsService.getSelectedDocumentById(id).subscribe({
    next: (res)=>{
    console.log("Server Response", res);
    this.document = res.entity;
    this.documentsService.setSelectedDocument(this.document);
    console.log("Document", this.document);
    this.router.navigate(['/document-view']);
    this.isLoading=false;
    },
    error:(error)=>{
      console.log("Error fetching Document", error);
    },
    complete:()=>{}
  }) 
 }

  openDeleteConfirmation(documentId: number, document:any) {
  console.log('Eye clicked');
  
  const id = documentId;
  console.log('Document ID:', id);
  const dialogRef = this.dialog.open(DeleteDialogComponent, {
    data: { documentName: document.documentName } // Pass document name as data to dialog
  });

  dialogRef.afterClosed().subscribe((result) => {
    if (result) {
  this.documentsService.deleteDocument(documentId).subscribe({
    next: (res)=>{
    console.log( res);
    this.openSnackBar('Document deleted Successfully', true);
    this.fetchDocuments();
    },
    error:(error)=>{
     // console.log("Error deleting Document:", error);
      this.openSnackBar('Error deleting Document', false);
    },
    complete:()=>{}
  });
 }});
  
 }

  applyFilter() {
    this.dataSource.filter = this.searchTerm.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }
  
  exportData(type: string): void {
    switch(type) {
      case 'excel': {
        this.exportToExcel();
        break;
      }
      case 'csv': {
        this.exportToCSV();
        break;
      }
      case 'json': {
        this.exportToJSON();
        break;
      }
      case 'text': {
        this.exportToText();
        break;
      }
      default: {
        console.log('Invalid export type.');
      }
    }
  }

  exportToExcel(): void {
    const exportData = this.documents.map(doc => ({
      DocumentName: doc.documentName,
      FileType: doc.fileType,
      CreateDate: doc.createDate,
      FileSize: doc.fileSize,
      Status: doc.status
    }));
    
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Documents');
    
    const filename: string = 'documents.xlsx';
    XLSX.writeFile(wb, filename);
  }

  exportToCSV(): void {
    const header = ['Document Name', 'File Type', 'Create Date', 'File Size', 'Status'];
    const csvData = this.documents.map(doc => [doc.documentName, doc.fileType, doc.createDate, doc.fileSize, doc.status]);
    csvData.unshift(header);
  
    const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(csvData);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Documents');
  
    const filename: string = 'documents.CSV';
    const wbout: ArrayBuffer = XLSX.write(wb, { bookType: 'csv', type: 'array' });
  
    const blob = new Blob([wbout], { type: 'text/csv;charset=utf-8' });
    saveAs(blob, filename);
  }
  

  exportToJSON(): void {
    const exportData = JSON.stringify(this.documents, null, 2);
    const filename: string = 'documents.json';
    const blob = new Blob([exportData], { type: 'application/json' });
    saveAs(blob, filename);
  }

  exportToText(): void {
    const exportData = this.documents.map(doc => 
      `Document Name: ${doc.documentName}, FileType: ${doc.fileType}, Create Date: ${doc.createDate}, File Size: ${doc.fileSize}, Status: ${doc.status}`
    ).join('\n');

    const filename: string = 'documents.txt';
    const blob = new Blob([exportData], { type: 'text/plain;charset=utf-8' });
    saveAs(blob, filename);
  }
}
