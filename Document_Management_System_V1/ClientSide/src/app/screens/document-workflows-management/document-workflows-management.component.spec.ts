import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DocumentWorkflowsManagementComponent } from './document-workflows-management.component';

describe('DocumentWorkflowsManagementComponent', () => {
  let component: DocumentWorkflowsManagementComponent;
  let fixture: ComponentFixture<DocumentWorkflowsManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DocumentWorkflowsManagementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DocumentWorkflowsManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
