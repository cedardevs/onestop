package org.cedar.onestop.test.common;

 // import org.cedar.schemas.avro.util.AvroUtils;
import org.cedar.schemas.avro.psi.ParsedRecord;
import org.cedar.onestop.indexer.util.TransformationUtils;
import org.cedar.schemas.avro.psi.RecordType;
import java.util.Set;
import java.util.HashSet;

public class Main {

// ./gradlew test-common:shadowJar
// java -cp test-common/build/libs/onestop-test-common-unspecified-all.jar org.cedar.onestop.test.common.Main Hello World

  public static void main(String[] args) {
    // String json = "{\"name\":\"testfile\"}";
    System.out.println( ParsedRecord.newBuilder().build() );
    Set<String> fields = new HashSet<String>();
    fields.add("description");
    System.out.println( TransformationUtils.reformatMessageForAnalysis(ParsedRecord.newBuilder().build(), fields, RecordType.granule) );
    System.out.println( args[0] );
    System.out.println( args[1] );
  }

}
