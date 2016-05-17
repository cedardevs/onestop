import React from 'react'
import { connect } from 'react-redux'

const styles = {
  header: {
    height: 24,
    'textAlign': 'center',
    'color' : '#ffffff',
    'backgroundColor': '#01568D'
  }
};

let Header = ({dispatch}) => {
  return (
  <div style={styles.header}>OneStop Sandbox</div>
  );
};

Header = connect()(Header);
export default Header