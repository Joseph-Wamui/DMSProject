import { Component } from '@angular/core';
import { User } from 'src/app/core/models/user';
import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-retention-dialog',
  templateUrl: './retention-dialog.component.html',
  styleUrls: ['./retention-dialog.component.css']
})
export class RetentionDialogComponent {
  fileName:string ='';
  retentionOption: string = '1';

  constructor(
    private workflowService: GetDocumentsService,
  ){}
  ngOnInit(): void {
    this.getFileName();
  }
  public getFileName(){
    const selectedFile= this.workflowService.getSelectedDocument();
    this.fileName = selectedFile.documentName;
  }
 
}
