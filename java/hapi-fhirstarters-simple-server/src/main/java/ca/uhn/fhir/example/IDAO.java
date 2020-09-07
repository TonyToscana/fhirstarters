package ca.uhn.fhir.example;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;

//Todo: change (implement DAO pattern better)
public interface IDAO<T extends IBaseResource> {
   IBaseResource read(IdType theId);
   void create(IBaseResource resource);
   void update(IBaseResource resource);
   void patch(IdType theId, String patchBody);
   void delete(IdType theId);
   long retrieveEntityCount(String type);
   String getNextId(String entityType);
}
