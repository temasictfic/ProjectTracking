import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '59687cda-700d-43fc-b484-3438a24c8e52',
};

export const sampleWithPartialData: IAuthority = {
  name: '34a861e7-f73b-475d-a54a-c9f3857d8c84',
};

export const sampleWithFullData: IAuthority = {
  name: 'e2495a4a-b209-4bf0-80cc-a557cfad7600',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
