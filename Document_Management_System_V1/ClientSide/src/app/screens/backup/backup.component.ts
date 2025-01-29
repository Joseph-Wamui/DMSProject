import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';
import { backupService } from 'src/app/services/backupService';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

interface Document {
  id: number;
  documentName: string;
  fileType: string;
  department: string;
  createdBy: string;
  backUpDate: string;
}

@Component({
  selector: 'app-backup',
  templateUrl: './backup.component.html',
  styleUrls: ['./backup.component.css']
})
export class BackupComponent implements OnInit, AfterViewInit {

  searchTerm: string = '';
  loading = true;
  allDocuments: Document[] = [];
  displayedColumns: string[] = ['id', 'documentName', 'fileType', 'department', 'createdBy', 'backUpDate'];
  dataSource = new MatTableDataSource<Document>();
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 10;
  pageIndex = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private service: backupService, private snackBar: MatSnackBar) { }

  ngOnInit(): void {
    this.getArchivedDocuments();
  }

  ngAfterViewInit(): void {
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
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

  getArchivedDocuments(): void {
    this.loading = true;
    this.service.getBackUp().subscribe({
      next: (res) => {
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
          this.loading = false;
        } else {
          this.openSnackBar(message, true);
          this.loading = false;
          this.allDocuments = res.entity;
          this.dataSource.data = this.allDocuments;
        }
      },
      error: (error) => {
        this.openSnackBar('Error backing up documents', false);
        this.loading = false;
      }
    });
  }

  handlePageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
  }

  applyBackedUpFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value.trim().toLowerCase();
    this.dataSource.filterPredicate = (data: Document, filter: string) => {
      return data.documentName.toLowerCase().includes(filter)
        || data.fileType.toLowerCase().includes(filter);
    };
    this.dataSource.filter = filterValue;
  }

  exportToExcel(): void {
    const dataToExport = this.dataSource.data.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Backup Date': doc.backUpDate
    }));

    const ws: XLSX.WorkSheet = XLSX.utils.json_to_sheet(dataToExport);
    const wb: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Documents');
    XLSX.writeFile(wb, 'documents.xlsx');
  }

  exportToCSV(): void {
    const csvData = this.dataSource.data.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Backup Date': doc.backUpDate
    }));

    const headers = Object.keys(csvData[0]) as Array<keyof typeof csvData[0]>;

    const csvRows = [
      headers.join(','),
      ...csvData.map(row =>
        headers.map(header =>
          JSON.stringify(row[header], (key, value) => value ?? '')
            .replace(/\"/g, '')
        ).join(',')
      )
    ];

    const blob = new Blob([csvRows.join('\n')], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, 'documents.csv');
  }

  exportToJSON(): void {
    const jsonBlob = new Blob([JSON.stringify(this.dataSource.data.map(doc => ({
      'ID': doc.id,
      'Document Name': doc.documentName,
      'File Type': doc.fileType,
      'Department': doc.department,
      'Created By': doc.createdBy,
      'Backup Date': doc.backUpDate
    }), null, ))], { type: 'application/json' });
    saveAs(jsonBlob, 'documents.json');
  }

  exportToText(): void {
    const textBlob = new Blob([this.convertToText(this.dataSource.data)], { type: 'text/plain' });
    saveAs(textBlob, 'documents.txt');
  }

  private convertToText(data: Document[]): string {
    return data.map(doc => `ID: ${doc.id}, Document Name: ${doc.documentName}, File Type: ${doc.fileType}, Department: ${doc.department}, Created By: ${doc.createdBy}, Backup Date: ${doc.backUpDate}`).join('\n');
  }
}
