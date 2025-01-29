import { Injectable } from '@angular/core';
import { Router, RouterStateSnapshot, RouterEvent, NavigationStart } from '@angular/router';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, filter } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {
  private breadcrumbsSource = new BehaviorSubject<string[]>([]);
  breadcrumbs$ = this.breadcrumbsSource.asObservable();

  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event =>!(event instanceof NavigationStart)),
      map(() => this.getUpdatedBreadcrumbs())
    ).subscribe(breadcrumbs => this.breadcrumbsSource.next(breadcrumbs));
  }

  private getUpdatedBreadcrumbs(): string[] {
    let breadcrumbs = [];
    const state = this.router.routerState.snapshot;
    let urlTree = state.root;
    while (urlTree.firstChild) {
      urlTree = urlTree.firstChild;
      if (urlTree.data && urlTree.data['title']) {
        breadcrumbs.unshift(urlTree.data['title']);
      }
    }
    return breadcrumbs;
  }
}