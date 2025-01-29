import { Component } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-view-permisions',
  templateUrl: './view-permisions.component.html',
  styleUrls: ['./view-permisions.component.css']
})
export class ViewPermisionsComponent {
  roleForm!: FormGroup;
  privileges: string[] = [];
  privileges3:string[] = [];// Array for all privileges
  selectedPrivileges: string[] = [];
  showPrivilegesList: boolean = false; // Flag to show/hide privileges list
checked:boolean =false
indeterminate: boolean= false;
roles: string[]=[];
showRoles: boolean=false;

  constructor(
    private formBuilder: FormBuilder,
    private roleService: UsersService,
    private snackBar: MatSnackBar
  ) { }
  ngOnInit(): void {
    this.getRoles();
    this.roleForm = this.formBuilder.group({
      role: [''],
      roleName: [''],  // Ensure this is initialized correctly
      privileges: this.formBuilder.array([]) // Initialize with an empty FormArray
  });
    // Fetch all privileges for display
    this.fetchPrivileges2();
}

// Fetch all roles
getRoles(): void {
    this.roleService.getRoles().subscribe({
        next: (res) => {
            console.log('Server Response for Get Roles:', res);
            this.roles = res.entity;
        },
        error: (error) => {
            console.error('Server Error for Get Roles:', error);
            this.openSnackBar("Error Fetching Roles", false);
        },
        complete: () => {}
    });
}

// Fetch and display all available privileges
fetchPrivileges2(): void {
    this.roleService.getPrivileges().subscribe({
        next: (res) => {
            console.log("Server Response for Privileges:", res);
            this.privileges = res.entity.map((key: string) => key.replace(/_/g, ' '));
            this.privileges3 = [...this.privileges]; // For display purposes
            this.addCheckboxes(); // Initialize checkboxes
            // this.openSnackBar(`${this.privileges.length} Privileges Found`, true);
        },
        error: (error) => {
            console.error("Server Error for Privileges:", error);
            this.openSnackBar("Error Fetching Privileges", false);
        },
        complete: () => {}
    });
}

// Function to add checkbox controls to the FormArray
addCheckboxes() {
    const formArray = this.roleForm.get('privileges') as FormArray;
    formArray.clear(); // Clear existing controls

    this.privileges.forEach(() => {
        formArray.push(new FormControl(false)); 
    });
}

fetchPrivileges(role: string): void {
    this.roleService.getPermissions(role).subscribe({
        next: (res) => {
            console.log("Server Response for Role Privileges:", res);
            const rolePrivileges = res.entity.map((key: string) => key.replace(/_/g, ' '));
            this.showRoles=true;
            
            // Update checkboxes to tick those that match the role privileges
            this.updateCheckboxes(rolePrivileges);

            this.openSnackBar(`${rolePrivileges.length} Privileges Found for Role`, true);
        },
        error: (error) => {
            console.error("Server Error for Role Privileges:", error);
            this.openSnackBar("Error Fetching Role Privileges", false);
        },
        complete: () => {}
    });
}


updateCheckboxes(rolePrivileges: string[]) {
    const formArray = this.roleForm.get('privileges') as FormArray;

    // Update checkboxes to tick those that match the role privileges
    this.privileges.forEach((privilege, index) => {
        if (rolePrivileges.includes(privilege)) {
            formArray.at(index).setValue(true); // Tick the checkbox
        } else {
            formArray.at(index).setValue(false); // Ensure others are unchecked
        }
    });
}
  openSnackBar(message: string, success: boolean): void {
    const panelClass = success ? ['snackbar-success'] : ['snackbar-danger'];
    this.snackBar.open(message, 'X', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: panelClass
    });
  }

  onSubmit(): void {
    // Access the 'roleName' form control properly
    const roleName = this.roleForm.get('role')?.value; // Use safe navigation
    const selectedPrivileges = this.roleForm.value.privileges
        .map((checked: any, i: number) => checked ? this.privileges[i].replace(/ /g, '_') : null)
        .filter((v: string | null) => v !== null); // Correct filtering with types

    console.log(selectedPrivileges); 
    console.log(roleName);// Debugging line to check selected privileges

    const roleData = new FormData();
    roleData.append('name', roleName); // Ensure roleName is not undefined
    roleData.append('privilegesList', selectedPrivileges.join(',')); // Ensure the correct data format
    this.roleService.updateRole(roleData).subscribe({
      next: (res) => {
        console.log("Server RES for Create Role:", res);
        this.openSnackBar(res.message, true);
      },
      error: (error) => {
        console.log("Server ERROR for Create Role:", error);
        this.openSnackBar("Error creating Role", false);
      },
      complete: () => {}
    });
}


  onPrivilegeChange(privilegeId: string, event: any): void {
    // Toggle selected privileges based on checkbox state
    if (event.target.checked) {
      this.selectedPrivileges.push(privilegeId);
    } else {
      this.selectedPrivileges = this.selectedPrivileges.filter(id => id !== privilegeId);
    }
  }

}
