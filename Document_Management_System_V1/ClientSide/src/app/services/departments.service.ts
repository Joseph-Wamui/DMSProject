import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment.prod';
@Injectable({
  providedIn: 'root',
})
export class DepartmentsService {
  

  constructor(private http: HttpClient) { }

  getDepartments(): Observable<any> {
    const DEPARTMENTS_URL = `${environment.testUrl}/api/v1/document-search/distinct-departments`;
  

    console.log('Fetching departments from:', DEPARTMENTS_URL); 

    return this.http.get<any[]>(DEPARTMENTS_URL).pipe(
      catchError((error) => {
        console.error('Error fetching departments:', error); // Log the error
        return throwError(() => error);
      }),
      tap(response => console.log('Fetched departments:', response)) // Log the response
    );
  }
}
