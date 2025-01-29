import { Component, OnInit } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { UsersService } from 'src/app/services/users.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NumberInput } from '@angular/cdk/coercion';
import { PageEvent } from '@angular/material/paginator';
import { CdkTableDataSourceInput } from '@angular/cdk/table';
import { User } from 'src/app/core/models/user';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';

@Component({
  selector: 'app-userroleupdate',
  templateUrl: './userroleupdate.component.html',
  styleUrls: ['./userroleupdate.component.css']
})
export class UserroleupdateComponent implements OnInit {
  loading: boolean =false;
  selectedUser: User = new User;
  userForm!: FormGroup;
  curentUser:User = new User;
  roles: string [] = [];
  
 

 //roles = ['ADMIN', 'APPROVER','USER' ];
  
 
 constructor(
      private userService:UsersService,
      private snackBar: MatSnackBar,
      private router: Router,
      private userAuthService: UserAuthService
    ){}
 
  ngOnInit(): void {
    this.getRoles();
    this.curentUser= this.userAuthService.getLoggedInUser();
    this.selectedUser = this.userService.getSelectedUser();
   this.userForm = new FormGroup({
      firstName: new FormControl(this.selectedUser.firstName),
      lastName: new FormControl(this.selectedUser.lastName),
      employeeNumber: new FormControl(this.selectedUser.employeeNumber),
      emailAddress: new FormControl(this.selectedUser.emailAddress),
      role: new FormControl(this.selectedUser.role),
      id: new FormControl(this.selectedUser.id),
    });

  }

  getRoles(): void{
    this.userService.getRoles().subscribe({
      next:(res) => {
        console.log('Server Response for Get Roles:', res);
        this.roles =res.entity;
     
      },
     error: ((error) => {
      console.error('Server Error for Get Roles:', error);
      this.openSnackBar("Error Updating Role", false);
      }),
      complete: (()=> {})
    });
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
  

 onSubmit(): any {
  this.loading=true;
  const userId =this.selectedUser.id;
  const role =this.userForm.controls['role'].value;
    if(this.curentUser.emailAddress === this.selectedUser.emailAddress){
      this.loading =false;
      this.openSnackBar("Forbidden: You cannot change your role!!", false);
      return;
    }else{
    this.userService.updateUserRole(userId, role).subscribe({
      next: ((res) => {
        
        this.openSnackBar(res.message, true);
        this.loading=false;
        this.router.navigate(['/userlist']);
      }),
      error: ((error) => {
        console.error('Error updating user role:', error);
        this.openSnackBar('Error updating user role', false);
        this.loading=false;
      }),
      complete: (() => {})
    });
  }
}

  Cancel(): void {
    this.router.navigate(['userlist']);
  }
}
