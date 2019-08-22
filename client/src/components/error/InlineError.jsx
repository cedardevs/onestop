import React from 'react'
import Button from '../common/input/Button'
import {boxShadow} from '../../style/defaultStyles'
import _ from 'lodash'
import Meta from '../helmet/Meta'

const defaultError = {
  title: 'Sorry, something has gone wrong',
  detail:
    'Looks like something has gone wrong on our end. Please try again later.',
}

const styleError = {
  backgroundColor: '#E74C3C',
  margin: '1.618em',
  padding: '2em',
  boxShadow: boxShadow,
}

const styleErrorHeading = {
  margin: '0 0 0.618em 0',
  padding: 0,
}

const styleErrorDescription = {
  margin: 0,
  padding: 0,
}

class InlineError extends React.Component {
  constructor(props) {
    super(props)

    this.errors = this.extractErrors(props)
  }

  componentWillReceiveProps(nextProps) {
    this.errors = this.extractErrors(nextProps)
  }

  extractErrors(props) {
    return _.chain(this.getErrorsArray(props.errors))
      .map(this.normalizeError)
      .uniqWith((a, b) => a.title === b.title && a.detail === b.detail)
      .value()
  }

  getErrorsArray(errors) {
    if (_.isArray(errors) && errors.length > 0) {
      return errors
    }
    else if (_.isObject(errors)) {
      return [ errors ]
    }
    else {
      return [ defaultError ]
    }
  }

  normalizeError(error) {
    if (_.isError(error)) {
      return defaultError
    }
    else if (error.title) {
      return error
    }
    else if (error.message) {
      error.title = error.message
      return error
    }
    else {
      return defaultError
    }
  }

  render() {
    return (
      <div style={styleError}>
        {this.props.meta}
        {this.errors.map((error, i) => {
          return (
            <div key={i}>
              <h2 style={styleErrorHeading}>{error.title}</h2>
              <p style={styleErrorDescription}>{error.detail}</p>
            </div>
          )
        })}
        {this.props.action}
      </div>
    )
  }
}

export default InlineError
