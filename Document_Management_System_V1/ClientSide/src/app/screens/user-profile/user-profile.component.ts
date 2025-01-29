import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';
import { User } from 'src/app/core/models/user';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-profile',
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.css']
})
export class UserProfileComponent implements OnInit {
  userProfileForm!: FormGroup;
  showPasswordField = false;
  isEditing = false;
  user: User = new User;
  fullName: string = '';

  constructor(
    private formBuilder: FormBuilder,
    private httpclient: HttpClient,
    private userService: UserAuthService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.getCurrentUser();
    this.userProfileForm = this.formBuilder.group({
      name: ['', Validators.required],
      employeeNumber: ['', Validators.required],
      emailAddress: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', Validators.required],
      department: ['', Validators.required],
      role: ['', Validators.required],
    });
  }

  getCurrentUser() {
    this.user = this.userService.getLoggedInUser();
    this.fullName = this.user.firstName + ' ' + this.user.lastName;
    console.log("CurrentUser:", this.user);
  }

  populateForm(userData: any): void {
    this.userProfileForm = this.formBuilder.group({
      name: [userData.name, Validators.required],
      employeeNumber: [userData.employeeNumber, Validators.required],
      emailAddress: [userData.emailAddress, [Validators.required, Validators.email]],
      phoneNumber: [userData.phoneNumber, Validators.required],
      department: [userData.department, Validators.required],
      role: [userData.role, Validators.required],
    });
    this.isEditing = true; // Switch to edit mode
  }

  onsubmit() {
    if (this.userProfileForm.valid) {
      console.log(this.userProfileForm.value);
    }
  }

  cancel(event?: Event) {
    if (event) {
      event.preventDefault();
    }
    this.router.navigate(['/dashboard']);
  }

}
