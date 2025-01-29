import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class ApprovalService {
  private apiUrl = 'http://your-api-url.com/api/documents'; //replace with my api

  constructor(private http: HttpClient) { }

  updateDocumentStatus(documentId: string, action: 'approve' | 'reject'): Observable<any> {
    return this.http.post('${this.apiUrl}/${documentId}/${action}', {});
  }


getSelectedWorkflows():Observable<any>{
  const url= `${environment.testUrl}//api/v1/workflow/SelectExistingWorkflow`

  return this.http.get(url);
}
 }





 
