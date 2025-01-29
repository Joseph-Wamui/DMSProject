import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { AbstractControl } from '@angular/forms';
import { UserAuthService } from "../_service/user-auth.service";
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-changepassword',
  templateUrl: './changepassword.component.html',
  styleUrls: ['./changepassword.component.css']
})
export class ChangepasswordComponent implements OnInit {
  public showNewPassword = false;
  public showConfirmPassword = false;
  public showPasswordRequirementsPopup = false; // Added variable
  public changePasswordForm: FormGroup;
  public passwordStrength: string = '';
  public passwordStrengthText: string = '';
  public passwordStrengthColor: string = '';
  email: any;
  emailAddress:any;
  password:any;

  isMinLengthFulfilled: boolean = false;
  isUpperCaseFulfilled: boolean = false;
  isLowerCaseFulfilled: boolean = false;
  isNumberFulfilled: boolean = false;
  isSpecialCharFulfilled: boolean = false;

  constructor(
    private formBuilder: FormBuilder, 
    private router: Router,
    private userService:UserAuthService,
    private snackBar: MatSnackBar
   ) {
    this.changePasswordForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [Validators.required, this.passwordStrengthValidator()]],
      confirmPassword: ['', Validators.required]
    }, {
      validator: this.passwordMatchValidator
    });
  }

  // Function to validate password input and update requirement flags
  validatePassword(): void {
    const password: string = this.changePasswordForm.get('newPassword')?.value;

    // Update flags based on password requirements
    this.isMinLengthFulfilled = password.length >= 8;
    this.isUpperCaseFulfilled = /[A-Z]/.test(password);
    this.isLowerCaseFulfilled = /[a-z]/.test(password);
    this.isNumberFulfilled = /\d/.test(password);
    this.isSpecialCharFulfilled = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password);
  }

  ngOnInit(): void {
  }

  cancelChangePassword(): void {
    // Navigate back to the previous window
    window.history.back();
  }

  togglePassword(field: string) {
    switch (field) {
      case 'current':
      case 'new':
        this.showNewPassword = !this.showNewPassword;
        break;
      case 'confirm':
        this.showConfirmPassword = !this.showConfirmPassword;
        break;
      default:
        break;
    }
  }

  togglePasswordRequirementsPopup(show: boolean): void {
    this.showPasswordRequirementsPopup = show;
  }

  registerForm = new FormGroup({
    password: new FormControl<string>('')
  });

get passwordFormField() {
    return this.registerForm.get('password');
  }

  passwordMatchValidator(formGroup: FormGroup) {
    const newPassword = formGroup.get('newPassword')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;

    if (newPassword !== confirmPassword) {
      formGroup.get('confirmPassword')?.setErrors({ passwordMismatch: true });
    } else {
      formGroup.get('confirmPassword')?.setErrors(null);
    }
  }

  onSubmit() {
    // Check if the form is valid and the passwords match
    if (this.changePasswordForm.valid && this.changePasswordForm.get('newPassword')?.value === this.changePasswordForm.get('confirmPassword')?.value) {

      const newPassword = this.changePasswordForm.get('newPassword')?.value;
      const userDetails = {
        emailAddress: this.changePasswordForm.value.email,
        password: newPassword
      };
      console.log('sent', userDetails);

      // Send a request to change the password
      this.userService.changePassword(userDetails).subscribe({
        next: (res: any) => {
          if (res && res.statusCode === 200) {
            console.log('User password changed successfully', res);
            this.openSnackBar(res.message, true);
            this.userService.logout();
            this.router.navigate(['/auth/signin'])
          } else {
            console.log('Password change failed');
            this.openSnackBar("Password change failed", false);
          }
        },
        error: (error: any) => {
          console.log('Error:', error);
          this.openSnackBar("Error Occured While changing password", false);
        },
        complete: () => {}
      });
    } else {
      // Handle invalid form or mismatched passwords
      console.log('Invalid form or mismatched passwords!');
    }
  }

  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: panelClass, // Use the appropriate class based on success or failure
    });
  }


  passwordStrengthValidator() {
    const spaceRegex = /\s/; // Regex for spaces

    const spaceValidator = (control: FormControl): { [key: string]: boolean } | null => {
      const value: string = control.value;
      if (value.trim().length === 0) {
        return { 'spaces': true };
      }
      return null;
    };

    return (control: AbstractControl) => {
      const password = control.value;
      const minLength = 8; // Minimum password length
      const uppercaseRegex = /[A-Z]/; // Regex for uppercase letters
      const lowercaseRegex = /[a-z]/; // Regex for lowercase letters
      const numberRegex = /[0-9]/; // Regex for numbers
      const specialCharRegex = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/; // Regex for special characters

      const isStrong = password.length >= minLength &&
        uppercaseRegex.test(password) &&
        lowercaseRegex.test(password) &&
        numberRegex.test(password) &&
        !spaceRegex.test(password) &&
        specialCharRegex.test(password);

      if (!isStrong) {
        return { 'weakPassword': true };
      }
      return null;
    };
  }

  updatePasswordStrength(): void {
    const newPassword = this.changePasswordForm.get('newPassword')?.value;
    this.passwordStrength = this.calculatePasswordStrength(newPassword);
    this.updatePasswordStrengthText();
  }

  calculatePasswordStrength(password: string): string {
    const minLength = 8;
    const minUppercase = 1;
    const minLowercase = 1;
    const minNumbers = 1;
    const minSpecialChars = 1;

    let score = 0;

    if (password.length >= minLength) {
      score += 1;
    }

    if (/[A-Z]/.test(password)) {
      score += 1;
    }

    if (/[a-z]/.test(password)) {
      score += 1;
    }

    if (/\d/.test(password)) {
      score += 1;
    }

    if (/[^A-Za-z0-9]/.test(password)) {
      score += 1;
    }

    if (score <= 2) {
      return 'weak';
    } else if (score <= 4) {
      return 'medium';
    } else {
      return 'strong';
    }
  }

  updatePasswordStrengthText(): void {
    switch (this.passwordStrength) {
      case 'weak':
        this.passwordStrengthText = 'Weak Password';
        this.passwordStrengthColor = 'red';
        break;
      case 'medium':
        this.passwordStrengthText = 'Good Password';
        this.passwordStrengthColor = 'orange';
        break;
      case 'strong':
        this.passwordStrengthText = 'Strong Password';
        this.passwordStrengthColor = 'green';
        break;
      default:
        this.passwordStrengthText = '';
        this.passwordStrengthColor = '';
        break;
    }
  }

}
