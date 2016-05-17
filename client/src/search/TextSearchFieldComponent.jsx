import React from 'react'
import IndexDropDown from './IndexDropDownComponent'
import TextField from '../../node_modules/material-ui/lib/text-field';
import Colors from '../../node_modules/material-ui/lib/styles/colors';

const TextSearchField = ({onEnterKeyDown}) => {
  const handleSubmit = (e) => onEnterKeyDown(e.target.value);

  return <TextField
      hintText="Enter Search Term"
      fullWidth={true}
      onEnterKeyDown={handleSubmit}
  />
};

export default TextSearchField