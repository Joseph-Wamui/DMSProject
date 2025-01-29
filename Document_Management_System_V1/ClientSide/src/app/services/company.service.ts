import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment.prod';
import { Company } from '../Models/company.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
 private company: any =null;

  constructor(
    private http: HttpClient
  ) { }

  public fetchOrganizationInfo(): Observable<any>{
    const fetchUrl =` ${environment.testUrl}/api/v1/company/getCompanyDetails`;
    return this.http.get(fetchUrl);
  }
  
  public postOrganizationInfo( comanyInfo: any): Observable<any>{
    const postUrl =` ${environment.testUrl}/api/v1/company/details`;
    return this.http.post(postUrl, comanyInfo);
  }
    
  public updateOrganizationInfo( companyInfo: any, organizationId:number): Observable<any>{
    const putUrl =` ${environment.testUrl}/api/v1/company/updating?organizationId=${organizationId}`;
    return this.http.put(putUrl, companyInfo);
  }

  setcompanyInfo(company: Company): void {
    this.company = company;
  }

  getCompanyInfo(): any {
    return this.company;
  }
}
