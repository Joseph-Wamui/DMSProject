import { Injectable, EventEmitter } from '@angular/core';

@Injectable({
 providedIn: 'root',
})
export class SnackbarService {
 private snackbarEvent = new EventEmitter<{ message: string; action: string }>();

 constructor() {}

 showSnackbar(message: string, action: string = 'Close') {
    this.snackbarEvent.emit({ message, action });
 }

 getSnackbarEvent() {
    return this.snackbarEvent.asObservable();
 }
}
