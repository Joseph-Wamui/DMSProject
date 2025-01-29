import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { UserAuthService } from '../user-auth/_service/user-auth.service';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SearchCriteria } from '../Models/search-criteria.model';
import { environment } from 'src/environments/environment.prod';

interface SearchResult {
   id: string;
   documentName: string;
   fileType: string;
   createdBy: string;
   startDate: string;
   endDate: string;
   notes: string;
   approverComments: string;
   status: string;
   // Add other properties as needed
}

@Injectable({
   providedIn: 'root',
})
export class SearchService {
   private SEARCH_URL = `${environment.testUrl}/api/v1/document-search/documents/search`;


   constructor(private http: HttpClient, private userService: UserAuthService) { }

   searchDocuments(searchCriteria: SearchCriteria): Observable<SearchResult[]> {

      // Construct the query string from the searchCriteria object
      const queryString = Object.entries(searchCriteria)
         .filter(([key, value]) => value && typeof value === 'string' && value.trim() !== '')
         .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value as string)}`)
         .join('&');

      const url = `${this.SEARCH_URL}?${queryString}`;

      return this.http.get<SearchResult[]>(url).pipe(
         catchError((error) => {
            console.error('Error fetching search results:', error);
            return throwError(()=>error);
         })
      );
   }



}