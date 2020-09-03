package ca.uhn.fhir.example;

import org.json.JSONObject;

public class Mapper {
   private final static String TYPE = "type";
   private final static String RESOURCE_TYPE = "resourceType";
   private final static String ID = "id";

   public String resourceToEntity(String resource) {
      JSONObject entityJsonArray = new JSONObject(resource);

      entityJsonArray.putOpt(TYPE, entityJsonArray.opt(RESOURCE_TYPE));
      if(entityJsonArray.has(TYPE))
         entityJsonArray.remove(RESOURCE_TYPE);

      return entityJsonArray.toString();
   }

   public String entityToResource(String entity) {
      JSONObject entityJsonArray = new JSONObject(entity);

      entityJsonArray.putOpt(RESOURCE_TYPE, entityJsonArray.opt(TYPE));
      if(entityJsonArray.has(RESOURCE_TYPE))
         entityJsonArray.remove(TYPE);

      return entityJsonArray.toString();
   }

   public String prepareEntityForUpdate(String entity) {
      JSONObject entityJsonArray = new JSONObject(entity);

      if (entityJsonArray.has(TYPE)) {
         entityJsonArray.remove(TYPE);
      }

      if (entityJsonArray.has(ID)) {
         entityJsonArray.remove(ID);
      }

      return entityJsonArray.toString();
   }
}
