import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationsService {
  private apiUrl = 'http://your-backend-api-url';


  constructor(private http: HttpClient) { }
  sendNotifications(selectedEmails: string[]): Observable<any> {
    return this.http.post<any>('/send-notifications', { selectedEmails })
  }
  
}
