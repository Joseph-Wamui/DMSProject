import { Component, ElementRef, Inject, ViewChild } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-delete-dialog',
  templateUrl: './delete-dialog.component.html',
  styleUrls: ['./delete-dialog.component.css']
})
export class DeleteDialogComponent {
 document: any;
  @ViewChild('yesButton')
  yesButton!: ElementRef;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    public dialogRef: MatDialogRef<DeleteDialogComponent>,
   // private documentService: GetDocumentsService,
  ) {}

ngOnInit() {
  console.log("Delete Dialog Initialized.........");
 // this.document = this.documentService.getSelectedDocument();
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
