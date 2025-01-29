import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  constructor(
    private http: HttpClient,
  ) { }

  public documentCount(): Observable<any>{
    const docUrl= `${environment.testUrl}/api/v1/document-search/documents/count-date`;
    return  this.http.get(docUrl, {});
  }
}
