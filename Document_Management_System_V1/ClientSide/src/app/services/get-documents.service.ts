import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { environment } from 'src/environments/environment.prod';
import { UserAuthService } from '../user-auth/_service/user-auth.service';

@Injectable({
  providedIn: 'root'
})
export class GetDocumentsService {
  fileData: string='';
  fileName: string='';
  fileSize: string='';
  fileType: string='';
  filepath: string='';
  notes: string='';
  tags: any;
  createDate:Date | undefined;
  dueDate:Date |undefined;
  SelectedType: any;
  selectedDocument: any;



  constructor(
    private http: HttpClient,
    private authService: UserAuthService,
    ) { }
    setSelectedDocument(document: any): void {
      this.selectedDocument = document;
    }

    getSelectedDocument(): any {
      return this.selectedDocument;
    }

    

  // Function to fetch documents
  getDocuments(): Observable<any> {

    const apiUrl = `${environment.testUrl}/api/v1/Documents/getMyDocuments`;
    return this.http.get(apiUrl );

  }

  getRecentDocuments(): Observable<any> {

    const apiUrl = `${environment.testUrl}/api/v1/Documents/last-five-accessed`;
    return this.http.get(apiUrl );

  }

  getDocumentById(id: any): Observable<Blob> {
    const getDocUrl = `${environment.testUrl}/api/v1/Documents/downloadDocument?id=${id}`;


    return this.http.get(getDocUrl, {responseType: 'blob' });
 }
 startWorkflow(approvalData: any): Observable<any> {
  const url = `${environment.testUrl}/api/v1/workflow/AddNewWorkflow`;
  return this.http.post<any>(url, approvalData).pipe(
    catchError(error => {
      // Handle error here
      console.error('Error creating workflow', error);
      return throwError(()=>error);
    })
  );
}

startDefinedtWorkflow(approvalData: any): Observable<any> {
  const url = `${environment.testUrl}/api/v1/workflow/SelectExistingWorkflow`;
  return this.http.post<any>(url, approvalData).pipe(
    catchError(error => {
      // Handle error here
      console.error('Error creating workflow', error);
      return throwError(()=>error);
    })
  );
}

updateWorkflows(documentid:any, approved: any, comments:string): Observable<any> {
  const url = `${environment.testUrl}/api/v1/workflow/send-to-next-step`;
  const data = {
    documentId: documentid,
    approved: approved,
    approverComments: comments
  }
  const approveData: FormData = new FormData();
  approveData.append('documentId', documentid);
  approveData.append('approved', approved);
  approveData.append('approverComments', comments);
  console.log("data:"+data)

  return this.http.post<any>(url, approveData);
}
getWorkflowUsers( documentId:String) :Observable<any>{
  const url = `${environment.testUrl}/api/v1/workflow/approverscomments?documentID=${documentId}`;
  return this.http.get(url);
}


 getWorkflows():Observable<any>{
  const url= `${environment.testUrl}/api/v1/workflow/showMyProgress`;

  return this.http.get(url);

 }

 getSavedWorkflows():Observable<any>{
  const url= `${environment.testUrl}/api/v1/workflow/ViewExistingWorkflows`;

  return this.http.get(url);

 }

 getStatus():Observable<any>{
  const url= `${environment.testUrl}/api/v1/Documents/getStatus`;

  return this.http.get(url);

 }
 getDocmentStatus():Observable<any>{
  const url= `${environment.testUrl}/api/v1/Documents/total`;

  return this.http.get(url);

 }

setSelectedType(type:any){
  this.SelectedType= type;

}
getSelectedType(): any{
  return this.SelectedType
}

getSelectedDocumentById(id: number): Observable<any> {
  const getDocUrl = `${environment.testUrl}/api/v1/Documents/getDocumentById?id=${id}`;

  return this.http.get(getDocUrl);

 }
public getAuditLogs(documentId: number): Observable<any> {
  const logUrl = `${environment.testUrl}/api/v1/Audit_trail/documentAuditLogs/documentId?documentId=${documentId}`;
 
  return this.http.get(logUrl);

}

public getOCRData(documentId: number): Observable<any> {
   const OCRDataUrl = `${environment.testUrl}/api/v1/Ocr/documentAttributes?documentId=${documentId}`;
 
  return this.http.get(OCRDataUrl);

}

public deleteDocument(documentId:number):  Observable<any> {
  const deleteUrl = `${environment.testUrl}/api/v1/Documents/deleteDocumentById?documentId=${documentId}`;
 
  return this.http.delete(deleteUrl);
}

public  updateDocumentVersion(documentId: number, versionData: any): Observable<any>{
  const version_Url = `${environment.testUrl}/api/v1/Versions/add?documentId=${documentId}`;
  return this.http.post( version_Url, versionData,);
}

public getDocumentVersions(documentId: number): Observable<any>{
  const get_version_Url = `${environment.testUrl}/api/v1/Versions/getAllVersionsOfADocument?id=${documentId}`;
  return this.http.get( get_version_Url);
}
public getDocumentVersionData(documentId: number, versionNumber: number): Observable<any>{
  const get_version_data_Url = `${environment.testUrl}/api/v1/Versions/getAVersion?documentId=${documentId}&versionNumber=${versionNumber}`;
  return this.http.get( get_version_data_Url);
}

public generateQRCode(documentId:number): Observable<any>{
  const QRCode_Url = `${environment.testUrl}/api/v1/Documents/generateQRCode?documentId=${documentId}`;
  return this.http.get(QRCode_Url);
}
}