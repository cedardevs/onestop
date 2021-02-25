import React from 'react'
import {Route} from 'react-router'

import CollectionSearchContainer from '../filters/collections/CollectionSearchContainer'
import Logo from './Logo'
import HeaderLink from './HeaderLink'

import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'

import {fontFamilySerif} from '../../utils/styleUtils'
import FlexRow from '../common/ui/FlexRow'

import HeaderCartLinkContainer from './HeaderCartLinkContainer'
import Link from '../common/link/Link'

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
  justifyContent: 'flex-end',
}

const styleLinkList = {
  padding: 0,
  margin: '0 0.618em 0 0',
  listStyleType: 'none',
  fontSize: '1.2em',
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

const styleLink = {
  textDecoration: 'none',
  color: '#d7d7d7',
  fontWeight: 300,
  transition: 'color 0.3s ease',
  paddingRight: '0.309em',
  paddingLeft: '0.309em',
}

const styleLinkHover = {
  color: '#21ABE2',
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
      user,
      authEnabled,
      cartEnabled,
      loginEndpoint,
      logoutEndpoint,
      logoutUser,
    } = this.props

    const {focusingSkipLink} = this.state
    const userId = user && user.profile ? user.profile.userId : null
    const userButtonDisplayed = authEnabled && (user && !user.isFetching)

    const userActionButton = userButtonDisplayed ? !user.isAuthenticated ? (
      <HeaderLink title="Login" to={loginEndpoint} isExternalLink={true} />
    ) : (
      // <HeaderLink
      //   title="Logout"
      //   to={logoutEndpoint}
      //   isExternalLink={true}
      //   onClick={() => logoutUser()}
      // />
      // TODO - switch back to router-based HeaderLink after customizing logout view? see #1448
      <Link
        title="Logout"
        onClick={() => logoutUser(logoutEndpoint)}
        style={styleLink}
        styleHover={styleLinkHover}
      >
        Logout
      </Link>
    ) : null

    const userListItem = userButtonDisplayed ? (
      <li style={styleLinkListItem(false, !cartEnabled)}>{userActionButton}</li>
    ) : null

    const userDashboardItem =
      userButtonDisplayed && user.isAuthenticated ? (
        <li style={styleLinkListItem(false, !cartEnabled)}>
          <HeaderLink title="Dashboard" to="/dashboard">
            Dashboard
          </HeaderLink>
        </li>
      ) : null

    const cartListItem = cartEnabled ? (
      <li style={styleLinkListItem(false, cartEnabled)}>
        {cartEnabled ? <HeaderCartLinkContainer /> : null}
      </li>
    ) : null

    const welcomeUser = userId ? (
      <FlexRow
        style={styleUserWelcome}
        key="welcomeUser"
        items={[
          <div style={styleUserWelcome} key="emailDisplay">
            Logged in as {userId}
          </div>,
        ]}
      />
    ) : null

    const menuContent = (
      <ul style={styleLinkList}>
        {userDashboardItem}
        {userListItem}
        <li style={styleLinkListItem(true, false)}>
          <HeaderLink title="About" to="/about">
            About
          </HeaderLink>
        </li>
        <li style={styleLinkListItem(false, !authEnabled && !cartEnabled)}>
          <HeaderLink title="Help" to="/help">
            Help
          </HeaderLink>
        </li>

        {cartListItem}
      </ul>
    )

    const insignia = <Logo key="insignia" style={{flex: '0 0 275px'}} />

    const search = this.props.showSearchInput ? (
      <CollectionSearchContainer key="search" />
    ) : null

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
      <Route path="/">
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
        </div>
      </Route>
    )
  }
}

export default Header
