import { Component, OnInit } from "@angular/core";
import { UserAuthService } from "src/app/user-auth/_service/user-auth.service";
import { Router } from '@angular/router';
import { GetDocumentsService } from "src/app/services/get-documents.service";
import { User } from "src/app/core/models/user";
import { MatTableDataSource } from "@angular/material/table";

interface RecentDocument {
  createdBy: string;
  id: number;
  documentName: string;
  fileType: string;
  createDate: string;
  status: string;
}

@Component({

    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],

})

export class DashboardComponent implements OnInit{
  user:User = new User;
    firstName: string='';
    allDocumentStatus: any;
    approvedCount: number = 0;
    rejectedCount: number = 0;
    pendingCount: number = 0;
    totalCount: number =0;
    sum: number =0;
    email: string ='';
  selectedType: string='';
  pendingProgressValue: number =0;
  approvedProgressValue: number = 0;
  rejectedProgressValue: number = 0;
  recentDataSource = new MatTableDataSource<RecentDocument>([]);
  recentDisplayedColumns: string[]= ['documentId', 'documentName', 'uploadedBy', 'date'];
  recentDocuments: RecentDocument[] = [];
  recentDocument: RecentDocument[] = [];
  zeroDocuments: boolean = false;
  isLoading: boolean = false;
  documentId: number = 0;

    constructor(
        private router: Router,
        private userService: UserAuthService,
        private documentService:GetDocumentsService
    ){}

    ngOnInit(): void {
    this.getDocumentStatus();
    this.getCurrentUser();
    this.getRecentDocuments();
    }
    public getCurrentUser(){
      this.user = this.userService.getLoggedInUser();
      this.firstName= this.user.firstName;
    }

  getDocumentStatus(){
      this.documentService.getDocmentStatus().subscribe({
        next: (res) => {
          console.log("DocumentStatus Server Response:", res);
          if (res.statusCode === 200) {
            this.allDocumentStatus = res.entity;
            this.pendingCount=this.allDocumentStatus.Pending;
            this.approvedCount = this.allDocumentStatus.Approved;
            this.rejectedCount= this.allDocumentStatus.Rejected;
            this.totalCount = this.allDocumentStatus.Total;
            this.sum = this.pendingCount + this.approvedCount + this.rejectedCount;
            console.log("Sum of  All Documents", this.sum);
            if (this.sum > 0) {
              // Calculate progress values as percentages
              this.pendingProgressValue = (this.pendingCount / this.sum) * 100;
              this.approvedProgressValue = (this.approvedCount / this.sum) * 100;
              this.rejectedProgressValue = (this.rejectedCount / this.sum) * 100;
          } else {
              // Handle case where total count is 0 to avoid division by zero
              this.pendingProgressValue = 0;
              this.approvedProgressValue = 0;
              this.rejectedProgressValue = 0;
          }
         }

        },
        error: (error) => {
          console.error('Error fetching documents:', error);

        },
      })
  }

  getRecentDocuments() {
    this.documentService.getRecentDocuments().subscribe({
      next: (res) => {
        console.log('Fetching Recent Documents:', res);
        if (res.statusCode === 200) {
          console.log('Server Response for Recent Documents:', res);
          this.recentDocuments = res.entity;
          this.recentDataSource.data = this.recentDocuments;
          console.log('Recent Documents: ', this.recentDocuments)
         
        } else {
          this.zeroDocuments = true;
        }
      },
      error: (error) => {
        console.error('Error loading recentDocuments:', error);
      },
      complete:()=>{}
    });
  }

  public viewDocument(documentId: number){
    console.log('Selected Id:', documentId);
    this.isLoading=true;
    const id = documentId;
    console.log('Document ID:', id);
    this.documentService.getSelectedDocumentById(id).subscribe({
      next: (res)=>{
      console.log("Server Response", res);
      this.recentDocuments = res.entity;
      this.documentService.setSelectedDocument(this.recentDocuments);
      console.log("Document", this.recentDocuments);
      this.router.navigate(['/document-view']);
      this.isLoading=false;
      },
      error:(error)=>{
        console.log("Error fetching Document", error);
      },
      complete:()=>{}
    }) 
   }

  updateStatusList(allDocumentStatus: (string | null)[]) {
    // Reset counts
    this.approvedCount = 0;
    this.rejectedCount = 0;
    this.pendingCount = 0;

    // Iterate through each element in the array
    allDocumentStatus.forEach(status => {
        // Check if the status is not null
        if (status !== null) {
            // Increment counts based on status
            switch (status) {
                case "APPROVED":
                    this.approvedCount++;
                    break;
                case "REJECTED":
                    this.rejectedCount++;
                    break;
                case "PENDING":
                    this.pendingCount++;
                    break;
                default:
                    break;
            }
        }
    });

    // Sort the array (excluding null elements)
    const sortedStatus: (string | null)[] = allDocumentStatus.filter(status => status !== null).sort();

    console.log("Sorted Status:", sortedStatus);
    console.log("Approved Count:", this.approvedCount);
    console.log("Rejected Count:", this.rejectedCount);
    console.log("Pending Count:", this.pendingCount);
}

navigateToWorkflowProgress(type: string) {
  this.documentService.setSelectedType(type); // Update selected type
  console.log('Type: ',type);
        this.router.navigate(['/workflow-progress'])
      }

}
