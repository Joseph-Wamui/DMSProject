import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPermisionsComponent } from './view-permisions.component';

describe('ViewPermisionsComponent', () => {
  let component: ViewPermisionsComponent;
  let fixture: ComponentFixture<ViewPermisionsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ViewPermisionsComponent]
    });
    fixture = TestBed.createComponent(ViewPermisionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
