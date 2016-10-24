const logoPath = require('../../img/noaa_logo_circle_72x72.svg')
//const logoPath = require('../../img/cireslogo-cc.png')

import React from 'react'
import DetailContainer from '../detail/DetailContainer'
import Breadcrumbs from 'react-breadcrumbs'
import Favicon from 'react-favicon'
import Footer from './Footer.jsx'
import AlphaBanner from './AlphaBanner.jsx'
import styles from './root.css'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import LoadingContainer from '../loading/LoadingContainer'

class RootComponent extends React.Component {
  constructor(props) {
    super(props)
    this.breadcrumbs
  }

  render() {
    var searchlabel
    if (this.props.location.pathname !== "/"){
        this.breadcrumbs = <Breadcrumbs
            routes={this.props.routes}
            params={this.props.params}
        />
        searchlabel = 'searchHover'
    } else {
        this.breadcrumbs = undefined
        searchlabel =  "searchMapSpace"
    }
    return <div className={styles.rootContainer}>
      <Favicon url={["//cires1.colorado.edu/favicon.ico"]}/>
      <div className={styles.mainContent}>
        <AlphaBanner/>
        <DetailContainer/>
        <div id="header" className={styles.headerArea}>
          <div className={'pure-g'}>
            <div className={`pure-u-1-4 ${styles.orgBox}`}>
              <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
              <div className={styles.orgInfo}>
                <a className={styles.noaa}>National Oceanic and Atmospheric Administration</a>
                <a className={styles.doc} href="//www.commerce.gov">U.S. Department of Commerce</a>
              </div>
            </div>
            <div  className={`pure-u-3-4 ${styles.landingComponents} ${styles[searchlabel]}`}>
              <SearchFieldsContainer  />
            </div>
          </div>
        </div>
        <div className={styles.main}>
          <LoadingContainer/>
          <div className={styles.breadCrumbs}>
            {this.breadcrumbs}
          </div>
          <div className={styles.results}>
            {this.props.children}
          </div>
        </div>
      </div>
      <div className={styles.footer}>
        <Footer/>
     </div>
   </div>
  }
}


export default RootComponent
