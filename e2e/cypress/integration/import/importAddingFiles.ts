import {
  cleanImportData,
  generateBaseImportData,
  login,
} from '../../common/apiCalls';
import 'cypress-file-upload';
import { gcy } from '../../common/shared';
import {
  getFileIssuesDialog,
  getLanguageRow,
  visitImport,
} from '../../common/import';

describe('Import Adding files', () => {
  beforeEach(() => {
    cleanImportData();

    generateBaseImportData().then((project) => {
      login('franta');
      visitImport(project.body.id);
    });
  });

  it('uploads .po with dropzone', () => {
    cy.get('[data-cy=dropzone]').attachFile('import/po/example.po', {
      subjectType: 'drag-n-drop',
    });
    gcy('import-result-total-count-cell').should('contain.text', '8');
  });

  it('uploads with input', () => {
    gcy('import-file-input').attachFile('import/po/example.po');
    gcy('import-result-total-count-cell').should('contain.text', '8');
  });

  it('uploads multiple xliffs', () => {
    gcy('import-file-input').attachFile([
      'import/xliff/larger.xlf',
      'import/xliff/example.xliff',
      'import/xliff/error_example.xliff',
    ]);

    gcy('import-result-total-count-cell', { timeout: 10000 }).should('exist');
    getLanguageRow('larger.xlf (en)').should('contain.text', '1151');
    getLanguageRow('larger.xlf (cs)').should('contain.text', '1151');
    getLanguageRow('example.xliff (en)')
      .findDcy('import-result-total-count-cell')
      .should('contain.text', '176');
  });

  it(
    'has valid xliff errors',
    {
      retries: {
        runMode: 3,
      },
    },
    () => {
      gcy('import-file-input').attachFile('import/xliff/error_example.xliff');

      gcy('import-result-file-cell')
        .findDcy('import-result-file-warnings')
        .should('contain.text', '4');
      gcy('import-result-file-cell')
        .findDcy('import-file-issues-button')
        .click();
      getFileIssuesDialog()
        .contains('Target translation not provided (key name: vpn.main.back)')
        .should('be.visible');
      getFileIssuesDialog()
        .contains(
          'Id attribute of translation not provided ' +
            '(File: ../src/platforms/android/androidauthenticationview.qml)'
        )
        .should('be.visible');
    }
  );

  it('dropzone highlights on drag over', () => {
    cy.fixture('import/simple.json').then((json) => {
      const dt = new DataTransfer() || new ClipboardEvent('').clipboardData;
      const blob = new Blob([JSON.stringify(json, null, 2)], {
        type: 'application/json',
      });
      const file = new File([blob], 'sample.jpg', { type: 'image/png' });
      dt.items.add(file);
      cy.get('[data-cy=dropzone]').trigger('dragenter', {
        dataTransfer: dt,
      });
      cy.get('[data-cy=dropzone-inner]').should(
        'have.css',
        'background-color',
        'rgb(232, 245, 233)'
      );
      cy.get('[data-cy=dropzone]').trigger('dragleave', {
        dataTransfer: dt,
      });
      cy.get('[data-cy=dropzone-inner]').should(
        'have.css',
        'background-color',
        'rgba(0, 0, 0, 0)'
      );
    });
  });

  it('dropzone highlights on drag over (error)', () => {
    cy.fixture('import/simple.json').then((json) => {
      const dt = new DataTransfer() || new ClipboardEvent('').clipboardData;
      const blob = new Blob([JSON.stringify(json, null, 2)], {
        type: 'application/json',
      });
      const file = new File([blob], 'sample.jpg', { type: 'image/png' });
      for (let i = 0; i < 21; i++) {
        dt.items.add(file);
      }
      cy.get('[data-cy=dropzone]').trigger('dragenter', {
        dataTransfer: dt,
      });
      cy.get('[data-cy=dropzone-inner]').should(
        'have.css',
        'background-color',
        'rgb(255, 235, 238)'
      );
      cy.get('[data-cy=dropzone]').trigger('dragleave', {
        dataTransfer: dt,
      });
      cy.get('[data-cy=dropzone-inner]').should(
        'have.css',
        'background-color',
        'rgba(0, 0, 0, 0)'
      );
    });
  });

  after(() => {
    cleanImportData();
  });
});
