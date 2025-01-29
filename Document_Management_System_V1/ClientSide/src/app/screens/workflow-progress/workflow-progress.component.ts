import { Component, ViewChild } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { DocumentService } from 'src/app/services/upload-service.service';
import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-workflow-progress',
  templateUrl: './workflow-progress.component.html',
  styleUrls: ['./workflow-progress.component.css']
})
export class WorkflowProgressComponent {

  documents: any[] = [];
  isLoading: boolean = false;
  allDocuments: any[] = [];
  filteredDocuments: any[] = [];
  dataSource!: MatTableDataSource<Document>;
  displayedColumns: string[] = ['id','documentName', 'filetype', 'startedOn', 'currentApproverEmail', 'progressPercentage'];
  pageSizeOptions: number[] = [5, 10, 20];
  searchTerm: string = '';
  pageSize = 10;
  pageIndex = 0;

 // private subscription: Subscription = new Subscription();
  page: any;
  selectedType: any;
  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private service: GetDocumentsService,
    private sharedService: DocumentService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.getWorkflowProgress();
  }

  handlePageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updateDisplayedDocuments();
  }

  updateDisplayedDocuments() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.documents = this.filteredDocuments.slice(startIndex, endIndex);
  }

  applyFilter() {
  this.dataSource.filter = this.searchTerm.trim().toLowerCase();
  if (this.dataSource.paginator) {
    this.dataSource.paginator.firstPage();
  }
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
  
  getWorkflowProgress() {
    console.log('fetch workflows');
    this.service.getWorkflows().subscribe({
      next: (res) => {
        if (res.statusCode === 200) {
          
          this.allDocuments = res.entity.map((doc: { documentName: string; }) => ({
            ...doc,
            formattedFileType: this.sharedService.determineFileType(doc.documentName)
          }));
          this.filterDocumentsByType();
          this.dataSource = new MatTableDataSource<Document>(this.documents);
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
          this.updateDisplayedDocuments();
        } else {
          console.error('fetching documents has failed');
        }
      },
      error: (error) => {
        console.log('Error fetching documents:', error);
        this.isLoading = false;
      },
      complete: () => {}
    });
  }
  
  filterDocumentsByType() {
    // Get the selected type from the service
    this.selectedType = this.service.getSelectedType();
    console.log(this.selectedType)
  
    switch (this.selectedType) {
      case 'PENDING':
        this.documents = this.allDocuments.filter(document => document.Status === 'PENDING');
        break;
      case 'APPROVED':
        this.documents = this.allDocuments.filter(document => document.Status === 'APPROVED');
        break;
      case 'REJECTED':
        this.documents = this.allDocuments.filter(document => document.Status === 'REJECTED');
        break;
   
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
  const dataSource = this.dataSource;
  const displayedColumns = this.displayedColumns;
  
  const exportData = dataSource.filteredData.map(row => {
    const exportRow: any = {};
    displayedColumns.forEach(column => {
      exportRow[column] = (row as any)[column]; 
    });
    return exportRow;
  });
    
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