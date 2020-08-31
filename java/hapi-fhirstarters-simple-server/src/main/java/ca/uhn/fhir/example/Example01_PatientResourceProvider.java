package ca.uhn.fhir.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
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

   /**
    * Simple implementation of the "read" method
    */
   @Read()
   public IBaseResource read(@IdParam IdType theId) {
      Client client = ClientBuilder.newClient();
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
   public MethodOutcome create(@ResourceParam IBaseResource patient) {
      patient.setId(getNextId(patient.fhirType()));

      IParser parser = ctx.newJsonParser();
      String resourceJsonString = parser.encodeResourceToString(patient);

      Client client = ClientBuilder.newClient();
      final String URL = ORION_BASE + "?options=keyValues";
      Response response = client.target(URL)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .post(Entity.json(resourceToEntity(resourceJsonString)));

      MethodOutcome retVal = new MethodOutcome();

      retVal.setId(patient.getIdElement());

      return retVal;
   }

   private long getEntityCount(String entityType) {
      Client client = ClientBuilder.newClient();
      String url = ORION_BASE + "?type=" + entityType + "&options=count&limit=1";
      Response response = client.target(url)
         .request(MediaType.APPLICATION_JSON_TYPE)
         .get();

      return Long.parseLong(response.getHeaderString("Fiware-Total-Count"));
   }

   private String getNextId(String entityType) {
      return Long.toString(getEntityCount(entityType) + 1);
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
