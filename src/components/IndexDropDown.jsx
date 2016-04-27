import React from 'react';
import { connect } from 'react-redux'
import DropDownMenu from 'material-ui/lib/DropDownMenu';
import MenuItem from 'material-ui/lib/menus/menu-item';
import Colors from 'material-ui/lib/styles/colors';

const facets = [
  {value: '', label: 'Full Text'},
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

const IndexDropDown = ({indexName, onChange}) => {
  const styles = {
    root: {
      boxShadow: '2px 1px 2px 0px #888888',
      borderColor: Colors.black,
      border: '2px'
    },
    iconStyle: {
      background: Colors.black,
      borderColor: Colors.black,
      border: '2px'
    },
    labelStyle: {
      borderColor: Colors.black,
      border: '2px'
    },
    menuStyle: {
      borderColor: Colors.black,
      border: '2px'
    },
    underlineStyle: {
      borderColor: Colors.white,
      border: '2px'
    }
  };

  const handleChange = (event, index, value) => {
    console.debug("Changing index to "+ index+" with value "+ facets[index].value);
    onChange(value);
  };

  return <DropDownMenu
      maxHeight={300}
      value={indexName}
      onChange={handleChange}
      autoWidth={true}
      style={styles.root}
      iconStyle={styles.icon}
      labelStyle={styles.labelStyle}
      menuStyle={styles.menuStyle}
      underlineStyle={styles.underlineStyle}>
    {dropDownEntries}
  </DropDownMenu>
};

export default IndexDropDown