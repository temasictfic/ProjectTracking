import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { ProjectDetailComponent } from './project-detail.component';

describe('Project Management Detail Component', () => {
  let comp: ProjectDetailComponent;
  let fixture: ComponentFixture<ProjectDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProjectDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              component: ProjectDetailComponent,
              resolve: { project: () => of({ id: 123 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(ProjectDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load project on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', ProjectDetailComponent);

      // THEN
      expect(instance.project()).toEqual(expect.objectContaining({ id: 123 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
