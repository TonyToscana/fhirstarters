package ca.uhn.fhir.example;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

public class OrionDAO implements IDAO {
   private final static String ORION_BASE_URI = "http://localhost:1026/v2";
   private final static String ORION_ENTITIES_URI = ORION_BASE_URI + "/entities";
   private final static String TOTAL_COUNT_HEADER = "Fiware-Total-Count";
   private final static String PATH_SEPARATOR = "/";
   private final static String PARAM_OPTIONS = "options";
   private final static String OPTION_KEY_VALUES = "keyValues";
   private final static String OPTION_COUNT ="count";
   private final static String PARAM_LIMIT = "limit";
   private final static String PARAM_TYPE = "type";
   private final static String PATH_ATTRS = "attrs";
   // Property needed for the Client to accept PATCH operations https://stackoverflow.com/questions/55778145/how-to-use-patch-method-with-jersey-invocation-builder#comment98235093_55778145
   private final Client client = ClientBuilder.newClient().property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
   private final static Mapper mapper = new Mapper();
   protected long nextId = -1;

   public OrionDAO() {

   }

   @Override
   public String read(String id, String type) {
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(id)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES)
         .queryParam(PARAM_TYPE, type);

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      return mapper.entityToResource(response.readEntity(String.class));

   }

   @Override
   public void create(String resource) {
      final String entity = mapper.resourceToEntity(resource);
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .post(Entity.json(entity));
   }

   @Override
   public void update(String id, String type, String resource) {
      final String entity = mapper.resourceToEntity(resource);
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(id)
         .path(PATH_ATTRS)
         .queryParam(PARAM_TYPE, type)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      final String URL = builder.toString();
      Entity payload = Entity.json(mapper.prepareEntityForUpdate(entity));

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .put(payload);

      System.out.println(response.toString());
   }

   @Override
   public void patch(String id, String type, String resource) {
      final String entity = mapper.resourceToEntity(resource);
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(id)
         .path(PATH_ATTRS)
         .queryParam(PARAM_TYPE, type)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      final String URL = builder.toString();
      Entity payload = Entity.json(mapper.prepareEntityForUpdate(entity));

      //https://stackoverflow.com/questions/55778145/how-to-use-patch-method-with-jersey-invocation-builder
      Response response = client.target(URL)
         // Does not use JSON PATCH TYPE because Orion returns an error
         .request(MediaType.APPLICATION_JSON_TYPE)
         .method("PATCH", payload);
   }

   @Override
   public void delete(String id, String type) {
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(id)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES)
         .queryParam(PARAM_TYPE, type);

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .delete();
   }

   @Override
   public long retrieveEntityCount(String type) {
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .queryParam(PARAM_TYPE, type)
         .queryParam(PARAM_OPTIONS, OPTION_COUNT)
         .queryParam(PARAM_LIMIT, 1);

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      return Long.parseLong(response.getHeaderString(TOTAL_COUNT_HEADER));
   }

   @Override
   public String getNextId(String entityType) {
      // Cached so it doesn't have to ask the server every time, can be changed if desired
      if(nextId == -1) {
         this.nextId = retrieveEntityCount(entityType) + 1;
      } else {
         nextId++;
      }

      return Long.toString(nextId);
   }
}

