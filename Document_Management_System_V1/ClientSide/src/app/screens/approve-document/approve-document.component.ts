import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap'; // Import NgbModal
import { UsersService } from 'src/app/services/users.service';
import { MatTableDataSource } from '@angular/material/table';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';

interface Comments{
  email: string;
  comment: string;
  timestamp: Date;
}
@Component({
  selector: 'app-approve-document',
  templateUrl: './approve-document.component.html',
  styleUrls: ['./approve-document.component.css']
})
export class ApproveDocumentComponent {
  isApproved: boolean = false;
  document: any;
  loading: boolean = false;
  fileName: string = '';
  users: any[] = [];
  selectedUsers: any[] = [];
  Comments: any[] = [];
 CommentDataSource = new MatTableDataSource<Comments>([]);
 DisplayedColumns: string[] = ['id', 'email', 'comments', 'timestamp'];
 comments: string='';
 workflowInfo:any[]=[];
 
 


  constructor(
    private documentService: GetDocumentsService,
    private snackBar: MatSnackBar,
    private userService: UsersService,
    private modalService: NgbModal, // Inject NgbModal
    private userAuthService: UserAuthService
  ) {}

  ngOnInit(): void {
    this.getFileName();
   // this.getUsers();
    this.getWorkflowUsers();
  }

 

  public getFileName() {
    const selectedFile = this.documentService.getSelectedDocument();
    this.fileName = selectedFile.documentName;
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

  approveDocument(): void {
    this.isApproved = true;
    this.sendToNext();
    this.modalService.dismissAll(); // Close the modal
  }

  rejectDocument(): void {
    this.isApproved = false;
    this.sendToNext();
    this.modalService.dismissAll(); // Close the modal
  }
  
  getWorkflowUsers() {
    const selecteddoc = this.documentService.getSelectedDocument();
    const documentId = selecteddoc.id;

    this.documentService.getWorkflowUsers(documentId).subscribe({
      next: (res) => {
        console.log('Approvers in the workflow fetched successfully', res);
        const message = res.message;
        const status = res.statusCode;
        if (status === 200) {
          //this.workflowInfo = res.entity;
          this.workflowInfo = res.entity.map((item: { TimeStamp: any }) => {
            return {
              ...item,
              formattedTimestamp: this.formatTimestamp(item.TimeStamp),
            };
          });
          this.CommentDataSource.data = this.workflowInfo;
        } else {
          this.openSnackBar(message, false);
        }
      },
      error: (error) => {
        this.openSnackBar('Error occured while Fetching Workflow', false);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  transformTableData(data: any[]): any[] {
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
 
  


  sendToNext(): void {
    const selecteddoc = this.documentService.getSelectedDocument();
    const documentid = selecteddoc.id;
    const approved = this.isApproved;

    this.documentService.updateWorkflows(documentid, approved, this.comments).subscribe({
      next: (res) => {
        console.log('Workflow Server Response:', res);
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
        } else {
          this.openSnackBar(message, true);
        }
      },
      error: (error) => {
        this.openSnackBar('Error starting workflow', false);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }
}
