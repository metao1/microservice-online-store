package order.presentation;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import tech.allegro.schema.json2avro.converter.AvroConversionException;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.util.Arrays;

public class Json2AvroConverter {
    public static void main(String[] args) {

// Avro schema with one string field: username
        String schema =
                "{\n" +
                        "    \"type\": \"record\",\n" +
                        "    \"name\": \"OrderAvro\",\n" +
                        "    \"namespace\": \"com.order.microservice.avro\",\n" +
                        "    \"fields\": [\n" +
                        "      {\n" +
                        "        \"name\": \"orderId\",\n" +
                        "        \"type\": \"string\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"productId\",\n" +
                        "        \"type\": \"string\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"customerId\",\n" +
                        "        \"type\": \"string\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"status\",\n" +
                        "        \"type\": {\n" +
                        "          \"type\": \"enum\",\n" +
                        "          \"name\": \"Status\",\n" +
                        "          \"symbols\": [\n" +
                        "            \"NEW\",\n" +
                        "            \"ACCEPT\",\n" +
                        "            \"CONFIRM\",\n" +
                        "            \"REJECT\",\n" +
                        "            \"PAYMENT\",\n" +
                        "            \"STOCK\",\n" +
                        "            \"ROLLBACK\"\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        \"namespace\": \"com.order.microservice.avro\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"quantity\",\n" +
                        "        \"type\": \"int\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"price\",\n" +
                        "        \"type\": \"double\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "        \"name\": \"currency\",\n" +
                        "        \"type\": {\n" +
                        "          \"type\": \"enum\",\n" +
                        "          \"name\": \"Currency\",\n" +
                        "          \"symbols\": [\n" +
                        "            \"eur\",\n" +
                        "            \"dlr\"\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        \"namespace\": \"com.order.microservice.avro\"\n" +
                        "      }\n" +
                        "    ]\n" +
                        "  }";


        JsonAvroConverter converter = new JsonAvroConverter();

        String invalidJson = "{ \"orderId\": \"1\", \"productId\": \"1\", \"customerId\": \"1\", \"status\": \"NEW\", \"quantity\": 1, \"price\": 1.0, \"currency\": \"eur\" }";

        try {
            System.out.println(Arrays.toString(converter.convertToAvro(invalidJson.getBytes(), schema)));
        } catch (AvroConversionException ex) {
            System.err.println("Caught exception: " + ex.getMessage());
        }
    }
}
