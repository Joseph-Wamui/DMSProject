import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { DocumentService } from 'src/app/services/upload-service.service';
import { UsersService } from 'src/app/services/users.service';
import { SaveWorkflowsService } from 'src/app/services/save-workflows.service';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { User } from 'src/app/core/models/user';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTableDataSource } from '@angular/material/table';
import { MatDialogRef } from '@angular/material/dialog';
import { ApprovalService } from 'src/app/services/approval.service';

interface Invitee {
  name: string; // Optional name if provided
  email: string; // Email address
}
interface Workflows {
  id: number;
  approvers: string;
  assigner: string;
  name: string;
}



@Component({
  selector: 'app-start-approval-workflow',
  templateUrl: './start-approval-workflow.component.html',
  styleUrls: ['./start-approval-workflow.component.css'],
})
export class StartApprovalWorkflowComponent implements OnInit {
  comment:string = '';
  workflowName: string = '';
  rows: any[] = [];
  isRead: false = false;
  users: any[] = [];
  allUsers: number = 0;
  allRows: number = 0;
  inviteList: Invitee[] = [];
  folders: string[] = [];
  resolution = '';
  isApproved = false;
  destinationFolder = this.folders[0];
  selectedEmails: string[] = [];
  allUsersApproved = false;
  showDropdown = false; // Flag to control dropdown visibility
  userEmails: string[] = [];
  moveAfterWorkflow: boolean = false;
  selectedValue: boolean | null = null;
  loading = false;
  notificationMessage: string = '';
  pageSize = 10;
  pageIndex = 0;
  displayedRows!: any[];
  fileName: string = '';
  selectedOption: string | null = null;
  filteredUsers: User[] = [];
  documentShared: boolean = false;
  workflowForm!: FormGroup;
  sharedUsers = new MatTableDataSource<Invitee>([]);
  displayedColumns: string[] = ['id', 'name', 'email'];
  displayedColumnsworkflows: string[] = ['id', 'creator', 'approvers'];
  isLoading:boolean = false;
  showWorkflows:boolean= false;
  savedWorkflows: string[] = [];
  workflows:string = '';
  WorkflowsDataSource = new MatTableDataSource<Workflows>([]);
  workflowInfo:any[]=[];
  showInput: boolean = false;
  documentName: string = '';
  useSavedWorkflow: boolean = false;
  selectedWorkflow: any =null;

  constructor(
    private approvalService: ApprovalService,
    private saveworkflowsService: SaveWorkflowsService,
    private cdRef: ChangeDetectorRef,
    private documentService: GetDocumentsService,
    private userService: UsersService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<StartApprovalWorkflowComponent>
  ) {
    this.folders = [];
    this.workflowForm = this.fb.group({
      searchQuery: [''],
      workflowComment: [''],
      workflowName:[''],
      isPredefined:[null]
    });
  }

