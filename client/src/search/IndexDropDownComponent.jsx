import React from 'react';
import { connect } from 'react-redux'
import SelectField from '../../node_modules/material-ui/lib/select-field';
import MenuItem from '../../node_modules/material-ui/lib/menus/menu-item';

const IndexDropDown = ({indexName, onChange}) => {
  const styles = {
    root: {
      width: '100%'
    }
  };

  const facets = [
    {value: '', label: 'Full Text Search'},
    {value: 'title', label: 'Title'},
    {value: 'people', label: 'People'},
    {value: 'fileIdentifier', label: 'File Identifier'},
    {value: 'enddate', label: 'End Date'},
    {value: 'startdate', label: 'Start Date'}
  ];

  let i = 0;
  const dropDownEntries = facets.map((facet) => {
    return <MenuItem value={facet.value} key={i++} label={facet.label} primaryText={facet.label}/>
  });

  const handleChange = (e, i, v) => onChange(v);

  return <SelectField style={styles.root} value={indexName} onChange={handleChange}>
    {dropDownEntries}
  </SelectField>
};

export default IndexDropDown
