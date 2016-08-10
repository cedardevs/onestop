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
    return <div>
      <Favicon url={["//cires1.colorado.edu/favicon.ico"]}/>
          {/*<Favicon url={["//www.noaa.gov/sites/all/themes/custom/noaa/favicon.ico"]}/>*/}
      <AlphaBanner/>
      <DetailContainer/>
      <div className={styles.bottomBorder}>
        <div className={styles.panel}>
          <div className={'pure-g'}>
            <div className={`pure-u-1-6 ${styles.logSty}`}>
              {/*               <img className={styles.logo} id='logo' src={logoPath} alt="CIRES Logo"/>
              <a className={styles.orgName}>CIRES-NCEI OneStop</a>
              <a className={styles.deptName}>DEMONSTRATION SITE</a>
               */}
              <img className={styles.logo} id='logo' src={logoPath} alt="NOAA Logo"/>
              <a className={styles.orgName}>National Oceanic and Atmospheric Administration</a>
              <a className={styles.deptName} href="//www.commerce.gov">U.S. Department of Commerce</a>
            </div>
            <div  className={`pure-u-5-6 ${styles.landingComponents} ${styles[searchlabel]}`}>
              <SearchFieldsContainer  />
            </div>
          </div>
        </div>
      </div>
      <div className={styles.breadCrumbs}>
              {this.breadcrumbs}
      </div>
      <div className={styles.results}>
        {this.props.children}
      </div>
      <Footer/>
     </div>
    }
}


export default RootComponent
