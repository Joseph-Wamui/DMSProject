import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ArchivedDocumentsComponent } from './archived-documents.component';

describe('ArchivedDocumentsComponent', () => {
  let component: ArchivedDocumentsComponent;
  let fixture: ComponentFixture<ArchivedDocumentsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ArchivedDocumentsComponent]
    });
    fixture = TestBed.createComponent(ArchivedDocumentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
