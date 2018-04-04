import React from 'react'
import PropTypes from 'prop-types'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import Logo from './Logo'
import HeaderLink from './HeaderLink'
import {boxShadow} from '../common/defaultStyles'

import FlexRow from '../common/FlexRow'

const styleHeader = {
  backgroundColor: '#222C37',
  padding: '1em',
  boxShadow: boxShadow,
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

  render() {
    const menuContent = (
      <ul style={styleLinkList}>
        <li style={styleLinkListItem(true, false)}>
          <HeaderLink title="Home" to="/">
            Home
          </HeaderLink>
        </li>
        <li style={styleLinkListItem(false, false)}>
          <HeaderLink title="About Us" to="/about">
            About Us
          </HeaderLink>
        </li>
        <li style={styleLinkListItem(false, true)}>
          <HeaderLink title="Help" to="/help">
            Help
          </HeaderLink>
        </li>
      </ul>
    )

    const insignia = (
      <Logo
        key="insignia"
        onClick={this.props.goHome}
        style={{flex: '0 0 275px'}}
      />
    )

    const search = this.props.showSearch ? (
      <SearchFieldsContainer key="search" />
    ) : null

    const menu = (
      <nav key="menu" aria-label="Main" style={styleNav}>
        {menuContent}
      </nav>
    )

    return (
      <header style={styleHeader}>
        <FlexRow
          style={styleHeaderFlexRow}
          items={[
            <FlexRow key="insignia-and-search" items={[ insignia, search ]} />,
            menu,
          ]}
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
