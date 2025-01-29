import { Injectable, EventEmitter } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
 providedIn: 'root'
})
export class SelectionService {
 private selectedDocuments = new BehaviorSubject<string[]>([]);
 public onSelectionChange = new EventEmitter<string[]>();

 selectedDocuments$ = this.selectedDocuments.asObservable();

 constructor() {
   this.selectedDocuments.subscribe(documentIds => {
     this.onSelectionChange.emit(documentIds);
   });
 }

 selectDocument(id: string): void {
    const currentSelection = this.selectedDocuments.getValue();
    if (!currentSelection.includes(id)) {
      this.selectedDocuments.next([...currentSelection, id]);
    }
 }

 deselectDocument(id: string): void {
    const currentSelection = this.selectedDocuments.getValue();
    this.selectedDocuments.next(currentSelection.filter(docId => docId !== id));
 }

 isDocumentSelected(id: string): boolean {
    return this.selectedDocuments.getValue().includes(id);
 }

}
