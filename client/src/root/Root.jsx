const logoPath = require('../../img/noaa_logo_circle_72x72.svg')

import React from 'react'
import { Link } from 'react-router'
import DetailContainer from '../detail/DetailContainer'
import Footer from './Footer.jsx'
import BannerContainer from './banner/BannerContainer'
import styles from './root.css'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import LoadingContainer from '../loading/LoadingContainer'

class RootComponent extends React.Component {
  constructor(props) {
    super(props)

    this.renderHeader = this.renderHeader.bind(this)

    this.location = props.location.pathname
  }

  componentWillUpdate(nextProps) {
    this.location = nextProps.location.pathname
  }

  renderHeader() {
    if(this.location !== '/') {
      return (
        <div className={'pure-g'}>
          <div className={`pure-u-5-24 ${styles.orgBox}`}>
            <div className={styles.logoLinks}>
              <a href="//www.noaa.gov"><img className={styles.noaaLogo} id='logo' src={logoPath} alt="NOAA Home"/></a>
              <Link to='/' activeClassName="active" onlyActiveOnIndex={true}>
                <span className={styles.oneStopText}><i className={`fa fa-stop-circle-o fa-md ${styles.oneStopText}`}></i>neStop</span>
              </Link>
            </div>
          </div>
          <div className={`pure-u-1 pure-u-sm-3-4 ${styles.landingComponents}`}>
            <SearchFieldsContainer/>
          </div>
        </div>
      )
    }

    else {
      return (
        <div className={`pure-g`}>
          <div className={`pure-u-7-24`}>
            <a href="//www.ncei.noaa.gov/">
              <img className={styles.nceiLogo} src="https://www.ncei.noaa.gov/sites/default/files/ncei_dark_test_75.png" alt="NCEI Home"/>
            </a>
          </div>
          <div className={`pure-u-2-3 ${styles.nceiMenu}`}>
            <div className={`pure-menu pure-menu-horizontal`}>
              <ul className={`pure-menu-list`}>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/" className={styles.nceiMenuItem}>Home</a></li>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/about" className={styles.nceiMenuItem}>About</a></li>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/news" className={styles.nceiMenuItem}>News</a></li>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/access" className={styles.nceiMenuItem}>Access</a></li>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/archive" className={styles.nceiMenuItem}>Archive</a></li>
                <li className={`pure-menu-item`}><a href="https://www.ncei.noaa.gov/contact" className={styles.nceiMenuItem}>Contact</a></li>
                <li className={`pure-menu-item`}><a href="#" className={styles.selected}>Search</a></li>
              </ul>
            </div>
          </div>
{/*          <div className={`pure-u-2-3 ${styles.nceiBurgerMenuLanding}`}>
            <div className={styles.nceiMenuItem}>
              <a href="#" className={styles.nceiMenuItem}><i className={`fa fa-bars fa-3x`}></i></a>
              <ul>
                <li><a href="https://www.ncei.noaa.gov/" className={styles.nceiMenuItem}>Home</a></li>
                <li><a href="https://www.ncei.noaa.gov/about" className={styles.nceiMenuItem}>About</a></li>
                <li><a href="https://www.ncei.noaa.gov/news" className={styles.nceiMenuItem}>News</a></li>
                <li><a href="https://www.ncei.noaa.gov/access" className={styles.nceiMenuItem}>Access</a></li>
                <li><a href="https://www.ncei.noaa.gov/archive" className={styles.nceiMenuItem}>Archive</a></li>
                <li><a href="https://www.ncei.noaa.gov/contact" className={styles.nceiMenuItem}>Contact</a></li>
                <li><a href="#" className={styles.selected}>Search</a></li>
              </ul>
            </div>
          </div>*/}
        </div>
      )
    }
  }

  // renderBurgerMenu() {
  //   if(this.location)
  // }

  render() {
    return <div className={styles.rootContainer}>
      <div className={styles.mainContent}>
        <BannerContainer/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>{this.renderHeader()}</div>
        <div className={styles.main}>
          <LoadingContainer/>
          {this.props.children}
        </div>
      </div>
      <div className={styles.footer}>
        <Footer/>
     </div>
   </div>
  }
}


export default RootComponent
