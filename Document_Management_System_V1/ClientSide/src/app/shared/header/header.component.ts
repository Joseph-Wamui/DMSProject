import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';
import { DialogComponent } from '../dialog/dialog.component';

import { Location } from '@angular/common';

import { MatDialog } from '@angular/material/dialog';

import { GetDocumentsService } from 'src/app/services/get-documents.service';
import { NotificationDialogComponent } from '../notification-dialog/notification-dialog.component';
import { NotificationService } from 'src/app/services/notificationService';


@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent implements OnInit {

  allNotifications: any[] = [];
  unreadNotificationCount: number = 0;
  readNotifications: any[] = [];
  unreadNotifications: any[] = [];
  isNotificationDropdownOpen = false;
  showUnread: boolean = true;
  selectedNotifications: any[] = [];


  constructor(
    private userService: UserAuthService,
    private router: Router,
    private dialog: MatDialog,
    private location: Location,
    private service: NotificationService,
    private changeDetectorRef: ChangeDetectorRef,
    private documentService:GetDocumentsService
  ) {}
  ngOnInit(): void {
    this.getNotifications();


  }


  openLogoutConfirmation(): void {
    const dialogRef = this.dialog.open(DialogComponent);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.userService.logout().subscribe({
          next: (res) => {
            sessionStorage.clear();
            console.log('Response:', res);
            this.router.navigate(['/auth/signin'], {
              skipLocationChange: true,
            });

            // Reset the browser's history
            this.location.replaceState('/');
          },
          error: (error) => {
            // Handle logout error
            console.log('Logout error:', error);
          },
          complete: () => {},
        });
      }
    });
  }


  getNotifications() {
    console.log('Button clicked');
    if (!this.allNotifications.length) {
      // Fetch notifications only if they are not already loaded
      this.service.getNotificationNumber().subscribe({
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

  updateNotifications(allNotifications: (string | null)[]) {

    this.unreadNotificationCount = 0;
    allNotifications.forEach(status => {
        // Check if the status is not null
        if (status !== null) {

            switch (status) {
                case "UNREAD":
                    this.unreadNotificationCount++;
                    break;

            }
        }
    });
    const sortedStatus: (string | null)[] = allNotifications.filter(status => status !== null).sort();

    console.log("Sorted Status:", sortedStatus);
}
OpenNotifications() {
  this.dialog.open(NotificationDialogComponent, {
    width:'600px'
  });
  }

}
