import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminLayot } from './admin-layot';

describe('AdminLayot', () => {
  let component: AdminLayot;
  let fixture: ComponentFixture<AdminLayot>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminLayot],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminLayot);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
