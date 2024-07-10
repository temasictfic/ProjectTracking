import dayjs from 'dayjs/esm';
import { IProject } from 'app/entities/project/project.model';
import { TaskStatus } from 'app/entities/enumerations/task-status.model';

export interface ITask {
  id: number;
  title?: string | null;
  description?: string | null;
  creationDate?: dayjs.Dayjs | null;
  status?: keyof typeof TaskStatus | null;
  project?: Pick<IProject, 'id' | 'name'> | null;
}

export type NewTask = Omit<ITask, 'id'> & { id: null };
