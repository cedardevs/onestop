import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {cloud} from '../common/SvgIcon'

export const protocols = Immutable([
  {
    id: 'C',
    names: [ 'ogc:wcs' ],
    color: 'coral',
    label: 'OGC Web Coverage Service',
  },
  {
    id: 'cloud',
    names: [ 'cloud' ],
    color: '#327cac',
    label: 'Cloud File Access',
    svgPath: cloud,
  },
  {id: 'D', names: [ 'download' ], color: 'blue', label: 'Download'},
  {id: 'F', names: [ 'ftp' ], color: 'red', label: 'FTP'},
  {
    id: 'H',
    names: [ 'http', 'https' ],
    color: 'purple',
    label: 'HTTP/HTTPS',
  },
  {
    id: 'L',
    names: [ 'noaa:las' ],
    color: 'aqua',
    label: 'NOAA Live Access Server',
  },
  {
    id: 'M',
    names: [ 'ogc:wms' ],
    color: 'goldenrod',
    label: 'OGC Web Map Service',
  },
  {
    id: 'O',
    names: [ 'opendap', 'opendap:opendap', 'opendap:hyrax' ],
    color: 'green',
    label: 'OPeNDAP',
  },
  {
    id: 'T',
    names: [ 'thredds', 'unidata:thredds' ],
    color: 'grey',
    label: 'THREDDS',
  },
  {id: 'W', names: [ '' ], color: '#e69500', label: 'Web'},
])

export const identifyProtocol = link => {
  const name = _.toLower(link.linkProtocol || '')
  return _.find(protocols, p => p.names.includes(name))
}
