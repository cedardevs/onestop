package org.cedar.psi.manager.config

class Constants {

  // Application Info
  static final String APP_ID = 'stream-manager'
  static final String BOOTSTRAP_DEFAULT = 'localhost:9092'

  // Application Topics
  static final String RAW_TOPIC = 'raw_granule'
  static final String UNPARSED_TOPIC = 'unparsed-granule'
  static final String PARSED_TOPIC = 'parsed-granule'
  static final String SME_TOPIC = 'sme-granule'
  static final String ERROR_TOPIC = 'error-granule'

  // SME Splitting Info
  static final String SPLIT_FIELD = 'dataStream'
  static final List<String> SPLIT_VALUES = new ArrayList<>([
      'dscovr'
  ])
}
