package ca.uhn.fhir.example;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Type;

public class OrionDAO<T extends IBaseResource> implements IDAO<T> {
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

   private final Class<T> resourceClass;

   // Property needed for the Client to accept PATCH operations https://stackoverflow.com/questions/55778145/how-to-use-patch-method-with-jersey-invocation-builder#comment98235093_55778145
   private final Client client = ClientBuilder.newClient().property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
   private final static Mapper mapper = new Mapper();
   protected long nextId = -1;

   public OrionDAO(Class<T> resourceClass) {
      this.resourceClass = resourceClass;
   }

   @Override
   public IBaseResource read(IdType theId) {
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(theId.getIdPart())
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES)
         .queryParam(PARAM_TYPE, theId.getResourceType());

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      Entity entity = Entity.json(response.readEntity(String.class));

      return mapper.entityToResource(entity, resourceClass);

   }

   @Override
   public void create(IBaseResource resource) {
      resource.setId(getNextId(resource.fhirType()));

      final Entity payload = mapper.resourceToEntity(resource);
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      final String URL = builder.toString();

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .post(payload);
   }

   @Override
   public void update(IBaseResource resource) {
      final Entity entity = mapper.resourceToEntity(resource);

      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(resource.getIdElement().getIdPart())
         .path(PATH_ATTRS)
         .queryParam(PARAM_TYPE, resource.fhirType())
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      final String URL = builder.toString();
      Entity payload = mapper.prepareEntityForUpdate(entity);

      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .put(payload);
   }

   // TODO create new method for patch resource body
   // TODO Review patch method reason in todo inside
   @Override
   public void patch(IdType theId, String patchBody) {
      Entity entity = Entity.json(patchBody);
      //Entity entity = mapper.resourceToEntity(resource);

      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(theId.getIdPart())
         .path(PATH_ATTRS)
         .queryParam(PARAM_TYPE, theId.getResourceType())
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES);

      String URL = builder.toString();
      Entity payload = Entity.json(mapper.prepareEntityForUpdate(entity));

      //https://stackoverflow.com/questions/55778145/how-to-use-patch-method-with-jersey-invocation-builder
      Response response = client.target(URL)
         // TODO test if the error orion gave was because the payload was not formatted to a correct json patch
         // Does not use JSON PATCH TYPE because Orion returns an error
         .request(MediaType.APPLICATION_JSON_TYPE)
         .method("PATCH", payload);
   }

   @Override
   public void delete(IdType theId) {
      UriBuilder builder = UriBuilder
         .fromUri(ORION_ENTITIES_URI)
         .path(theId.getIdPart())
         .queryParam(PARAM_OPTIONS, OPTION_KEY_VALUES)
         .queryParam(PARAM_TYPE, theId.getResourceType());

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

