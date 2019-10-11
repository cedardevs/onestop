import React from 'react'

import _ from 'lodash'
import './LoadingBar.css'
import InlineError from '../error/InlineError'
import defaultStyles from '../../style/defaultStyles'
import {Route, Switch} from 'react-router'

export class LoadingBar extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loadingText: '',
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      return {
        ...prevState,
        loadingText: !_.isEqual(this.props.loadingText, nextProps.loadingText)
          ? nextProps.loadingText
          : '',
      }
    })
  }

  render() {
    const {loading, loadingText, style, error} = this.props
    const displayErrors = !_.isEmpty(error) ? (
      <InlineError errors={this.props.errors} />
    ) : null

    // TODO should this be aria-atomic true?

    return (
      <Switch>
        <Route path="/" exact />
        <Route path="/">
          <div style={style}>
            <div
              aria-live="polite"
              aria-atomic="false"
              style={defaultStyles.hideOffscreen}
            >
              <div id={this.props.loadingAlertId}>{this.state.loadingText}</div>
            </div>
            <div className={loading ? 'loadingContainer' : null} />
            {displayErrors}
          </div>
        </Route>
      </Switch>
    )
  }
}

export default LoadingBar
