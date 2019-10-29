import _ from 'lodash'

export const consolidateStyles = (...args) => {
  return _.reduce(
    args,
    (result, arg) => {
      if (arg == null) return result
      return {...result, ...arg}
    },
    {}
  )
}

export const fontFamilySerif = () => {
  return "'Merriweather', 'Cambria', 'Times New Roman', 'Times', serif"
}

export const fontFamilySansSerif = () => {
  return "'Source Sans Pro', 'Helvetica Neue', 'Helvetica', 'Roboto', 'Arial', sans-serif"
}

export const fontFamilyMonospace = () => {
  return "'Courier New', Courier, monospace"
}

export const cleanParamsRGB = str => {
  // $1 = everything up to and including opening parenthesis
  // $2 = everything between the parentheses
  // $3 = everything including and after the last parenthesis
  let parenthesesMatch = /^(.*\()([^)]+)(\).*)$/
  let matches = parenthesesMatch.exec(str)
  if (matches && matches.length > 1) {
    // collect params within parenthesis, delimited by comma, and trimmed
    let params = matches[2].split(',').map(x => x.trim())
    // the 4th "alpha" param exists but will be interpreted as `1`,
    // so the CSS style will be stored as 'rgb' without the 'a'
    if (params.length === 4 && Number(params[3]) === 1) {
      params.pop() // remove alpha component
      str = str.replace('rgba', 'rgb')
    }
    // CSS colors can use 'rgba' without specifying the 4th "alpha" param,
    // but the CSS style will be stored as 'rgb'
    if (params.length === 3) {
      str = str.replace('rgba', 'rgb')
    }
    // to be properly compared, CSS rgb color params are separated by single spaces
    let paramsClean = params.join(', ')
    // take the newly cleaned params and replace what used to be between parentheses
    let cleanStrColor = str.replace(parenthesesMatch, '$1' + paramsClean + '$3')
    return cleanStrColor
  }
  // if no parentheses were matched, it's not valid rgb(a), return what was given
  return str
}

export const isColor = strColor => {
  if (!strColor) {
    return false
  }
  let strColorCleaned = cleanParamsRGB(strColor.trim().toLowerCase())
  let s = new Option().style
  s.color = strColorCleaned
  // match non-hex colors
  let test1 = s.color === strColorCleaned
  // match both 3 & 6-digit hex colors
  let test2 = /^#([0-9A-F]{3}|[0-9A-F]{6})$/i.test(strColorCleaned)
  return test1 || test2
}
