
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UploadPageComponent } from './upload-page/upload-page.component';
import { MatIconModule} from '@angular/material/icon';
import { EditMetadataComponent } from './edit-metadata/edit-metadata.component';
import { MyDocumentsComponent } from './my-documents/my-documents.component';
import { UserroleupdateComponent } from './userroleupdate/userroleupdate.component';
import { PaginationComponent } from './pagination/pagination.component';
import { UserlistComponent } from './userlist/userlist.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AdvancedSearchPageComponent } from './advanced-search-page/advanced-search-page.component';
import { BackupComponent } from './backup/backup.component';
// import { OopsComponent } from './oops/oops.component';
import { ReportsComponent } from './reports/reports.component';
import { createuserComponent } from './createuser/createuser.component';
import { StartApprovalWorkflowComponent } from './start-approval-workflow/start-approval-workflow.component';
import { DocumentWorkflowsManagementComponent } from './document-workflows-management/document-workflows-management.component';
import { WorkflowProgressComponent } from './workflow-progress/workflow-progress.component';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ChartsModule } from '../charts/charts.module';
import { SharedModule } from '../shared/shared.module';
import { CoreModule } from '../core/core.module';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSortModule } from '@angular/material/sort';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgxDocViewerModule } from 'ngx-doc-viewer'; // Add this import
import {MatCheckboxModule} from '@angular/material/checkbox';
import {  MatCardModule } from '@angular/material/card';

import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { DocumentViewComponent } from './document-view/document-view.component';
import { ApproveDocumentComponent } from './approve-document/approve-document.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatProgressBarModule} from '@angular/material/progress-bar';
import { PopupComponent } from '../popup/popup.component';
import { ReportCardComponent } from './report-card/report-card.component';
import { OopsComponent } from './oops/oops.component';
import { ArchivedDocumentsComponent } from '../archived-documents/archived-documents.component';
import { CreateroleComponent } from './createrole/createrole.component';
import { MatDividerModule } from '@angular/material/divider';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { RouterModule, RouterOutlet } from '@angular/router';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ViewPermisionsComponent } from './view-permisions/view-permisions.component';
import { SystemConfigComponent } from './system-config/system-config.component';
import { MatTabsModule } from '@angular/material/tabs';



@NgModule({
  declarations: [
    UploadPageComponent,
    EditMetadataComponent,
    MyDocumentsComponent,
    UserroleupdateComponent,
    PaginationComponent,
    UserlistComponent,
    DashboardComponent,
    AdvancedSearchPageComponent,
    BackupComponent,
    ReportsComponent,
    createuserComponent,
    OopsComponent,
    DocumentWorkflowsManagementComponent,
    DocumentViewComponent,
    ApproveDocumentComponent,
    UserProfileComponent,
    PopupComponent,
    ReportCardComponent,
    ArchivedDocumentsComponent,
    BackupComponent,
    WorkflowProgressComponent,
    CreateroleComponent,
    StartApprovalWorkflowComponent,
    ViewPermisionsComponent,
    SystemConfigComponent
  ],
  imports: [
    CommonModule,
    MatDividerModule,
    MatIconModule,
    MatPaginatorModule,
    MatTableModule,
    MatSelectModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSortModule,
    ChartsModule,
    SharedModule,
    CoreModule,
    MatFormFieldModule,
    NgxDocViewerModule,
    MatCardModule,   
    MatInputModule,
    MatSnackBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCardModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    FlexLayoutModule,
    MatSelectModule,
    FormsModule,
    MatCheckboxModule,
    NgbModule,
    RouterOutlet,
    RouterModule,
    MatTooltipModule,
    ReactiveFormsModule,
    MatTooltipModule,
    MatTabsModule
     

  ],
  exports:[
    ReportCardComponent,
    UploadPageComponent,
    EditMetadataComponent,
    MyDocumentsComponent,
    UserroleupdateComponent,
    PaginationComponent,
    UserlistComponent,
    DashboardComponent,
    AdvancedSearchPageComponent,
    ReportsComponent,
    OopsComponent,
    createuserComponent,
    DocumentWorkflowsManagementComponent,
    UserProfileComponent,
    ArchivedDocumentsComponent
    
  ],
  providers:[]
})
export class ScreensModule { }