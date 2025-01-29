import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment.prod';
import { UserAuthService } from '../user-auth/_service/user-auth.service';


@Injectable({
  providedIn: 'root'
})
export class DocumentService {

  private selectedUser: any = null;
 private fileData?: File | null = null;
 private fileName: string = '';
 private fileSize: number = 0;
 private fileType: string = '';
 private fileURL: string = '';
 //private docUrl: URL;

 constructor(private http: HttpClient, private authService: UserAuthService) { }

 setFileData(file: File | undefined): void {
    this.fileData = file;
    this.fileName = file!.name;
    this.fileSize = file!.size;
    this.fileType = this.determineFileType(file!.name);
 }

 getFileData(): { file: File | null| undefined, fileName: string, fileSize: number, fileType: string } {
    return { file: this.fileData, fileName: this.fileName, fileSize: this.fileSize, fileType: this.fileType };
 }
 setFileURL(fileURL: string) {
  this.fileURL = fileURL;
}
getFileURL(): { fileURL: string } {
  return { fileURL: this.fileURL};
}

 determineFileType(fileName: string): string {
    const fileExtension = fileName.split('.').pop()?.toLowerCase();
    switch (fileExtension) {
      case 'doc':
      case 'docx':
        return 'Word';
      case 'pdf':
        return 'PDF';
      case 'ppt':
      case 'pptx':
        return 'PowerPoint';
      case 'jpg':
      case 'jpeg':
      case 'png':
      case 'tiff':
        return 'Image';
      case 'xls':
      case 'xlsx':
        return 'Excel';
      default:
        return 'Unknown';
    }
 }
 formatSizeUnits(bytes: number) {
  if (bytes >= 1073741824) {
     return (bytes / 1073741824).toFixed(2) + " GB";
  } else if (bytes >= 1048576) {
     return (bytes / 1048576).toFixed(2) + " MB";
  } else if (bytes >= 1024) {
     return (bytes / 1024).toFixed(2) + " KB";
  } else if (bytes > 1) {
     return bytes + " bytes";
  } else if (bytes === 1) {
     return bytes + " byte";
  } else {
     return "0 bytes";
  }
 }

  saveDocument(documentInfo:any): Observable<any> {
    const apiUrl = `${environment.testUrl}/api/v1/Documents/upload_and_save`;
    const formData: FormData = new FormData();
    formData.append('file', documentInfo.file, documentInfo.fileName); // Append the file
    formData.append('documentName', documentInfo.documentName);
    formData.append('fileSize', documentInfo.fileSize);
    formData.append('fileType', documentInfo.fileType);
    formData.append('notes', documentInfo.notes);
    formData.append('tags', documentInfo.tags);
    formData.append('dueDate', documentInfo.dueDate);
    formData.append('location', documentInfo.location);
    console.log('Object:', documentInfo);

    return this.http.post(apiUrl, formData).pipe(

      catchError(error => {
        console.error('Error in saveDocument:', error);
        return throwError(() =>error);
      })
    );
 }
 
 public ocrCapture(OcrData: any): Observable<any> {
  const ocrUrl = `${environment.testUrl}/api/v1/OpenAi/OcrExtract`;
 
  return this.http.post(ocrUrl, OcrData).pipe(
     tap(response => {
       // Return the updated values from the tap operator
       return response as { key: string, value: string }[];
     }),
     catchError(error => {
       console.error('Error occurred while performing OCR', error);
       throw error; // Rethrow the error for the subscriber to handle
     })
  );
 }
 
 public uploadMultipleDocuments(files: File[]): Observable<any>{
  const upload_url = `${environment.testUrl}/api/v1/Documents/uploadMultiple`;
  const formData: FormData = new FormData();
  Array.from(files).forEach(file => {
    formData.append('files', file, file.name);
  });
  return this.http.post(upload_url, formData)
 }

 }
