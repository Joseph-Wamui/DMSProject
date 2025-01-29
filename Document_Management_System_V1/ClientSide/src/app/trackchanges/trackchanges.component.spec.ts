import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrackchangesComponent } from './trackchanges.component';

describe('TrackchangesComponent', () => {
  let component: TrackchangesComponent;
  let fixture: ComponentFixture<TrackchangesComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [TrackchangesComponent]
    });
    fixture = TestBed.createComponent(TrackchangesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
