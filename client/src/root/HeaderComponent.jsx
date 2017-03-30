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

  render() {
    return <header className={`pure-g ${styles.headerArea}`}>
      <div className={`pure-u-1-4 ${styles.orgBox}`}>
        {this.renderLogo()}
      </div>
      <div className={`pure-u-1 pure-u-sm-3-4`}>
        {this.renderContent()}
      </div>
    </header>
  }

  renderContent() {
    const menu = <nav className={styles.headerLinks}>
              <a href={this.props.homeUrl} title="Home">Home</a>
              <a title="About" onClick={() => this.props.toggleAbout()}>About</a>
              <a title="Help" onClick={() => this.props.toggleHelp()}>Help</a>
            </nav>
    const { showSearch } = this.props
    return <div>
      <div className={`pure-u-1 pure-u-sm-3-4 ${styles.headerRow}`}>
        {showSearch ? <SearchFieldsContainer/> : <div></div>}</div>
      <div className={`pure-u-1 pure-u-sm-1-4 ${styles.headerRow}`}>{menu}</div>
    </div>
  }

  renderLogo() {
    if (this.props.showSearch) {
      return <div>
        <a href="//www.noaa.gov" title="NOAA Home">
          <img className={styles.noaaLogo} id='logo' alt="NOAA Logo" src={noaaLogo}/>
        </a>
        <a href="#" title="One Stop Home" className={styles.oneStopLink} onClick={() => this.props.goHome()}>
          <i className={`fa fa-stop-circle-o fa-md`}/>neStop
        </a>
      </div>
    }
    else {
      return <a href="//www.ncei.noaa.gov/" title="NCEI Home">
        <img className={styles.nceiLogo} alt="NCEI Logo" src={nceiLogo}/>
      </a>
    }
  }
}

HeaderComponent.propTypes = {
  showSearch: PropTypes.bool.isRequired,
  goHome: PropTypes.func.isRequired,
  toggleHelp: PropTypes.func.isRequired,
  toggleAbout: PropTypes.func.isRequired
}

HeaderComponent.defaultProps = {
  showSearch: true
}

export default HeaderComponent
