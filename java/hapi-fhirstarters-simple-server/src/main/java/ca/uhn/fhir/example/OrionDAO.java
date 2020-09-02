package ca.uhn.fhir.example;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class OrionDAO implements IDAO {
   private final static String ORION_BASE = "http://localhost:1026/v2/entities";
   private final static String TOTAL_COUNT_HEADER = "Fiware-Total-Count";
   private final Client client = ClientBuilder.newClient();
   private final static Mapper mapper = new Mapper();

   public OrionDAO() {

   }

   @Override
   public String read(String id, String type) {
      final String URL = ORION_BASE + "/" + id + "?options=keyValues&type=" + type;
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      String resource = mapper.entityToResource(response.readEntity(String.class));
      return resource;

   }

   @Override
   public void create(String resource) {
      final String entity = mapper.resourceToEntity(resource);
      final String URL = ORION_BASE + "?options=keyValues";
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .post(Entity.json(entity));
   }

   @Override
   public void delete(String id, String type) {
      final String URL = ORION_BASE + "/" + id + "?options=keyValues&type=" + type;
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .delete();
   }

   @Override
   public long retrieveEntityCount(String type) {
      String url = ORION_BASE + "?type=" + type + "&options=count&limit=1";
      Response response = client.target(url)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      return Long.parseLong(response.getHeaderString(TOTAL_COUNT_HEADER));
   }
}

