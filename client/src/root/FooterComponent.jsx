import React, { PropTypes }  from 'react'
import styles from './footer.css'
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
                          {links.map((link, i) => <li key={i} ><a href={link.href} title={link.text} >{link.text} </a></li>)}
                      </ul>
                    </nav>
                  </div>
                </div>
                <div className={`${styles.versionInfo}`}>
                  <a href ="https://github.com/cires-ncei/onestop/releases" >
                    Version: {this.props.version} <i className={'fa fa-github'} aria-hidden="true"></i>
                  </a>
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
