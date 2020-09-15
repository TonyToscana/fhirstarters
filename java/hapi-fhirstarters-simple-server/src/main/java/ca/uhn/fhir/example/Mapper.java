package ca.uhn.fhir.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;
import org.hl7.fhir.instance.model.api.IBaseResource;

import javax.ws.rs.client.Entity;

public class Mapper {
   private final static String TYPE = "type";
   private final static String RESOURCE_TYPE = "resourceType";
   private final static String ID = "id";
   protected final FhirContext ctx = FhirContext.forR4();
   protected final ObjectMapper oMapper = new ObjectMapper();

   public Entity resourceToEntity(IBaseResource resource) {
      ObjectNode resourceJson = resourceToJson(resource);

      resourceJson.set(TYPE, resourceJson.get(RESOURCE_TYPE));

      if (resourceJson.has(TYPE))
         resourceJson.remove(RESOURCE_TYPE);

      return Entity.json(resourceJson.toString());
   }

   public IBaseResource entityToResource(Entity entity, Class<? extends IBaseResource> resourceClass) {
      ObjectNode entityJson = entityToJson(entity);

      entityJson.set(RESOURCE_TYPE, entityJson.get(TYPE));

      if(entityJson.has(RESOURCE_TYPE))
         entityJson.remove(TYPE);

      IParser parser = ctx.newJsonParser();

      return parser.parseResource(resourceClass, entityJson.toString());
   }

   public Entity prepareEntityForUpdate(Entity entity) {
      ObjectNode entityJson = entityToJson(entity);

      entityJson.remove(TYPE);
      entityJson.remove(ID);

      return Entity.json(entityJson.toString());
   }

   public IBaseResource applyPatchToResource(String patchBody, IBaseResource resource) {
      JsonNode patch = patchBodyToJson(patchBody);
      JsonNode resourceJson = resourceToJson(resource);
      JsonPatch.validate(patch);

      JsonNode patchedResourceJson = JsonPatch.apply(patch, resourceJson);

      return jsonToResource(patchedResourceJson, resource.getClass());
   }

   private IBaseResource jsonToResource(JsonNode resourceJson, Class<? extends IBaseResource> resourceClass) {
      IParser parser = ctx.newJsonParser();

      return parser.parseResource(resourceClass, resourceJson.toString());
   }

   private ObjectNode resourceToJson(IBaseResource resource) {
      IParser parser = ctx.newJsonParser();

      try {
         return (ObjectNode) oMapper.readTree(parser.encodeResourceToString(resource));
      } catch (JsonProcessingException e) {
         e.printStackTrace();
         throw new UnprocessableEntityException("Error when converting Resource to Json");
      }
   }

   private ObjectNode entityToJson(Entity entity) {
      try {
         return (ObjectNode) oMapper.readTree(entity.getEntity().toString());
      } catch (JsonProcessingException e) {
         e.printStackTrace();
         throw new UnprocessableEntityException("Error when converting Entity to Json");
      }
   }

   private JsonNode patchBodyToJson(String patchBody) {
      try {
         return oMapper.readTree(patchBody);
      } catch (JsonProcessingException e) {
         e.printStackTrace();
         throw new UnprocessableEntityException("Wrong patch body");
      }
   }
}
