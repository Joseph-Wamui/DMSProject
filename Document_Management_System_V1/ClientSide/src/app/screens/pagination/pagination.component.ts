import { Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import { PageEvent } from '@angular/material/paginator';

@Component({
  selector: 'app-paginator',
  templateUrl: './pagination.component.html',
  styleUrls: ['./pagination.component.css']
})
export class PaginationComponent implements OnInit {
  ngOnInit(): void { }
  @Input() length: number = 0;
 @Input() pageSize: number = 5;
 @Input() pageSizeOptions: number[] = [5, 10, 20];
 @Output() page = new EventEmitter<PageEvent>();

 handlePageChange(event: PageEvent) {
    this.page.emit(event);
 }
}