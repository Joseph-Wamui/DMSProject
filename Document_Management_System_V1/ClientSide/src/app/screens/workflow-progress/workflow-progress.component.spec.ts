import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkflowProgressComponent } from './workflow-progress.component';

describe('WorkflowProgressComponent', () => {
  let component: WorkflowProgressComponent;
  let fixture: ComponentFixture<WorkflowProgressComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [WorkflowProgressComponent]
    });
    fixture = TestBed.createComponent(WorkflowProgressComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
