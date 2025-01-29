import { Component, OnInit } from '@angular/core';


@Component({
  selector: 'app-document-workflows-management',
  templateUrl: './document-workflows-management.component.html',
  styleUrls: ['./document-workflows-management.component.css']
})
export class DocumentWorkflowsManagementComponent implements OnInit {
  searchQuery: any;
  assignedUser: string='';
  workflowName: string = '';
  workflowAssignedUser: string = '';
  workflowDueDate: string = '';
  workflowDescription: string = '';
  workflowSteps: any[] = [{ name: 'step 1', type: 'approver', approvers: 'Joseph' }];
  step: any = {};
  documents: any[] = [
    { name: 'Document 1', status: 'Pending', progress: 25 },
    { name: 'Document 2', status: 'In Progress', progress: 50 },
    { name: 'Document 3', status: 'Completed', progress: 100 },
  ];

  ngOnInit(): void {
    // Initialize component
  }

  addStep(): void {
    this.workflowSteps.push({
      name: `Step ${this.workflowSteps.length + 1}`,
      type: '',
      approvers: '', // set an empty string as default
    });
  }

  removeStep(index: number): void {
    this.workflowSteps.splice(index, 1);
  }

  saveWorkflow(): void {
    // Save workflow to backend
  }

  cancelWorkflow(): void {
    // Reset form fields
    this.workflowName = '';
    this.workflowDescription = '';
    this.workflowSteps = [];
  }

  saveStep(_step: any)  {
    // Save step configuration to backend
  }

  cancelStep(): void {
    // Reset step configuration fields
    this.step.type = '';
    this.step.approvers = '';
  }

  viewDetails(_document: any): void {
    // Show detailed information about selected document
  }
}
