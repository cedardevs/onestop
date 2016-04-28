import React from 'react'
import IndexDropDown from '../components/IndexDropDown'
import TextSearchField from '../components/TextSearchField'
import Colors from 'material-ui/lib/styles/colors';

const SearchFacet = ({indexName, submit, handleIndexChange}) => {
  const styles = {
    divStyle: {
      paddingRight: 20
    },
    textField: {
      display: 'inline-block',
      width: 300
    },
    dropDown: {
      display: 'inline-block',
      width: 200,
      paddingLeft: 10
    }
  };

  return <div style={styles.divStyle}>
    <div style={styles.textField}>
      <TextSearchField onEnterKeyDown={submit}/>
    </div>
    <div style={styles.dropDown}>
      <IndexDropDown indexName={indexName} onChange={handleIndexChange}/>
    </div>
  </div>
};

export default SearchFacet