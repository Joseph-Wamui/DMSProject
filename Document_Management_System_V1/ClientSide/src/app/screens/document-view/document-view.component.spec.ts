import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentViewComponent } from './document-view.component';

describe('DocumentViewComponent', () => {
  let component: DocumentViewComponent;
  let fixture: ComponentFixture<DocumentViewComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [DocumentViewComponent]
    });
    fixture = TestBed.createComponent(DocumentViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
