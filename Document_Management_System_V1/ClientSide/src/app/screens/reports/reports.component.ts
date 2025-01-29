import { Component } from '@angular/core';
//import { saveAs } from 'file-saver'; // Import saveAs fm file-saverro

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})

export class ReportsComponent{
  expandedCardId: string = '';

  expandCard(cardId: string) {
    this.expandedCardId = this.expandedCardId === cardId ? '' : cardId;
  }
  
}
