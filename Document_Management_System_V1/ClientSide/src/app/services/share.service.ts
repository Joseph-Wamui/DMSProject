import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root',
})
export class ShareService {
  constructor(private http: HttpClient) {}

  public shareDocument(shareData: any): Observable<any> {
    const share_url = `${environment.testUrl}/api/v1/workflow/share`;
    return this.http.post(share_url, shareData);
  }
  public getUsersSharedWith(documentId: number): Observable<any>{
    const get_url = `${environment.testUrl}/api/v1/notifications/getusers?DocumentId=${documentId}`;
    return this.http.get(get_url);
  }
}
