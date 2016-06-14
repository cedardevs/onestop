const twitterIconPath = require('../../img/twitter_icon.png');
const noaaIconPath = require('../../img/NOAA_logo.png');

import React from 'react'
import { connect } from 'react-redux'

const ICON_SIZE = 30;

const styles = {
  top: {
    backgroundColor: 'blue',
    display: '-webkit-flex',
    WebkitJustifyContent: 'space-around',
    justifyContent: 'space-around',
    width: '100vw',
    height: '220px',
    fontFamily: 'Source Sans Pro, Merriweather',
    fontSize: '20px',
    lineHeight: '20px',
    color: 'white',
  },
  leftHalf: {
    textAlign: 'left',
    width: '35%',
  },
  rightHalf: {
    textAlign: 'right',
    width: '55%',
  },
  about: {
    display: 'flex',
    fontSize: '25px',
    lineHeight: '35px',
    textDecoration: 'none',
    color: 'white'
  },
  hidden: {
    visibility: 'hidden'
  },
  icons: {
    display: 'inline-block',
  },
  twitterLogo: {
    backgroundImage: 'url('+twitterIconPath+')',
    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',
    width: ICON_SIZE+'px',
    height: ICON_SIZE+'px',
    display: 'inline-block',
    margin: '10px'
  },
  facebookLogo: {
    backgroundImage: 'url('+twitterIconPath+')',
    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',
    width: ICON_SIZE+'px',
    height: ICON_SIZE+'px',
    display: 'inline-block',
    margin: '10px',
  },
  instagrammLogo: {
    backgroundImage: 'url('+twitterIconPath+')',
    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',
    width: ICON_SIZE+'px',
    height: ICON_SIZE+'px',
    display: 'inline-block',
    margin: '10px'
  },
  youtubeLogo: {
    backgroundImage: 'url('+twitterIconPath+')',
    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',
    width: ICON_SIZE+'px',
    height: ICON_SIZE+'px',
    display: 'inline-block',
    margin: '10px'
  },

  noaaLogo: {
    backgroundImage: 'url('+noaaIconPath+')',
    backgroundSize: 'contain',
    backgroundRepeat: 'no-repeat',
    width: '50px',
    height: '50px',
    display: 'inline-block',
    margin: '15px'
  },
  feedbackButton: {
    backgroundColor: 'grey',
    margin: '10px',
  },
  menu: {
    listStyleType: 'none',
    margin: '10px',
    lineHeight: '15px',
    fontSize: '15px',
  },
  menuLi: {
    borderLeft: '2px solid white',
    display: 'inline-block',
    margin: '3px',
    padding: '0px 0px 0px 5px',
  },
  menuLiA: {
    color: 'white',
    textDecoration: 'none',
  }
};

let Footer = ({dispatch}) => {
  return (
    <div style={styles.top}>
      <div style={styles.leftHalf}>
        <a href="" style={styles.about}>About our agency</a>
        <a href="" style={styles.about}>News and features</a>
        <a href="" style={styles.about}>Our work</a>
        <a href="/" style={styles.noaaLogo}>
          <span style={styles.hidden}>NOAA Home</span>
        </a>
        <span>Science. Service. Stewardship.</span>
      </div>

      <div style={styles.rightHalf}>
        <div style={styles.icons}>
          Stay connected:
          <a href="" style={styles.twitterLogo}>
            <span style={styles.hidden}>Share to Twitter</span></a>
          <a href="" style={styles.facebookLogo}>
            <span style={styles.hidden}>Share to Facebook</span></a>
          <a href="" style={styles.instagrammLogo}>
            <span style={styles.hidden}>Share to Instagramm</span></a>
          <a href="" style={styles.youtubeLogo}>
            <span style={styles.hidden}>Share to Youtube</span></a>
        </div>
        <div>
          How are we doing? <button style={styles.feedbackButton}>Feedback</button>
        </div>
        <div>
          <ul style={styles.menu}>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Protecting your Privacy</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>FOIA</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Information Quality</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Disclaimer</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>USA.gov</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Ready.gov</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>EEO</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Employee Check-In</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Contact us</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Staff Directory</a></li>
            <li style={styles.menuLi}><a href="" style={styles.menuLiA}>Need help?</a></li>
          </ul>
        </div>
      </div>
    </div>
  );
};

Footer = connect()(Footer);
export default Footer
