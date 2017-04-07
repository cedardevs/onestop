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
        {this.getMainOr508Link()}
        <a href='//data.noaa.gov/dataset' title='Previous Data Catalog'>Previous Catalog</a>
      </ul>
    const menu = <nav className={styles.headerLinks}>{menuContent}</nav>

    return <header className={`${styles.headerArea}`}>
      <div className={styles.headerRow}>
        <div className={styles.orgBox}>
          {this.renderLogo()}
        </div>
        <div className={styles.searchBox}>
          {this.props.showSearch ? <SearchFieldsContainer/> : <div></div>}
        </div>
        <div className={styles.standardMenu}>
          {menu}
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

  getMainOr508Link() {
    const lHref = `${new RegExp(/^.*\//).exec(window.location.href)}`
    let linkTitle = 'Main Site'
    let siteLink = `${lHref.slice(0,lHref.indexOf('#')+2)}`
    if (window.location.href.indexOf('508') === -1) {
      siteLink = `${siteLink}508/`
      linkTitle = 'Accessible Site'
    }
    return <a title={linkTitle} href={siteLink}><span>{linkTitle}</span></a>
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
