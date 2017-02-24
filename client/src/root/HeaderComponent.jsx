import React, { PropTypes } from 'react'
import { Link } from 'react-router'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import styles from './header.css'

const noaaLogo = require('../../img/noaa_logo_circle_72x72.svg')
const nceiLogo = require('../../img/ncei_dark_test_75.png')

class HeaderComponent extends React.Component {
  constructor(props) {
    super(props)
  }

  renderContent() {
    if (this.props.showSearch) {
      return <SearchFieldsContainer/>
    }
    else {
      return <div className={`${styles.headerLinks}`}>
        <a href="#" title="Home">Home</a>
        <a href="//data.noaa.gov/dataset" title="NOAA Data Catalog">Data Catalog</a>
        <a href="#" title="About">About</a>
        <a href="#" title="Help">Help</a>
      </div>
    }
  }

  renderLogo() {
    if (this.props.showSearch) {
      return <div className={styles.logoLinks}>
        <a href="//www.noaa.gov"><img className={styles.noaaLogo} id='logo' src={noaaLogo} alt="NOAA Home"/></a>
        <Link to='/' activeClassName="active" onlyActiveOnIndex={true}>
          <span className={styles.oneStopText}>
            <i className={`fa fa-stop-circle-o fa-md ${styles.oneStopText}`}/>neStop
          </span>
        </Link>
      </div>
    }
    else {
      return <a href="//www.ncei.noaa.gov/" title="NCEI Home">
        <img className={styles.nceiLogo} src={nceiLogo}/>
      </a>
    }
  }

  render() {
    return <div className={`pure-g ${styles.headerArea}`}>
      <div className={`pure-u-1-4 ${styles.orgBox}`}>
        {this.renderLogo()}
      </div>
      <div className={`pure-u-1 pure-u-sm-3-4`}>
        {this.renderContent()}
      </div>
    </div>
  }
}

HeaderComponent.propTypes = {
  showSearch: PropTypes.bool.isRequired
}

HeaderComponent.defaultProps = {
  showSearch: true
}

export default HeaderComponent
