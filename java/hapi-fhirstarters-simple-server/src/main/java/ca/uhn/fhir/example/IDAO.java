package ca.uhn.fhir.example;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

public interface IDAO<T extends IBaseResource> {
   IBaseResource read(IIdType theId);
   void create(IBaseResource resource);
   void update(IBaseResource resource);
   void patch(IIdType theId, String patchBody);
   void delete(IIdType theId);
   long retrieveEntityCount(String type);
   String getNextId(String entityType);
}
