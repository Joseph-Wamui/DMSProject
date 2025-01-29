import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UsersService } from 'src/app/services/users.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';



@Component({
 selector: 'app-createuser',
 templateUrl: './createuser.component.html',
 styleUrls: ['./createuser.component.css']
})
export class createuserComponent implements OnInit {
Reset() {
throw new Error('Method not implemented.');
}
GeneratePassword() {
  const password = Math.floor(1000 + Math.random() * 9000);
  
  this.createUserForm.controls['password'].setValue(password);
} 
  createUserForm!: FormGroup;
   firstName: string='';
   lastName:  string='';
   role: string='';
   privilege: string= '' ;
   password: string='';
   department: string= '';
  employeeNumber: string = '';
  emailAddress:string = '';
  phoneNumber:string = '';
  horizontalPosition: any;
  verticalPosition: any;
  dialog: any;
  users: any;
  roles: string[]=[];
displayedColumns: any;
loading: boolean =false;

   constructor(
      private userService: UsersService, dialog: MatDialog,
      private formBuilder : FormBuilder,
      private snackBar: MatSnackBar,
      private router: Router
       
     
   ){}

 ngOnInit(): void {
  this.createUserForm = this.formBuilder.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    employeeNumber: ['', Validators.required],
    emailAddress: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.required],
    department: ['', Validators.required],
    role: ['', Validators.required],
    privileges: ['', Validators.required],
    password: ['', Validators.required],
  });
  this.getRoles();
    }

 //roles = ['ADMIN','APPROVER' ,'USER' ];
getRoles(): void{
  this.userService.getRoles().subscribe({
    next:(res) => {
      console.log('Server Response for Get Roles:', res);
      this.roles =res.entity;
   
    },
   error: ((error) => {
    console.error('Server Error for Get Roles:', error);
    this.openSnackBar("Error Creating User", false);
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

 onSubmit(): void {
  this.loading= true;
   console.log('add button clicked');
   const user = {
    firstname: this.createUserForm.get('firstName')?.value,
      lastname: this.createUserForm.get('lastName')?.value,
      employeeNumber: this.createUserForm.get('employeeNumber')?.value,
      emailAddress: this.createUserForm.get('emailAddress')?.value,
      phoneNumber: this.createUserForm.get('phoneNumber')?.value,
      department: this.createUserForm.get('department')?.value,
      password: this.createUserForm.get('password')?.value,
     };
    const role = this.createUserForm.get('role')?.value
     

     console.log('UserInfo:', user);
     this.userService.createUser(JSON.stringify(user), role).subscribe({
       next:((res) => {
         console.log('User created successfully:', res);
         this.loading=false;
         this.openSnackBar("User created successfully", true);
         this.router.navigate(['/userlist'])
       }),
      error: ((error) => {
       console.error('Error creating user:', error);
       this.loading=false;
       this.openSnackBar("Error Creating User", false);
       }),
       complete: (()=> {})
     })
     
   }
 

 cancel(): void {
    console.log('Cancelled');
 }

 generate(): void {
  const lowercaseChars = 'abcdefghijklmnopqrstuvwxyz';
  const uppercaseChars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  const numbers = '0123456789';
  const specialChars = '!@#$%^&*()-_=+[{]}\\|;:\'",<.>/?';

  const allChars = lowercaseChars + uppercaseChars + numbers + specialChars;
  const length = 8; // Length of the generated password

  let generatedPassword = '';
  for (let i = 0; i < length; i++) {
    const randomIndex = Math.floor(Math.random() * allChars.length);
    generatedPassword += allChars[randomIndex];
  }

  this.createUserForm.controls['password'].setValue(generatedPassword);
  console.log('Generated password:', generatedPassword);
}

 resetForm(): void {
  this.firstName = '';
  this.lastName = '';
  this.role = '';
  this.password = '';
  this.department = '';
  this.employeeNumber = '';
  this.emailAddress = '';
  this.phoneNumber = '';
}

}


