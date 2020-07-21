package org.cedar.onestop.test.common;

 // import org.cedar.schemas.avro.util.AvroUtils;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Discovery;
import org.cedar.onestop.indexer.util.TransformationUtils;
import org.cedar.schemas.avro.psi.RecordType;
import java.util.Set;
import java.util.HashSet;
import org.cedar.schemas.parse.ISOParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.Exception;
import org.cedar.schemas.avro.util.AvroUtils;


public class Main {

// ./gradlew test-common:shadowJar
// java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main Hello World

/*
  public static void main(String[] args) {
    // String json = "{\"name\":\"testfile\"}";
    System.out.println( ParsedRecord.newBuilder().build() );
    Set<String> fields = new HashSet<String>();
    fields.add("description");
    System.out.println( TransformationUtils.reformatMessageForAnalysis(ParsedRecord.newBuilder().build(), fields, RecordType.granule) );
    System.out.println( args[0] );
    System.out.println( args[1] );
  }
*/
  // commands: parse, analyze, searchIndex, errorIndex
  // type: collection, granule
  // usage: java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main TYPE COMMAND FILE

  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println(usage());
      System.err.println("Expected exactly 3 args.");
      System.exit(1);
      return;
    }
    if( !args[0].equals("collection") && !args[0].equals("granule") ) {
      System.err.println(usage());
      System.err.println("Expected type: collection or granule.");
      System.err.println("'"+args[0]+"'");
      System.exit(1);
      return;
    }
    if( !args[1].equals("parse") && !args[1].equals("analyze") && !args[1].equals("searchIndex") && !args[1].equals("errorIndex") ) {
      System.err.println(usage());
      System.err.println("Expected command: parse, analyze, searchIndex, or errorIndex.");
      System.err.println("'"+args[1]+"'");
      System.exit(1);
      return;
    }

    //>> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main granule parse ../onestop-test-data/COOPS/granules/CO-OPS.NOS_1820000_201602_D1_v00.xml
    //>> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main granule parse ../onestop-test-data/analysisErrors/collections/invalid_dates.json
    System.out.println("processing "+args[0]+" "+args[1]+" "+args[2]);

    try {

      if( args[1].equals("parse") ) {
        parse(args[2]); // TODO doesn't require TYPE - revisit input pattern
      }
    }
    catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }

  }

  public static String usage() {
    return "commands: parse, analyze, searchIndex, errorIndex\ntype: collection, granule\nusage: TYPE COMMAND FILE";
  }

  public static void parse(String filename) throws Exception {
    if (filename.endsWith("xml")) {
      ParsedRecord record = ParsedRecord.newBuilder().setDiscovery(parseISO(filename)).build();
      System.out.println(record.toString());
    } else {
      System.out.println(parseJSON(filename).toString());
    }
  }

  public static Discovery parseISO(String filename) throws Exception {
    return ISOParser.parseXMLMetadataToDiscovery(Files.readString(Path.of(filename)));
  }

  public static ParsedRecord parseJSON(String filename) throws Exception {
    // TODO currently requires commenting out fixing jackson databind versions to work
    return AvroUtils.jsonToAvroLenient(Files.readString(Path.of(filename)), ParsedRecord.getClassSchema());

  }
}
