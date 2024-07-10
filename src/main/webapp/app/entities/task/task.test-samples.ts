import dayjs from 'dayjs/esm';

import { ITask, NewTask } from './task.model';

export const sampleWithRequiredData: ITask = {
  id: 26965,
  title: 'tall',
  creationDate: dayjs('2024-07-09T00:50'),
  status: 'COMPLETED',
};

export const sampleWithPartialData: ITask = {
  id: 22716,
  title: 'yuck',
  description: 'furthermore',
  creationDate: dayjs('2024-07-09T23:38'),
  status: 'NEW',
};

export const sampleWithFullData: ITask = {
  id: 4041,
  title: 'so',
  description: 'video except',
  creationDate: dayjs('2024-07-09T22:41'),
  status: 'NEW',
};

export const sampleWithNewData: NewTask = {
  title: 'outrageous excellent depart',
  creationDate: dayjs('2024-07-09T21:16'),
  status: 'IN_PROGRESS',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
