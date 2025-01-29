import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SharedModule } from './shared/shared.module';
import { LayoutModule } from './layout/layout.module';
import { RouterModule, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UserAuthModule } from './user-auth/user-auth.module';
import { ErrorStateMatcher, MatNativeDateModule, ShowOnDirtyErrorStateMatcher } from '@angular/material/core';
import { ChartsModule } from './charts/charts.module';
import { CoreModule } from './core/core.module';
import { MatTableModule } from '@angular/material/table';
import { ScreensModule } from './screens/screens.module';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule, MatLabel } from '@angular/material/form-field';
import { MatDialogModule } from '@angular/material/dialog';
import { PopupComponent } from './popup/popup.component';
import { MatCardContent, MatCardModule } from '@angular/material/card';
import { FormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { AuthInterceptor } from './services/auth.interceptor';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { User } from './core/models/user';
import { ArchivedDocumentsComponent } from './archived-documents/archived-documents.component';

import{ ReactiveFormsModule} from '@angular/forms';
import { MatCheckbox, MatCheckboxModule } from '@angular/material/checkbox';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ViewPermisionsComponent } from './screens/view-permisions/view-permisions.component';






@NgModule({
  declarations: [
    AppComponent,
  
    

    
  ],
  imports: [
    ChartsModule,
    UserAuthModule,
    BrowserModule,
    FormsModule,
    MatTableModule,
    AppRoutingModule,
    SharedModule,
    LayoutModule,
    RouterModule,
    RouterOutlet,
    CommonModule,
    BrowserAnimationsModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    CoreModule,
    FormsModule,
    MatInputModule,
    MatIconModule,
    MatFormFieldModule,
    MatDialogModule,
    MatCardModule,
    MatButtonModule, 
    MatProgressSpinnerModule,
    MatTabsModule,
    MatSelectModule, 
    MatInputModule,
    MatFormFieldModule,
    MatTooltipModule
    
     

  
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass:AuthInterceptor,
      multi:true
    },
    { provide: ErrorStateMatcher, useClass: ShowOnDirtyErrorStateMatcher },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
