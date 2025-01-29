import { Component, ElementRef, ViewChild } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-dialog',
  templateUrl: './dialog.component.html',
  styleUrls: ['./dialog.component.css']
})
export class DialogComponent {
  @ViewChild('yesButton')
  yesButton!: ElementRef;

  constructor(public dialogRef: MatDialogRef<DialogComponent>) {}

ngOnInit() {
  // Listen for keydown event on the document
  document.addEventListener('keydown', (event) => {
    // Check if the pressed key is Enter
    if (event.key === 'Enter') {
      // Trigger click event on the Yes button
      this.yesButton.nativeElement.click();
    }
  });
}
 
}
