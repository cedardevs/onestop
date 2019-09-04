import React from 'react'
import {boxShadow} from '../../style/defaultStyles'
import {FilterColors, FilterStyles} from '../../style/defaultStyles'

const styleFieldset = {
  ...FilterStyles.LIGHT,
  ...{
    alignSelf: 'center',
    border: 'none',
    boxShadow: boxShadow,
    marginBottom: '0.618em',
    borderRadius: '0.309em',
    padding: '0.618em',
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

export default class FilterFieldset extends React.Component {
  render() {
    const fieldsetLegend = (
      <legend style={styleLegend}>
        <div style={mask} />
        {this.props.legendText}
      </legend>
    )

    return (
      <fieldset style={styleFieldset}>
        {this.props.legendText ? fieldsetLegend : null}
        {this.props.children}
      </fieldset>
    )
  }
}
