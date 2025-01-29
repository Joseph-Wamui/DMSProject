import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RetentionDialogComponent } from './retention-dialog.component';

describe('RetentionDialogComponent', () => {
  let component: RetentionDialogComponent;
  let fixture: ComponentFixture<RetentionDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [RetentionDialogComponent]
    });
    fixture = TestBed.createComponent(RetentionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
