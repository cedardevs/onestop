import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import Logo from './Logo'

import FlexRow from '../common/FlexRow'

const styleHeader = {
  backgroundColor: '#222C37',
  padding: '1em',
}

const styleLinkList = {
  padding: 0,
  listStyleType: 'none',
  fontSize: '1.5em',
  display: 'inline-flex',
    justifyContent: 'flex-end',
  flexWrap: 'wrap'
}

const styleLinkListItem = lastItem => {
  return {
      padding: lastItem ? '0' : '0 0.618em 0 0',
      lineHeight: '1.618em'
  }
}

const styleLink = {
    textDecoration: 'none',
    color: '#d7d7d7',
    fontWeight: '300',
    transition: 'color 0.3s ease',
}

const styleLinkHover = {
  color: '#277CB2'
}

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
    // const burgerToggle = (
    //   <div className={styles.burgerToggle}>
    //     <input
    //       type="checkbox"
    //       checked={this.state.menuOpen}
    //       onChange={this.toggleBurgerMenu}
    //       aria-hidden="true"
    //     />
    //     <span />
    //     <span />
    //     <span />
    //   </div>
    // )

    const aboutLink =
      window.location.href.indexOf('508') === -1 ? (
        <Link title="About" to="/about" style={styleLink}>
          About
        </Link>
      ) : (
        <Link title="About" to="/508/about" style={styleLink}>
          About
        </Link>
      )

    const helpLink =
      window.location.href.indexOf('508') === -1 ? (
        <Link title="Help" to="/help" style={styleLink}>
          Help
        </Link>
      ) : (
        <Link title="Help" to="/508/help" style={styleLink}>
          Help
        </Link>
      )

    const menuContent = (
      <ul role="menubar" style={styleLinkList}>
        <li style={styleLinkListItem(false)}>
          <a href={this.props.homeUrl} title="Home" style={styleLink}>
            Home
          </a>
        </li>
        <li style={styleLinkListItem(false)}>{aboutLink}</li>
        <li style={styleLinkListItem(false)}>{helpLink}</li>
        <li style={styleLinkListItem(false)}>{this.getMainOr508Link()}</li>
        <li style={styleLinkListItem(true)}>
          <a href="//data.noaa.gov/dataset" title="Previous Data Catalog" style={styleLink}>
            Previous Catalog
          </a>
        </li>
      </ul>
    )

    const insignia = (
      <Logo key="insignia" onClick={this.props.goHome} style={{ flex: '0' }} />
    )

    const search = this.props.showSearch ? (
      <SearchFieldsContainer key="search" header={true} />
    ) : null

    const menu = (
      <nav
        key="menu"
        aria-label="Main Navigation"
        style={{
          flex: '1',
          // backgroundColor: 'magenta',
          textAlign: 'center',
          display: 'inline-block',
          minWidth: '22em'
        }}
      >
        {menuContent}
      </nav>
    )

    return (
      <header style={styleHeader}>
        <FlexRow
          style={{
            justifyContent: 'space-between',
            flexWrap: 'wrap',
            alignItems: 'center',
          }}
          items={[<FlexRow items={[insignia, search]} />, menu]}
        />
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
      <a title={linkTitle} href={siteLink} style={styleLink}>
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

// <header className={`${styles.headerArea}`}>
// <div className={styles.headerRow}>
// <div className={styles.orgBox}>
// <Logo onClick={this.props.goHome} />
// </div>
// <div className={styles.searchBox}>
//     {this.props.showSearch ? (
//         <SearchFieldsContainer header={true} />
//     ) : (
//         <div />
//     )}
// </div>
// <div className={styles.standardMenu} role="navigation">
//     {menu}
// </div>
// </div>
// <div className={styles.burgerMenu} role="navigation">
//     {burgerToggle}
//   <div
//       className={`${styles.menuContainer} ${this.state.menuOpen
//           ? styles.menuOpen
//           : ''}`}
//   >
//     <div
//         className={`${styles.section} ${this.state.menuOpen
//             ? ''
//             : styles.collapsed}`}
//     >
//         {menu}
//     </div>
//     </div>
//   </div>
// </header>
