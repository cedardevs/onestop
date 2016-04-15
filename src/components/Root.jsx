import React from 'react'
import AppBar from 'material-ui/lib/app-bar';
import TextSearchField from './TextSearchField.jsx';

const Root = () => (
    <AppBar title="Evan's Fancy OneStop Sandbox" showMenuIconButton={false}>
      <TextSearchField/>
    </AppBar>
);

export default Root
