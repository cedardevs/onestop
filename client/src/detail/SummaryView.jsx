import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import styles from './DetailStyles.css'
import A from '../common/link/Link'
import MapThumbnail from '../common/MapThumbnail'
import FlexRow from '../common/FlexRow'
import Expandable from '../common/Expandable'
import {buildCoordinatesString, buildTimePeriodString} from "../utils/resultUtils";
import {
  star,
  star_o,
  star_half_o,
  info_circle,
  SvgIcon,
} from '../common/SvgIcon'

const styleContainer = {
  padding: '1.618em',
}

const styleEqualFlexItem = {
  flex: '1 1 auto',
  width: '50%',
}

const styleStar = {
  fill: 'goldenrod',
}

const styleLink = {
  display: 'inline-block',
  color: 'rgb(85, 172, 228)',
  margin: '0 0 0.618em 0',
  textDecorationLine: 'underline',
}

class SummaryView extends Component {
  constructor(props) {
    super(props)
    this.state = {
      showAllThemes: false,
      showAllInstruments: false,
      showAllPlatforms: false,
    }

    this.handleShowGCMD = this.handleShowGCMD.bind(this)
  }

  handleShowGCMD(type) {
    if (type === 'gcmdScience') {
      this.setState({
        showAllThemes: !this.state.showAllThemes,
      })
    }
    else if (type === 'gcmdInstruments') {
      this.setState({
        showAllInstruments: !this.state.showAllInstruments,
      })
    }
    else if (type === 'gcmdPlatforms') {
      this.setState({
        showAllPlatforms: !this.state.showAllPlatforms,
      })
    }
  }

  render() {

    const { granuleSearch, totalGranuleCount, item } = this.props

    const timeSpaceSummary = (
      <div key={'timeSpaceSummary'} style={styleEqualFlexItem}>
        <div className={styles.sectionHeading}>Time Period:</div>
        <div>
          {buildTimePeriodString(item.beginDate, item.beginYear, item.endDate, item.endYear)}
        </div>
        <div className={styles.sectionHeading}>Spatial Bounding Map:</div>
        <div className={styles.previewMap}>
          <MapThumbnail
            geometry={item.spatialBounding}
            interactive={true}
          />
        </div>
        <div className={styles.sectionHeading}>Bounding Coordinates:</div>
        <div>{buildCoordinatesString(item.spatialBounding)}</div>
        <div className={styles.sectionHeading}>DSMM Rating:</div>
        {this.renderDSMMRating()}
      </div>
    )

    const granules =
      totalGranuleCount == 0 ? (
        <div>No granules in this collection</div>
      ) : (
        <div>
          <a style={styleLink} onClick={granuleSearch}>
            Show Files Matching My Search
          </a>
        </div>
      )

    const keywordSummary = (
      <div key="granuleAndKeywordSummary" style={styleEqualFlexItem}>
        <div>{granules}</div>
        <div>
          <div className={styles.sectionHeading}>Themes:</div>
          {this.renderGCMDKeywords(
            'gcmdScience',
            '#008445',
            this.state.showAllThemes
          )}
          <div className={styles.sectionHeading}>Instruments:</div>
          {this.renderGCMDKeywords(
            'gcmdInstruments',
            '#0965a1',
            this.state.showAllInstruments
          )}
          <div className={styles.sectionHeading}>Platforms:</div>
          {this.renderGCMDKeywords(
            'gcmdPlatforms',
            '#008445',
            this.state.showAllPlatforms
          )}
        </div>
      </div>
    )

    return (
      <div style={styleContainer}>
        <div>
          <span className={styles.sectionHeading}>Total Files:&nbsp;</span>
          {totalGranuleCount}
        </div>
        <FlexRow
          style={{justifyContent: 'space-between'}}
          items={[ timeSpaceSummary, keywordSummary ]}
        />
      </div>
    )
  }

  renderDSMMRating() {
    const {item} = this.props
    const dsmmScore = item.dsmmAverage
    const fullStars = Math.floor(dsmmScore)
    const halfStar = dsmmScore % 1 >= 0.5
    const dsmmDesc = _.round(dsmmScore, 2).toFixed(2)

    const stars = []
    if (dsmmScore === 0) {
      stars.push(
        <span key={42} className={styles.dsmmMissing}>
          DSMM Rating Unavailable
        </span>
      )
    }
    else {
      for (let i = 0; i < 5; i++) {
        let starType
        if (i < fullStars) {
          starType = star
        }
        else if (i === fullStars && halfStar) {
          starType = star_half_o
        }
        else {
          starType = star_o
        }
        stars.push(
          <SvgIcon key={`dsmm-star-${i}`} style={styleStar} path={starType} />
        )
      }
    }

    return (
      <FlexRow
        items={[
          <FlexRow
            key="dsmm-stars"
            items={[
              stars,
              <span
                key="dsmm-text-value"
                style={{fontSize: '0px'}}
              >{`${dsmmDesc} DSMM rating`}</span>,
            ]}
          />,
          <Expandable
            key="dsmm-info"
            heading={
              <div aria-label="DSMM info">
                <SvgIcon path={info_circle} />
              </div>
            }
            open={false}
            content={
              <div>
                {' '}
                This is the average DSMM rating of this collection. The{' '}
                <A
                  href="http://doi.org/10.2481/dsj.14-049"
                  target="_blank"
                  title="Data Stewardship Maturity Matrix Information"
                >
                  Data Stewardship Maturity Matrix (DSMM)
                </A>{' '}
                is a unified framework that defines criteria for the following
                nine components based on measurable practices:
                <ul>
                  <li>Accessibility</li>
                  <li>Data Integrity</li>
                  <li>Data Quality Assessment</li>
                  <li>Data Quality Assurance</li>
                  <li>Data Quality Control Monitoring</li>
                  <li>Preservability</li>
                  <li>Production Sustainability</li>
                  <li>Transparency Traceability</li>
                  <li>Usability</li>
                </ul>
              </div>
            }
          />,
        ]}
      />
    )
  }

  renderGCMDKeywords(type, bgColor, showAll) {
    const {item} = this.props
    let keywords = (item && item[type]) || []

    if (!_.isEmpty(keywords)) {
      if (type === 'gcmdScience') {
        keywords = keywords
          .map(k => k.split('>')) // split GCMD keywords apart
          .reduce((list, keys) => list.concat(keys), []) // flatten
          .map(k => k.trim()) // you can figure this one out
          .filter((k, i, a) => a.indexOf(k) === i) // dedupe
      }
      else {
        keywords = keywords.map(term => term.split('>').pop().trim())
      }
      keywords = keywords.map(
        (k, index) =>
          index > 2 && !showAll ? null : (
            <div
              className={styles.keyword}
              style={{backgroundColor: bgColor}}
              key={k}
            >
              {k}
            </div>
          )
      )

      if (keywords.length > 3) {
        return (
          <div>
            <div className={styles.keywords}>{keywords}</div>
            <div
              className={styles.showMoreButton}
              onClick={() => {
                this.handleShowGCMD(type)
              }}
            >
              {!showAll ? 'Show All' : 'Collapse'}
            </div>
          </div>
        )
      }
      else {
        return <div className={styles.keywords}>{keywords}</div>
      }
    }
    else {
      return (
        <div style={{fontStyle: 'italic', color: bgColor}}>None Provided</div>
      )
    }
  }
}

SummaryView.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default SummaryView
