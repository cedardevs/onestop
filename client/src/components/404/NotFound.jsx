import React from 'react'
import Meta from '../helmet/Meta'
import {Link} from 'react-router-dom'

import {fontFamilyMonospace, fontFamilySerif} from '../../utils/styleUtils'

const styleWrapper = {
  width: '100%',
  color: '#111',
}

const style404 = {
  fontSize: '1.318em',
  padding: '1.618em',
  minHeight: '100vh',
  margin: '0 auto',
  maxWidth: '45em',
}

const styleH1 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.5em',
  margin: '0 0 0.618em 0',
}

const styleH2 = {
  fontFamily: fontFamilySerif(),
  fontSize: '1.1em',
  margin: '0 0 0.618em 0',
}

const styleSuggestedQuery = {
  fontFamily: fontFamilyMonospace(),
  margin: '0 0 0.618em 0',
  padding: 0,
  color: '#277cb2',
  textDecoration: 'underline',
}

export default class NotFound extends React.Component {
  search = query => {
    const {submit} = this.props
    submit(query)
  }

  render() {
    // retrieve the attempted route location
    const {location} = this.props

    // split the unresolved route by slashes, filtering out any non-truthy values
    let possibleQueryComponents = location.pathname.split('/').filter(Boolean)

    // if route is deeply nested, only suggest queries based on first 3 path components
    if (possibleQueryComponents.length > 3) {
      possibleQueryComponents = possibleQueryComponents.slice(0, 3)
    }

    // use simple logical AND/OR in query text suggestions
    const joinPossibilities = [ ' AND ', ' OR ' ]

    const uniqueSuggestedQueries = [
      // ensure suggestions are unique (in case 1 path component)
      ...new Set(
        joinPossibilities.map(joinChar => {
          return possibleQueryComponents.join(joinChar)
        })
      ),
    ]

    // construct links to trigger searches based on suggestions
    const suggestions = uniqueSuggestedQueries.map((query, index) => {
      return (
        <li key={index}>
          <a style={styleSuggestedQuery} onClick={() => this.search(query)}>
            "{query}"
          </a>
        </li>
      )
    })

    return (
      <div style={styleWrapper}>
        <Meta title="Page Not Found (404) for NOAA OneStop" />
        <section style={style404}>
          <h1 style={styleH1}>Page Not Found</h1>
          <ul>
            <li>
              Return to the <Link to="/">Home</Link> page to start over.
            </li>
            <li>
              Consult our <Link to="/help">Help</Link> page for constructing
              queries.
            </li>
          </ul>
          <h2 style={styleH2}>There is no route to this path:</h2>
          <pre>{location.pathname}</pre>
          <p>
            Did you intend send a text query? Here are some suggested queries:
          </p>
          <ul>{suggestions}</ul>
        </section>
      </div>
    )
  }
}
