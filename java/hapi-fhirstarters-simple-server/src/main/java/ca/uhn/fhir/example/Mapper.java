package ca.uhn.fhir.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.json.JSONObject;

import javax.ws.rs.client.Entity;

public class Mapper {
   private final static String TYPE = "type";
   private final static String RESOURCE_TYPE = "resourceType";
   private final static String ID = "id";
   protected final FhirContext ctx = FhirContext.forR4();

   public Entity resourceToEntity(IBaseResource resource) {
      JSONObject resourceJson = resourceToJson(resource);

      resourceJson.putOpt(TYPE, resourceJson.opt(RESOURCE_TYPE));
      if(resourceJson.has(TYPE))
         resourceJson.remove(RESOURCE_TYPE);

      return Entity.json(resourceJson.toString());
   }

   public IBaseResource entityToResource(Entity entity, Class<? extends IBaseResource> resourceClass) {
      JSONObject entityJson = entityToJson(entity);

      entityJson.putOpt(RESOURCE_TYPE, entityJson.opt(TYPE));
      if(entityJson.has(RESOURCE_TYPE))
         entityJson.remove(TYPE);

      IParser parser = ctx.newJsonParser();

      return parser.parseResource(resourceClass, entityJson.toString());
   }

   public Entity prepareEntityForUpdate(Entity entity) {
      JSONObject entityJson = entityToJson(entity);

      if (entityJson.has(TYPE)) {
         entityJson.remove(TYPE);
      }

      if (entityJson.has(ID)) {
         entityJson.remove(ID);
      }

      return Entity.json(entityJson.toString());
   }

   private JSONObject resourceToJson(IBaseResource resource) {
      IParser parser = ctx.newJsonParser();
      return new JSONObject(parser.encodeResourceToString(resource));
   }

   private JSONObject entityToJson(Entity entity) {
      return new JSONObject(entity.getEntity().toString());
   }
}
