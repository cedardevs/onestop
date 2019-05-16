import React from 'react'
import './LoadingBar.css'

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
    const {loading, loadingText, style} = this.props
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
          </div>
        </Route>
      </Switch>
    )
  }
}

export default LoadingBar
