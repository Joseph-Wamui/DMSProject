import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserroleupdateComponent } from './userroleupdate.component';

describe('UserroleupdateComponent', () => {
  let component: UserroleupdateComponent;
  let fixture: ComponentFixture<UserroleupdateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserroleupdateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserroleupdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
