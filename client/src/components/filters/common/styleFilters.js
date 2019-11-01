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

export const styleRelationIllustration = {
  included: {
    backgroundColor: '#86D29A',
    backgroundColorHover: '#6fbb83',
    borderColor: '#56B770',
    color: 'inherit',
  },
  excluded: {
    backgroundColor: '#4E5F53',
    backgroundColorHover: '#415547',
    borderColor: '#414642',
    color: '#FFF',
  },
  query: {
    backgroundColor: '#277cb2',
    backgroundColorHover: '#1d6796',
    borderColor: '#285489',
    color: '#FFF',
  },
  general: {
    // TODO I don't know what else to name this...
    display: 'block',
    borderRadius: '.2em',
    overflow: 'visible',
    boxShadow: '2px 2px 5px 2px #2c2c2c59',
  },
}
