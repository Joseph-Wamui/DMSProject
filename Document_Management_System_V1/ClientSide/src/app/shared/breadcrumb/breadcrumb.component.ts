import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';


@Component({
  selector: 'app-breadcrumb',
  templateUrl: './breadcrumb.component.html',
  styleUrls: ['./breadcrumb.component.css']
})
export class BreadcrumbComponent implements OnInit, OnDestroy {
  crumbs: string[] = [];
  private subscription: Subscription = new Subscription;

  constructor(private breadcrumbService: BreadcrumbService) { }

  ngOnInit(): void {
    this.subscription = this.breadcrumbService.breadcrumbs$.subscribe(crumbs => {
      this.crumbs = crumbs;
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
}