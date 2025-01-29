import { ChangeDetectorRef, Component } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { GetDocumentsService } from '../services/get-documents.service';
import { DialogComponent } from '../shared/dialog/dialog.component';
import { NotificationDialogComponent } from '../shared/notification-dialog/notification-dialog.component';
import { NotificationService } from '../services/notificationService';


interface Notification {
  id: number;
  message: string;
  isRead: boolean;
  timestamp: Date;
  status: string; // "Unread" | "Read"
}

@Component({
  selector: 'app-notification-service',
  templateUrl: './notification-service.component.html',
  styleUrls: ['./notification-service.component.css']
})
export class NotificationServiceComponent {
  readItems: any;
  unreadItems: any;
  document: any;
  notificationCount = 0;
  notifications: Notification[] = [];

  constructor(
    private service: NotificationService,
    private documentService: GetDocumentsService,
    private router: Router,
    public dialogRef: MatDialogRef<NotificationDialogComponent>
  ) {}

  ngOnInit(): void {
    this.getNotifications();
    
  }

  allNotifications: any[] = [];
  unreadNotificationCount: number = 0;
  readNotifications: any[] = [];
  unreadNotifications: any[] = [];
  isNotificationDropdownOpen = false;
  showUnread: boolean = true;
  selectedNotifications: any[] = [];

  getNotifications() {
    console.log('Button clicked');
    if (!this.allNotifications.length) {
      // Fetch notifications only if they are not already loaded
      this.service.getNotifications().subscribe({
        next: (res) => {
          console.log('Received notifications:', res);
          if (res.statusCode === 200) {
            this.allNotifications = res.entity;
            this.updateNotifications(this.allNotifications);
          } else {
            console.error(
              'Failed to fetch notifications. Status code:',
              res.statusCode
            );
          }
        },
        error: (error) => {
          console.error('Error fetching notifications:', error);
        },
      });
    }
  }

  updateNotifications(allNotifications: any[]) {
    this.readNotifications = allNotifications.filter(
      (notification) => notification.status === 'READ'
    );
    this.unreadNotifications = allNotifications.filter(
      (notification) => notification.status === 'UNREAD'
    );

    // Update unread notification count
    this.unreadNotificationCount = this.unreadNotifications.length;

    // Update selected notifications based on the showUnread flag
  }

  formatTimestamp(timestamp: number[]): string {
    const date = new Date(
      timestamp[0],
      timestamp[1],
      timestamp[2],
      timestamp[3],
      timestamp[4],
      timestamp[5],
      timestamp[6]
    );
    const year = timestamp[0];
    const month = String(timestamp[1]).padStart(2, '0');
    const day = String(timestamp[2]).padStart(2, '0');
    const hour = String(timestamp[3]).padStart(2, '0');
    const minute = String(timestamp[4]).padStart(2, '0');
    return `${year}-${month}-${day} ${hour}:${minute}`;
  }

  toggleNotificationDropdown(): void {
    this.isNotificationDropdownOpen = !this.isNotificationDropdownOpen;
    if (this.isNotificationDropdownOpen) {
      this.getNotifications();
    }
  }

  toggleReadStatus(): void {
    this.showUnread = !this.showUnread;
    // Update selected notifications based on the showUnread flag
    this.updateSelectedNotifications();
  }

  updateSelectedNotifications(): void {
    if (this.showUnread) {
      this.selectedNotifications = this.unreadNotifications;
    } else {
      this.selectedNotifications = this.readNotifications;
    }
  }

  markAsRead(notification: any): void {
    const idparam = notification.id;
    const param = `${idparam}`;
    console.log('Notification ID:', param);
    this.service.setNotification(param).subscribe({
      next: (res) => {
        if (res.statusCode === 200) {
          console.log('Notification status changed successfully');
          // Move the notification from unread to read
          notification.status = 'READ';
          this.unreadNotifications = this.unreadNotifications.filter(
            (notif) => notif.id !== notification.id
          );
          this.readNotifications.push(notification);

          // Update unread notification count
          this.unreadNotificationCount = this.unreadNotifications.length;
        }
      },
      error: (error) => {
        console.error('Error marking notification as read:', error);
      },
    });
  }

  getDocbyId(notification: any) {
    console.log('Eye clicked');
    
    const id = notification.documentId;
    console.log('Document ID:', id);
    this.documentService.getSelectedDocumentById(id).subscribe({
      next: (res) => {
        console.log('Document', res);
        this.document = res.entity;
        this.documentService.setSelectedDocument(this.document);
        console.log('Document', this.document);
        this.dialogRef.close();
        this.markAsRead(notification);
        this.router.navigate(['/document-view']);
      },
      error: (error) => {
        console.log('Error fetching Document', error);
      },
      complete: () => {}
    });
  }

  handleNotificationClick(notification: any) {
    this.getDocbyId(notification);
    this.markAsRead(notification);
    
  }
}

