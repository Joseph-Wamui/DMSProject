import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment.prod';
import { UserAuthService } from '../user-auth/_service/user-auth.service';

@Injectable({
  providedIn: 'root'
})
export class ReportsService {

  constructor(private http: HttpClient
  ) { }


  // generateVersionReports(endpoint:string,startDate: string, endDate: string): Observable<any> {
  //   return this.getReports(endpoint,startDate, endDate);
  // }

  getPDFReports(startDate: string, endDate: string, endpoint: string): Observable<any> {
    const url = `${environment.testUrl}/api/v1/reports/${endpoint}?startDate=${startDate}&endDate=${endDate}`;
    return this.http.get(url);
  }
  getOtherReportTypes(url:string): Observable<any>{
    const report_url = `${environment.testUrl}${url}`;
    return this.http.get(report_url);
  }

  public getReportColumn(tableId: string): Observable<any>{
    const reportColumn_url = `${environment.testUrl}/api/v1/custom/data?tableId=${tableId}`;
    return this.http.get(reportColumn_url);
  }
  public generateCustom(tableId: string, columns:string[],startDate:string,endDate:string): Observable<any>{
   const report_url =`${environment.testUrl}/api/v1/custom/customData?tableId=${tableId}&columns=${columns}&startDate=${startDate}&endDate=${endDate}`;
   return this.http.get(report_url);
  }
 }
