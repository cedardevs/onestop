import React from 'react';
import { connect } from 'react-redux'
import DropDownMenu from 'material-ui/lib/DropDownMenu';
import MenuItem from 'material-ui/lib/menus/menu-item';
import Colors from 'material-ui/lib/styles/colors';


const facetText = ["title", "people", "fileIdentifier", "enddate", "startdate"];//TODO determine how to let them select nothing.
const dropDownEntries = [];
for (let i = 0; i < facetText.length; i++ ) {
  dropDownEntries.push(
      <MenuItem value={i} key={i} label={`${facetText[i]}`} primaryText={`${facetText[i]}`}/>);
}

const IndexDropDown = ({index, onChange}) => {
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
    },
  }

  const handleChange = (event, index) => {
    console.log("Changing index to "+ index+" with value "+ facetText[index]);
    onChange(index, facetText[index]);
  }

  return <DropDownMenu
      maxHeight={300}
      value={index}
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