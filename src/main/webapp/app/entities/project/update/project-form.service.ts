import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IProject, NewProject } from '../project.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IProject for edit and NewProjectFormGroupInput for create.
 */
type ProjectFormGroupInput = IProject | PartialWithRequiredKeyOf<NewProject>;

type ProjectFormDefaults = Pick<NewProject, 'id'>;

type ProjectFormGroupContent = {
  id: FormControl<IProject['id'] | NewProject['id']>;
  name: FormControl<IProject['name']>;
  startDate: FormControl<IProject['startDate']>;
  endDate: FormControl<IProject['endDate']>;
};

export type ProjectFormGroup = FormGroup<ProjectFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ProjectFormService {
  createProjectFormGroup(project: ProjectFormGroupInput = { id: null }): ProjectFormGroup {
    const projectRawValue = {
      ...this.getFormDefaults(),
      ...project,
    };
    return new FormGroup<ProjectFormGroupContent>({
      id: new FormControl(
        { value: projectRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(projectRawValue.name, {
        validators: [Validators.required],
      }),
      startDate: new FormControl(projectRawValue.startDate, {
        validators: [Validators.required],
      }),
      endDate: new FormControl(projectRawValue.endDate, {
        validators: [Validators.required],
      }),
    });
  }

  getProject(form: ProjectFormGroup): IProject | NewProject {
    return form.getRawValue() as IProject | NewProject;
  }

  resetForm(form: ProjectFormGroup, project: ProjectFormGroupInput): void {
    const projectRawValue = { ...this.getFormDefaults(), ...project };
    form.reset(
      {
        ...projectRawValue,
        id: { value: projectRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ProjectFormDefaults {
    return {
      id: null,
    };
  }
}
