import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LineGraphComponent } from './linegraph.component';

describe('LinegraphComponent', () => {
  let component: LineGraphComponent;
  let fixture: ComponentFixture<LineGraphComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [LineGraphComponent]
    });
    fixture = TestBed.createComponent(LineGraphComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
