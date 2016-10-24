import React from 'react'
import {connect} from 'react-redux'
import styles from './footer.css'
import 'purecss'

let Footer = ({dispatch}) => {

    var externalResources = [
        {
            href: "//www.noaa.gov/protecting-your-privacy",
            text: "Protecting your Privacy"
        }, {
            href: "//www.noaa.gov/foia-freedom-information-act",
            text: "FOIA"
        }, {
            href: "//www.cio.noaa.gov/services_programs/info_quality.html",
            text: "Information Quality"
        }, {
            href: "//www.noaa.gov/disclaimer",
            text: "Disclaimer"
        }, {
            href: "https://www.usa.gov/",
            text: "USA.gov"
        }, {
            href: "https://www.ready.gov/",
            text: "Ready.gov"
        }, {
            href: "//www.eeo.noaa.gov/noaa/",
            text: "EEO"
        }, {
            href: "//www.homelandsecurity.noaa.gov/",
            text: "Employee Check-In"
        }, {
            href: "//www.noaa.gov/contact-us",
            text: "Contact Us"
        }, {
            href: "https://nsd.rdc.noaa.gov/nsd",
            text: "Staff Directory"
        }, {
            href: "//www.noaa.gov/need-help",
            text: "Need help?"
        }
    ]
    externalResources = externalResources.map(function (el, idx) {
        return <li key={idx} className={`${styles.externalResource}
        ${styles.rowElement}`}><a href={`${el.href}`}
        className={`${styles.footerLink}`}>{`${el.text}`}</a>
        </li>
    })

    return (
      <div className={styles.container}>
          <div className={styles.panel}>
              <div className={'pure-g'}>
                  <div className={`pure-u-2-5 ${styles.logoPanel}`}>
                     <a href="//www.noaa.gov/" className={`${styles.noaaLogo} ${styles.rowElement}`}>
                          <span className={styles.hidden}>NOAA Home</span>
                      </a>
                     <span className={`${styles.rowElement} ${styles.slogan}`}>Science. Service. Stewardship.</span>
                  </div>
              </div>
          </div>
      </div>
    )
}

Footer = connect()(Footer)
export default Footer
