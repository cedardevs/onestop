import React from 'react'
import { connect } from 'react-redux'
import styles from './footer.css'

console.log("Stylez:" + JSON.stringify(styles)); 
let Footer = ({dispatch}) => {
  return (
    <div className={styles.top}>
      <div className={styles.leftHalf}>
        <a href="" className={styles.about}>About our agency</a>
        <a href="" className={styles.about}>News and features</a>
        <a href="" className={styles.about}>Our work</a>
        <a href="/" className={styles.noaaLogo}>
          <span className={styles.hidden}>NOAA Home</span>
        </a>
        <span>Science. Service. Stewardship.</span>
      </div>

      <div className={styles.rightHalf}>
        <div className={styles.icons}>
          Stay connected:
          <a href="" className={styles.twitterLogo}>
            <span className={styles.hidden}>Share to Twitter</span></a>
          <a href="" className={styles.facebookLogo}>
            <span className={styles.hidden}>Share to Facebook</span></a>
          <a href="" className={styles.instagrammLogo}>
            <span className={styles.hidden}>Share to Instagramm</span></a>
          <a href="" className={styles.youtubeLogo}>
            <span className={styles.hidden}>Share to Youtube</span></a>
        </div>
        <div>
          How are we doing? <button className={styles.feedbackButton}>Feedback</button>
        </div>
        <div>
          <ul className={styles.menu}>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Protecting your Privacy</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>FOIA</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Information Quality</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Disclaimer</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>USA.gov</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Ready.gov</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>EEO</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Employee Check-In</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Contact us</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Staff Directory</a></li>
            <li className={styles.menuLi}><a href="" className={styles.menuLiA}>Need help?</a></li>
          </ul>
        </div>
      </div>
    </div>
  );
};

Footer = connect()(Footer);
export default Footer
