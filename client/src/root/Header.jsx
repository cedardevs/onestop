import React from 'react'
import PropTypes from 'prop-types'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import Logo from './Logo'
import HeaderLink from './HeaderLink'
import Button from '../common/input/Button'
import {boxShadow} from '../common/defaultStyles'

import {SiteColors} from '../common/defaultStyles'
import {fontFamilySerif} from '../utils/styleUtils'
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
  minWidth: '17em',
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
      ? '0 0 0 0.309em'
      : firstItem ? '0 0.309em 0 0' : '0 0.309em 0 0.309em',
    borderRight: !lastItem ? '1px solid white' : 0,
  }
}

const styleSkipLinkWrapper = {
  overflowX: 'hidden',
  transition: 'width 0.9s ease, padding 0.9s ease',
}

const styleShowSkipLink = {
  padding: '0 1em',
  width: '15em',
}

const styleHideSkipLink = {
  width: 0,
}

const styleSkipLink = {
  textDecoration: 'underline',
  minWidth: '15em',
  transition: 'color 0.3s ease',
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  border: '.1em dashed #d7d7d7',
  background: 'transparent',
  color: '#d7d7d7',
  marginTop: '1em',
  marginRight: '1em',
  outline: 'none',
}

const styleSkipLinkFocus = {
  outline: 'none',
  background: 'transparent',
}

const styleSkipLinkHover = {
  color: '#277cb2',
  background: 'transparent',
}

class Header extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      focusingSkipLink: false,
    }
  }

  handleFocus = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingSkipLink: true,
      }
    })
  }

  handleBlur = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingSkipLink: false,
      }
    })
  }

  render() {
    const {focusingSkipLink} = this.state

    const menuContent = (
      <ul style={styleLinkList}>
        <li style={styleLinkListItem(true, false)}>
          <HeaderLink title="Home" href={`${this.props.homeUrl}`}>
            Home
          </HeaderLink>
        </li>
        <li style={styleLinkListItem(false, false)}>
          <HeaderLink title="About Us" href={`${this.props.homeUrl}about`}>
            About Us
          </HeaderLink>
        </li>
        <li style={styleLinkListItem(false, true)}>
          <HeaderLink title="Help" href={`${this.props.homeUrl}help`}>
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

    const stylesMerged = {
      ...styleSkipLinkWrapper,
      ...(this.state.focusingSkipLink ? styleShowSkipLink : styleHideSkipLink),
    }

    const skipLink = (
      <div key="skipLink" style={stylesMerged}>
        <Button
          style={styleSkipLink}
          styleHover={styleSkipLinkHover}
          styleFocus={styleSkipLinkFocus}
          text="Skip To Main Content"
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          onClick={() => {
            document.getElementById('mainBlock').focus()
          }}
        />
      </div>
    )

    return (
      <div style={styleHeader}>
        <FlexRow
          style={styleHeaderFlexRow}
          items={[
            <FlexRow
              key="insignia-and-search"
              items={[ skipLink, insignia, search ]}
            />,
            menu,
          ]}
        />
      </div>
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
