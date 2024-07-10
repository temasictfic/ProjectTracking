import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'projectTrackingApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'project',
    data: { pageTitle: 'projectTrackingApp.project.home.title' },
    loadChildren: () => import('./project/project.routes'),
  },
  {
    path: 'task',
    data: { pageTitle: 'projectTrackingApp.task.home.title' },
    loadChildren: () => import('./task/task.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
