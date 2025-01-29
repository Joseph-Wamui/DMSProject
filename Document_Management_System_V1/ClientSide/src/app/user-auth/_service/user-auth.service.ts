import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from "@angular/common/http";
import { Observable, catchError, tap, throwError } from "rxjs";
import { environment } from "src/environments/environment.prod";
import { User } from 'src/app/core/models/user';

@Injectable({
  providedIn: 'root'
})
export class UserAuthService {
  user: User = new User;


  constructor(private http: HttpClient) {}

  headers = new HttpHeaders().set("Content-Type", "application/json");

  public userLogin(userDetails: any): Observable<any> {
    const url = `${environment.testUrl}/api/v1/auth/Login`;
    return this.http.post<any>(url, userDetails);
  }

  verifyOTP(otpValue: any): Observable<any> {
    const OTP_URL = `${environment.testUrl}/api/v1/auth/otp`;
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });

    return this.http.post<any>(OTP_URL, otpValue, { headers }).pipe(
      tap(response => {
        console.log('Response after submitting OTP:', response);
        if (response && response.entity.access_token) {
          sessionStorage.setItem('access_token', response.entity.access_token);
        } else {
          console.log('No access token received in the response.');
        }
      }),
      catchError(error => {
        console.error('Error occurred while verifying OTP:', error);
        throw error; // Rethrow the error for the subscriber to handle
      })
    );
  }

  public getToken(): any {
    return sessionStorage.getItem('access_token');
  }

  public setToken(token: string) {
    sessionStorage.setItem('access_token', token);
  }

  public setLoggedInUser(user: User[]) {
    // Serialize the user array into a JSON string
    const serializedUser = JSON.stringify(user);
    
    // Save the serialized user in session storage
    sessionStorage.setItem('loggedInUser', serializedUser);
  }
  
  public getLoggedInUser() {
    // Retrieve the serialized user from session storage
    const serializedUser = sessionStorage.getItem('loggedInUser');
    
    // Check if there is a user in session storage
    if (serializedUser) {
      // Deserialize the user from the JSON string
      const user = JSON.parse(serializedUser);
      
      // Return the deserialized user
      return user;
    } else {
      // Return null or an empty array if no user is found in session storage
      return [];
    }
  }
  

  public setFirstName(firstName: string) {
    sessionStorage.setItem('firstName', firstName);
  }

  public getFirstName(): any {
    return sessionStorage.getItem('firstName');
  }

  public setRole(role: string) {
    sessionStorage.setItem('role', role);
  }

  public getRole(): string | null {
    return sessionStorage.getItem('role');
  }

  // public isLoggedIn() {
  //   return this.getRole() && this.getLoggedInUser();
  // }

  public logout(){
    const logoutUrl = `${environment.testUrl}/api/v1/auth/logout`;
    
    return this.http.post( logoutUrl,{});
  }

  errorMgmt(error: HttpErrorResponse) {
    let errorMessage = "";
    if (error.error instanceof ErrorEvent) {
      // Get client-side error
      errorMessage = error.error.message;
    } else {
      // Get server-side error
      errorMessage = `${error.error.message}`;
    }
    return throwError(()=>errorMessage);
  }

  public changePassword(userDetails: any): Observable<any> {
    const url = `${environment.testUrl}/api/v1/auth/changePassword`;
    return this.http.put<any>(url, userDetails);
  }
}
