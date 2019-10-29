import React, {useState} from 'react'

import _ from 'lodash'

import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'

const HEIGHT_RATIO = 9
const WIDTH_RATIO = 14

const BOXES = [
  {
    // global
    label: 'global',
    global: true,
    top: 0,
    height: HEIGHT_RATIO, // 100%
    left: 0,
    width: WIDTH_RATIO, // 100%
    description: 'global result',
    // description: (relation, excludeGlobal) => {
    //   let desc = 'global result'
    // },
    relation: {
      contains: true,
      within: false,
      intersects: true,
      disjoint: false,
    },
  },
  {
    // contains
    label: 'ex 1',
    top: 0,
    height: 9,
    left: 0,
    width: 10, // TODO or 11?
    description:
      'result is larger than query, with complete overlap (result is a superset)',
    relation: {
      contains: true,
      within: false,
      intersects: true,
      disjoint: false,
    },
  },
  {
    // query included here to get the layer order correct
    label: 'filter',
    query: true,
    top: 1,
    height: 7,
    left: 1,
    width: 5,
    description: 'user defined location filter',
    relation: {
      contains: true,
      within: false,
      intersects: true,
      disjoint: false,
    }, // TODO nonsense
  },
  {
    // within
    label: 'ex 2',
    top: 2,
    height: 2,
    left: 2,
    width: 2,
    description:
      'result is smaller than query, with complete overlap (result is a subset)',
    relation: {
      contains: false,
      within: true,
      intersects: true,
      disjoint: false,
    },
  },
  {
    // disjoint
    label: 'ex 3',
    top: 1,
    height: 2,
    left: 7,
    width: 2,
    description: 'result is outside query, with no overlap',
    relation: {
      contains: false,
      within: false,
      intersects: false,
      disjoint: true,
    },
  },
  {
    // intersects
    label: 'ex 4',
    top: 4,
    height: 2,
    left: 4,
    width: 2,
    description: 'result partially overlaps query',
    relation: {
      contains: false,
      within: false,
      intersects: true,
      disjoint: false,
    },
  },
]

const COLORS = {
  // TODO move to common?
  included: {backgroundColor: '#86D29A', borderColor: '#56B770'},
  excluded: {backgroundColor: '#4E5F53', borderColor: '#414642'},
  query: {backgroundColor: '#277cb2', borderColor: '#28323E'},
}

const styleBox = {
  cursor: 'pointer',
  display: 'block',
  position: 'absolute',
  borderRadius: '.2em',
  borderStyle: 'solid',
  borderWidth: '1px',
  overflow: 'visible',
  boxShadow: '2px 2px 5px 2px #2c2c2c59',
}

const stylePosition = ({left, width, top, height}) => {
  return {
    left: leftEdge(left),
    width: calculateWidth(width),
    height: calculateHeight(height),
    top: topEdge(top),
  }
}

const colorRelation = (isMatched, isBorder) => {
  if (isMatched) {
    return isBorder
      ? COLORS.included.borderColor
      : COLORS.included.backgroundColor
  }
  else {
    return isBorder
      ? COLORS.excluded.borderColor
      : COLORS.excluded.backgroundColor
  }
}

const leftEdge = offset => {
  return `${10 * ((offset == null ? 0 : offset) + 0)}%`
}
const calculateWidth = width => {
  return `${100 * (width / WIDTH_RATIO)}%`
}
const topEdge = offset => {
  return `${10 * ((offset == null ? 0 : offset) + 0)}%`
}
const calculateHeight = height => {
  return `${100 * (height / HEIGHT_RATIO)}%`
}

const TimeLineResult = ({
  // TODO rename
  id,
  box,
  relation,
  excludeGlobal,
}) => {
  let includedBasedOnRelationship =
    excludeGlobal && box.global
      ? false // always exclude the global result when excludeGlobal filter is active
      : box.relation[relation] // otherwise base it on the relation

  let description = ''
  if (box.query) {
    description = box.description
  }
  else {
    if (excludeGlobal && box.global && box.relation[relation]) {
      description = `NOT included in search results: ${box.description}, due to 'Exclude Global Results' filter`
    }
    else {
      description = `${includedBasedOnRelationship
        ? 'Included in search results:'
        : 'NOT included in search results:'} ${box.description}`
    }
  }
  const styleColors = {
    borderColor: box.query
      ? COLORS.query.borderColor
      : colorRelation(includedBasedOnRelationship, true),
    backgroundColor: box.query
      ? COLORS.query.backgroundColor
      : colorRelation(includedBasedOnRelationship),
  }

  return (
    <output
      id={id}
      title={description}
      style={consolidateStyles(styleBox, stylePosition(box), styleColors)}
    >
      <label
        style={{
          color: includedBasedOnRelationship && !box.query ? 'inherit' : '#FFF', // TODO more color style logic
          position: 'absolute',
          right: '0.25em',
          bottom: 0,
        }}
      >
        {box.label}
      </label>
      <div style={defaultStyles.hideOffscreen}>{description}</div>
    </output>
  )
}

const GeoRelationIllustration = ({relation, excludeGlobal}) => {
  const outputs = _.map(BOXES, (box, index) => {
    return (
      <TimeLineResult
        key={`illustration${index + 1}`}
        id={`illustration${index + 1}`}
        box={box}
        relation={relation}
        excludeGlobal={excludeGlobal}
      />
    )
  })

  return (
    <div
      style={{
        width: '100%',
        height: '7em', // TODO picked an arbitrary total height for now...
        position: 'relative',
        marginTop: '.609em',
      }}
    >
      {outputs}
    </div>
  )
}
export default GeoRelationIllustration
