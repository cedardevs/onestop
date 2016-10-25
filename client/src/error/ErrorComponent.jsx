import React from 'react'
import style from './error.css'
import _ from 'lodash'

const defaultError = {
  title: 'Sorry, something has gone wrong',
  detail: 'Looks like something has gone wrong on our end. Please try again later.'
}

class ErrorComponent extends React.Component {
  constructor(props) {
    super(props)

    this.errors = _.isArray(props.errors) && props.errors ||
        _.isObject(props.errors) && [props.errors] ||
        [defaultError]

    this.goBack = props.goBack.bind(this)
    this.goHome = props.goHome.bind(this)
  }

  render() {
    return <div className="pure-g">
      <div className="pure-u-md-1-4"></div>
      <div className={`pure-u-md-1-2 pure-u-1 ${style.messageContainer}`}>
        {this.errors.map( (error, i) => {
          return <div key={i}>
            <h2>{error.title}</h2>
            <p>{error.detail}</p>
          </div>
        })}
        <div>
          <button className={`pure-button ${style.homeButton}`} onClick={this.goHome}>Start a New Search</button>
          {/* back behavior doesn't execute search again, ends up showing empty results page */}
          {/*<button className={`pure-button ${style.backButton}`} onClick={this.goBack}>Go Back</button>*/}
        </div>
      </div>
      <div className="pure-u-md-1-4"></div>
    </div>
  }
}

export default ErrorComponent