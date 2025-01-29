import { Component, Input, Output, EventEmitter, OnInit, OnDestroy } from '@angular/core';

@Component({
 selector: 'app-custom-snackbar',
 templateUrl: './custom-snackbar.component.html',
 styleUrls: ['./custom-snackbar.component.css']
})
export class CustomSnackbarComponent implements OnInit, OnDestroy {
 @Input() message: string = '';
 @Input() action: string = 'Close';
 @Input() duration: number = 5000; // Default duration in milliseconds
 @Output() close = new EventEmitter<void>();

 private closeTimeout: any;

 ngOnInit(): void {
    this.closeTimeout = setTimeout(() => {
      this.closeSnackbar();
    }, this.duration);
 }

 ngOnDestroy(): void {
    clearTimeout(this.closeTimeout);
 }

 closeSnackbar(): void {
    console.log('Close button clicked or snackbar duration expired');
    this.close.emit();
 }
}
