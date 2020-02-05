import React from 'react'

import _ from 'lodash'
import './LoadingBar.css'
import InlineError from '../error/InlineError'
import {Route, Switch} from 'react-router'

export class LoadingBar extends React.Component {
  // TODO this isn't really used as a loading bar at all anymore - rename or get rid of?

  render() {
    const {style, error} = this.props
    const displayErrors = !_.isEmpty(error) ? (
      <InlineError errors={this.props.errors} />
    ) : null

    return (
      <Switch>
        <Route path="/" exact />
        <Route path="/">
          <div style={style}>{displayErrors}</div>
        </Route>
      </Switch>
    )
  }
}

export default LoadingBar
