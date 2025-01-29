import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SigninComponent } from './signin/signin.component';
import { OtpComponent } from './otp/otp.component';
import {FormsModule, ReactiveFormsModule, } from '@angular/forms';
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatCheckboxModule } from '@angular/material/checkbox';
import { RouterModule } from '@angular/router';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { HttpClientModule } from '@angular/common/http';
import { ChangepasswordComponent } from './changepassword/changepassword.component';



const routes = [
  { path: 'signin', component: SigninComponent },
  {path: 'otp', component: OtpComponent},
{ path: 'changepassword', component:
  ChangepasswordComponent },
];


@NgModule({
  declarations: [
    SigninComponent,
    OtpComponent,
    ChangepasswordComponent,
  ],

  imports: [
    HttpClientModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatButtonModule,
    MatSnackBarModule,
    MatCardModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    RouterModule.forChild(routes)
  ],
  exports:[
    SigninComponent,
    OtpComponent,
    RouterModule,
  ]
})
export class UserAuthModule { }
