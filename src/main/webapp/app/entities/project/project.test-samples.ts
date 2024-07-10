import dayjs from 'dayjs/esm';

import { IProject, NewProject } from './project.model';

export const sampleWithRequiredData: IProject = {
  id: 16756,
  name: 'nor irradiate',
  startDate: dayjs('2024-07-09'),
  endDate: dayjs('2024-07-09'),
};

export const sampleWithPartialData: IProject = {
  id: 16929,
  name: 'against but wage',
  startDate: dayjs('2024-07-09'),
  endDate: dayjs('2024-07-09'),
};

export const sampleWithFullData: IProject = {
  id: 24469,
  name: 'establish',
  startDate: dayjs('2024-07-09'),
  endDate: dayjs('2024-07-09'),
};

export const sampleWithNewData: NewProject = {
  name: 'alienated',
  startDate: dayjs('2024-07-09'),
  endDate: dayjs('2024-07-09'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
