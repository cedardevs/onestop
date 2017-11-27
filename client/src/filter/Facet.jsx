import React, { Component } from 'react'
import Checkbox from '../common/input/Checkbox'
import { titleCaseKeyword } from "../utils/keywordUtils"

const styleContainer = {
  display: 'flex',
}

const styleCheckbox = {
  display: 'flex',
  alignItems: 'center',
  marginRight: '0.616em',
}

const styleTerm = {
  width: '100%',
  color: '#FFF'
}

export default class Facet extends Component {
  render() {
    const label = `${titleCaseKeyword(this.props.term)} (${this.props.count})`
    return (
        <div style={{...styleContainer, ...this.props.style}}>
          <div style={styleCheckbox}>
            <Checkbox
                label={label}
                id={`checkbox-${this.props.id}`}
                checked={this.props.selected}
                value={{term: this.props.term, category: this.props.category}}
                onChange={this.props.onChange}
            />
          </div>
          <div aria-hidden='true' style={styleTerm}>
            {label}
          </div>
        </div>
    )
  }
}
