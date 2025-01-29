import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { Router } from '@angular/router';
import { User } from 'src/app/core/models/user';
import { UsersService } from 'src/app/services/users.service';
import { MatDialog } from '@angular/material/dialog';
import { formatDate } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { DeleteDialogComponent } from 'src/app/shared/delete-dialog/delete-dialog.component';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';



@Component({
  selector: 'app-user-management',
  templateUrl: './userlist.component.html',
  styleUrls: ['./userlist.component.css']
})
export class UserlistComponent implements OnInit {
  user: User = new User;
  dataSource!: MatTableDataSource<User>;
  users: User[] = [];
  allUsers: User[] = []; // Store the full list of users
  filteredUsers: User[] = [];
  pageSizeOptions: number[] = [5, 10, 20];
  pageSize = 5;
  pageIndex = 0;
  userId: number | undefined;
  firstName: string | undefined;
  lastName: string | undefined;
  employeeNumber: any;
  emailAddress: any;
  phoneNumber: any;
  department: any;
  isLoading: boolean = false;
  displayedColumns: string[] = ['id', 'fullName', 'employeeNumber', 'emailAddress', 'phoneNumber', 'department', 'createdOn', 'role', 'action'];
  searchTerm: string = '';
  departmentSearchTerm: string = '';
  phoneNumberSearchTerm: string = '';
  employeeNumberSearchTerm: string = '';
  emailSearchTerm: string = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private userService: UsersService,
    private userSelectionService: UsersService,
    private router: Router,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.getUsers();

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


  handlePageChange(event: PageEvent) {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.updateDisplayedUsers();
  }

  updateDisplayedUsers() {
    const startIndex = this.pageIndex * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.users = this.filteredUsers.slice(startIndex, endIndex);
  }

  getUsers(): void {
    this.isLoading = true;
    this.userService.getUsers().subscribe({
      next: ((res) => {
        console.log('Server Response for Users:', res);
        if (res.statusCode === 200) {
          this.allUsers = res.entity.map((user: { firstName: string; lastName: string; createdOn: string; }) => ({
            ...user,
            fullName: user.firstName + ' ' + user.lastName,
            // Parse the date string and format it
            createdOn: formatDate(new Date(user.createdOn), 'dd/MM/yyyy', 'en-US')
          }));
          this.filteredUsers = this.allUsers;
          this.dataSource = new MatTableDataSource<User>(this.filteredUsers);
          this.dataSource.paginator = this.paginator;
          this.dataSource.sort = this.sort;
          this.updateDisplayedUsers();
          this.isLoading = false;
        } else {
          console.error('Failed to fetch users. Status code:', res.statusCode);
        }
      }),
      error: ((error) => {
        this.isLoading = false;
      }),
      complete: (() => { })
    });
  }

  navigateToUpdateRole(user: User) {
    const selectedUser = {
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      employeeNumber: user.employeeNumber,
      emailAddress: user.emailAddress,
      phoneNumber: user.phoneNumber,
      department: user.department,
      role: user.role
    };
    this.userSelectionService.setSelectedUser(selectedUser);
    this.router.navigate(['/userroleupdate']);
  }

  applyFilter() {
    const filterValue = this.searchTerm.trim().toLowerCase();
    this.filteredUsers = this.allUsers.filter(user =>
      user.fullName.toLowerCase().includes(filterValue)
      || user.emailAddress.toLowerCase().includes(filterValue)
      || user.role.toLowerCase().includes(filterValue)
      || user.department.toLowerCase().includes(filterValue)
      || user.phoneNumber.includes(filterValue)
      || user.employeeNumber.includes(filterValue)
    );
    this.updateDisplayedUsers();
  }

  editUser(user: User) {
    this.userSelectionService.setSelectedUser(user);
    this.router.navigate(['/userroleupdate']);
  }
  
  openDeleteConfirmation(userId: number, user: User) {
  
    const dialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { firstName: user.firstName, email: user.emailAddress }
    });
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) { // If user confirms deletion
        this.deleteUser(userId); // Call the deleteUser function to perform deletion
      }
    });
  }
  
  deleteUser(userId: number): void {
    this.userService.deleteLogicalUser(userId).subscribe({
      next: (res) => {
        // Optionally, perform additional actions after successful deletion
        this.getUsers();
        this.openSnackBar(res.message, true);
      },
      error: (error) => {
        console.error('Error deleting User:', error);
        // Handle error gracefully (e.g., show error message)
        this.openSnackBar("Error deleting User", false);
      }
    });
  }
  



  exportToExcel() {
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.filteredUsers);
    const workbook: XLSX.WorkBook = { Sheets: { 'data': worksheet }, SheetNames: ['data'] };
    XLSX.writeFile(workbook, 'users.xlsx');
  }

  exportToCSV() {
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(this.filteredUsers);
    const csvOutput: string = XLSX.utils.sheet_to_csv(worksheet);
    const blob = new Blob([csvOutput], { type: 'text/csv;charset=utf-8;' });
    saveAs(blob, 'users.csv');
  }

  exportToJSON() {
    const jsonOutput = JSON.stringify(this.filteredUsers, null, 2);
    const blob = new Blob([jsonOutput], { type: 'application/json' });
    saveAs(blob, 'users.json');
  }

  exportToText() {
    const textOutput = this.filteredUsers.map(user => JSON.stringify(user)).join('\n');
    const blob = new Blob([textOutput], { type: 'text/plain;charset=utf-8;' });
    saveAs(blob, 'users.txt');
  }
}
