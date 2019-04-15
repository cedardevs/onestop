import React from 'react'
import {Route, Switch} from 'react-router'

import CollectionSearchContainer from '../collections/filters/CollectionSearchContainer'
import Logo from './Logo'
import HeaderLink from './HeaderLink'

import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'

import {fontFamilySerif} from '../../utils/styleUtils'
import FlexRow from '../common/ui/FlexRow'

import HeaderCartLinkContainer from './HeaderCartLinkContainer'

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
    const userEmail = user && user.info ? user.info.email : null

    const userActionButton = authEnabled ? !user.isAuthenticated ? (
      <HeaderLink title="Login" to={loginEndpoint} isExternalLink={true} />
    ) : (
      <HeaderLink
        title="Logout"
        to={logoutEndpoint}
        isExternalLink={true}
        onClick={() => logoutUser()}
      />
    ) : null

    const userListItem = authEnabled ? (
      <li style={styleLinkListItem(false, !cartEnabled)}>{userActionButton}</li>
    ) : null

    const cartListItem = cartEnabled ? (
      <li style={styleLinkListItem(false, cartEnabled)}>
        {cartEnabled ? <HeaderCartLinkContainer /> : null}
      </li>
    ) : null

    const welcomeUser = userEmail ? (
      <div style={styleUserWelcome} key="emailDisplay">
        Logged in as {userEmail}
      </div>
    ) : null

    const menuContent = (
      <ul style={styleLinkList}>
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
        {userListItem}
        {cartListItem}
      </ul>
    )

    const insignia = <Logo key="insignia" style={{flex: '0 0 275px'}} />

    const search = (
      <Switch key="header:search:route">
        <Route exact path="/">
          {null}
        </Route>
        <Route path="/">
          <CollectionSearchContainer key="search" />
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
          <FlexRow
            style={styleUserWelcome}
            key="welcomeUser"
            items={[ welcomeUser ]}
          />
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
    )
  }
}

export default Header
