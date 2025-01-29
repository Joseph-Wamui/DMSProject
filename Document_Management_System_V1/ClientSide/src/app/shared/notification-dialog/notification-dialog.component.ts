import { ChangeDetectorRef, Component } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';



@Component({
  selector: 'app-notification-dialog',
  templateUrl: './notification-dialog.component.html',
  styleUrls: ['./notification-dialog.component.css']
})
export class NotificationDialogComponent {
  constructor(public dialogRef: MatDialogRef<NotificationDialogComponent>) {}

  closeDialog(): void {
      this.dialogRef.close();
  }
}