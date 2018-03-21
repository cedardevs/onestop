import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import styles from './DetailStyles.css'
import A from '../common/link/Link'
import MapThumbnail from '../common/MapThumbnail'
import FlexRow from '../common/FlexRow'
import Expandable from '../common/Expandable'

const styleContainer = {
  padding: '1.618em',
}

const styleEqualFlexItem = {
  flex: '1 1 auto',
  width: '50%',
}

const styleStar = {
  maxHeight: '1em',
  maxWidth: '1em',
  fill: 'goldenrod',
}

const styleInfoIcon = {
  maxHeight: '1em',
  maxWidth: '1em',
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
        if (i < fullStars) {
          stars.push(this.renderStar(i, this.fullStarPath())) //this.renderFullStar(i))
        }
        else if (i === fullStars && halfStar) {
          stars.push(this.renderStar(i, this.halfStarPath()))
        }
        else {
          stars.push(this.renderStar(i, this.emptyStarPath()))
        }
      }
    }

    return (
      <FlexRow
        items={[
          <FlexRow
            items={[
              stars,
              <span
                style={{fontSize: '0px'}}
              >{`${dsmmDesc} DSMM rating`}</span>,
            ]}
          />,
          <Expandable
            heading={this.renderInfoCircle()}
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

  renderStar = (i, path) => {
    return (
      <span style={{width: '1em'}}>
        <svg key={i} style={styleStar} viewBox="0 0 1792 1792">
          {path}
        </svg>
      </span>
    )
  }
  fullStarPath = () => {
    // from font-awesome star.svg
    return (
      <path d="M1728 647q0 22-26 48l-363 354 86 500q1 7 1 20 0 21-10.5 35.5t-30.5 14.5q-19 0-40-12l-449-236-449 236q-22 12-40 12-21 0-31.5-14.5t-10.5-35.5q0-6 2-20l86-500-364-354q-25-27-25-48 0-37 56-46l502-73 225-455q19-41 49-41t49 41l225 455 502 73q56 9 56 46z" />
    )
  }
  halfStarPath = () => {
    // from font-awesome star-half-o.svg
    return (
      <path d="M1250 957l257-250-356-52-66-10-30-60-159-322v963l59 31 318 168-60-355-12-66zm452-262l-363 354 86 500q5 33-6 51.5t-34 18.5q-17 0-40-12l-449-236-449 236q-23 12-40 12-23 0-34-18.5t-6-51.5l86-500-364-354q-32-32-23-59.5t54-34.5l502-73 225-455q20-41 49-41 28 0 49 41l225 455 502 73q45 7 54 34.5t-24 59.5z" />
    )
  }
  emptyStarPath = () => {
    // from font-awesome star-o.svg
    return (
      <path d="M1201 1004l306-297-422-62-189-382-189 382-422 62 306 297-73 421 378-199 377 199zm527-357q0 22-26 48l-363 354 86 500q1 7 1 20 0 50-41 50-19 0-40-12l-449-236-449 236q-22 12-40 12-21 0-31.5-14.5t-10.5-35.5q0-6 2-20l86-500-364-354q-25-27-25-48 0-37 56-46l502-73 225-455q19-41 49-41t49 41l225 455 502 73q56 9 56 46z" />
    )
  }
  renderInfoCircle = () => {
    // from font-awesome info-circle.svg
    return (
      <div aria-label="DSMM info" style={{width: '1em'}}>
        <svg style={styleInfoIcon} viewBox="0 0 1792 1792">
          <path d="M1152 1376v-160q0-14-9-23t-23-9h-96v-512q0-14-9-23t-23-9h-320q-14 0-23 9t-9 23v160q0 14 9 23t23 9h96v320h-96q-14 0-23 9t-9 23v160q0 14 9 23t23 9h448q14 0 23-9t9-23zm-128-896v-160q0-14-9-23t-23-9h-192q-14 0-23 9t-9 23v160q0 14 9 23t23 9h192q14 0 23-9t9-23zm640 416q0 209-103 385.5t-279.5 279.5-385.5 103-385.5-103-279.5-279.5-103-385.5 103-385.5 279.5-279.5 385.5-103 385.5 103 279.5 279.5 103 385.5z" />
        </svg>
      </div>
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
