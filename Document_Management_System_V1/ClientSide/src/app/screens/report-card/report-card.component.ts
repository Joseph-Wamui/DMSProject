import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReportsService } from 'src/app/services/reports.service';
import { saveAs } from 'file-saver';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import 'jspdf-autotable';

interface ReportType{
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-report-card',
  templateUrl: './report-card.component.html',
  styleUrls: ['./report-card.component.css'],
})
export class ReportCardComponent {
  @Input() cardId!: string;
  @Input() title!: string;
  @Input() apiEndpoint!: string;
  @Input() url!: string;
  @Input() expanded = false;
  @Input()tableId: string ='';

  @Output() expand = new EventEmitter<string>();
  selectedCard: string = '';
  dateForm: FormGroup;
  loading = false;
  generateComplete = false;
  reportName: string = '';
  reportData: any;
  fileType: string = '';
  retrievedReport: any;
  showReport: boolean = false;
  reportDataArray: any[] = [];
  reportType: ReportType[]=[
    {value: 'default', viewValue: 'System Report'},
    {value: 'custom', viewValue: 'User-Defined Report'}
  ];
  selectedReport = this.reportType[0].value;
  columns: string[]=[];
  columnsFetchComplete:boolean = false;
  cardVisible:boolean =false; 
   isCustomReport: boolean = false; // Flag to toggle card visibility



