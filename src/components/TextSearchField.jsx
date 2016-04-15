import React from 'react'
import { connect } from 'react-redux'
import TextField from 'material-ui/lib/text-field';
import Colors from 'material-ui/lib/styles/colors';
import { textSearch } from '../actions/search'

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

let TextSearchField = ({dispatch}) => {
  let text = '';
  const updateText = (e) => text = e.target.value;
  const submit = () => dispatch(textSearch(text));

  return (
      <TextField
          hintText="Search for NCEI data"
          underlineStyle={styles.underlineStyle}
          underlineFocusStyle={styles.underlineFocusStyle}
          inputStyle={styles.inputStyle}
          onChange={updateText}
          onEnterKeyDown={submit}
      />
  );
};

TextSearchField = connect()(TextSearchField);
export default TextSearchField