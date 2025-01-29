import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from './sidebar/sidebar.component';
import { HeaderComponent } from './header/header.component';
import { FooterComponent } from './footer/footer.component';
import { RightSidebarComponent } from './right-sidebar/right-sidebar.component';
import { CustomSnackbarComponent } from './custom-snackbar/custom-snackbar.component';
import { DialogComponent } from './dialog/dialog.component';

import { MatDialogModule } from '@angular/material/dialog';
import { ShareDialogComponent } from './share-dialog/share-dialog.component';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RetentionDialogComponent } from './retention-dialog/retention-dialog.component';
import {MatRadioModule} from '@angular/material/radio';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import {MatTableModule} from '@angular/material/table';
import { DeleteDialogComponent } from './delete-dialog/delete-dialog.component';
import { NotificationDialogComponent } from './notification-dialog/notification-dialog.component';
import { NotificationServiceComponent } from '../notification-service/notification-service.component';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatListModule } from '@angular/material/list';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { BreadcrumbComponent } from './breadcrumb/breadcrumb.component';




@NgModule({
  declarations: [
    SidebarComponent,
    HeaderComponent,
    FooterComponent,
    RightSidebarComponent,
    CustomSnackbarComponent,
    DialogComponent,
    ShareDialogComponent,
    RetentionDialogComponent,
    DeleteDialogComponent,
    NotificationDialogComponent,
    NotificationServiceComponent,
    BreadcrumbComponent,
  
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatDialogModule,
    MatIconModule,
    MatButtonModule,
    MatRadioModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatCardModule,
    MatTabsModule,
    MatListModule,
    MatAutocompleteModule
     
  ],
  exports:[
    SidebarComponent,
    HeaderComponent,
    FooterComponent,
    RightSidebarComponent,
    CustomSnackbarComponent,
    RouterModule, 
    BreadcrumbComponent
  ]
})
export class SharedModule { }
