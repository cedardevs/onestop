import React from 'react'
import Select from 'react-select'
import {
  COLOR_GREEN,
  COLOR_GREEN_LIGHT,
  FilterColors,
} from '../common/defaultStyles'

const selectTheme = theme => {
  return {
    ...theme,
    borderRadius: '0.309em',
    colors: {
      ...theme.colors,
      primary: FilterColors.DARKEST,
      primary75: FilterColors.DARK,
      primary50: FilterColors.MEDIUM,
      primary25: FilterColors.LIGHT,
      danger: '#2c833e',
      dangerLight: COLOR_GREEN_LIGHT,
    },
  }
}

const cursorStyle = (styles, {isDisabled, isFocused, isSelected}) => {
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

const selectStyles = {
  control: cursorStyle,
  option: cursorStyle,
  placeholder: placeholderStyle,
}

const optionStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
}
const optionBadgeStyle = {
  backgroundColor: '#2c833e',
  borderRadius: '2em',
  color: '#F9F9F9',
  display: 'inline-block',
  fontSize: 12,
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
          aria-expanded={isMenuOpen}
          theme={selectTheme}
          styles={selectStyles}
          aria-labelledby={`cartDownloadOptionsLabel`}
          placeholder={`Select download protocol and source...`}
          defaultValue={defaultValue}
          options={options}
          formatOptionLabel={formatOptionLabel}
          onChange={onChange}
          onMenuOpen={onMenuOpen}
          onMenuClose={onMenuClose}
        />
      </div>
    )
  }
}
