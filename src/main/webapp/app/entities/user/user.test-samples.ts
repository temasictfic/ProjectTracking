import { IUser } from './user.model';

export const sampleWithRequiredData: IUser = {
  id: 3282,
  login: 'a0La@fr5fa\\oF\\.UDVJ\\ts\\SazJE\\7EsN',
};

export const sampleWithPartialData: IUser = {
  id: 10829,
  login: 'LDg0vC',
};

export const sampleWithFullData: IUser = {
  id: 13586,
  login: 'r3kLtq@RD\\bqPN',
};
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
