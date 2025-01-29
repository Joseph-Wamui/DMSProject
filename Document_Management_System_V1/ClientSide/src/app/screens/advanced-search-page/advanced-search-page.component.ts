import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchService } from '../../services/search.service';
import { Subscription } from 'rxjs';
import { SearchCriteria } from 'src/app/Models/search-criteria.model';
import { FileTypeUtils } from 'src/app/services/file-types-utils.service';
import { SnackbarService } from '../../services/snack-bar.service';
import { SelectionService } from 'src/app/services/selectionservice.service';
import { DocumentNamesService } from 'src/app/services/documents-name.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { DepartmentsService } from 'src/app/services/departments.service';
import { FileTypesService } from 'src/app/services/file-types.service';
import { MatTableDataSource } from '@angular/material/table';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
   selector: 'app-advanced-search-page',
   templateUrl: './advanced-search-page.component.html',
   styleUrls: ['./advanced-search-page.component.css'],
})
export class AdvancedSearchPageComponent implements OnInit, OnDestroy {
   searchCriteria: SearchCriteria = {
      documentName: '',
      createdBy: '',
      startDate: '',
      endDate: '',
      notes: '',
      department: '',
      fileType: '',
      approverComments: '',
      status: ''
   };
   accordionVisible = false;
   searchResults: any[] = [];
   selectedResults: any[] = [];
   private subscription: Subscription = new Subscription();
   viewType: 'list' | 'grid' = 'list';
   showSearchResults = false;
   errorMessage: string = '';
   showSnackbar = false;
   isLoading: boolean = false;
   searchInitiated: boolean = false;
   hoveredResult: any = null;
   documentNames: string[] = [];
   filteredDocumentNames: string[] = [];
   document: any[] = [];
   departments: any[] = [];
   fileTypes: any[] = [];

   sortingOptions = [
      { value: 'createDateAsc', label: 'Create Date (Ascending)' },
      { value: 'createDateDesc', label: 'Create Date (Descending)' },
      { value: 'createdByAsc', label: 'Created By (Ascending)' },
      { value: 'createdByDesc', label: 'Created By (Descending)' },
      { value: 'documentNameAsc', label: 'Document Name (Ascending)' },
      { value: 'documentNameDesc', label: 'Document Name (Descending)' },
   ];
   showDropdown = false;
   
   selectedSortingOption: string = 'createDateAsc'; // Default sorting option
   currentDirection = 'asc'; // Default sorting direction
   combinedResults: any[] = []; // Combined data source for both grid and table views
   allDocuments: any[] = [];
   pageSizeOptions: number[] = [5, 10, 20, 30, 40];
   pageSize = 10;
   pageIndex = 0;
   currentPage: number = 1; // Initialize with 1 as the default value
   displayedColumns: string[] = ['select', 'id', 'documentName', 'fileType', 'createDate', 'fileSize', 'status', 'action'];

   dataSource!: MatTableDataSource<any>;
   fileTypeMap = {
      'application/pdf': 'PDF',
      'text/plain': 'TXT',
      'image/jpeg': 'JPEG',
      'image/png': 'PNG',
      'application/msword': 'DOC',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'DOCX',
   };

   constructor(
      private snackBar: MatSnackBar,
      private searchService: SearchService,
      private selectionService: SelectionService,
      private route: ActivatedRoute,
      private documentNamesService: DocumentNamesService,
      private documentsService: GetDocumentsService,
      private router: Router,
      private departmentsService: DepartmentsService,
      private fileTypesService: FileTypesService,
      private snackbarService: SnackbarService,
   ) {
      this.fetchDocumentNames();
   }

