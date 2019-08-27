import React from 'react'
import {fontFamilySerif} from '../../utils/styleUtils'

const styleContainer = {
  display: 'flex',
}

const styleIcon = {
  display: 'flex',
  alignItems: 'center',
  marginRight: '0.618em',
  width: '2em',
  height: '2em',
}

const styleIconImage = {
  width: '100%',
  height: '100%',
}

const styleText = {
  width: '100%',
  alignSelf: 'center',
  margin: '0',
  fontFamily: fontFamilySerif(),
  fontSize: '1em',
  fontWeight: 'normal',
}

export default class FilterHeading extends React.Component {
  render() {
    return (
      <div style={{...styleContainer, ...this.props.style}}>
        <div style={styleIcon} aria-hidden="true">
          <img
            width="2em"
            height="2em"
            style={styleIconImage}
            src={this.props.icon}
            alt={`${this.props.text} Icon`}
          />
        </div>
        <h3 style={styleText}>{this.props.text}</h3>
      </div>
    )
  }
}
