import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import {Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Observable, catchError, throwError } from 'rxjs';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';
import { environment } from 'src/environments/environment.prod';
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private unreadCount = new BehaviorSubject<number>(0);
  unreadCount$ = this.unreadCount.asObservable();


  constructor(
    private http: HttpClient,
    private authService:UserAuthService
    ) { }
    

    getNotifications(): Observable<any> {
      const url= `${environment.testUrl}/api/v1/notifications/current-user`;

      return this.http.get(url);
    };
    getNotificationNumber(): Observable<any>{
        const url= `${environment.testUrl}/api/v1/notifications/getStatus`;
  
        return this.http.get(url)

    }

    setNotification(id: String): Observable<any> {
      const url = `${environment.testUrl}/api/v1/notifications/changeStatus`;
      const headers = new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      });
      
      const params = new HttpParams().set('id', id.toString());
  
      return this.http.post<any>(url, null, { headers, params }).pipe(
        catchError(error => {
          console.error('Error setting notification', error);
          return throwError(() => error);
        })
      );
    }
  }