import React from 'react'
import IndexDropDown from '../components/IndexDropDown'
import TextField from 'material-ui/lib/text-field';
import Colors from 'material-ui/lib/styles/colors';

const TextSearchField = ({onEnterKeyDown}) => {
  const styles = {
    inputStyle: {
      color: Colors.white
    },
    underlineStyle: {
      borderColor: Colors.white
    },
    underlineFocusStyle: {
      borderColor: Colors.indigo400
    }
  };

  const handleSubmit = (e) => onEnterKeyDown(e.target.value);

  return <div>
      <TextField
          hintText="Search for NCEI data"
          fullWidth={true}
          underlineStyle={styles.underlineStyle}
          underlineFocusStyle={styles.underlineFocusStyle}
          inputStyle={styles.inputStyle}
          onEnterKeyDown={handleSubmit}
      /></div>
};

export default TextSearchField