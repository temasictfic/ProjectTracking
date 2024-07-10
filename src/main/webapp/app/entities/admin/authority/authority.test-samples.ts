import { IAuthority, NewAuthority } from './authority.model';

export const sampleWithRequiredData: IAuthority = {
  name: '8aa28436-5d8f-45a1-9920-c9b567f7a303',
};

export const sampleWithPartialData: IAuthority = {
  name: '11e72a88-515b-48f7-a0a0-883fac22e55a',
};

export const sampleWithFullData: IAuthority = {
  name: '8ce5475b-5c57-46e2-9eb3-3ed4b1d0265d',
};

export const sampleWithNewData: NewAuthority = {
  name: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
