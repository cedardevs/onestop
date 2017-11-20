import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import styles from './header.css'
import Logo from './Logo'

class Header extends React.Component {
  constructor(props) {
    super(props)
    this.toggleBurgerMenu = this.toggleBurgerMenu.bind(this)
    this.state = { menuOpen: false }
  }

  toggleBurgerMenu() {
    this.setState({ menuOpen: !this.state.menuOpen })
  }

  render() {
    const burgerToggle = (
      <div className={styles.burgerToggle}>
        <input
          type="checkbox"
          checked={this.state.menuOpen}
          onChange={this.toggleBurgerMenu}
          aria-hidden="true"
        />
        <span />
        <span />
        <span />
      </div>
    )
    const menuContent = (
      <ul role="menubar">
        <a href={this.props.homeUrl} title="Home">
          Home
        </a>
        {window.location.href.indexOf('508') === -1 ? (
          <Link title="About" to="/about">
            About
          </Link>
        ) : (
          <Link title="About" to="/508/about">
            About
          </Link>
        )}
        {window.location.href.indexOf('508') === -1 ? (
          <Link title="Help" to="/help">
            Help
          </Link>
        ) : (
          <Link title="Help" to="/508/help">
            Help
          </Link>
        )}
        {this.getMainOr508Link()}
        <a href="//data.noaa.gov/dataset" title="Previous Data Catalog">
          Previous Catalog
        </a>
      </ul>
    )
    const menu = (
      <nav className={styles.headerLinks} aria-label="Main Navigation">
        {menuContent}
      </nav>
    )

    return (
      <header className={`${styles.headerArea}`}>
        <div className={styles.headerRow}>
          <div className={styles.orgBox}>
            <Logo onClick={this.props.goHome} />
          </div>
          <div className={styles.searchBox}>
            {this.props.showSearch ? (
              <SearchFieldsContainer header={true} />
            ) : (
              <div />
            )}
          </div>
          <div className={styles.standardMenu} role="navigation">
            {menu}
          </div>
        </div>
        <div className={styles.burgerMenu} role="navigation">
          {burgerToggle}
          <div
            className={`${styles.menuContainer} ${
              this.state.menuOpen ? styles.menuOpen : ''
            }`}
          >
            <div
              className={`${styles.section} ${
                this.state.menuOpen ? '' : styles.collapsed
              }`}
            >
              {menu}
            </div>
          </div>
        </div>
      </header>
    )
  }

  getMainOr508Link() {
    const lHref = `${new RegExp(/^.*\//).exec(window.location.href)}`
    let linkTitle = 'Main Site'
    let siteLink = `${lHref.slice(0, lHref.indexOf('#') + 2)}`
    if (window.location.href.indexOf('508') === -1) {
      siteLink = `${siteLink}508/`
      linkTitle = 'Accessible Site'
    }
    return (
      <a title={linkTitle} href={siteLink}>
        <span>{linkTitle}</span>
      </a>
    )
  }
}

Header.propTypes = {
  showSearch: PropTypes.bool.isRequired,
  goHome: PropTypes.func.isRequired,
}

Header.defaultProps = {
  showSearch: true,
}

export default Header
