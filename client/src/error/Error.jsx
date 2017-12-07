import React from 'react'
import style from './error.css'
import _ from 'lodash'

const defaultError = {
  title: 'Sorry, something has gone wrong',
  detail:
    'Looks like something has gone wrong on our end. Please try again later.',
}

class Error extends React.Component {
  constructor(props) {
    super(props)

    this.errors = this.extractErrors(props)
    this.goBack = props.goBack.bind(this)
    this.goHome = props.goHome.bind(this)
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
    console.log('errors:', errors)
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
      <div className="pure-g">
        <div className="pure-u-md-1-4" />
        <div className={`pure-u-md-1-2 pure-u-1 ${style.messageContainer}`}>
          {this.errors.map((error, i) => {
            return (
              <div key={i}>
                <h2>{error.title}</h2>
                <p>{error.detail}</p>
              </div>
            )
          })}
          <div>
            <button
              className={`pure-button ${style.homeButton}`}
              onClick={this.goHome}
            >
              Start a New Search
            </button>
            {/* back behavior doesn't execute search again, ends up showing empty results page */}
            {/*<button className={`pure-button ${style.backButton}`} onClick={this.goBack}>Go Back</button>*/}
          </div>
        </div>
        <div className="pure-u-md-1-4" />
      </div>
    )
  }
}

export default Error
