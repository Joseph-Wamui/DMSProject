import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl, FormArray } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UsersService } from 'src/app/services/users.service';

@Component({
  selector: 'app-createrole',
  templateUrl: './createrole.component.html',
  styleUrls: ['./createrole.component.css']
})
export class CreateroleComponent implements OnInit {
  roleForm: FormGroup;
  privileges: string[] = []; // Array for all privileges
  selectedPrivileges: string[] = [];
  showPrivilegesList: boolean = false; // Flag to show/hide privileges list
checked:boolean =false
indeterminate: boolean= false;

  constructor(
    private formBuilder: FormBuilder,
    private roleService: UsersService,
    private snackBar: MatSnackBar
  ) {
    this.roleForm = this.formBuilder.group({
      roleName: ['', Validators.required],
      privileges: this.formBuilder.array([])
    });
  }

  ngOnInit(): void {
    // Optionally, fetch privileges here or elsewhere based on your app's requirements
    this.fetchPrivileges();
  }


  private fetchPrivileges(): void {
    this.roleService.getPrivileges().subscribe({
      next: (res) => {
        console.log("Server RES for Privileges:", res);
        this.privileges = res.entity.map((key: string) => key.replace(/_/g, ' '));
        this.addCheckboxes();
        //this.openSnackBar(this.privileges.length + " Privileges Found", true);
        //console.log("privileges", this.privileges)
      },
      error: (error) => {
        console.log("Server ERROR for Privileges:", error);
      },
      complete: () => {}
    });
  }

  private addCheckboxes(): void {
    this.privileges.forEach(() => {
      const control = new FormControl(false); // initially unselected
      (this.roleForm.controls['privileges'] as FormArray).push(control);
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
    const roleName = this.roleForm.controls['roleName'].value;
    const selectedPrivileges = this.roleForm.value.privileges
      .map((checked: any, i: number) => checked ? this.privileges[i] .replace(/ /g, '_') : null)
      .filter((v: null) => v !== null);

    const roleData =new FormData()
    roleData.append('name', roleName);
    roleData.append('privilegesList', selectedPrivileges);
    // {
    //   name: this.roleForm.controls['roleName'].value,
    //   privilegesList: selectedPrivileges
    // };
    // Call role service to create role
    this.roleService.createRole(roleData).subscribe({
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
