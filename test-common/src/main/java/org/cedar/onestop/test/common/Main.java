package org.cedar.onestop.test.common;

import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.onestop.indexer.util.TransformationUtils;
import org.cedar.schemas.avro.psi.RecordType;
import org.cedar.schemas.parse.ISOParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.Exception;
import org.cedar.schemas.analyze.Analyzers;
import org.cedar.onestop.parsalyzer.util.RecordParser;

import com.fasterxml.jackson.databind.ObjectMapper;

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
      Object esResult = null;
      for (int i=1; i<args.length-1; i++) {
        ParsedRecord updated = step(args[i], record);
        if (updated == null) {
          esResult = esStep(args[i], record);
          // ObjectMapper mapper = new ObjectMapper();
          // System.out.println(mapper.writeValueAsString(esResult));
        } else {
          record = updated;
        }
      }
      if (esResult != null ) {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(esResult));
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

  public static Object esStep(String command, ParsedRecord record) {
    switch(command) {
      case "granuleSearch":
      return TransformationUtils.reformatGranuleForSearch(System.currentTimeMillis(), record);
      // TODO add flatttened granule as an option?
      case "collectionSearch":
      return TransformationUtils.reformatCollectionForSearch(System.currentTimeMillis(), record);
      case "granuleError":
      return TransformationUtils.reformatGranuleForAnalysis(System.currentTimeMillis(), record);
      case "collectionError":
      return TransformationUtils.reformatCollectionForAnalysis(System.currentTimeMillis(), record);
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
    return RecordParser.parseRaw(Files.readString(Path.of(filename)), RecordType.granule); // TODO type is arbitrary and potentially wrong here, just hardcoded to test
  }
}
