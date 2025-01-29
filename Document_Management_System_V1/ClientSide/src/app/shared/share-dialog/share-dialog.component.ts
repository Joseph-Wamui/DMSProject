import { Component, OnInit } from '@angular/core';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsersService } from 'src/app/services/users.service';
import { MatTableDataSource } from '@angular/material/table';
import { ShareService } from 'src/app/services/share.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { User } from 'src/app/core/models/user';

interface NewUser {
  fullName: string;
  emailAddress: string;
}

@Component({
  selector: 'app-share-dialog',
  templateUrl: './share-dialog.component.html',
  styleUrls: ['./share-dialog.component.css'],
})
export class ShareDialogComponent implements OnInit {
sharedUsers = new MatTableDataSource<NewUser>([]);
  displayedColumns: string[] = ['id', 'name', 'email'];
  users: User[] = [];
  fileName: string = '';
  filteredUsers: User[] = [];
  documentShared: boolean = false;
  searchForm!: FormGroup;
  documentId: any;
  userEmails: string[] =[];

  constructor(
    private fb: FormBuilder,
    private workflowService: GetDocumentsService,
    private userService: UsersService,
    private shareService: ShareService,
    private snackBar: MatSnackBar
  ) {
    this.searchForm = this.fb.group({
      searchQuery: ['', Validators.required],
    });
  }

  ngOnInit(): void {
   
    this.getFileName();
    this.getUsers();
    this.searchForm.get('searchQuery')?.valueChanges.subscribe((value) => {
      this.filterUsers(value);
    });
    this.getUsersSharedWith();
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

  public getFileName() {
    const selectedFile = this.workflowService.getSelectedDocument();
    this.fileName = selectedFile.documentName;
    this.documentId = selectedFile.id;
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

  shareWithUser(): void {
    const selectedUser = this.searchForm.get('searchQuery')?.value;
    const userMail = selectedUser.emailAddress;
    const shareData= new FormData();
    shareData.append('userMail', userMail);
    shareData.append('documentID', this.documentId);
    this.shareService.shareDocument(shareData).subscribe({
      next:(res)=>{
        console.log("Server Response for Share API:", res);
        this.openSnackBar(res.message, true);
      },
      error:(error)=>{
        console.log("Server Error for Share API:", error);
        this.openSnackBar("Error sharing Document", false);
      },
      complete:()=>{
        // Clear the input field after sharing
        this.searchForm.get('searchQuery')?.setValue('');
        this.getUsersSharedWith();
      }

    });
  }
  
  getUsersSharedWith() {
    this.shareService.getUsersSharedWith(this.documentId).subscribe({
      next: (res) => {
        // Assuming the server response entity is an array of strings
        this.userEmails = res.entity;
  
        // Transform the response entity into an array of user objects
        const users = this.userEmails.map((entry: string) => {
          const [emailAddress, fullName] = entry.split(',');
          return { emailAddress, fullName };
        });
  
        // Update the data source for the table
        this.sharedUsers.data = users;
  
        console.log("Server Response for Share API:", res);
        this.openSnackBar(res.message, true);
      },
      error: (error) => {
        console.log("Server Error for Share API:", error);
        this.openSnackBar("Error sharing Document", false);
      },
      complete: () => {}
    });
  }
  
}
