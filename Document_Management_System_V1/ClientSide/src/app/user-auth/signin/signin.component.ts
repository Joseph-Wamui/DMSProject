import { Component, OnInit } from '@angular/core';
import {
  FormControl,
  FormGroupDirective,
  NgForm,
  Validators,
  FormBuilder,
  FormGroup,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { Router } from '@angular/router';
import {
  MatSnackBar,
  MatSnackBarHorizontalPosition,
  MatSnackBarVerticalPosition,
} from '@angular/material/snack-bar';
import { UserAuthService } from '../_service/user-auth.service';

/** Error when invalid control is dirty, touched, or submitted. */
export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(
    control: FormControl | null,
    form: FormGroupDirective | NgForm | null
  ): boolean {
    const isSubmitted = form && form.submitted;
    return !!(
      control &&
      control.invalid &&
      (control.dirty || control.touched || isSubmitted)
    );
  }
}

@Component({
  selector: 'app-signin',
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.scss'],
})
export class SigninComponent implements OnInit {
  loginForm!: FormGroup;
  // authService: any;

  constructor(
    private formBuilder: FormBuilder,
    private authService: UserAuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}
  emailFormControl = new FormControl('', [
    Validators.required,
    Validators.email,
  ]);
  passwordFormControl = this.formBuilder.control('', [
    Validators.required,
    Validators.minLength(8), // Example: Password should be at least 8 characters long
    //Validators.pattern(/^\d{4}$/),
    Validators.pattern('^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,}$') // Example: Password should contain at least one uppercase letter, one lowercase letter, one number, and one special character
  ]);

  matcher = new MyErrorStateMatcher();

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      emailAddress: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.pattern('^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_]).{8,}$')]],
      //rememberPassword: [false]
    });
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

  submitted = false;
  loading = false;
  error = '';
  hide = true;

  onSubmit() {
    this.submitted = true;
    this.loading = true;
    this.error = '';
    if (this.loginForm.invalid) {
      this.error = 'Username and Password not valid !';
      return;
    }
    // // Simulate login process
    // setTimeout(() => {
    //   this.loading = false;
    //   // Handle login success or failure here
    // }, 5000); // Simulate a delay

    const loginCredentials = {
      emailAddress: this.loginForm.controls['emailAddress'].value,
      password: this.loginForm.controls['password'].value,
    };

    console.log('Object', loginCredentials );
    this.authService.userLogin(loginCredentials ).subscribe({
      next: (res: any) => {
        if (res) {
          console.log('Hitting endpoint');
          console.log('response', res);
          if (res.statusCode !== 200) {
            this.openSnackBar(res.message, false);
            this.loading = false;
          } else {
            this.openSnackBar(res.message, true);
            this.router.navigate(['/auth/otp']);
          }
        }
      },
      error: (error: any) => {
        console.log(error);

        this.loading = false;
        //this.router.navigate(["/auth/signin"]);
        this.openSnackBar('Server Error', false);
      },
      complete: () => {},
    });
  }
}
