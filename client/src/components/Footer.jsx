import React from 'react'
import { connect } from 'react-redux'
import styles from './footer.css'
import 'purecss'

let Footer = ({dispatch}) => {
  return (
      <div className={styles.container}>
      <div className={styles.panel}>
        <div className={`${styles['pure-g']} ${styles['pure-u-lg']}`}>
          <div className={`${styles['pure-u-1-2']}`}>
            <a href="http://www.noaa.gov/about-our-agency" className={styles.about}>About our agency</a>
            <a href="http://www.noaa.gov/news-features" className={styles.about}>News and features</a>
            <a href="http://www.noaa.gov/our-work" className={styles.about}>Our work</a>
          </div>
          <div className= {`${styles.feedback} ${styles['pure-u-1-2']} ${styles['pure-u-md-1-2']}`}>
            <div className={styles.socialMedia}>
              Stay connected:
              <a href="https://twitter.com/NOAA" className={`${styles.socialMediaLogo} ${styles.twitterLogo}`}>
                <span className={styles.hidden}>Share to Twitter</span></a>
              <a href="https://www.facebook.com/NOAA" className={`${styles.socialMediaLogo} ${styles.facebookLogo}`}>
                <span className={styles.hidden}>Share to Facebook</span></a>
              <a href="https://www.instagram.com/noaa" className={`${styles.socialMediaLogo} ${styles.instagramLogo}`}>
                <span className={styles.hidden}>Share to Instagramm</span></a>
              <a href="https://www.youtube.com/user/noaa" className={`${styles.socialMediaLogo} ${styles.youtubeLogo}`}>
                <span className={styles.hidden}>Share to Youtube</span></a>
            </div>

            <form className={`${styles.feedbackForm} ${styles['pure-form']}`} method="link" action="https://www8.nos.noaa.gov/survey">
              How are we doing?
              <button type="submit" className={`${styles['pure-button']} ${styles['pure-button-primary']}`}>Feedback</button>
            </form>
          </div>
        </div>
        <div className={`${styles.items} ${styles['pure-g']}`}>
          <div className={`${styles['pure-u-1-4']} ${styles.logoPanel}`}>
            <a href="http://www.noaa.gov/" className={styles.noaaLogo}>
              <span className={styles.hidden}>NOAA Home</span>
            </a>
            <span>Science. Service. Stewardship.</span>
          </div>

          <div className={`${styles['pure-u-3-4']} ${styles['pure-menu']} ${styles.externalLinksPanel}`}>
            <ul className = {`${styles['pure-menu-list']}`}>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.noaa.gov/protecting-your-privacy" className={`${styles['pure-menu-link']}`}>Protecting your Privacy</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.noaa.gov/foia-freedom-information-act" className={`${styles['pure-menu-link']}`}>FOIA</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.cio.noaa.gov/services_programs/info_quality.html" className={`${styles['pure-menu-link']}`}>Information Quality</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.noaa.gov/disclaimer" className={`${styles['pure-menu-link']}`}>Disclaimer</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="https://www.usa.gov/" className={`${styles['pure-menu-link']}`}>USA.gov</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="https://www.ready.gov/" className={`${styles['pure-menu-link']}`}>Ready.gov</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.eeo.noaa.gov/noaa/" className={`${styles['pure-menu-link']}`}>EEO</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.homelandsecurity.noaa.gov/" className={`${styles['pure-menu-link']}`}>Employee Check-In</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.noaa.gov/contact-us" className={`${styles['pure-menu-link']}`}>Contact us</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="https://nsd.rdc.noaa.gov/nsd" className={`${styles['pure-menu-link']}`}>Staff Directory</a></li>
              <li className={`${styles['pure-menu-item']}`}><a href="http://www.noaa.gov/need-help" className={`${styles['pure-menu-link']}`}>Need help?</a></li>
            </ul>
          </div>
          </div>
        </div>
      </div>
  )
}

Footer = connect()(Footer)
export default Footer
