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
import org.cedar.schemas.analyze.Analyzers;

public class Main {

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println(usage());
      System.err.println("Expected 2 or more args.");
      System.exit(1);
      return;
    }

    String filename = args[args.length-1];

    try {
      // ParsedRecord record = ParsedRecord.newBuilder().build();
      // switch(args[0]) {
      //   case "parseISO":
      //   record = parseISO(filename);
      //   break;
      //   case "parseAvro":
      //   record = parseJSON(filename);
      //   break;
      //   default:
      //   System.err.println("Could not recognize step "+args[0]);
      //   System.exit(1);
      // }
      ParsedRecord record = init(args[0], filename);
      for (int i=1; i<args.length-1; i++) {
        record = step(args[i], record);
      }
      System.out.println(record.toString());

    }
    catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }
    // >> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main parseAvro ../onestop-test-data/analysisErrors/collections/invalid_dates.json

    //>> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main parseISO ../onestop-test-data/COOPS/granules/CO-OPS.NOS_1820000_201602_D1_v00.xml

    // >> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main parseAvro analyze ../onestop-test-data/analysisErrors/collections/invalid_dates.json

    //>> java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main parseISO analyze ../onestop-test-data/COOPS/granules/CO-OPS.NOS_1820000_201602_D1_v00.xml

  }

  public static String usage() {
    return "usage: STEP [STEP...] filename";
  }

  public static ParsedRecord init(String command, String filename) throws Exception {
    ParsedRecord record = ParsedRecord.newBuilder().build();
    switch(command) {
      case "parseISO":
      record = parseISO(filename);
      break;
      case "parseAvro":
      record = parseJSON(filename);
      break;
      default:
      System.err.println("Could not recognize step "+command);
      System.exit(1);
    }
    return record;
  }

  public static ParsedRecord step(String command, ParsedRecord record) throws Exception {
    switch(command) {
      case "analyze":
      return analyze(record);
      default:
      System.err.println("Could not recognize step "+command);
      System.exit(1);
    }
    return null;
  }

  public static ParsedRecord analyze(ParsedRecord record) {
    return Analyzers.addAnalysis(record);
  }

  public static ParsedRecord parseISO(String filename) throws Exception {
    return ParsedRecord.newBuilder().setDiscovery(ISOParser.parseXMLMetadataToDiscovery(Files.readString(Path.of(filename)))).build();
  }

  public static ParsedRecord parseJSON(String filename) throws Exception {
    // TODO currently requires commenting out fixing jackson databind versions to work
    return AvroUtils.jsonToAvroLenient(Files.readString(Path.of(filename)), ParsedRecord.getClassSchema());

  }
}
