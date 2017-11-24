import React from 'react'
import PropTypes from 'prop-types'
import { Link } from 'react-router'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import Logo from './Logo'
import styles from './HeaderStyles.css'

import FlexRow from '../common/FlexRow'

const styleHeader = {
  backgroundColor: '#222C37',
  padding: '1em',
}

const styleHeaderFlexRow = {
  justifyContent: 'space-between',
  flexWrap: 'wrap',
  alignItems: 'flex-end',
}

const styleNav = {
  flex: '1',
  minWidth: '30em',
  display: 'flex',
  justifyContent: 'flex-end',
  marginTop: '1em',
}

const styleLinkList = {
  padding: 0,
  margin: 0,
  listStyleType: 'none',
  fontSize: '1.5em',
  display: 'inline-flex',
  justifyContent: 'center',
  flexWrap: 'wrap',
}

const styleLinkListItem = (firstItem, lastItem) => {
  return {
    padding: lastItem
      ? '0 0 0 0.618em'
      : firstItem ? '0 0.618em 0 0' : '0 0.618em 0 0.618em',
    borderRight: !lastItem ? '1px solid white' : 0,
  }
}

class Header extends React.Component {
  constructor(props) {
    super(props)
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
      <a title={linkTitle} href={siteLink} className={styles.link}>
        <span>{linkTitle}</span>
      </a>
    )
  }

  render() {

    const aboutLink =
      window.location.href.indexOf('508') === -1 ? (
        <Link title="About Us" to="/about" className={styles.link}>
          About Us
        </Link>
      ) : (
        <Link title="About Us" to="/508/about" className={styles.link}>
          About Us
        </Link>
      )

    const helpLink =
      window.location.href.indexOf('508') === -1 ? (
        <Link title="Help" to="/help" className={styles.link}>
          Help
        </Link>
      ) : (
        <Link title="Help" to="/508/help" className={styles.link}>
          Help
        </Link>
      )

    const menuContent = (
      <ul role="menubar" style={styleLinkList}>
        <li style={styleLinkListItem(true, false)}>
          <a href={this.props.homeUrl} title="Home" className={styles.link}>
            Home
          </a>
        </li>
        <li style={styleLinkListItem(false, false)}>{aboutLink}</li>
        <li style={styleLinkListItem(false, false)}>{helpLink}</li>
        <li style={styleLinkListItem(false, true)}>
          {this.getMainOr508Link()}
        </li>
      </ul>
    )

    const insignia = (
      <Logo key="insignia" onClick={this.props.goHome} style={{ flex: '0 0 275px' }} />
    )

    const search = this.props.showSearch ? (
      <SearchFieldsContainer key="search" header={true} />
    ) : null

    const menu = (
      <nav key="menu" aria-label="Main Navigation" style={styleNav}>
        {menuContent}
      </nav>
    )

    return (
      <header style={styleHeader}>
        <FlexRow
          style={styleHeaderFlexRow}
          items={[<FlexRow key="insignia-and-search" items={[insignia, search]}/>, menu]}
        />
      </header>
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
