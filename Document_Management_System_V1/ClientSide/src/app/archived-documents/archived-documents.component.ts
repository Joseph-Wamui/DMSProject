import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { backupService } from 'src/app/services/backupService';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PageEvent } from '@angular/material/paginator';

interface Document {
  id: number;
  documentName: string;
  fileType: string;
  department: string;
  createdBy: string;
  archiveDate: string;
}

@Component({
  selector: 'app-archived-documents',
  templateUrl: './archived-documents.component.html',
  styleUrls: ['./archived-documents.component.css']
})
export class ArchivedDocumentsComponent implements OnInit, AfterViewInit {
  searchTerm: string = '';
  loading = true;
  archivedDisplayedColumns: string[] = ['id', 'documentName', 'fileType', 'department', 'createdBy', 'archiveDate'];
  archivedDataSource = new MatTableDataSource<Document>();
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 10;
  pageIndex = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private service: backupService, private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.getArchivedDocuments();
  }

  ngAfterViewInit(): void {
    this.archivedDataSource.paginator = this.paginator;
  }

  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 2000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: panelClass
    });
  }

  getArchivedDocuments(): void {
    this.loading = true;
    this.service.getArchivedDocuments().subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
          this.loading = false;
        } else {
          this.openSnackBar(message, true);
          this.loading = false;
          this.archivedDataSource.data = res.entity;
        }
      },
      error: () => {
        this.openSnackBar('Error fetching documents', false);
        this.loading = false;
      }
    });
  }

  handlePageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
  }

  applyArchivedFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.archivedDataSource.filterPredicate = (data: Document, filter: string) => {
      return data.documentName.toLowerCase().includes(filter)
        || data.fileType.toLowerCase().includes(filter)
        || data.archiveDate.toLowerCase().includes(filter)
        || data.department.toLowerCase().includes(filter)
        || data.createdBy.toLowerCase().includes(filter);
    };
    this.archivedDataSource.filter = filterValue;
  }

  exportToCSV(): void {
    console.log('exportToCSV called'); // Debugging line
    const csvData = this.archivedDataSource.filteredData.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Archived On': doc.archiveDate
    }));

    const headers = Object.keys(csvData[0]);
    const csvRows = [
      headers.join(','), 
      ...csvData.map(row => headers.map(header => (row as Record<string, any>)[header]).join(','))
    ];

    const csvContent = csvRows.join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });

    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.setAttribute('download', 'archived_documents.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  exportToExcel(): void {
    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.archivedDataSource.filteredData.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Archived On': doc.archiveDate
    })));
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Archived Documents');
    XLSX.writeFile(wb, 'archived_documents.xlsx');
  }

  exportToJSON(): void {
    const jsonBlob = new Blob([JSON.stringify(this.archivedDataSource.filteredData.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Archived On': doc.archiveDate
    })), null, 2)], { type: 'application/json' });
    saveAs(jsonBlob, 'archived_documents.json');
  }

  exportToText(): void {
    const textBlob = new Blob([this.convertToText(this.archivedDataSource.filteredData)], { type: 'text/plain' });
    saveAs(textBlob, 'archived_documents.txt');
  }

  private convertToText(data: Document[]): string {
    return data.map(doc => 
      `ID: ${doc.id}, Document Name: ${doc.documentName}, File Type: ${doc.fileType}, Department: ${doc.department}, Created By: ${doc.createdBy}, Archived On: ${doc.archiveDate}`
    ).join('\n');
  }
}
