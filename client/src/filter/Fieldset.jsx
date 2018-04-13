import React from 'react'
import {boxShadow} from '../common/defaultStyles'
import {FilterTheme} from '../common/defaultStyles'

const styleFieldset = {
  alignSelf: 'center',
  backgroundColor: FilterTheme.LIGHT,
  border: 'none',
  boxShadow: boxShadow,
  marginBottom: '1em',
  borderRadius: '0.309em',
}

const magic = {
  //     position: 'absolute',
  // bottom: '-5px',
  // left: '-6px',
  // height: '0.1em',
  // width: '105%',
  // backgroundColor: ``${FilterTheme.LIGHT}`,
  // borderLeft: `6px solid ${FilterTheme.LIGHT}`,
  // borderRight: `6px solid ${FilterTheme.LIGHT}`,
}
const magic2 = {
  position: 'absolute',
  bottom: '0',
  left: '-6px',
  height: '.9em',
  width: '100%',
  borderLeft: `6px solid ${FilterTheme.LIGHT}`,
  borderRight: `6px solid ${FilterTheme.LIGHT}`,
}

const styleLegend = {
  backgroundColor: `${FilterTheme.LIGHT}`,
  margin: '0 auto',
  width: 'auto',
  background: `linear-gradient(${FilterTheme.LIGHT_EMPHASIS} 0%, ${FilterTheme.LIGHT} 50%)`,
  padding: '.309em .619em',
  position: 'relative',

  border: 'none',
  boxShadow: 'rgba(50, 50, 50, 0.75) 0px 1px 3px',

  color: 'inherit',
  borderRadius: '0.309em',
}

export default class Fieldset extends React.Component {
  render() {
    return (
      <fieldset style={styleFieldset} onChange={this.props.onFieldsetChange}>
        <legend style={styleLegend}>
          <div style={magic} /> <div style={magic2} />
          {this.props.legendText}
        </legend>
        {this.props.children}
      </fieldset>
    )
  }
}
