import {FilterStyles, FilterColors} from '../../../style/defaultStyles'

export const styleFilterPanel = {
  ...FilterStyles.MEDIUM,
  ...{
    padding: '0.618em',
    position: 'relative',
  },
}

export const styleFieldsetBorder = {
  borderWidth: '2px',
  borderStyle: 'groove',
  borderColor: FilterColors.LIGHT_SHADOW,
  padding: '0.618em',
}

export const styleForm = {
  display: 'flex',
  flexDirection: 'column',
}