  constructor(
    private fb: FormBuilder,
    private reportsService: ReportsService,
    private snackBar: MatSnackBar
  ) {
this.dateForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      selectedReport: [this.selectedReport, Validators.required],
      columns: this.fb.array([])
    });

    // Subscribe to changes in the selected report type
    this.dateForm.get('selectedReport')?.valueChanges.subscribe(value => {
      this.selectedReport = value; // Update the selected report when it changes in the form

      if (value === 'custom') {
        this.fetchReportColumns(); // Fetch columns if the custom report is selected
      }
    });
  }

  toggleCard(event: Event) {
    event.stopPropagation();
    this.expanded = !this.expanded;
    this.expand.emit(this.cardId);
  }

  collapseCard(event: Event) {
    event.stopPropagation();
    this.expanded = false;
    this.expand.emit('');
  }
  expandCard(card: string) {
    this.selectedCard = card;
  }

  // collapseCard(card: string) {
  //   this.selectedCard = '';
  // }

  onDateChange(event: any, dateType: string) {
    const formattedDate = this.formatDate(event.value);
    if (dateType === 'start') {
      this.dateForm.controls['startDate'].setValue(formattedDate);
    } else if (dateType === 'end') {
      this.dateForm.controls['endDate'].setValue(formattedDate);
    }
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    let month: any = date.getMonth() + 1; // Months are 0-based in JavaScript
    let day: any = date.getDate();

    month = month < 10 ? '0' + month : month;
    day = day < 10 ? '0' + day : day;

    return `${year}-${month}-${day}`;
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

  fetchReportColumns(): void {
    if (this.reportType[1].value) {
      this.reportsService.getReportColumn(this.tableId).subscribe({
        next: (res) => {
          console.log('Server Response for User-defined Reports:', res);
          if (res.entity.length < 0 || res.statusCode !== 200) {
            this.openSnackBar(res.message, false);
          } else {
            this.columns = res.entity.map((key: string) => key.replace(/_/g, ' '));
            this.openSnackBar(res.message, true);
            this.columnsFetchComplete = true;
  
            // Clear existing controls in the FormArray
            const columnsArray = this.dateForm.get('columns') as FormArray;
            columnsArray.clear();
  
            // Add a new control for each fetched column
            this.columns.forEach(() => {
              columnsArray.push(new FormControl(false));  // Initially unselected
            });
          }
        },
        error: (error) => {
          console.log('Server Error for other Reports:', error);
          this.openSnackBar('Error generating report', false);
        },
        complete: () => {},
      });
    }
  }

    // Method to handle dropdown selection change
    onReportTypeChange(selectedReport: string): void {
      this.isCustomReport = selectedReport === 'custom';
    }

  generateReport(event: Event): void {
    console.log("Selected Report:", this.selectedReport)
    event.stopPropagation();
  
    if (this.selectedReport === 'custom') {
      this.generateCustomReport(event);
      
    } else if (this.selectedReport === 'default') {
      this.generateSystemReport(event);
      this.cardVisible = false;
    }
  }
  
  generateCustomReport(event:Event):void{
    event.stopPropagation();
    this.loading = true;
    const startDate = this.dateForm.controls['startDate'].value;
    const endDate = this.dateForm.controls['endDate'].value;
    const selectedColumns = this.dateForm.value.columns
  .map((checked: any, i: number) => checked ? this.columns[i].replace(/ /g, '_') : null)
  .filter((v: null) => v !== null);
      this.reportsService.generateCustom(this.tableId, selectedColumns,startDate,endDate).subscribe({
        next: (res) => {
          console.log('Server Response for User-defined Reports:', res);
          if (res.entity.length < 0 || res.statusCode !== 200) {
            this.openSnackBar(res.message, false);
          } else {
            this.reportDataArray = res.entity;
            this.openSnackBar(res.message, true);
            this.generateComplete = true;
          }
          this.loading = false;
        },
        error: (error) => {
          console.log('Server Error for other Reports:', error);
          this.openSnackBar('Error generating report', false);
          this.loading = false;
        },
        complete: () => {},
      })
    

  }

  generateSystemReport(event: Event) {
    this.columnsFetchComplete = false;
    event.stopPropagation();
    if (this.dateForm.invalid) {
      this.openSnackBar('Please select valid start and end dates.', false);
      return;
    }

    this.loading = true;
    const startDate = this.dateForm.controls['startDate'].value;
    const endDate = this.dateForm.controls['endDate'].value;

    //console.log('Dates Picked:', startDate, endDate);
    this.reportsService.getOtherReportTypes(this.url).subscribe({
      next: (res) => {
        console.log('Server Response for OTher Reports:', res);
        if (res.entity.length < 0 || res.statusCode !== 200) {
          this.generateComplete = false;
          this.loading = false;
          //this.openSnackBar(res.message, false);
        } else {
          this.reportDataArray = res.entity;
          //this.openSnackBar(res.message, true);
          // this.generateComplete = true;
          // this.loading = false;
        }
      },
      error: (error) => {
        console.log('Server Error for other Reports:', error);
        this.openSnackBar('Error generating report', false);
      },
      complete: () => {},
    });
    this.reportsService
      .getPDFReports(startDate, endDate, this.apiEndpoint)
      .subscribe({
        next: (res) => {
          console.log('Server Response:', res);
          if (res.entity === null || res.statusCode !== 200) {
            this.generateComplete = false;
            this.loading = false;
            this.openSnackBar(res.message, false);
          } else {
            this.reportName = res.entity.filename;
            this.reportData = res.entity.fileData;
            this.fileType = res.entity.filetype;
            this.openSnackBar(res.message, true);
            this.generateComplete = true;
            this.loading = false;
          }
        },
        error: (error) => {
          console.log('Server Error:', error);
          this.openSnackBar('Error generating report', false);
          this.loading = false;
        },
        complete: () => {},
      });
  }

  viewReport(event: Event) {
    event.stopPropagation();
    this.showReport = true;
    this.retrievedReport =
      'data:' +
      this.fileType +
      ';base64,' +
      encodeURIComponent(this.reportData);
    console.log('Document to view:', this.retrievedReport);
  }

  // Export to Excel
  exportToExcel(event: Event): void {
    event.stopPropagation();
    console.log('Report Data Array:', this.reportDataArray); // Log the data to check its format

    // Transform the data if necessary
    const data = this.reportDataArray;

    // Ensure the data is in the correct format
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      this.reportDataArray = [data]; // Convert object to array of objects
    }

    if (
      !Array.isArray(this.reportDataArray) ||
      this.reportDataArray.length === 0
    ) {
      console.error('Invalid data format. Expected an array of objects.');
      this.openSnackBar('Error exporting data: Invalid data format', false);
      return;
    }

    try {
      const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(
        this.reportDataArray
      );
      const workbook: XLSX.WorkBook = {
        Sheets: { data: worksheet },
        SheetNames: ['data'],
      };
      XLSX.writeFile(workbook, this.title + '.xlsx');
      this.openSnackBar('Export successful', true);
    } catch (error) {
      console.error('Error exporting to Excel:', error);
      this.openSnackBar('Error exporting to Excel', false);
    }
  }

  // Export to CSV
  exportToCSV(event: Event): void {
    event.stopPropagation();
    console.log('Report Data Array:', this.reportDataArray); // Log the data to check its format

    // Transform the data if necessary
    const data = this.reportDataArray;

    // Ensure the data is in the correct format
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      this.reportDataArray = [data]; // Convert object to array of objects
    }

    if (
      !Array.isArray(this.reportDataArray) ||
      this.reportDataArray.length === 0
    ) {
      console.error('Invalid data format. Expected an array of objects.');
      this.openSnackBar('Error exporting data: Invalid data format', false);
      return;
    }

    try {
      const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(
        this.reportDataArray
      );
      const csvData = XLSX.utils.sheet_to_csv(worksheet);
      const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' });
      saveAs(blob, this.title + '.csv');
    } catch (error) {
      console.error('Error exporting to CSV:', error);
      this.openSnackBar('Error exporting to CSV', false);
    }
  }

  // Export to JSON
  exportToJSON(event: Event): void {
    event.stopPropagation();
    const jsonData = JSON.stringify(this.reportDataArray, null, 2);
    const blob = new Blob([jsonData], {
      type: 'application/json;charset=utf-8;',
    });
    saveAs(blob, this.title + '.json');
  }

  // Export to Text
  exportToText(event: Event): void {
    event.stopPropagation();
    console.log('Report Data Array:', this.reportDataArray); // Log the data to check its format

    // Transform the data if necessary
    const data = this.reportDataArray;

    // Ensure the data is in the correct format
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      this.reportDataArray = [data]; // Convert object to array of objects
    }

    if (
      !Array.isArray(this.reportDataArray) ||
      this.reportDataArray.length === 0
    ) {
      console.error('Invalid data format. Expected an array of objects.');
      this.openSnackBar('Error exporting data: Invalid data format', false);
      return;
    }

    try {
      const textData = this.reportDataArray
        .map((item) => JSON.stringify(item))
        .join('\n');
      const blob = new Blob([textData], { type: 'text/plain;charset=utf-8;' });
      saveAs(blob, this.title + '.txt');
      this.openSnackBar('Export successful', true);
    } catch (error) {
      console.error('Error exporting to Txt:', error);
      this.openSnackBar('Error exporting to Txt', false);
    }
  }


  exportToPDF(event: Event): void {
    event.stopPropagation();
  
    // // Create a new jsPDF instance
    const doc = new jsPDF();
  
    // // Add the logo
    // const logo = 'assets/images/emt.png'; // Replace with the actual path to your logo
    // doc.addImage(logo, 'PNG', 10, 10, 50, 30);
  
    // // Add the company name and other details
    // doc.setFontSize(16);
    // doc.text('EMTECH HOUSE', 70, 20);
  
    // doc.setFontSize(10);
    // doc.text('www.emtechhouse.co.ke', 70, 25);
    // doc.text('info@emtechhouse.co.ke', 70, 30);
    // doc.text('254710897654', 70, 35);
  
    // // Add the report title and date
    // doc.setFontSize(12);
    // doc.text('DOCUMENT MANAGEMENT SYSTEM', 10, 50);
    // doc.text('DOCUMENT LOGS REPORT', 140, 50);
  
    // const today = new Date().toLocaleDateString('en-US', {
    //   year: 'numeric',
    //   month: 'long',
    //   day: 'numeric',
    // });
    // doc.text(today, 140, 10);  // Adjust position if necessary
  
    // Prepare the headers (keys from the response)
    const headers = Object.keys(this.reportDataArray[0]).map(key => key.replace(/_/g, ' '));
  
    // Prepare the rows (values from the response)
    const rows = this.reportDataArray.map((item: any) => Object.values(item));
  
    // Generate the PDF with autoTable
    (doc as any).autoTable({
      //startY: 60,         // Ensure the table starts after the letterhead
      head: [headers],    // Table headers
      body: rows,         // Table rows
    });
  
    // Save the PDF with the specified title
    doc.save(this.title +'.pdf');
  }
}