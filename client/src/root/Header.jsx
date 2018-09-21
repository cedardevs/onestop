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

import cart from 'fa/cart-arrow-down.svg'

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

const styleCartButton = {
  fontSize: '0.618em',
}

const styleCartIcon = {
  width: '1.618em',
  height: '1.618em',
  marginRight: '0.309em',
}

class Header extends React.Component {
  constructor(props) {
    super(props)
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

  handleRedirectToCart = () => {
    const {history} = this.props
    history.push('/cart')
  }

  render() {
    const {
      abbreviatedNumberOfGranulesSelected,
      shoppingCartEnabled,
    } = this.props
    const {focusingSkipLink} = this.state
    const shoppingCartLink = shoppingCartEnabled ? (
      <li style={styleLinkListItem(false, true)}>
        <Button
          style={styleCartButton}
          title="Shopping Cart"
          text={abbreviatedNumberOfGranulesSelected}
          icon={cart}
          styleIcon={styleCartIcon}
          onClick={this.handleRedirectToCart}
        />
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
        <li style={styleLinkListItem(false, !shoppingCartEnabled)}>
          <HeaderLink title="Help" to="/help">
            Help
          </HeaderLink>
        </li>
        {shoppingCartLink}
      </ul>
    )

    const insignia = (
      <Logo
        key="insignia"
        onClick={this.props.goHome}
        style={{flex: '0 0 275px'}}
      />
    )

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
    // this.props.showSearch ? (
    //   <SearchFieldsContainer key="search" />
    // ) : null

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
  goHome: PropTypes.func.isRequired,
}

export default Header
