import React, { PropTypes }  from 'react'
import styles from './footer.css'
import github from 'fa/github.svg'
import A from 'LinkComponent'
import 'purecss'

class FooterComponent  extends React.Component {

    constructor(props) {
        super(props)
    }

    render () {
        const links = [
            {
                href: "//www.ncdc.noaa.gov/about-ncdc/privacy",
                text: "Privacy Policy"
            }, {
                href: "//www.noaa.gov/foia-freedom-of-information-act",
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
        ];

        return (
            <nav role="footer">
              <div className={styles.footer}>
                <div className={'pure-g'}>
                  <div className={`pure-u-1`} >
                    <nav className={styles.headerLinks} role="external links">
                      <ul className={`${styles.footerLinks}`} >
                          {links.map((link, i) => <li key={i} ><A href={link.href} title={link.text} >{link.text} </A></li>)}
                      </ul>
                    </nav>
                  </div>
                </div>
                <div className={`${styles.versionInfo}`}>
                  <A href ="https://github.com/cedardevs/onestop/releases" >
                    Version: {this.props.version} <img src={github} className={styles.github} aria-hidden="true"></img>
                  </A>
                </div>
              </div>
            </nav>
        )
    }

}

FooterComponent.propTypes = {
    version: PropTypes.string.isRequired
}

FooterComponent.defaultProps = {
    version: ""
}

export default FooterComponent
