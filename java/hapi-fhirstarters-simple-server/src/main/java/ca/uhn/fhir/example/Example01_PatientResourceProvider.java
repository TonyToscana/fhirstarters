package ca.uhn.fhir.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.json.JSONObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.ParameterizedType;


public abstract class Example01_PatientResourceProvider<T extends IBaseResource> implements IResourceProvider {

   private final String ORION_BASE = "http://localhost:1026/v2/entities";
   private FhirContext ctx = FhirContext.forR4();

   private final String TYPE = "type";
   private final String RESOURCE_TYPE = "resourceType";
   private final Client client = ClientBuilder.newClient();

   private long nextId = -1;

   private Class<T> inferedClass;

   /**
    * Constructor
    */
   public Example01_PatientResourceProvider() {

   }

   @Override
   public Class<? extends IBaseResource> getResourceType() {
      if(inferedClass == null){
         inferedClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      }

      return inferedClass;
   }


   //TODO in general, handle sucess and error cases

   //TODO LogicalID vs Identifier

   /**
    * Simple implementation of the "read" method
    */
   @Read()
   public IBaseResource read(@IdParam IdType theId) {
      final String URL = ORION_BASE + "/" + theId.getIdPart() + "?options=keyValues&type=" + theId.getResourceType();
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      String entityJsonString = response.readEntity(String.class);

      IParser parser = ctx.newJsonParser();

      //Patient retVal = myPatients.get(theId.getIdPart());
      //if (retVal == null) {
      //   throw new ResourceNotFoundException(theId);
      //}
      return parser.parseResource(getResourceType(), entityToResource(entityJsonString));
   }

   @Create()
   public MethodOutcome create(@ResourceParam IBaseResource resource) {
      resource.setId(getNextId(resource.fhirType()));

      IParser parser = ctx.newJsonParser();
      String resourceJsonString = parser.encodeResourceToString(resource);

      final String URL = ORION_BASE + "?options=keyValues";
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .post(Entity.json(resourceToEntity(resourceJsonString)));

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(resource.getIdElement());

      return retVal;
   }


   @Delete()
   public MethodOutcome delete(@IdParam IdType theId) {
      //TODO return on error and on success, and exceptions when not found
      final String URL = ORION_BASE + "/" + theId.getIdPart() + "?options=keyValues&type=" + theId.getResourceType();
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .delete();

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(theId);

      // otherwise, delete was successful
      return retVal; // can also return MethodOutcome
   }

   private long retrieveEntityCount(String entityType) {
      Client client = ClientBuilder.newClient();
      String url = ORION_BASE + "?type=" + entityType + "&options=count&limit=1";
      Response response = client.target(url)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      return Long.parseLong(response.getHeaderString("Fiware-Total-Count"));
   }

   private String getNextId(String entityType) {
      // Cached so it doesn't have to ask the server every time, can be changed if desired
      if(nextId == -1) {
         this.nextId = retrieveEntityCount(entityType) + 1;
      } else {
         nextId++;
      }

      return Long.toString(nextId);
   }

   private String resourceToEntity(String resource) {
      JSONObject entityJsonArray = new JSONObject(resource);

      entityJsonArray.putOpt(TYPE, entityJsonArray.opt(RESOURCE_TYPE));
      if(entityJsonArray.has(TYPE))
         entityJsonArray.remove(RESOURCE_TYPE);

      return entityJsonArray.toString();
   }

   private String entityToResource(String entity) {
      JSONObject entityJsonArray = new JSONObject(entity);

      entityJsonArray.putOpt(RESOURCE_TYPE, entityJsonArray.opt(TYPE));
      if(entityJsonArray.has(RESOURCE_TYPE))
         entityJsonArray.remove(TYPE);

      return entityJsonArray.toString();
   }
}
