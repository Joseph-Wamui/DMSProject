import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class StateService {
  private expandedCardIdSource = new BehaviorSubject<string | null>(null);
  expandedCardId$ = this.expandedCardIdSource.asObservable();

  setExpandedCardId(cardId: string | null) {
    this.expandedCardIdSource.next(cardId);
  }

  private state: any = {};

  setState(key: string, value: any) {
    this.state[key] = value;
  }

  getState(key: string) {
    return this.state[key];
  }
}
