import { Injectable, } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

@Injectable({
  providedIn: 'root'
})
export class SaveWorkflowsService {
  private apiUrl= '';
  public isLoading = false;

  constructor(private http: HttpClient,
    private modalService: NgbModal
  ) { }

 public saveWorkflow(value: boolean): Observable<any> {
    return this.http.post(this.apiUrl, {value:value});
  }
  

  showSpinner() {
    this.isLoading = true;
  }

  hideSpinner() {
    this.isLoading = false;
  }

  closeModal() {
    this.modalService.dismissAll();
  }
}
