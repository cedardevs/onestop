package org.cedar.psi.manager.config

class Constants {

  // Application Info
  static final String APP_ID = 'stream-manager'
  static final String BOOTSTRAP_DEFAULT = 'localhost:9092'

  // granule Topics
  static final String RAW_GRANULES_TOPIC = 'metadata-aggregator-raw-granules-changelog'
  static final String UNPARSED_TOPIC = 'unparsed-granules'
  static final String PARSED_TOPIC = 'parsed-granules'
  static final String SME_TOPIC = 'sme-granules'

  // collection Topics
  static final String RAW_COLLECTIONS_TOPIC = 'metadata-aggregator-raw-collections-changelog'
  static final String PARSED_COLLECTIONS_TOPIC = 'parsed-collections'

  // common Topics
  static final String ERROR_TOPIC = 'error-events'

  // SME Splitting Info
  static final String SPLIT_FIELD = 'source'
  static final List<String> SPLIT_VALUES = new ArrayList<>([
      'common-ingest'
  ])
}
