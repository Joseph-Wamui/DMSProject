import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationServiceComponent } from './notification-service.component';

describe('NotificationServiceComponent', () => {
  let component: NotificationServiceComponent;
  let fixture: ComponentFixture<NotificationServiceComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NotificationServiceComponent]
    });
    fixture = TestBed.createComponent(NotificationServiceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
