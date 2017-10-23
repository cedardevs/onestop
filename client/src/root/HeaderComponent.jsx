import React from 'react'
import PropTypes from 'prop-types'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import stopCircle from 'fa/stop-circle-o.svg'
import A from 'LinkComponent'
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
      <input type="checkbox" checked={this.state.menuOpen} onChange={this.toggleBurgerMenu} aria-hidden="true"/>
        <span></span>
        <span></span>
        <span></span>
      </div>
    const menuContent = <ul role="menubar">
        <button title="Home" onClick={() => location.href=this.props.homeUrl}>Home</button>
        <button title="About" onClick={() => this.props.toggleAbout()}>About</button>
        <button title="Help" onClick={() => this.props.toggleHelp()}>Help</button>
        {this.getMainOr508Link()}
        <button title='Previous Data Catalog' onClick={() => location.href='//data.noaa.gov/dataset'}>Previous Catalog</button>
      </ul>
    const menu = <nav className={styles.headerLinks}>{menuContent}</nav>

    return <header className={`${styles.headerArea}`}>
      <div className={styles.headerRow}>
        <div className={styles.orgBox}>
          {this.renderLogo()}
        </div>
        <div className={styles.searchBox}>
          {this.props.showSearch ? <SearchFieldsContainer header={true}/> : <div></div>}
        </div>
        <div className={styles.standardMenu} role="navigation">
          {menu}
        </div>
      </div>
      <div className={styles.burgerMenu} role="navigation">
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
    return <button title={linkTitle} onClick={()=>location.href=siteLink}> {linkTitle}</button>
  }

  renderLogo() {
    if (this.props.showSearch) {
      return <div>
        <A href="//www.noaa.gov" title="NOAA Home">
          <img className={styles.noaaLogo} id='logo' alt="NOAA Logo" src={noaaLogo}/>
        </A>
        <a href="#" title="One Stop Home" className={styles.oneStopLink} onClick={() => this.props.goHome()}>
          <img src={stopCircle} className={styles.stopCircle}></img>neStop
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
