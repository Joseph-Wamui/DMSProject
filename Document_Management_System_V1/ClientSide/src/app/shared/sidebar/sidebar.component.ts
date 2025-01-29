import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/core/models/user';
import { UserAuthService } from 'src/app/user-auth/_service/user-auth.service';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit{
  user:User= new User;
  role: string = '';
  isSubMenuVisible = false;

  constructor(
    private userService: UserAuthService,
  ){
    
  }
  ngOnInit(): void {
    this.getCurrentUser();
    //console.log("role:", this.role);
  }

  toggleSubMenu() {
     this.isSubMenuVisible = !this.isSubMenuVisible;
  }
  public getCurrentUser(){
    this.user = this.userService.getLoggedInUser();
    if (this.user) {
      this.role = this.user.role;
    }
  }

  isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

}
