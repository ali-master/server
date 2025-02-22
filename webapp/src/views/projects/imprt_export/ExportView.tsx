import { default as React, FunctionComponent, useEffect } from 'react';
import { Box, Button } from '@material-ui/core';
import { T, useTranslate } from '@tolgee/react';
import { useSelector } from 'react-redux';
import { useRouteMatch } from 'react-router-dom';
import { container } from 'tsyringe';

import { BaseView } from 'tg.component/layout/BaseView';
import { LINKS, PARAMS } from 'tg.constants/links';
import { useProject } from 'tg.hooks/useProject';
import { AppState } from 'tg.store/index';
import { ExportActions } from 'tg.store/project/ExportActions';

const actions = container.resolve(ExportActions);

export const ExportView: FunctionComponent = () => {
  const match = useRouteMatch();
  const project = useProject();
  const projectId = match.params[PARAMS.PROJECT_ID];
  const state = useSelector((state: AppState) => state.export.loadables.export);
  const t = useTranslate();

  useEffect(() => {
    if (state.loaded) {
      const url = URL.createObjectURL(state.data);
      const a = document.createElement('a');
      a.href = url;
      a.download = project.name + '.zip';
      a.click();
      actions.loadableReset.export.dispatch();
    }
  }, [state.loading, state.loaded]);

  useEffect(
    () => () => {
      actions.loadableReset.export.dispatch();
    },
    []
  );

  const onJsonExport = () => {
    actions.loadableActions.export.dispatch(projectId);
  };

  return (
    <BaseView
      navigation={[
        [
          project.name,
          LINKS.PROJECT_TRANSLATIONS.build({
            [PARAMS.PROJECT_ID]: project.id,
          }),
        ],
        [
          t('export_translations_title'),
          LINKS.PROJECT_EXPORT.build({
            [PARAMS.PROJECT_ID]: project.id,
          }),
        ],
      ]}
      lg={7}
      md={9}
      containerMaxWidth="lg"
    >
      <Box mt={2}>
        <Button
          component="a"
          variant="outlined"
          color="primary"
          onClick={onJsonExport}
        >
          <T>export_to_json_button</T>
        </Button>
      </Box>
    </BaseView>
  );
};
