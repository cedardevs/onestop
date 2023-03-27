import React from 'react'
import PropTypes from 'prop-types'
import FooterLink from './FooterLink'

import {creative_commons, github, SvgIcon} from '../common/SvgIcon'

const styleFooter = {
  width: '100%',
  color: 'white',
  textAlign: 'center',
}

const styleFooterList = {
  listStyleType: 'none',
  lineHeight: '2em',
  fontWeight: '400',
  fontSize: '1.1em',
  display: 'inline-block',
  padding: '0 0 1em 0',
  borderBottom: '1px solid white',
}

const styleFooterItem = {
  display: 'inline-block',
  margin: 0,
}

const styleLinkNotLast = {
  borderRight: '1px solid white',
}

const styleLinkLast = {
  border: 'none',
}

const styleDetails = {
  marginBottom: '1.618em',
}

const styleVersionInfo = {
  display: 'inline',
}

const styleImageAttribution = {
  display: 'inline',
}

const styleIcon = {
  fill: 'white',
  position: 'relative',
  top: '.15em',
}

const links = [
  {
    href: '//www.ncei.noaa.gov/privacy',
    text: 'Privacy Policy',
  },
  {
    href: '//www.noaa.gov/foia-freedom-of-information-act',
    text: 'Freedom of Information Act',
  },
  {
    href: '//www.cio.noaa.gov/services_programs/info_quality.html',
    text: 'Information Quality',
  },
  {
    href: '//www.noaa.gov/disclaimer.html',
    text: 'Disclaimer',
  },
  {
    href:
      'https://docs.google.com/forms/d/e/1FAIpQLSeYcbKOaK50do35QbgTprXAVSnBDC00eY22HPPA2aRdkbhujg/viewform',
    text: 'Take Our Survey',
  },
  {
    href: 'mailto:noaa.data.catalog@noaa.gov?Subject=NOAA%20OneStop%20Feedback',
    text: 'Contact Us',
  },
  {
    href: '//www.commerce.gov/',
    text: 'Department of Commerce',
  },
  {
    href: '//www.noaa.gov/',
    text: 'NOAA',
  },
  {
    href: '//www.nesdis.noaa.gov/',
    text: 'NESDIS',
  },
]

class Footer extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    const linkElements = links.map((link, i) => {
      0
      const styleLink =
        i === links.length - 1 ? styleLinkLast : styleLinkNotLast
      return (
        <li style={styleFooterItem} key={i}>
          <FooterLink href={link.href} title={link.text} style={styleLink}>
            {link.text}{' '}
          </FooterLink>
        </li>
      )
    })

    return (
      <div>
        <nav aria-label="Footer" style={styleFooter}>
          <div>
            <ul style={styleFooterList}>{linkElements}</ul>
          </div>
          <div style={styleDetails}>
            <div style={styleVersionInfo}>
              <FooterLink
                href={'https://github.com/cedardevs/onestop/releases'}
                target={'_blank'}
                title={'code on GitHub'}
              >
                Version: {this.props.version}{' '}
                <SvgIcon
                  size="1em"
                  style={styleIcon}
                  verticalAlign="initial"
                  path={github}
                />
              </FooterLink>
            </div>
            {' | '}
            <div style={styleImageAttribution}>
              <FooterLink to={'/about#attribution'}>
                Image Attribution{' '}
                <SvgIcon
                  size="1em"
                  style={styleIcon}
                  path={creative_commons}
                  verticalAlign="initial"
                />
              </FooterLink>
            </div>
          </div>
        </nav>
      </div>
    )
  }
}

Footer.propTypes = {
  version: PropTypes.string.isRequired,
}

Footer.defaultProps = {
  version: '',
}

export default Footer
