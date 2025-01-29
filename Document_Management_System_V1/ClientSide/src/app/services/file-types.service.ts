// file-types.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from 'src/environments/environment.prod';

@Injectable({
    providedIn: 'root',
})
export class FileTypesService {
    private FILETYPESURL = `${environment.testUrl}/api/v1/document-search/distinct-file-types`

  constructor(private http: HttpClient) {}

  getFileTypes(): Observable<any> {
    return this.http.get<any[]>(this.FILETYPESURL).pipe(
      catchError(error => {
        console.error('Error fetching file types:', error);
        return throwError(() => error);
      })
    );
  }
}