   ngOnInit(): void {
      this.departmentsService.getDepartments().subscribe({
         next: (res) => {
            console.log("Server Response for Departments:", res);
            this.departments = res.entity;
         },
         error: (error) => {
            console.log(error);
         }
      });

      this.fileTypesService.getFileTypes().subscribe({
         next: (res) => {
            console.log("File Types:", res);
            this.fileTypes = res.entity;
         },
         error: (error) => {
            console.error('Error fetching file types:', error);
         }
      });

      this.subscription.add(
         this.snackbarService.getSnackbarEvent().subscribe(({ message }) => {
            console.log('New message received:', message);
            this.errorMessage = message;
            this.showSnackbar = !!message;
            console.log('showSnackbar set to:', this.showSnackbar);
         })
      );
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

   onSubmit(): void {
      if (this.accordionVisible) {
         this.toggleAccordion();
      }

      if (!this.hasAtLeastOneCriteria()) {
         console.warn('Please enter at least one search criterion to refine your search.');
         this.openSnackBar('Please enter at least one search criterion.', false);
         return;
      }

      console.log('Search Criteria:', this.searchCriteria);

      this.searchInitiated = true;
      this.isLoading = true;
      this.searchResults = [];

      this.searchService.searchDocuments(this.searchCriteria).subscribe({
         next:(res: any) => {
            console.log('Response:', res);
            this.isLoading = false;
            this.searchInitiated = true;
            this.searchResults = res.entity;
            this.updateDataSource();
            this.sortSearchResults();

            if (res.length === 0) {
               this.openSnackBar('No documents found matching your search criteria.', false);
            } else {
               this.showSearchResults = true;
               this.errorMessage = '';
               this.showSnackbar = false;
            }
         },
         error:(error) => {
            this.isLoading = false;
            console.error('Error fetching search results:', error);
            this.openSnackBar('An error occurred while searching. Please try again later.', false);
         },
         complete:()=>{}
    } );
   }

   updateDataSource(): void {
      const startIndex = this.pageIndex * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      this.dataSource = new MatTableDataSource(this.searchResults.slice(startIndex, endIndex));
    }
    

   hasAtLeastOneCriteria(): boolean {
      return Object.values(this.searchCriteria).some(value => typeof value === 'string' && value.trim() !== '');
   }

   toggleAccordion() {
      this.accordionVisible = !this.accordionVisible;
   }

   toggleViewType(type: 'list' | 'grid') {
      this.viewType = type;
   }

   toggleGridSelection(result: any): void {
      console.log("ToggleGridSelection Called")
      const isSelected = this.selectionService.isDocumentSelected(result.id);
      if (isSelected) {
         this.selectionService.deselectDocument(result.id);
      } else {
         this.selectionService.selectDocument(result.id);
      }
   }

   toggleTableSelection(result: any, event: Event): void {
      const target = event.target as HTMLElement;
      if (!target) {
         return;
      }

      const isSelected = this.selectionService.isDocumentSelected(result.id);
      if (isSelected) {
         this.selectionService.deselectDocument(result.id);
      } else {
         this.selectionService.selectDocument(result.id);
      }
   }

   isDocumentSelected(id: string): boolean {
      return this.selectionService.isDocumentSelected(id);
   }

   isHovered(result: any): boolean {
      return this.hoveredResult === result;
   }

   ngOnDestroy(): void {
      this.subscription.unsubscribe();
   }

   fetchDocumentNames(): void {
      this.documentNamesService.getDocumentNames().subscribe({
         next: (res) => {
            this.documentNames = res.entity;
            this.filteredDocumentNames = res.entity;
            console.log('Document names fetched successfully:', this.documentNames);
         },
         error: (error) => {
            if (error.status === 200) {
               console.log('Request was successful, but an error occurred:', error);
            } else {
               console.error('Error fetching document names:', error);
            }
         }
      });
   }

   onDocumentNameChange(value: string): void {
      this.filterDocumentNames(value);
   }

   filterDocumentNames(query: string): void {
      this.filteredDocumentNames = this.documentNames.filter(name =>
         name.toLowerCase().includes(query.toLowerCase())
      );
   }

   onDocumentNameSelected(event: any): void {
      this.searchCriteria.documentName = event.option.value;
   }

   getSimplifiedFileType(fileType: string): string {
      return FileTypeUtils.getDocumentIcon(fileType);
   }

   
   onSnackbarClose() {
      console.log('Snackbar close event received');
      this.errorMessage = '';
      this.showSnackbar = false;
   }

   resetSearchCriteria(): void {
      this.searchCriteria = {
         documentName: '',
         createdBy: '',
         startDate: '',
         endDate: '',
         notes: '',
         department: '',
         fileType: '',
         approverComments: ''
      };
      this.searchResults = []
   }

   sortSearchResults() {
      this.searchResults.sort((a, b) => {
         switch (this.selectedSortingOption) {
            case 'createDateAsc':
            case 'createDateDesc':
               return this.currentDirection === 'desc'
                  ? new Date(b.createDate).getTime() - new Date(a.createDate).getTime()
                  : new Date(a.createDate).getTime() - new Date(b.createDate).getTime();
            case 'createdByAsc':
            case 'createdByDesc':
               return this.currentDirection === 'desc'
                  ? b.createdBy.localeCompare(a.createdBy)
                  : a.createdBy.localeCompare(b.createdBy);
            case 'documentNameAsc':
            case 'documentNameDesc':
               return this.currentDirection === 'desc'
                  ? b.documentName.localeCompare(a.documentName)
                  : a.documentName.localeCompare(b.documentName);
            default:
               return 0;
         }
      });

      // Reverse the array if the direction is descending
      if (this.currentDirection === 'desc') {
         this.searchResults.reverse();
      }

      // Update the data source to reflect the sorted search results
      this.updateDataSource();
   }

   handleSortOption(option: any) {
      this.selectedSortingOption = option.value;
      this.currentDirection = option.value.endsWith('Asc') ? 'asc' : 'desc';
      this.sortSearchResults();
   }

   toggleSortDirection() {
      this.currentDirection = this.currentDirection === 'asc' ? 'desc' : 'asc';
      this.sortSearchResults();
   }

   onMouseEnterSortingIcon() {
      console.log('Mouse entered sorting icon');
      this.showDropdown = true;
   }

   onMouseLeaveSortingIcon() {
      console.log('Mouse left sorting icon');
      this.showDropdown = false;
   }

   toggleSortingDropdown() {
      console.log('Sorting dropdown toggled');
      this.showDropdown = !this.showDropdown;
   }

   public viewDocument(id: number) {
      console.log('Document clicked');
      console.log('Document ID:', id);
      this.documentsService.getSelectedDocumentById(id).subscribe({
         next: (res) => {
            console.log("Document", res);
            this.document = res.entity;
            this.documentsService.setSelectedDocument(this.document);
            console.log("Document", this.document);
            this.router.navigate(['/document-view']);
         },
         error: (error) => {
            console.log("Error fetching Document", error);
         }
      })
   }

   getHumanReadableFileType(mimeType: string): string {
      return this.fileTypeMap[mimeType as keyof typeof this.fileTypeMap] || mimeType;
   }

   getHumanReadableFileSize(sizeInBytes: number): string {
      if (sizeInBytes >= 1073741824) {
         return (sizeInBytes / 1073741824).toFixed(2) + ' GB';
      } else if (sizeInBytes >= 1048576) {
         return (sizeInBytes / 1048576).toFixed(2) + ' MB';
      } else if (sizeInBytes >= 1024) {
         return (sizeInBytes / 1024).toFixed(2) + ' KB';
      } else {
         return sizeInBytes + ' Bytes';
      }
   }
   // Export methods
  

 exportToJson(): void {
   const jsonData = JSON.stringify(this.searchResults, null, 2);
   const blob = new Blob([jsonData], { type: 'application/json' });
   saveAs(blob, 'documents.json');
 }

 xportToCSV(): void {
   const header = ['Document Name', 'File Type', 'Create Date', 'File Size', 'Status'];
   const csvData = this.searchResults.map(doc => [doc.documentName, doc.fileType, doc.createDate, doc.fileSize, doc.status]);
   csvData.unshift(header);
 
   const ws: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(csvData);
   const wb: XLSX.WorkBook = XLSX.utils.book_new();
   XLSX.utils.book_append_sheet(wb, ws, 'Documents');
 
   const filename: string = 'documents.CSV';
   const wbout: ArrayBuffer = XLSX.write(wb, { bookType: 'csv', type: 'array' });
 
   const blob = new Blob([wbout], { type: 'text/csv;charset=utf-8' });
   saveAs(blob, filename);
 }
 

 exportToText(): void {
   const textContent = this.searchResults.map(item => item.documentName).join('\n');
   const blob = new Blob([textContent], { type: 'text/plain' });
   saveAs(blob, 'documents.txt');
 }

 exportToExcel(): void {
   const exportData = this.searchResults.map(doc => ({
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
 onPageChange(event: any): void {
   this.pageSize = event.pageSize;
   this.pageIndex = event.pageIndex;
   this.updateDataSource();
 }

 navigateToDocumentView(): void {
    this.router.navigate(['/document-view']);
  }
 
}