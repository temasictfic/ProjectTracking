import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityCreateCancelButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('Task e2e test', () => {
  const taskPageUrl = '/task';
  const taskPageUrlPattern = new RegExp('/task(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const taskSample = { title: 'yowza through finally', creationDate: '2024-07-09T18:48:56.419Z', status: 'NEW' };

  let task;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/tasks+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/tasks').as('postEntityRequest');
    cy.intercept('DELETE', '/api/tasks/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (task) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/tasks/${task.id}`,
      }).then(() => {
        task = undefined;
      });
    }
  });

  it('Tasks menu should load Tasks page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('task');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Task').should('exist');
    cy.url().should('match', taskPageUrlPattern);
  });

  describe('Task page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(taskPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Task page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/task/new$'));
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/tasks',
          body: taskSample,
        }).then(({ body }) => {
          task = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/tasks+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/tasks?page=0&size=20>; rel="last",<http://localhost/api/tasks?page=0&size=20>; rel="first"',
              },
              body: [task],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(taskPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Task page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('task');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('edit button click should load edit Task page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('edit button click should load edit Task page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Task');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);
      });

      it('last delete button click should delete instance of Task', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('task').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', taskPageUrlPattern);

        task = undefined;
      });
    });
  });

  describe('new Task page', () => {
    beforeEach(() => {
      cy.visit(`${taskPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Task');
    });

    it('should create an instance of Task', () => {
      cy.get(`[data-cy="title"]`).type('sternly blah aha');
      cy.get(`[data-cy="title"]`).should('have.value', 'sternly blah aha');

      cy.get(`[data-cy="description"]`).type('meanwhile');
      cy.get(`[data-cy="description"]`).should('have.value', 'meanwhile');

      cy.get(`[data-cy="creationDate"]`).type('2024-07-09T16:48');
      cy.get(`[data-cy="creationDate"]`).blur();
      cy.get(`[data-cy="creationDate"]`).should('have.value', '2024-07-09T16:48');

      cy.get(`[data-cy="status"]`).select('COMPLETED');

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        task = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', taskPageUrlPattern);
    });
  });
});
