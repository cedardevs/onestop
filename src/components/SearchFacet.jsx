import React from 'react'
import IndexDropDown from '../components/IndexDropDown'
import TextSearchField from '../components/TextSearchField'
import Colors from 'material-ui/lib/styles/colors';

const SearchFacet = ({indexIndex, submit, handleIndexChange}) => {
  const styles = {
    divStyle: {
      margin: 'auto',
      width: 800
    },
    textField: {
      float: 'left',
      width: '20%'
    },
    dropDown: {
      float: 'left',
      width: '50%'
    }
  };

  return <div style={styles.divStyle}>
    <div style={styles.textField}>
      <TextSearchField onEnterKeyDown={submit}/>
    </div>
    <div style={styles.dropDown}>
      <IndexDropDown index={indexIndex} onChange={handleIndexChange}/>
    </div>
    </div>
};

export default SearchFacet