import React from 'react'
import { connect } from 'react-redux'
import styles from './footer.css'
import 'purecss'

let Footer = () => {

  const links = [
    {
      href: "//www.ncdc.noaa.gov/about-ncdc/privacy",
      text: "Privacy Policy"
    }, {
      href: "//www.rdc.noaa.gov/~foia/",
      text: "Freedom of Information Act"
    }, {
      href: "//www.cio.noaa.gov/services_programs/info_quality.html",
      text: "Information Quality"
    }, {
      href: "//www.noaa.gov/disclaimer.html",
      text: "Disclaimer"
    }, {
      href: "//www.ncdc.noaa.gov/survey",
      text: "Take Our Survey"
    }, {
      href: "//www.commerce.gov/",
      text: "Department of Commerce"
    }, {
      href: "//www.noaa.gov/",
      text: "NOAA"
    }, {
      href: "//www.nesdis.noaa.gov/",
      text: "NESDIS"
    }
  ]

  return (
      <div className={styles.footer}>
        <div className={'pure-g'}>
          <div className={`pure-u-1-3 ${styles.logoPanel}`}>
            <a href="//www.noaa.gov/" title="NOAA Home" className={`${styles.noaaLogo}`}/>
            <span className={`${styles.slogan}`}>Science. Service. Stewardship.</span>
          </div>
          <div className={`pure-u-2-3`}>
            <ul className={`${styles.footerLinks}`}>
              {links.map((link, i) => <li key={i}><a href={link.href} title={link.text}>{link.text}</a></li>)}
            </ul>
          </div>
        </div>
      </div>
  )
}

Footer = connect()(Footer)
export default Footer
