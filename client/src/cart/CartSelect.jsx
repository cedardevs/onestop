import React from 'react'
import Select from 'react-select'
import {
  COLOR_GREEN,
  COLOR_GREEN_LIGHT,
  FilterColors,
} from '../common/defaultStyles'

const cursorStyle = (styles, {isDisabled, isFocused, isSelected}) => {
  return {
    ...styles,
    cursor: isDisabled
      ? 'not-allowed'
      : isFocused || isSelected ? 'pointer' : 'default',
  }
}

const selectStyles = {
  control: cursorStyle,
  option: cursorStyle,
}

const optionStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
}
const optionBadgeStyle = {
  backgroundColor: COLOR_GREEN,
  borderRadius: '2em',
  color: '#F9F9F9',
  display: 'inline-block',
  fontSize: 12,
  fontWeight: 'normal',
  lineHeight: '1',
  minWidth: 1,
  padding: '0.166em 0.618em',
  textAlign: 'center',
}

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
    const {options, onChange, style} = this.props

    const defaultValue = options
      ? options[0] ? options[0][0] : options[0]
      : {}

    return (
      <div style={style}>
        <Select
          styles={selectStyles}
          placeholder={`Select download protocol and source...`}
          defaultValue={defaultValue}
          options={options}
          formatOptionLabel={formatOptionLabel}
          onChange={onChange}
          theme={theme => ({
            ...theme,
            borderRadius: '0.309em',
            colors: {
              ...theme.colors,
              primary: FilterColors.DARKEST,
              primary75: FilterColors.DARK,
              primary50: FilterColors.MEDIUM,
              primary25: FilterColors.LIGHT,
              danger: COLOR_GREEN,
              dangerLight: COLOR_GREEN_LIGHT,
            },
          })}
        />
      </div>
    )
  }
}
