import React from 'react'
import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {SvgIcon, cloud, video_camera} from '../components/common/SvgIcon'
import {fontFamilySansSerif} from './styleUtils'

export const countArray = arr => {
  return (arr && arr.length) || 0
}

export const countKeys = map => {
  return (map && Object.keys(map).length) || 0
}

export const mergeGranulesArrayIntoGranulesMap = (
  granulesArray,
  granulesMap
) => {
  // api search returns data from the payload as an array,
  // but in redux we store it in a map, keyed by id
  let granules = granulesArray ? granulesArray : []
  if (granules.length === 0) {
    return granulesMap
  }
  return granules.reduce(
    (existing, next) => existing.set(next.id, next.attributes),
    granulesMap
  )
}

export const styleProtocolListItem = {
  display: 'inline-flex',
  marginRight: '0.309em',
  alignSelf: 'center',
}

export const styleProtocolList = {
  justifyContent: 'flex-start',
  display: 'flex',
  flexFlow: 'row wrap',
  listStyle: 'none',
  margin: 0,
  padding: 0,
}

export const styleBadge = protocol => {
  return {
    borderRadius: '50%',
    width: '1em',
    height: '1em',
    lineHeight: '1em',
    padding: '0.25em',
    margin: '0.25em',
    fontFamily: fontFamilySansSerif(),
    color: 'white',
    fill: 'white',
    textAlign: 'center',
    alignSelf: 'center',
    textDecoration: 'none',
    background: `${protocol.color}`,
  }
}

export const protocols = Immutable([
  {
    id: 'C',
    names: [ 'ogc:wcs' ],
    color: '#ab4e2c',
    label: 'OGC Web Coverage Service',
  },
  {
    id: 'cloud',
    names: [
      'cloud',
      'aws:s3',
      'google:gcs',
      'google:gcp:gcs',
      'google:gcp:bigquery',
      'amazon:aws:s3',
    ],
    color: '#327cac',
    label: 'Cloud File Access',
    svgPath: cloud,
  },
  {id: 'D', names: [ 'download' ], color: 'blue', label: 'Download'},
  {id: 'F', names: [ 'ftp' ], color: '#c50000', label: 'FTP'},
  {
    id: 'H',
    names: [ 'http', 'https' ],
    color: 'purple',
    label: 'HTTP/HTTPS',
  },
  {
    id: 'L',
    names: [ 'noaa:las' ],
    color: '#008484',
    label: 'NOAA Live Access Server',
  },
  {
    id: 'M',
    names: [ 'ogc:wms' ],
    color: '#92631c',
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
    color: '#616161',
    label: 'THREDDS',
  },
  {id: 'W', names: [ '' ], color: '#a26a03', label: 'Web'},
  {
    id: 'Y',
    names: [ 'video:youtube', 'video:mp4' ],
    color: '#4f635c',
    label: 'Video',
    svgPath: video_camera,
  },
])

export const identifyProtocol = link => {
  const name = _.toLower(link.linkProtocol || '')
  const protocol = _.find(protocols, p => p.names.includes(name))
  if (protocol) {
    return protocol
  }
  return {id: '?', names: [], color: 'black', label: 'Unknown'}
}

export const renderBadgeIcon = protocol => {
  if (protocol.svgPath) {
    return (
      <SvgIcon
        wrapperStyle={{paddingBottom: '.23em'}}
        path={protocol.svgPath}
      />
    )
  }
  return <span>{protocol.id}</span>
}

export const buildCoordinatesString = geometry => {
  // For point, want: "Point at [0], [1] (longitude, latitude)"
  // For line, want: "Line from [0][0] (WS), [0][1] to [1][0], [1][1] (EN).
  // For polygon want: "Bounding box covering [0][0], [0][1], [2][0], [2][1] (N, W, S, E)"
  if (geometry) {
    const deg = '\u00B0'
    if (geometry.type.toLowerCase() === 'point') {
      return `Point at ${geometry.coordinates[0]}${deg}, ${geometry
        .coordinates[1]}${deg} (longitude, latitude).`
    }
    else if (geometry.type.toLowerCase() === 'linestring') {
      return `Line from ${geometry.coordinates[0][0]}${deg}, ${geometry
        .coordinates[0][1]}${deg} (WS) to ${geometry
        .coordinates[1][0]}${deg}, ${geometry.coordinates[1][1]}${deg} (EN).`
    }
    else {
      return `Bounding box covering ${geometry
        .coordinates[0][0][0]}${deg}, ${geometry
        .coordinates[0][0][1]}${deg}, ${geometry
        .coordinates[0][2][0]}${deg}, ${geometry
        .coordinates[0][2][1]}${deg} (W, N, E, S).`
    }
  }
  else {
    return 'No spatial bounding provided.'
  }
}

export const buildTimePeriodString = (
  beginDate,
  beginYear,
  endDate,
  endYear
) => {
  const start = beginDate
    ? beginDate.split('T')[0]
    : beginYear ? beginYear : undefined
  const end = endDate ? endDate.split('T')[0] : endYear ? endYear : 'Present'

  return start && end ? `${start} to ${end}` : 'Not Provided'
}
