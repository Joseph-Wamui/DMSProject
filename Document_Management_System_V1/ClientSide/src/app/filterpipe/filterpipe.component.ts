import { Component, Pipe, PipeTransform } from '@angular/core';
import { User } from 'src/app/core/models/user';

@Pipe({
  name: 'filter'
})
export class FilterPipe implements PipeTransform {
  transform(items: User[] | null, searchText: string): User[] | null {
    if (!items || !searchText) {
      return items;
    }
    searchText = searchText.toLowerCase();
    return items.filter((item: User) => {
      return item.fullName.toLowerCase().includes(searchText);
    });
  }
}

@Component({
  selector: 'app-filterpipe',
  templateUrl: './filterpipe.component.html',
  styleUrls: ['./filterpipe.component.css']
})
export class FilterpipeComponent {

}
