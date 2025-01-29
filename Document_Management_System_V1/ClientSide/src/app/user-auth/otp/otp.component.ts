import { Component } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserAuthService } from '../_service/user-auth.service';
import { User } from 'src/app/core/models/user';

@Component({
  selector: 'app-otp',
  templateUrl: './otp.component.html',
  styleUrls: ['./otp.component.scss']
})
export class OtpComponent {
  user:User = new User;
  isSubmitted:boolean= false;
  horizontalPosition: MatSnackBarHorizontalPosition = "end";
  verticalPosition: MatSnackBarVerticalPosition = "top";
  code: any;
  authForm!: UntypedFormGroup;
  loading: boolean = false;

  constructor(
    private formBuilder: UntypedFormBuilder,
    private authService: UserAuthService, // Corrected service name
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.authForm = this.formBuilder.group({
      otpValue: ["", Validators.required], // Corrected form control name
    });
  }

  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 5000,
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: panelClass // Use the appropriate class based on success or failure
    });
  }

  onOtpChange(event: any) {
    this.code = event;
    if(this.code.length == 4) {
      this.onSubmit()
    }
  }

  onSubmit(): void {
    if (this.authForm.invalid) {
      return;
    }
    this.loading = true;
    this.isSubmitted =true;
    const otpValue = this.authForm.value.otpValue;
    // Call your authentication service to verify the OTP
    this.authService.verifyOTP(otpValue).subscribe({
      next: (res: any) => {
        console.log("Hitting endpoint");
        console.log("response", res);
        this.authService.setToken(res.entity.access_token);
        this.authService.setLoggedInUser(res.entity.user);
       // this.authService.setRole(res.entity.user.role);
       //this.authService.setFirstName(res.entity.user.firstName);
        this.loading = false;
        this.router.navigate(['/dashboard']);
        this.openSnackBar(res.message, true);
      },
      error: (error: any) => {
        console.error('Error verifying OTP:', error);
        this.loading = false;
        this.router.navigate(['/auth/signin']);
        this.openSnackBar('Invalid OTP', false);
      }
  });
  }
}
