import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, tap, throwError } from 'rxjs';
import { environment } from 'src/environments/environment.prod';

@Injectable({
  providedIn: 'root'
})
export class backupService {


    constructor(
        private http: HttpClient
        
    ) {}
    
       backupNow(backupData:any) :Observable<any> {
           
            const apiUrl =` ${environment.testUrl}/api/v1/backup/backupnow`;
            return this.http.post(apiUrl,backupData);
            

        } 
        archiveNow(archiveData:any) :Observable<any> {
           
          const apiUrl =` ${environment.testUrl}/api/v1/archiving/archive-now`;
          return this.http.post(apiUrl,archiveData);
          

      } 
     scheduleBackup(hour:String) :Observable<any>{
            const apiUrl = `${environment.testUrl}/api/v1/backup/schedulebackup`;
            let params= new HttpParams().set('hours', hour.toString())
            return this.http.post(apiUrl,params);
        }
         public restore() :Observable<any>{
            const apiUrl = `${environment.testUrl}/api/v1/backup/restore`;

            return this.http.post(apiUrl,{});

        }
  archiving(days:number): Observable<any> {
            const apiUrl = `${environment.testUrl}/api/v1/archiving/schedule-archiving`;
            let params = new HttpParams()
              .set('days', days );
              const urlWithParams = `${apiUrl}?${params.toString()}`;
              console.log(urlWithParams);
              return this.http.post(urlWithParams, {});
            
          }

   setRetentionPeriod(days:number) :Observable<any>{
        const apiUrl = `${environment.testUrl}/api/v1/archiving/schedule-RETENTION`;
            let params = new HttpParams()
              .set('Days', days );
              const urlWithParams = `${apiUrl}?${params.toString()}`;
              console.log(urlWithParams);
              return this.http.post(urlWithParams, {});

    }

    getArchivedDocuments():Observable<any>{
        const apiUrl=`${environment.testUrl}/api/v1/archiving/all-archives`

        return this.http.get(apiUrl);
    }
    getBackUp():Observable<any>{
        const apiUrl=`${environment.testUrl}/api/v1/backup/backedupdocuments`

        return this.http.get(apiUrl);

    }

setRetentionperiod(retentionPeriodinminutes:String) :Observable<any>{
    const apiUrl=`${environment.testUrl}/api/v1/archiving/set-retention-period?retentionPeriodInMinutes=${retentionPeriodinminutes}`

    return this.http.post(apiUrl,{});

}
 backupFrequency(location: string, hours: number): Observable<any> {
    const apiUrl = `${environment.testUrl}/api/v1/backup/schedulebackup`;
    let params = new HttpParams()
      .set('backupLocation', location)
      .set('minutes', hours.toString());
      const urlWithParams = `${apiUrl}?${params.toString()}`;
              console.log(urlWithParams);
              return this.http.post(urlWithParams, {});
  }



}