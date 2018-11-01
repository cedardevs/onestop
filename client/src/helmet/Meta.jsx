import {Helmet} from 'react-helmet'
import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import {toJsonLd} from '../utils/jsonLdUtils'

export default class Meta extends Component {
  formatTitle = title => {
    const words = _.words(title)
    if (_.size(words) > 5) {
      return _.join(_.concat(_.slice(words, 0, 5), '... on NOAA OneStop'), ' ')
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
    const {
      title,
      formatTitle,
      description,
      robots,
      thumbnail,
      item,
      itemUuid,
    } = this.props

    /*
    Default values for every variable are critial, because otherwise helmet will leave meta tags set to old values when you return to a previous page (such as clicking the home link after visiting a collection.)
    */
    const titleValue = title ? title : 'NOAA OneStop'
    const formattedTitle = formatTitle
      ? this.formatTitle(titleValue)
      : titleValue
    const descriptionValue = description
      ? this.formatDescription(description)
      : 'A NOAA Data Search Platform.'
    const robotsValue = robots || 'index, nofollow'
    const imageValue =
      thumbnail || 'https://data.noaa.gov/datasetsearch/img/oneStop.jpg'
    const jsonLD = item ? (
      <script type="application/ld+json">
        {toJsonLd(itemUuid, item, location.href)}
      </script>
    ) : null

    return (
      <Helmet>
        <link href="/noaa-favicon.ico" rel="shortcut icon" />

        <meta property="robots" content={robotsValue} />

        <title>{formattedTitle}</title>
        <meta property="dcterms.title" content={formattedTitle} />
        <meta property="og:title" content={formattedTitle} />

        <meta property="description" content={descriptionValue} />
        <meta property="og:description" content={descriptionValue} />
        <meta
          property="og:site_name"
          content="National Oceanic and Atmospheric Administration"
        />

        <meta property="og:image" content={imageValue} />
        <meta property="og:image:width" content="800" />
        <meta property="og:image:height" content="400" />

        <meta property="og:url" content={`${location.href}`} />

        {jsonLD}
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
