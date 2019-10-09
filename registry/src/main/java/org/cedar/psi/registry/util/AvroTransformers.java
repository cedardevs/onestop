package org.cedar.psi.registry.util;

import org.apache.avro.Schema;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AvroTransformers {

  private static final EncoderFactory encoderFactory = EncoderFactory.get();
  private static final DecoderFactory decoderFactory = DecoderFactory.get();

  public static void avroToByteStream(SpecificRecord record, OutputStream outputStream) throws IOException {
    var schema = record.getSchema();
    var encoder = encoderFactory.binaryEncoder(outputStream, null);
    var writer = new SpecificDatumWriter<SpecificRecord>(schema);
    writer.write(record, encoder);
    encoder.flush();
  }

  public static byte[] avroToBytes(SpecificRecord record) throws IOException {
    var buffer = new ByteArrayOutputStream();
    avroToByteStream(record, buffer);
    return buffer.toByteArray();
  }

  public static <T extends SpecificRecord> T bytesToAvro(byte[] bytes, Schema schema) throws IOException {
    var decoder = decoderFactory.binaryDecoder(bytes, null);
    var reader = new SpecificDatumReader<T>(schema);
    var result = reader.read(null, decoder);
    return result;
  }

  public static <T extends SpecificRecord> T byteStreamToAvro(InputStream inputStream, Schema schema) throws IOException {
    var decoder = decoderFactory.binaryDecoder(inputStream, null);
    var reader = new SpecificDatumReader<T>(schema);
    var result = reader.read(null, decoder);
    return result;
  }

}
