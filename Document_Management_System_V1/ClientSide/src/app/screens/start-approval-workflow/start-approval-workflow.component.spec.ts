import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StartApprovalWorkflowComponent } from './start-approval-workflow.component';

describe('StartApprovalWorkflowComponent', () => {
  let component: StartApprovalWorkflowComponent;
  let fixture: ComponentFixture<StartApprovalWorkflowComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [StartApprovalWorkflowComponent]
    });
    fixture = TestBed.createComponent(StartApprovalWorkflowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
