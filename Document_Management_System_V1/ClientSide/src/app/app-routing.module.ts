import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { SigninComponent } from './user-auth/signin/signin.component';
import { DashboardComponent } from './screens/dashboard/dashboard.component';
import { EditMetadataComponent } from './screens/edit-metadata/edit-metadata.component';
import { MyDocumentsComponent } from './screens/my-documents/my-documents.component';
import { UploadPageComponent } from './screens/upload-page/upload-page.component';
import { UserlistComponent } from './screens/userlist/userlist.component';
import { UserroleupdateComponent } from './screens/userroleupdate/userroleupdate.component';
import { AdvancedSearchPageComponent } from './screens/advanced-search-page/advanced-search-page.component';
import { ReportsComponent } from './screens/reports/reports.component';
import { ChangepasswordComponent } from './user-auth/changepassword/changepassword.component';
import { AuthGuard } from './services/auth.guard';

import { ForgotPasswordComponent } from './user-auth/forgot-password/forgot-password.component';
import { DocumentWorkflowsManagementComponent } from './screens/document-workflows-management/document-workflows-management.component';
import { createuserComponent } from './screens/createuser/createuser.component';

import { WorkflowProgressComponent } from './screens/workflow-progress/workflow-progress.component';
import { DocumentViewComponent } from './screens/document-view/document-view.component';

import { NotificationServiceComponent } from './notification-service/notification-service.component';
import { UserProfileComponent } from './screens/user-profile/user-profile.component';
import { OopsComponent } from './screens/oops/oops.component';
import { BackupComponent } from './screens/backup/backup.component';
import { ArchivedDocumentsComponent } from './archived-documents/archived-documents.component';

import { CreateroleComponent } from './screens/createrole/createrole.component';
import { ViewPermisionsComponent } from './screens/view-permisions/view-permisions.component';
import { SystemConfigComponent } from './screens/system-config/system-config.component';

const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./user-auth/user-auth.module').then((m) => m.UserAuthModule),
  },
  { path: 'auth/signin', component: SigninComponent, data: { title: 'Sign In' } },
  {
    path: ' ',
    redirectTo: '/auth/signin',
    pathMatch: 'full',
  },
  { path: 'auth/resend-password', component: ForgotPasswordComponent, data: { title: 'Resend Password' } },
  {
    path: '',
    redirectTo: '/auth/signin',
    pathMatch: 'full',
  },
  {
    path: '',
    canActivate: [AuthGuard],
    component: MainLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent, data: { title: 'Dashboard' } },
      { path: 'edit-metadata', component: EditMetadataComponent, data: { title: 'Edit Metadata' } },
      { path: 'upload', component: UploadPageComponent, data: { title: 'Upload Page' } },
      { path: 'mydocuments', component: MyDocumentsComponent, data: { title: 'My Documents' } },
      { path: 'advancedsearch', component: AdvancedSearchPageComponent, data: { title: 'Advanced Search' } },
      { path: 'userroleupdate', component: UserroleupdateComponent, data: { title: 'User Role Update' } },
      { path: 'userlist', component: UserlistComponent, data: { title: 'Users' } },
      { path: 'reports', component: ReportsComponent, data: { title: 'Reports' } },
      { path: 'user-auth/changepassword', component: ChangepasswordComponent, data: { title: 'Change Password' } },
      
      { path: 'document-view', component: DocumentViewComponent, data: { title: 'Document View' } },
      
      { path: 'createUser', component: createuserComponent, data: { title: 'Create User' } },
      { path: 'createRole', component: CreateroleComponent, data: { title: 'Create Role'} },
      { path: 'document-workflows-management', component: DocumentWorkflowsManagementComponent, data: { title: 'Document Workflows Management' } },
      { path: 'workflow-progress', component: WorkflowProgressComponent, data: { title: 'Workflow Progress' } },
      { path: 'backup', component: OopsComponent, data: { title: 'Backup' } },
      { path: 'trials', component: NotificationServiceComponent, data: { title: 'Trials' } },

      {path: 'backuptable',component:BackupComponent},
      {path:'archivedDoc', component:ArchivedDocumentsComponent},
      {path: 'userprofile', component: UserProfileComponent, data:{ title: 'Profile'}},
      {path:'viewRole',component:ViewPermisionsComponent, data:{ title: 'Role-update'}},
      {path: 'system_config', component: SystemConfigComponent, data:{title:'Settings'}}
    ],
  },
  
];
``
@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
