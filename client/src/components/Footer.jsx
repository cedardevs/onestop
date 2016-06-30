import React from 'react'
import { connect } from 'react-redux'
import styles from './footer.css'
import 'purecss'

let Footer = ({dispatch}) => {

  var externalResources = [
              {
                href: "http://www.noaa.gov/protecting-your-privacy",
                text: "Protecting your Privacy"
              },{
                href: "http://www.noaa.gov/foia-freedom-information-act",
                text: "FOIA"
              },{
                href: "http://www.cio.noaa.gov/services_programs/info_quality.html",
                text: "Information Quality"
              },{
                href: "http://www.noaa.gov/disclaimer",
                text: "Disclaimer"
              },{
                href: "https://www.usa.gov/",
                text: "USA.gov"
              },{
                href: "https://www.ready.gov/",
                text: "Ready.gov"
              },{
                href: "http://www.eeo.noaa.gov/noaa/",
                text: "EEO"
              },{
                href: "http://www.homelandsecurity.noaa.gov/",
                text: "Employee Check-In"
              },{
                href: "http://www.noaa.gov/contact-us",
                text: "Contact Us"
              },{
                href: "https://nsd.rdc.noaa.gov/nsd",
                text: "Staff Directory"
              },{
                href: "http://www.noaa.gov/need-help",
                text: "Need help?"
              }
            ]
  externalResources = externalResources.map(function(el, idx){
    return <li key={idx} className={`${styles.externalResource} ${styles.rowElement}`}><a href={`${el.href}`} className={`${styles.footerLink}`}>{`${el.text}`}</a></li>
  })

  return (
      <div className={styles.container}>
      <div className={styles.panel}>
        <div className={`${styles['pure-g']} ${styles['pure-u-lg']}`}>
          <div className={`${styles['pure-u-1-2']} ${styles.noaaLinks}`}>
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

            <form className={`${styles.feedbackForm} ${styles['pure-form']}`} method="link" action="https://www.nos.noaa.gov/survey">
              <div className={styles.rowElement}>How are we doing?</div>
              <button type="submit" className={`${styles['pure-button']} ${styles['pure-button-primary']} ${styles.rowElement}`}>Feedback</button>
            </form>
          </div>
        </div>
        <div className={`${styles.items} ${styles['pure-g']}`}>
          <div className={`${styles['pure-u-2-5']} ${styles.logoPanel}`}>
            <a href="http://www.noaa.gov/" className={`${styles.noaaLogo} ${styles.rowElement}`}>
              <span className={styles.hidden}>NOAA Home</span>
            </a>
            <span className={`${styles.rowElement} ${styles.slogan}`}>Science. Service. Stewardship.</span>
          </div>

          <div className={`${styles['pure-u-3-5']} ${styles.externalLinksPanel}`}>
            <ul className = {`${styles.resourceList}`}>
            {externalResources}
            </ul>
          </div>
          </div>
        </div>
      </div>
  )
}

Footer = connect()(Footer)
export default Footer
