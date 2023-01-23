import React from 'react'
import Select from 'react-select'
import {FilterColors, selectTheme} from '../../style/defaultStyles'

const cursorStyle = (styles, {isDisabled}) => {
  return {
    ...styles,
    cursor: isDisabled ? 'not-allowed' : 'pointer',
  }
}

const placeholderStyle = styles => {
  return {
    ...styles,
    color: '#767676',
  }
}

const menuStyle = styles => {
  return {
    ...styles,
    // node_modules/leaflet/dist/leaflet.css appears use a z-index as high as 1000,
    // effectively, I've been able to overlap a map using zIndex > 400
    // but there's a possibility that this wouldn't include all potential leaflet elements
    // so I've opted for using 1000+1
    zIndex: 1001,
  }
}

const selectStyles = {
  control: cursorStyle,
  option: cursorStyle,
  placeholder: placeholderStyle,
  menu: menuStyle,
}

const optionStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
}
const optionBadgeStyle = {
  backgroundColor: '#277CB2',
  borderRadius: '2em',
  color: 'white',
  display: 'inline-block',
  fontWeight: 'normal',
  lineHeight: '1',
  minWidth: 1,
  padding: '0.166em 0.618em',
  textAlign: 'center',
  marginLeft: '1em',
}

// customize replaceable option label component in select menu
// this allows us to display the granule link count for each option
const formatOptionLabel = data => {
  return (
    <div style={optionStyle}>
      <span>{data.label}</span>
      <span style={optionBadgeStyle}>{`${data.count} ${data.count > 1
        ? 'links'
        : 'link'}`}</span>
    </div>
  )
}

export default class CartSelect extends React.Component {
  render() {
    const {
      options,
      onChange,
      onMenuOpen,
      onMenuClose,
      isMenuOpen,
      style,
    } = this.props

    // the `react-select` component is flexible to have grouped
    // or ungrouped options, so we have to be careful to fallback
    // to an appropriate object -- even if this component is always grouped
    const defaultValue = options
      ? options[0] ? options[0][0] : options[0]
      : {}

    return (
      <div style={style}>
        <Select
          theme={selectTheme}
          styles={selectStyles}
          aria-labelledby="cartDownloadOptionsLabel"
          placeholder="Select file link type"
          defaultValue={defaultValue}
          // options={options} // when a single item is in cart this options are not regular obj which cause an error on the page
          options={JSON.parse(JSON.stringify(options))} // this solved the problem but it needs further investigation
          formatOptionLabel={formatOptionLabel}
          onChange={onChange}
          onMenuOpen={onMenuOpen}
          onMenuClose={onMenuClose}
        />
      </div>
    )
  }
}
