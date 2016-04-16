import React from 'react'
import AppBar from 'material-ui/lib/app-bar';
import TextSearchField from './TextSearchField.jsx';
import ResultsContainer from '../containers/ResultsContainer';

const Root = () => (
    <div>
      <AppBar title="Evan's Fancy OneStop Sandbox" showMenuIconButton={false}>
        <TextSearchField/>
      </AppBar>
      <ResultsContainer/>
    </div>
);

export default Root
