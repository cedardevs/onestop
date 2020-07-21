package org.cedar.onestop.test.common;

import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.schemas.avro.psi.Discovery;
import org.cedar.onestop.indexer.util.TransformationUtils;
import org.cedar.schemas.avro.psi.RecordType;
import java.util.Set;
import java.util.Map;
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
      ParsedRecord record = init(args[0], filename);
      Map<String, Object> esResult = null;
      for (int i=1; i<args.length-1; i++) {
        ParsedRecord updated = step(args[i], record);
        if (updated == null) {
          esResult = esStep(args[i], record);
        } else {
          record = updated;
        }
      }
      if (esResult != null ) {
        System.out.println(esResult.toString());
      } else {
        System.out.println(record.toString());
      }
    }
    catch (Exception e) {
      System.err.println(e);
      System.exit(1);
    }

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
      // default:
      // System.err.println("Could not recognize step "+command);
      // System.exit(1);
    }
    return null;
  }

  public static Map<String, Object> esStep(String command, ParsedRecord record) {
    Set<String> fields = new HashSet<String>(); // TODO refactor reformatMessageForAnalysis to not need fields input
    fields.add("description");
    switch(command) {
      case "granuleSearch":
      return TransformationUtils.reformatMessageForSearch(record, fields);
      case "collectionSearch":
      return TransformationUtils.reformatMessageForSearch(record, fields);
      case "granuleError":
      return TransformationUtils.reformatMessageForAnalysis(record, fields, RecordType.granule);
      case "collectionError":
      return TransformationUtils.reformatMessageForAnalysis(record, fields, RecordType.collection);
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
