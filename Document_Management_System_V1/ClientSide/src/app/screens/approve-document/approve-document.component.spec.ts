import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApproveDocumentComponent } from './approve-document.component';

describe('ApproveDocumentComponent', () => {
  let component: ApproveDocumentComponent;
  let fixture: ComponentFixture<ApproveDocumentComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ApproveDocumentComponent]
    });
    fixture = TestBed.createComponent(ApproveDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
