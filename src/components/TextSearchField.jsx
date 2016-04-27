import React from 'react'
import IndexDropDown from '../components/IndexDropDown'
import TextField from 'material-ui/lib/text-field';
import Colors from 'material-ui/lib/styles/colors';

const TextSearchField = ({onEnterKeyDown}) => {
  const styles = {
    inputStyle: {
      color: Colors.black
    },
    underlineStyle: {
      borderColor: '#ffffff'
    },
    underlineFocusStyle: {
      borderColor: Colors.indigo400
    },
    root: {
      borderColor: Colors.black,
      border: '1px'
    }

  };

  const handleSubmit = (e) => onEnterKeyDown(e.target.value);

  return <div>
      <TextField
          hintText="Search for NCEI data"
          hintStyle={styles.inputStyle}
          fullWidth={true}
          onEnterKeyDown={handleSubmit}
          style={styles.root}
          underlineStyle={styles.underlineStyle}
          underlineFocusStyle={styles.underlineFocusStyle}
          inputStyle={styles.inputStyle}
      /></div>
};

export default TextSearchField