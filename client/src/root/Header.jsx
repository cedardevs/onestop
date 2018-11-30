import React from 'react'
import {Route, Switch} from 'react-router'

import PropTypes from 'prop-types'
import SearchFieldsContainer from '../search/SearchFieldsContainer'
import Logo from './Logo'
import HeaderLink from './HeaderLink'

import Button from '../common/input/Button'
import {boxShadow} from '../common/defaultStyles'

import {fontFamilySerif} from '../utils/styleUtils'
import FlexRow from '../common/FlexRow'

import HeaderDropdownMenuContainer from './HeaderDropdownMenuContainer'
import HeaderDropdownMenuButtonContainer from './HeaderDropdownMenuButtonContainer'

const styleWrapper = {
  position: 'relative',
}

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
  minWidth: '22em',
  display: 'flex',
  justifyContent: 'flex-end',
  marginTop: '1em',
}

const styleUserWelcome = {
  padding: 0,
  margin: 0,
  float: 'right',
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
    display: 'inline-flex',
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
    this.props = props
    this.state = {
      focusingSkipLink: false,
    }
  }

  // componentDidMount() {
  //   const {getUser, userProfileEndpoint, authEnabled} = this.props
  //     console.log("HEADER MOUNT")
  //     console.log(authEnabled)
  //     console.log(userProfileEndpoint)
  //     console.log(this.props)
  //
  //     if (authEnabled && userProfileEndpoint) {
  //     console.log("FETCHING USER")
  //     getUser(userProfileEndpoint)
  //   }
  // }

  handleFocusSkipLink = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingSkipLink: true,
      }
    })
  }

  handleBlurSkipLink = e => {
    this.setState(prevState => {
      return {
        ...prevState,
        focusingSkipLink: false,
      }
    })
  }

  render() {
    const {
      headerDropdownMenuFeatureAvailable,
      user,
      authEnabled,
      loginEndpoint,
      logoutEndpoint,
      logoutUser,
      getUser,
      userProfileEndpoint,
    } = this.props
    const {focusingSkipLink} = this.state
    const userEmail = user && user.info ? user.info.email : null

    if (user && !user.info && !user.expired) {
      getUser(userProfileEndpoint)
    }

    const userActionButton = authEnabled ? !user.isAuthenticated ? (
      <a href={loginEndpoint}>Login</a>
    ) : (
      <a href={logoutEndpoint} onClick={() => logoutUser(logoutUser)}>
        Logout
      </a>
    ) : null

    const welcomeUser = userEmail ? (
      <div style={styleUserWelcome}>Logged in as {userEmail}</div>
    ) : null

    const headerDropDownMenuListItem = headerDropdownMenuFeatureAvailable ? (
      <li style={styleLinkListItem(false, true)}>
        <HeaderDropdownMenuButtonContainer />
      </li>
    ) : null

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
        <li style={styleLinkListItem(false, !authEnabled)}>
          <HeaderLink title="Help" to="/help">
            Help
          </HeaderLink>
        </li>
        <li
          style={styleLinkListItem(false, !headerDropdownMenuFeatureAvailable)}
        >
          {userActionButton}
        </li>
        {headerDropDownMenuListItem}
      </ul>
    )

    const insignia = <Logo key="insignia" style={{flex: '0 0 275px'}} />

    const search = (
      <Switch key="header:search:route">
        <Route exact path="/">
          {null}
        </Route>
        <Route path="/">
          <SearchFieldsContainer key="search" />
        </Route>
      </Switch>
    )

    const menu = (
      <nav key="menu" aria-label="Main" style={styleNav}>
        {menuContent}
      </nav>
    )

    const stylesMerged = {
      ...styleSkipLinkWrapper,
      ...(focusingSkipLink ? styleShowSkipLink : styleHideSkipLink),
    }

    const skipLink = (
      <div key="skipLink" style={stylesMerged}>
        <Button
          style={styleSkipLink}
          styleHover={styleSkipLinkHover}
          styleFocus={styleSkipLinkFocus}
          text="Skip To Main Content"
          onFocus={this.handleFocusSkipLink}
          onBlur={this.handleBlurSkipLink}
          onClick={() => {
            document.getElementById('mainBlock').focus()
          }}
        />
      </div>
    )

    return (
      <div style={styleWrapper}>
        <div style={styleHeader}>
          {welcomeUser}
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
        <HeaderDropdownMenuContainer />
      </div>
    )
  }
}

export default Header
