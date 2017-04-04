import React, { PropTypes } from 'react'
import { Link } from 'react-router'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import styles from './header.css'

const noaaLogo = require('../../img/noaa_logo_circle_72x72.svg')
const nceiLogo = require('../../img/ncei_dark_test_75.png')

class HeaderComponent extends React.Component {
  constructor(props) {
    super(props)
    this.toggleBurgerMenu = this.toggleBurgerMenu.bind(this)
    this.state = { menuOpen: false }
  }

  toggleBurgerMenu() { this.setState({ menuOpen: !this.state.menuOpen }) }

  render() {
    const burgerToggle = <div className={styles.burgerToggle}>
        <input type="checkbox" checked={this.state.menuOpen} onChange={this.toggleBurgerMenu}/>
        <span></span>
        <span></span>
        <span></span>
      </div>
    const menuContent = <ul>
        <a href={this.props.homeUrl} title="Home">Home</a>
        <a title="About" onClick={() => this.props.toggleAbout()}>About</a>
        <a title="Help" onClick={() => this.props.toggleHelp()}>Help</a>
      </ul>
    const menu = <nav className={styles.headerLinks}>{menuContent}</nav>

    return <header className={`${styles.headerArea}`}>
      <div className={`pure-g`}>
        <div className={`pure-u-1-4 ${styles.orgBox}`}>
          {this.renderLogo()}
        </div>
        <div className={`pure-u-1 pure-u-sm-10-24 ${styles.headerRow}`}>
          {this.props.showSearch ? <SearchFieldsContainer/> : <div></div>}
        </div>
        <div className={`pure-u-1 pure-u-sm-8-24 ${styles.headerRow}`}>
          <div className={styles.standardMenu}>
            {menu}
          </div>
        </div>
      </div>
      <div className={styles.burgerMenu}>
        {burgerToggle}
        <div className={`${styles.menuContainer} ${this.state.menuOpen ? styles.menuOpen : ''}`}>
          <div className={`${styles.section} ${this.state.menuOpen ? '' : styles.collapsed}`}>
            {menu}
          </div>
        </div>
      </div>
    </header>
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
