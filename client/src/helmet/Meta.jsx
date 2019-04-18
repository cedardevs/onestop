import {Helmet} from 'react-helmet'
import React, {Component} from 'react'
import PropTypes from 'prop-types'
import _ from 'lodash'
import {toJsonLd, appJsonLd} from '../utils/jsonLdUtils'

import {getBasePath} from '../utils/urlUtils'

export default class Meta extends Component {
  formatTitle = title => {
    const words = _.words(title)

    const shortenedTitle =
      _.size(words) > 5
        ? _.join(_.concat(_.slice(words, 0, 5), '...'), ' ')
        : title

    return _.join(_.concat(shortenedTitle, 'on NOAA OneStop'), ' ')
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
      rootSearchAction,
    } = this.props

    /*
    Default values for every variable are critial, because otherwise helmet will leave meta tags set to old values when you return to a previous page (such as clicking the home link after visiting a collection.)
    */
    const URL = `${window.location.origin + window.location.pathname}`
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
        {toJsonLd(itemUuid, item, URL)}
      </script>
    ) : null
    const searchActionJsonLd = rootSearchAction ? (
      <script type="application/ld+json">{appJsonLd(URL)}</script>
    ) : null

    var faviconPath = `${getBasePath()}/static/noaa-favicon.ico`.replace(
      '//',
      '/',
      'g'
    )

    return (
      <Helmet>
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <link href={faviconPath} rel="shortcut icon" />
        <meta property="og:url" content={URL} />

        <meta property="robots" content={robotsValue} />

        <title>{formattedTitle}</title>
        <meta property="dcterms.title" content={formattedTitle} />
        <meta property="og:title" content={formattedTitle} />

        <meta property="description" content={descriptionValue} />
        <meta property="og:description" content={descriptionValue} />
        <meta property="dcterms.format" content="text/html" />
        <meta property="og:type" content="website" />
        <meta
          property="og:site_name"
          content="National Oceanic and Atmospheric Administration"
        />

        <meta property="og:image" content={imageValue} />
        <meta property="og:image:width" content="800" />
        <meta property="og:image:height" content="400" />

        {jsonLD}
        {searchActionJsonLd}
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
