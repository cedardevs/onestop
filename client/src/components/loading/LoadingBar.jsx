import React from 'react'

import _ from 'lodash'
import './LoadingBar.css'
import InlineError from '../error/InlineError'
import defaultStyles from '../../style/defaultStyles'
import {Route, Switch} from 'react-router'
import {LiveAnnouncer, LiveMessage} from 'react-aria-live'

export class LoadingBar extends React.Component {
  constructor(props) {
    super(props)

    this.state = {
      loadingText: '',
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState(prevState => {
      let newText = !_.isEqual(this.props.loadingText, nextProps.loadingText)
        ? nextProps.loadingText
        : ''
      return {
        ...prevState,
        loadingText: !_.isEqual(this.props.loadingText, nextProps.loadingText)
          ? nextProps.loadingText
          : '',
      }
    })
  }

  // TODO note no longer using this.props.loadingAlertId. Nuke that?
  render() {
    const {loading, loadingText, style, error} = this.props
    const displayErrors = !_.isEmpty(error) ? (
      <InlineError errors={this.props.errors} />
    ) : null

    return (
      <Switch>
        <Route path="/" exact />
        <Route path="/">
          <div style={style}>
            <LiveAnnouncer>
              <LiveMessage
                message={this.state.loadingText}
                aria-live="polite"
                style={defaultStyles.hideOffscreen}
              />
            </LiveAnnouncer>
            <div className={loading ? 'loadingContainer' : null} />
            {displayErrors}
          </div>
        </Route>
      </Switch>
    )
  }
}

export default LoadingBar
