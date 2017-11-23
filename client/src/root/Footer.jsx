import React from 'react'
import PropTypes from 'prop-types'
import styles from './footer.css'
import github from 'fa/github.svg'
import A from '../common/link/Link'

class Footer extends React.Component {

  constructor(props) {
    super(props)
  }

  render() {
    const links = [
      {
        href: "//www.ncdc.noaa.gov/about-ncdc/privacy",
        text: "Privacy Policy"
      }, {
        href: "http://www.noaa.gov/foia-freedom-of-information-act",
        text: "Freedom of Information Act"
      }, {
        href: "http://www.cio.noaa.gov/services_programs/info_quality.html",
        text: "Information Quality"
      }, {
        href: "http://www.noaa.gov/disclaimer.html",
        text: "Disclaimer"
      }, {
        href: "//www.ncdc.noaa.gov/survey",
        text: "Take Our Survey"
      }, {
        href: "mailto:noaa.data.catalog@noaa.gov?Subject=NOAA%20Data%20Catalog",
        text: "Contact Us"
      }, {
        href: "//www.commerce.gov/",
        text: "Department of Commerce"
      }, {
        href: "http://www.noaa.gov/",
        text: "NOAA"
      }, {
        href: "//www.nesdis.noaa.gov/",
        text: "NESDIS"
      }
    ]
    var strippedVersion = this.props.version.replace(/[^0-9\.]+/g, "")
    return (
        <nav role="footer">
          <div className={styles.footer}>
            <nav className={styles.headerLinks} role="external links">
              <ul className={`${styles.footerLinks}`}>
                {links.map((link, i) => <li key={i}><A href={link.href} title={link.text}>{link.text} </A></li>)}
              </ul>
            </nav>
            <div className={`${styles.versionInfo}`}>
              <A target="_blank" href="https://github.com/cedardevs/onestop/releases">
                Version: {strippedVersion} <img src={github} alt='github releases' className={styles.github} aria-hidden="true"></img>
              </A>
            </div>
          </div>
        </nav>
    )
  }

}

Footer.propTypes = {
  version: PropTypes.string.isRequired
}

Footer.defaultProps = {
  version: ""
}

export default Footer
