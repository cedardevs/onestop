import React from 'react'
import {boxShadow} from '../common/defaultStyles'
import {FilterColors, FilterStyles} from '../common/defaultStyles'

const styleFieldset = {
  ...FilterStyles.LIGHT,
  ...{
    alignSelf: 'center',
    border: 'none',
    boxShadow: boxShadow,
    marginBottom: '1em',
    borderRadius: '0.309em',
  },
}

const mask = {
  // masks part of the boxShadow / border to make the legend blend nicely with the rest of the fieldset
  position: 'absolute',
  bottom: '0',
  left: '-6px',
  height: '.9em',
  width: '100%',
  borderLeft: `6px solid ${FilterColors.LIGHT}`,
  borderRight: `6px solid ${FilterColors.LIGHT}`,
}

const styleLegend = {
  ...FilterStyles.LIGHT,
  ...{
    margin: '0 auto',
    width: 'auto',
    background: `linear-gradient(${FilterColors.LIGHT_EMPHASIS} 0%, ${FilterColors.LIGHT} 50%)`,
    padding: '.309em .619em',
    position: 'relative',

    border: 'none',
    boxShadow: 'rgba(50, 50, 50, 0.75) 0px 1px 3px',

    color: 'inherit',
    borderRadius: '0.309em',
  },
}

export default class Fieldset extends React.Component {
  render() {
    return (
      <fieldset style={styleFieldset} onChange={this.props.onFieldsetChange}>
        <legend style={styleLegend}>
          <div style={mask} />
          {this.props.legendText}
        </legend>
        {this.props.children}
      </fieldset>
    )
  }
}
