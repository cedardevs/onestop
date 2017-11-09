import _ from 'lodash'

const buildHierarchyMap = (category, terms) => {
  const createChildrenHierarchy = (map, hierarchy, term, value) => {
    const lastTerm = hierarchy.pop()
    if (!_.isEmpty(hierarchy)) {
      let i
      for (i = 0; i < hierarchy.length; i++) {
        // Since hierarchical strings are received in alphabetical order, this traversal
        // down the nested object won't error out
        //_.defaults(map, map[hierarchy[i]].children)
        map = map[hierarchy[i]].children = map[hierarchy[i]].children || {}
      }
    }

    map = map[lastTerm] = value
    return map
  }

  let categoryMap = {}

  Object.keys(terms).map(term => {
    let hierarchy = term.split(' > ')
    const value = {
      count: terms[term].count,
      children: {},
      category: category,
      term: term
    }

    createChildrenHierarchy(categoryMap, hierarchy, term, value)
  })

  return categoryMap
}

export const buildKeywordHierarchyMap = facetMap => {
  const hierarchyMap = {}
  _.map(facetMap, (terms, category) => {
    if (!_.isEmpty(terms)) { // Don't load categories that have no results
      let heading
      let categoryMap = {}

      if (category === 'science') {
        heading = 'Data Theme'
        categoryMap = buildHierarchyMap(category, terms)
      }
      else {
        heading = _.startCase(_.toLower((category.split(/(?=[A-Z])/).join(" "))))
        Object.keys(terms).map(term => {
          const name = term.split(' > ')
          categoryMap[name[0]] = {
            count: terms[term].count,
            children: {},
            category: category,
            term: term
          }
        })
      }

      hierarchyMap[heading] = categoryMap
    }
  })

  return hierarchyMap
}

// pulls out the last term in a GCMD-style keyword and attempts to
export const titleCaseKeyword = term => {
  if (!term) { return null }
  const trimmed = term.split('>').pop().trim()
  return (trimmed === trimmed.toUpperCase()) ? _.startCase(trimmed.toLowerCase()) : trimmed
}
