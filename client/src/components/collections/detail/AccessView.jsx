import React from 'react'
import DetailGrid from './DetailGrid'
import A from '../../common/link/Link'
import {fontFamilySerif} from '../../../utils/styleUtils'
import {SiteColors} from '../../../style/defaultStyles'

const styleParagraph = {
  padding: 0,
  margin: '0 0 1.618em 0',
}

const styleContent = (isServiceLink = false) => {
  let padding = isServiceLink
    ? '1.618em 0 0 1.618em'
    : '1.618em 1.618em 1.618em 1.618em'
  return {padding: padding}
}

const styleContentList = (showBulletPoints, isServiceLink = false) => {
  let padding = isServiceLink ? '0 1.618em 0 1.618em' : '0 1.618em 1.618em 0'
  return {
    ...{
      padding: padding,
    },
    ...(showBulletPoints ? {} : {listStyleType: 'none'}),
  }
}

const styleLink = {
  display: 'inline-block',
  color: SiteColors.LINK,
  margin: '0 0 0.618em 0',
}

const styleHeadingWrapper = {
  display: 'flex',
  backgroundColor: '#b0d1ea',
  justifyContent: 'center',
  alignItems: 'center',
  alignSelf: 'stretch',
  height: '100%',
  margin: 0,
}

const styleHeading = {
  margin: 0,
  padding: '0.618em',
  width: '100%',
  fontFamily: fontFamilySerif(),
}

export default class AccessView extends React.Component {
  renderAccessHeading = heading => {
    return (
      <div style={styleHeadingWrapper}>
        <h3 style={styleHeading}>{heading}</h3>
      </div>
    )
  }

  renderAccessLink = (link, index) => {
    const {linkUrl, linkName, linkProtocol, linkDescription} = link
    var linkTitle = linkName ? linkName : linkProtocol
    if (linkProtocol == null && linkName == null) {
      linkTitle = 'Unlabeled'
    }
    return (
      <li key={index} aria-label={linkTitle}>
        <div>
          <A href={linkUrl} target="_blank" title={linkTitle} style={styleLink}>
            {linkTitle}
          </A>
          <div style={styleParagraph}>{linkDescription}</div>
        </div>
      </li>
    )
  }

  renderAccessLinkList = (links, showEmpty) => {
    let listItems = links.map((link, index) => {
      return this.renderAccessLink(link, index)
    })
    const isEmpty = listItems.length < 1
    if (isEmpty && showEmpty) {
      return <div style={styleContent()}>No links in metadata.</div>
    }
    else if (isEmpty && !showEmpty) {
      return null
    }
    else {
      return (
        <div style={styleContent()}>
          <ul style={styleContentList(false)}>{listItems}</ul>
        </div>
      )
    }
  }

  renderAccessServiceLinkList = (title, links, serviceIndex) => {
    let listItems = links.map((link, index) => {
      return this.renderAccessLink(link, index)
    })
    const isEmpty = listItems.length < 1
    if (isEmpty) {
      return null
    }
    else {
      return (
        <div key={serviceIndex} style={styleContent(true)}>
          <div style={styleParagraph}>{title}</div>
          <ul style={styleContentList(false, true)}>{listItems}</ul>
        </div>
      )
    }
  }

  renderAccessList = (items, notAvailable) => {
    const list = items ? items : []
    if (list.length === 0) {
      return <div style={styleContent()}>{notAvailable}</div>
    }
    const distributionsFormats = list.map((format, index) => {
      return <li key={index}>{format.name}</li>
    })
    return (
      <div style={styleContent()}>
        <ul style={styleContentList(true)}>{distributionsFormats}</ul>
      </div>
    )
  }

  render() {
    const {item} = this.props

    const linkSections = [
      {linkFunction: 'information', heading: 'Information', showEmpty: true},
      {linkFunction: 'download', heading: 'Download Data', showEmpty: true},
      {linkFunction: 'search', heading: 'Search for Data'},
      {linkFunction: 'order', heading: 'Order'},
      {linkFunction: 'offlineAccess', heading: 'Offline Access'},
    ]

    let accessGrid = linkSections
      .map(section => {
        let heading = this.renderAccessHeading(section.heading)
        let links = item.links.filter(
          link => link.linkFunction === section.linkFunction
        )
        let list = this.renderAccessLinkList(links, !!section.showEmpty)
        return list === null ? null : [ heading, list ]
      })
      .filter(section => {
        return section !== null
      })

    const distributionFormatsHeading = this.renderAccessHeading(
      'Distribution Formats'
    )
    const distributionFormatsList = this.renderAccessList(
      item.dataFormats,
      'No formats in metadata.'
    )

    if (item.serviceLinks && item.serviceLinks !== [] && item.serviceLinks[0]) {
      const serviceLinks = [
        this.renderAccessHeading('Services'),
        item.serviceLinks.map((service, index) =>
          this.renderAccessServiceLinkList(service.title, service.links, index)
        ),
      ]

      accessGrid.push(serviceLinks)
    }
    accessGrid.push([ distributionFormatsHeading, distributionFormatsList ])

    return <DetailGrid grid={accessGrid} colWidths={[ {sm: 3}, {sm: 9} ]} />
  }
}
