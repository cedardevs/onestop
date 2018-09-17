package org.cedar.psi.common.constants

class Topics {

  static int DEFAULT_NUM_PARTITIONS = 1
  static short DEFAULT_REPLICATION_FACTOR = 1
  static final String RAW_GRANULE_TOPIC = 'raw-granule-events'
  static final String RAW_COLLECTION_TOPIC = 'raw-collection-events'
  static final String PARSED_GRANULE_TOPIC = 'parsed-granules'
  static final String PARSED_COLLECTION_TOPIC = 'parsed-collections'
  static final String COMBINED_GRANULE_TOPIC = 'combined-granules'
  static final String COMBINED_COLLECTION_TOPIC = 'combined-collections'
  static final String SME_GRANULE_TOPIC = 'sme-granules'
  static final String UNPARSED_GRANULE_TOPIC = 'unparsed-granules'

  static final String RAW_GRANULE_STORE = 'raw-granules'
  static final String RAW_COLLECTION_STORE = 'raw-collections'
  static final String PARSED_GRANULE_STORE = 'parsed-granules'
  static final String PARSED_COLLECTION_STORE = 'parsed-collections'

  static final String GRANULE_PUBLISH_TIMES = 'granule-publish-times'
  static final String GRANULE_PUBLISH_KEYS = 'granule-publish-keys'
  static final String COLLECTION_PUBLISH_TIMES = 'collection-publish-times'
  static final String COLLECTION_PUBLISH_KEYS = 'collection-publish-keys'
  static final String ERROR_HANDLER_TOPIC = 'error-events'
  static final String ERROR_HANDLER_STORE = 'error-store'

}
