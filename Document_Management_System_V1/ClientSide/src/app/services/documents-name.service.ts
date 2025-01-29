import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { UserAuthService } from '../user-auth/_service/user-auth.service';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment.prod';

interface DocumentName {
  documentName: string;
}


@Injectable({
  providedIn: 'root',
})
export class DocumentNamesService {
  private DOCUMENT_NAMES_URL = `${environment.testUrl}/api/v1/Documents/FetchDocumentNamesList`;

  constructor(private http: HttpClient, private userService: UserAuthService) {}

  getDocumentNames(): Observable<any> {
    const token = this.userService.getToken();
    if (!token) {
      return throwError(()=> new Error('User not authenticated'));
    }

    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token} `});

    return this.http.get(this.DOCUMENT_NAMES_URL, { headers });
  }
}