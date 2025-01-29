import { Component, OnInit } from "@angular/core";
//import { HttpParams } from "@angular/common/http";
//import { NotificationService } from "../_service/notification.service";
//import Swal from 'sweetalert2';

@Component({
  selector: "app-forgot-password",
  templateUrl: "./forgot-password.component.html",
  styleUrls: ["./forgot-password.component.scss"],
})
export class ForgotPasswordComponent implements OnInit {
  // authForm: FormGroup;
  // submitted = false;
  // returnUrl: string;
  // loading = false;
  constructor(
    // private formBuilder: FormBuilder,
    // private route: ActivatedRoute,
    // private router: Router,
    // private authService: AuthService,
    // private notificationAPI: NotificationService,

  ) { }
  ngOnInit() {
  //   this.authForm = this.formBuilder.group({
  //     email: [
  //       "",
  //       [Validators.required, Validators.email, Validators.minLength(5)],
  //     ],
  //   });
  //   // get return url from route parameters or default to '/'
  //   this.returnUrl = this.route.snapshot.queryParams["returnUrl"] || "/";
  // }
  // get f() {
  //   return this.authForm.controls;
  }
  
// onSubmit() {
//   this.submitted = true;
//   if (this.authForm.invalid) {
//     return;
//   } else {
//     const email = this.authForm.value.email;
//     console.log(email); 
//     this.authService.forgotPassword(email).subscribe(
//       (res) => {
//         console.log(res); 
//       },
//       (err) => {
//         console.log(err); 
//       }
//     );
//   }
// }


// onSubmit() {
//   this.submitted = true;
//   if (this.authForm.invalid) {
//     return;
//   }
//    else {
//     const email = this.authForm.value.email;
//     console.log(email);
//     this.loading = true;

//     this.authService.forgotPassword(email).subscribe(
//       (res) => {
//         console.log(res);
    
//         // Check if the password reset was successful based on the response from the API
//         if (res && res.statusCode === 200) {
//           // Show success notification using Swal
//           Swal.fire({
//             icon: 'success',
//             title: 'Password Reset Successful!',
//             text: 'Please check your email for further instructions.',
//           }).then(() => {
//             // Navigate the user to the sign-up page after a successful reset
//             this.router.navigate(['/supplier-authentication/signin']);
//           });
//         } else if (res && res.statusCode === 400) {
//           // Handle status code 400: Password cannot be re-used
//           Swal.fire({
//             icon: 'warning',
//             title: 'Password Reset Failed',
//             text: 'Password cannot be re-used within a period of 1 year!',
//           });
//         } else {
//           // Show warning notification for any other response (e.g., 404, error)
//           Swal.fire({
//             icon: 'warning',
//             title: 'Password Reset Failed',
//             text: 'Please try again or contact support.',
//           });
//         }
//       },
//       (err) => {
//         console.log(err);
//         // Show error notification for any HTTP error (e.g., network issue)
//         Swal.fire({
//           icon: 'error',
//           title: 'Password Reset Failed',
//           text: 'Password reset failed due to a network error. Please try again later.',
//         });
//       }
//     );
    
//   }
// }


}