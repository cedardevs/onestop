import React from 'react'
import { connect } from 'react-redux'
import styles from './footer.css'

let Footer = ({dispatch}) => {
  return (
    <div className={styles.container}>
        <div className={styles.leftHalf}>
          <a href="http://www.noaa.gov/about-our-agency" className={styles.about}>About our agency</a>
          <a href="http://www.noaa.gov/news-features" className={styles.about}>News and features</a>
          <a href="http://www.noaa.gov/our-work" className={styles.about}>Our work</a>
          <a href="http://www.noaa.gov/" className={styles.noaaLogo}>
            <span className={styles.hidden}>NOAA Home</span>
          </a>
          <span>Science. Service. Stewardship.</span>
        </div>

        <div className={styles.rightHalf}>
          <div className={styles.icons}>
            Stay connected:
            <a href="https://twitter.com/NOAA?ref_src=twsrc%5Egoogle%7Ctwcamp%5Eserp%7Ctwgr%5Eauthor" className={styles.twitterLogo}>
              <span className={styles.hidden}>Share to Twitter</span></a>
            <a href="https://www.facebook.com/NOAA" className={styles.facebookLogo}>
              <span className={styles.hidden}>Share to Facebook</span></a>
            <a href="https://www.instagram.com/noaa" className={styles.instagrammLogo}>
              <span className={styles.hidden}>Share to Instagramm</span></a>
            <a href="https://www.youtube.com/user/noaa" className={styles.youtubeLogo}>
              <span className={styles.hidden}>Share to Youtube</span></a>
          </div>
          <div className={styles.feedbackButton}>
            How are we doing?
            <button className={styles.button}>Feedback</button>
          </div>
          <div>
            <ul className={styles.menu}>
              <li className={styles.menuLi}><a href="http://www.noaa.gov/protecting-your-privacy" className={styles.menuLiA}>Protecting your Privacy</a></li>
              <li className={styles.menuLi}><a href="http://www.noaa.gov/foia-freedom-information-act" className={styles.menuLiA}>FOIA</a></li>
              <li className={styles.menuLi}><a href="http://www.cio.noaa.gov/services_programs/info_quality.html" className={styles.menuLiA}>Information Quality</a></li>
              <li className={styles.menuLi}><a href="http://www.noaa.gov/disclaimer" className={styles.menuLiA}>Disclaimer</a></li>
              <li className={styles.menuLi}><a href="https://www.usa.gov/" className={styles.menuLiA}>USA.gov</a></li>
              <li className={styles.menuLi}><a href="https://www.ready.gov/" className={styles.menuLiA}>Ready.gov</a></li>
              <li className={styles.menuLi}><a href="http://www.eeo.noaa.gov/noaa/" className={styles.menuLiA}>EEO</a></li>
              <li className={styles.menuLi}><a href="http://www.homelandsecurity.noaa.gov/" className={styles.menuLiA}>Employee Check-In</a></li>
              <li className={styles.menuLi}><a href="http://www.noaa.gov/contact-us" className={styles.menuLiA}>Contact us</a></li>
              <li className={styles.menuLi}><a href="https://nsd.rdc.noaa.gov/nsd" className={styles.menuLiA}>Staff Directory</a></li>
              <li className={styles.menuLi}><a href="http://www.noaa.gov/need-help" className={styles.menuLiA}>Need help?</a></li>
            </ul>
          </div>
        </div>
      </div>
  );
};

Footer = connect()(Footer);
export default Footer
