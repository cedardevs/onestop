import React from 'react';
import { connect } from 'react-redux'
import DropDownMenu from 'material-ui/lib/DropDownMenu';
import MenuItem from 'material-ui/lib/menus/menu-item';

const facetText = ["test", "title", "people"];
const dropDownEntries = [];
for (let i = 0; i < facetText.length; i++ ) {
  dropDownEntries.push(<MenuItem value={i} key={i} label={`${facetText[i]}`} primaryText={`${facetText[i]}`}/>);
}

const IndexDropDown = ({index, onChange}) => {

  const handleChange = (event, index, textContent) => {
    console.log("Changing index to "+ index+" with value "+ textContent);
    onChange(index, textContent);
  }

  return <DropDownMenu maxHeight={300} value={index} onChange={handleChange} autoWidth={false}>
    {dropDownEntries}
  </DropDownMenu>
};

export default IndexDropDown