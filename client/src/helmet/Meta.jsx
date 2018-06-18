import {Helmet} from 'react-helmet'
import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'

export default class Meta extends Component {
  formatTitle = title => {
    const words = _.words(title)
    if (_.size(words) > 5) {
      return _.join(_.concat(_.slice(words, 0, 5), '... || NOAA OneStop'), ' ')
    }
    return title
  }

  formatDescription = description => {
    const words = _.words(description)
    if (_.size(words) > 300) {
      return _.join(_.concat(_.slice(words, 0, 250), '...'), ' ')
    }
    return description
  }

  render() {
    const {title, description, robots, thumbnail} = this.props

    /*
    Default values for every variable are critial, because otherwise helmet will leave meta tags set to old values when you return to a previous page (such as clicking the home link after visiting a collection.)
    */
    const titleValue = title ? this.formatTitle(title) : 'NOAA OneStop'
    const descriptionValue = description
      ? this.formatDescription(description)
      : 'A NOAA Data Search Platform.'
    const robotsValue = robots || 'index, nofollow'
    const imageValue =
      thumbnail || 'https://data.noaa.gov/datasetsearch/img/oneStop.jpg'

    return (
      <Helmet>
        <link href="./noaa-favicon.ico" rel="shortcut icon" />

        <meta property="robots" content={robotsValue} />

        <title>{titleValue}</title>
        <meta property="dcterms.title" content={titleValue} />
        <meta property="og:title" content={titleValue} />

        <meta property="description" content={descriptionValue} />
        <meta property="og:description" content={descriptionValue} />
        <meta
          property="og:site_name"
          content="National Oceanic and Atmospheric Administration"
        />

        <meta property="og:image" content={imageValue} />
        <meta property="og:image:width" content="800" />
        <meta property="og:image:height" content="400" />

        {/*TODO <meta property="og:url" content="http://localhost:8080/onestop/">*/}
      </Helmet>
    )
  }
}

Meta.propTypes = {
  title: PropTypes.string,
  description: PropTypes.string,
  robots: PropTypes.string,
  thumbnail: PropTypes.string,
}
