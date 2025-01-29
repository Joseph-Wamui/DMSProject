import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment.prod';
import { UserAuthService } from '../user-auth/_service/user-auth.service';
import { User } from '../core/models/user';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  private selectedUser: any = null;
  apiUrl: any;

  constructor(
    private http: HttpClient,
  ) { }

    setSelectedUser(user: any): void {
      this.selectedUser = user;
    }

    getSelectedUser(): any {
      return this.selectedUser;
    }

  getUsers(): Observable<any> {
    const apiUrl = `${environment.testUrl}/api/v1/users/FetchAllUsers`;

    return this.http.get(
      apiUrl, 
      //{ headers }
    ).pipe(
      catchError(error => {
        console.error('Error fetching users', error);
        return throwError(()=>error);
      })
    );
  }

  createUser(user: any, role:string): Observable<any> {
    const createUserUrl = `${environment.testUrl}/api/v1/auth/CreateUser?role=${role}`;
   
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.post<any>(createUserUrl, user, { headers }).pipe(
      catchError(error => {
        console.error('Error creating user', error);
        return throwError(()=>error);
      })
    );
  }



  updateUserRole(userId: any, role:string): Observable<any> {
    const updateUserRoleUrl = `${environment.testUrl}/api/v1/users/updateUserRole?userId=${userId}`;
    const formData: FormData = new FormData();
   // formData.append('userId', userId);
    formData.append('newRole', role);
    return this.http.put<any>(updateUserRoleUrl, formData).pipe(
      catchError(error => {
        console.error('Error updating user role', error);
        return throwError(()=>error);
      })
    );
  }

  
  public deleteLogicalUser(userId:number):  Observable<any> {
    const deleteUrl = `${environment.testUrl}/api/v1/users/deleteUser?userId=${userId}`;
   
    return this.http.delete(deleteUrl); //patch for logical deletion
  }

  public getDeletedUsers(): Observable<User[]> {
    const deletedUsersUrl = `${environment.testUrl}/deletedUsers`;
    return this.http.get<User[]>(deletedUsersUrl);
  }

  public getPrivileges(): Observable<any> {
    const privilegesUrl = `${environment.testUrl}/api/v1/users/privileges`;
    return this.http.get<any>(privilegesUrl);
  }
  public getPermissions(role: string): Observable<any> {
    const privilegesUrl = `${environment.testUrl}/api/v1/users/rolePrivileges?role=${role}`;
    
    return this.http.get<any>(privilegesUrl);
  }

  public createRole(roleData: any): Observable<any> {
    const createRoleUrl = `${environment.testUrl}/api/v1/users/createRole`;
    return this.http.post<any>(createRoleUrl, roleData).pipe(
      catchError(error => {
        console.error('Error creating role', error);
        return throwError(() => error);
      })
    );
  }
  public updateRole(roleData: any): Observable<any> {
    const updateRoleUrl = `${environment.testUrl}/api/v1/users/UpdateRole`;
    return this.http.put<any>(updateRoleUrl, roleData).pipe(
      catchError(error => {
        console.error('Error creating role', error);
        return throwError(() => error);
      })
    );
  }

public getRoles(): Observable<any>{
  const getRolesUrl = `${environment.testUrl}/api/v1/users/GetRoles`;
  return this.http.get(getRolesUrl);
}  
}


