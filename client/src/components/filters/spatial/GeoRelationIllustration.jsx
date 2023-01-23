import React, {useState, useEffect} from 'react'

import _ from 'lodash'

import {LiveAnnouncer, LiveMessage} from 'react-aria-live'
import defaultStyles from '../../../style/defaultStyles'
import FlexColumn from '../../common/ui/FlexColumn'
import FlexRow from '../../common/ui/FlexRow'
import {consolidateStyles} from '../../../utils/styleUtils'
import {styleRelationIllustration} from '../common/styleFilters'

const HEIGHT_RATIO = 9
const WIDTH_RATIO = 14

const resultStyles = {
  color: include =>
    include
      ? styleRelationIllustration.included.color
      : styleRelationIllustration.excluded.color,
  backgroundColor: (include, hovering) => {
    if (hovering) {
      return include
        ? styleRelationIllustration.included.backgroundColorHover
        : styleRelationIllustration.excluded.backgroundColorHover
    }
    return include
      ? styleRelationIllustration.included.backgroundColor
      : styleRelationIllustration.excluded.backgroundColor
  },
  borderColor: include =>
    include
      ? styleRelationIllustration.included.borderColor
      : styleRelationIllustration.excluded.borderColor,
}

const resultDescription = (include, description) => {
  return `${include ? 'Included:' : 'Excluded:'} ${description}`
}

const resultLongDescription = (include, name, description, relation) => {
  return `${include
    ? 'Included:'
    : 'Excluded:'} ${name} ${description} for ${relation} filter`
}

const BOXES = [
  {
    // global
    label: 'global',
    global: true,
    top: 0,
    height: HEIGHT_RATIO, // 100%
    left: 0,
    width: WIDTH_RATIO, // 100%
    description: (include, excludeGlobal) =>
      excludeGlobal
        ? `Excluded: global result, due to 'Exclude Global Results' filter`
        : resultDescription(include, 'global result'),
    longDescription: (include, relation, excludeGlobal) =>
      excludeGlobal
        ? `Excluded: global result, due to 'Exclude Global Results' filter`
        : resultLongDescription(include, 'global result', '', relation),
    styles: {
      color: (include, excludeGlobal) =>
        excludeGlobal ? resultStyles.color(false) : resultStyles.color(include),
      backgroundColor: (include, hovering, excludeGlobal) =>
        excludeGlobal
          ? resultStyles.backgroundColor(false, hovering)
          : resultStyles.backgroundColor(include, hovering),
      borderColor: (include, excludeGlobal) =>
        excludeGlobal
          ? resultStyles.borderColor(false)
          : resultStyles.borderColor(include),
    },
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
    width: 11,
    description: include =>
      resultDescription(
        include,
        'result is larger than query, with complete overlap (result is a superset)'
      ),
    longDescription: (include, relation) =>
      resultLongDescription(
        include,
        'example 1',
        'result is larger than query, with complete overlap (result is a superset)',
        relation
      ),
    styles: resultStyles,
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
    description: () => 'user defined location filter',
    longDescription: () => 'user defined location filter',
    styles: {
      color: () => styleRelationIllustration.query.color,
      backgroundColor: (include, hovering) =>
        hovering
          ? styleRelationIllustration.query.backgroundColorHover
          : styleRelationIllustration.query.backgroundColor,
      borderColor: () => styleRelationIllustration.query.borderColor,
    },
    relation: {},
  },
  {
    // within
    label: 'ex 2',
    top: 2,
    height: 2,
    left: 2,
    width: 2,
    description: include =>
      resultDescription(
        include,
        'result is smaller than query, with complete overlap (result is a subset)'
      ),
    longDescription: (include, relation) =>
      resultLongDescription(
        include,
        'example 2',
        'result is smaller than query, with complete overlap (result is a subset)',
        relation
      ),
    styles: resultStyles,
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
    description: include =>
      resultDescription(include, 'result is outside query, with no overlap'),
    longDescription: (include, relation) =>
      resultLongDescription(
        include,
        'example 3',
        'result is outside query, with no overlap',
        relation
      ),
    styles: resultStyles,
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
    description: include =>
      resultDescription(include, 'result partially overlaps query'),
    longDescription: (include, relation) =>
      resultLongDescription(
        include,
        'example 4',
        'result partially overlaps query',
        relation
      ),
    styles: resultStyles,
    relation: {
      contains: false,
      within: false,
      intersects: true,
      disjoint: false,
    },
  },
]

const styleBox = consolidateStyles(styleRelationIllustration.general, {
  position: 'absolute',
  borderStyle: 'solid',
  borderWidth: '1px',
})

const stylePosition = ({left, width, top, height}) => {
  return {
    left: leftEdge(left),
    width: calculateWidth(width),
    height: calculateHeight(height),
    top: topEdge(top),
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

const BoxIllustration = ({id, box, relation, excludeGlobal}) => {
  let includedBasedOnRelationship = box.relation[relation]

  let description = box.description(includedBasedOnRelationship, excludeGlobal)
  let longDescription = box.longDescription(
    includedBasedOnRelationship,
    relation,
    excludeGlobal
  )

  const [ hovering, setHovering ] = useState(false)

  const styleColors = {
    borderColor: box.styles.borderColor(
      includedBasedOnRelationship,
      excludeGlobal
    ),
    backgroundColor: box.styles.backgroundColor(
      includedBasedOnRelationship,
      hovering,
      excludeGlobal
    ),
  }

  const styleLabel = {
    color: box.styles.color(includedBasedOnRelationship, excludeGlobal),
    position: 'absolute',
    right: '0.25em',
    bottom: 0,
  }

  return (
    <output
      id={id}
      onMouseOver={() => setHovering(true)}
      onMouseOut={() => setHovering(false)}
      title={description}
      style={consolidateStyles(styleBox, stylePosition(box), styleColors)}
    >
      <label style={styleLabel}>{box.label}</label>
      <div style={defaultStyles.hideOffscreen}>{longDescription}</div>
    </output>
  )
}

const GeoRelationIllustration = ({relation, excludeGlobal}) => {
  const [ notification, setNotification ] = useState('')
  useEffect(
    () => {
      setNotification(
        `Examples updated for ${relation} location filter${excludeGlobal
          ? ' with exclude global filter'
          : ''}`
      )
    },
    [ relation, excludeGlobal ]
  )

  const outputs = _.map(BOXES, (box, index) => {
    return (
      <BoxIllustration
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
        height: '7em',
        position: 'relative',
        marginTop: '.609em',
      }}
    >
      <LiveAnnouncer>
        <LiveMessage message={notification} aria-live="polite" />
      </LiveAnnouncer>
      {outputs}
    </div>
  )
}
export default GeoRelationIllustration
