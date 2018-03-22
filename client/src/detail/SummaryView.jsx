import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import styles from './DetailStyles.css'
import A from '../common/link/Link'
import MapThumbnail from '../common/MapThumbnail'
import FlexRow from '../common/FlexRow'
import Expandable from '../common/Expandable'
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
    const startDate = this.props.item.beginDate
    const endDate = this.props.item.endDate
      ? this.props.item.endDate
      : 'Present'

    const timeSpaceSummary = (
      <div key={'timeSpaceSummary'} style={styleEqualFlexItem}>
        <div className={styles.sectionHeading}>Time Period:</div>
        <div>
          {startDate && endDate ? (
            `${startDate.split('T')[0]} to ${endDate.split('T')[0]}`
          ) : (
            'Not Provided'
          )}
        </div>
        <div className={styles.sectionHeading}>Spatial Bounding Map:</div>
        <div className={styles.previewMap}>
          <MapThumbnail
            geometry={this.props.item.spatialBounding}
            interactive={true}
          />
        </div>
        <div className={styles.sectionHeading}>Bounding Coordinates:</div>
        <div>{this.buildCoordinatesString()}</div>
        <div className={styles.sectionHeading}>DSMM Rating:</div>
        {this.renderDSMMRating()}
      </div>
    )

    const keywordSummary = (
      <div key={'keywordSummary'} style={styleEqualFlexItem}>
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
    )

    return (
      <div style={styleContainer}>
        <div>
          <span className={styles.sectionHeading}>Total Files:&nbsp;</span>
          {this.props.totalGranuleCount}
        </div>
        <FlexRow
          style={{justifyContent: 'space-between'}}
          items={[ timeSpaceSummary, keywordSummary ]}
        />
      </div>
    )
  }

  renderDSMMRating() {
    const dsmmScore = this.props.item.dsmmAverage
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
    let keywords = (this.props.item && this.props.item[type]) || []

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

  buildCoordinatesString() {
    // For point, want: "Point at [0], [1] (longitude, latitude)"
    // For polygon want: "Bounding box covering [0][0], [0][1], [2][0], [2][1] (N, W, S, E)"
    const geometry = this.props.item.spatialBounding
    if (geometry) {
      const deg = '\u00B0'
      if (geometry.type.toLowerCase() === 'point') {
        return `Point at ${geometry.coordinates[0]}${deg}, ${geometry
          .coordinates[1]}${deg} (longitude, latitude).`
      }
      else {
        return `Bounding box covering ${geometry
          .coordinates[0][0][0]}${deg}, ${geometry
          .coordinates[0][0][1]}${deg}, ${geometry
          .coordinates[0][2][0]}${deg}, ${geometry
          .coordinates[0][2][1]}${deg} (W, N, E, S).`
      }
    }
    else {
      return 'No spatial bounding provided.'
    }
  }
}

SummaryView.propTypes = {
  id: PropTypes.string,
  item: PropTypes.object,
}

export default SummaryView