  ngOnInit(): void {
    this.workflowForm.get('searchQuery')?.valueChanges.subscribe(query => {
      this.filterUsers(query);
    });
    this.allRows = this.rows.length;
    this.getUsers();
    this.getSavedWorkflows();
    this.getFileName();
    console.log('Approval workflow component initialized');
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
      panelClass: panelClass,
    });
  }

  toggleFolderDropdown() {
    this.moveAfterWorkflow = !this.moveAfterWorkflow;
  }

 toggleWorkflowsList(): void {
    this.showWorkflows = !this.showWorkflows;
  }

  getSelectedWorkflows() {
    this.approvalService.getSelectedWorkflows().subscribe({
      next: (res) => {
        console.log('Fetching Selected Workflows ', res);
        const message = res.message;
        const status = res.statusCode;
        if (status === 200) {
          this.workflowInfo = res.entity;
          this.WorkflowsDataSource.data = this.workflowInfo;
          console.log('Selected Workflows', this.workflowInfo)
        } else {
          this.openSnackBar(message, false);
        }
      },
      error: (error) => {
        this.openSnackBar('Error occured while Fetching saved Workflow', false);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  getSavedWorkflows() {
    this.documentService.getSavedWorkflows().subscribe({
      next: (res) => {
        console.log('Fetching Saved workflows ', res);
        const message = res.message;
        const status = res.statusCode;
        if (status === 200) {
          this.workflowInfo = res.entity;
          this.WorkflowsDataSource.data = this.workflowInfo;
          console.log('Saved workflows', this.workflowInfo)
        } else {
          this.openSnackBar(message, false);
        }
      },
      error: (error) => {
        this.openSnackBar('Error occured while Fetching saved Workflow', false);
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  onWorkflowTypeChange() {
    const isPredefined = this.workflowForm.get('isPredefined')?.value;
    this.useSavedWorkflow = isPredefined;
    this.showWorkflows = isPredefined === true;
  }

  onWorkflowClick(row: any){
    if(this.selectedWorkflow === row){
      this.selectedWorkflow = null;
    }else{
      this.selectedWorkflow = row; // Select the clicked row.
    }
  console.log('selected workflow:', this.selectedWorkflow)
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

  getUsers(): void {
    this.userService.getUsers().subscribe({
      next: (res) => {
        if (res.statusCode === 200) {
          this.users = res.entity;
          this.filteredUsers = this.users;
          console.log('Users:', this.filteredUsers);
        } else {
          console.error('Failed to fetch users. Status code:', res.statusCode);
        }
      },
      error: (error) => {
        console.log('Error fetching users:', error);
      },
      complete: () => {},
    });
  }

  filterUsers(query: any): void {
    // Cast query to string
    const queryStr = String(query);
  
    if (!queryStr) {
      this.filteredUsers = this.users;
    } else {
      const lowerQuery = queryStr.toLowerCase();
      this.filteredUsers = this.users.filter(
        (user) =>
          user.firstName.toLowerCase().includes(lowerQuery) ||
          user.emailAddress.toLowerCase().includes(lowerQuery)
      );
    }
  }
  


  displayFn(user: User): string {
    return user ? `${user.firstName} (${user.emailAddress})` : '';
  }
  // Function to add the selected user to the table
  addUser() {
    const selectedUser = this.workflowForm.get('searchQuery')?.value;
    const validUser = this.users.find(user => user === selectedUser);

    if (validUser && !this.sharedUsers.data.includes(validUser)) {
      this.sharedUsers.data = [...this.sharedUsers.data, validUser];
      const invitee: Invitee = {
        name: validUser.firstName,
        email: validUser.emailAddress,
      };
      this.inviteList.push(invitee);
    }

    this.workflowForm.get('searchQuery')?.setValue('');
  }
  
  selectValue(value: boolean) {
    this.showInput = value;
    this.selectedValue = value;
    this.selectedOption = value ? 'Yes, Save Workflow!' : 'No, Do not save Workflow!';
    console.log('Selected value:', this.selectedOption);
  }


  removeInvitee(index: number): void {
    if (index > -1) {
      this.inviteList.splice(index, 1);
      this.selectedEmails.splice(index, 1); 
    }
  }

  selectWorkflowType() {
    if (this.useSavedWorkflow === true){
      this.startSavedWorkflow();
    }else{
      this.inviteUser();
    }
  }

  inviteUser() {
    if(this.documentService.getSelectedDocument() === undefined){
      alert('Save document first to continue')
    }
    else{ 
    this.isLoading= true;
    console.log('Button clicked');
    console.log('User emails', this.inviteList);
    this.workflowName = this.workflowForm.get('workflowName')?.value as string;
    this.comment = this.workflowForm.get('workflowComment')?.value as string;
    const userEmails = this.inviteList.map((invitee) => invitee.email);
    const selectedDoc = this.documentService.getSelectedDocument();
    const saveAsPredefinedParam = `${this.selectedValue}`;
    const approvalData = new FormData();
    approvalData.append('documentId', selectedDoc.id);
    approvalData.append('saveAsPredefined',  saveAsPredefinedParam);
    approvalData.append('approverComments ', this.comment);
    approvalData.append('approvers', userEmails.join(','));
    approvalData.append('name' , this.workflowName)
    console.log('Name', this.workflowName);

    this.documentService.startWorkflow(approvalData).subscribe({
      next: (res) => {
        console.log('Workflow started successfully:', res);
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
          
        } else {
          this.openSnackBar(message, true);
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.openSnackBar('Error starting workflow', false);
        this.isLoading = false;
      },
      complete:(()=>{
        this.dialogRef.close(); // Close dialog on success
      })
    });
  }
  }

  startSavedWorkflow() {
    this.isLoading= true;
    this.comment = this.workflowForm.get('workflowComment')?.value as string;
    const selectedDoc = this.documentService.getSelectedDocument();
    const approvalData = new FormData();
    approvalData.append('documentId', selectedDoc.id);
    approvalData.append('workflowId', this.selectedWorkflow.id);
    console.log('documentId', selectedDoc.id);
    console.log('workflowId', this.selectedWorkflow.id);
    approvalData.append('approverComments ', this.comment);


    this.documentService.startDefinedtWorkflow(approvalData).subscribe({
      next: (res) => {
        console.log('Workflow started successfully:', res);
        const message = res.message;
        const status = res.statusCode;
        if (status !== 200) {
          this.openSnackBar(message, false);
          
        } else {
          this.openSnackBar(message, true);
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.openSnackBar('Error starting workflow', false);
        this.isLoading = false;
      },
      complete:(()=>{
        this.dialogRef.close(); // Close dialog on success
      })
    });
  }

}
