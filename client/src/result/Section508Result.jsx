import React from 'react'
import { styleResult, styleResult508 } from './ResultStyles'

class ResultLayout extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {

    const styleResult508Merged = {
      ...styleResult,
      ...styleResult508
    }

    return (<div style={styleResult508Merged}>
      {this.props.children}
    </div>)
  }
}

export default ResultLayout
