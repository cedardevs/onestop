import React from 'react'
import DetailGrid from './DetailGrid'
import {fontFamilySerif} from '../../../utils/styleUtils'

const styleKeywordTitle = {
  padding: 0,
  margin: '0 0 1.618em 0',
  fontFamily: fontFamilySerif(),
  fontWeight: 'bold',
}

const styleContent = {
  padding: '1.618em',
}

const styleContentList = {
  padding: '0 1.618em 1.618em 1.618em',
  margin: '0 0 0 1.618em',
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

export default class KeywordsView extends React.Component {
  renderKeywordHeading = heading => {
    return (
      <div style={styleHeadingWrapper}>
        <h3 style={styleHeading}>{heading}</h3>
      </div>
    )
  }

  renderKeywordList = (title, keywords) => {
    let listItems = keywords.map((item, index) => {
      return <li key={index}>{item}</li>
    })
    let keywordList = <div style={styleContent}>Not available in metadata.</div>
    if (listItems.length > 0) {
      keywordList = (
        <div style={styleContent}>
          <p style={styleKeywordTitle}>{title}</p>
          <ul style={styleContentList}>{listItems}</ul>
        </div>
      )
    }
    return keywordList
  }

  render() {
    const {item} = this.props

    const dataCenterHeading = this.renderKeywordHeading('Data Center keywords')
    const dataCenterList = this.renderKeywordList(
      'Global Change Master Directory (GCMD) Data Center Keywords',
      item.gcmdDataCenters
    )

    const platformHeading = this.renderKeywordHeading('Platform keywords')
    const platformList = this.renderKeywordList(
      'Global Change Master Directory (GCMD) Platform Keywords',
      item.gcmdPlatforms
    )

    const instrumentHeading = this.renderKeywordHeading('Instrument keywords')
    const instrumentList = this.renderKeywordList(
      'Global Change Master Directory (GCMD) Instrument Keywords',
      item.gcmdInstruments
    )

    const locationHeading = this.renderKeywordHeading('Place keywords')
    const locationList = this.renderKeywordList(
      'Global Change Master Directory (GCMD) Location Keywords',
      item.gcmdLocations
    )

    const projectHeading = this.renderKeywordHeading('Project keywords')
    const projectList = this.renderKeywordList(
      'Global Change Master Directory (GCMD) Project Keywords',
      item.gcmdProjects
    )

    const accessGrid = [
      [ dataCenterHeading, dataCenterList ],
      [ platformHeading, platformList ],
      [ instrumentHeading, instrumentList ],
      [ locationHeading, locationList ],
      [ projectHeading, projectList ],
    ]

    return <DetailGrid grid={accessGrid} colWidths={[ {sm: 3}, {sm: 9} ]} />
  }
}
