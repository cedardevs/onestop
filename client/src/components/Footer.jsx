import React from 'react'
import { connect } from 'react-redux'

const styles = {
  footer: {
    height: 24,
    'textAlign': 'center',
    'color' : '#ffffff',
    'backgroundColor': '#01568D'
  }
};

let Footer = ({dispatch}) => {
  return (
  <div style={styles.footer}>
    Powered by NCEI-MD GeoPortal
  </div>
  );
};

Footer = connect()(Footer);
export default Footer