package ca.uhn.fhir.example;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;

import java.lang.reflect.ParameterizedType;


public abstract class AbstractResourceProvider<T extends IBaseResource> implements IResourceProvider {

   protected final IDAO dao = new OrionDAO();
   protected final FhirContext ctx = FhirContext.forR4();

   protected Class<T> inferedClass;

   /**
    * Constructor
    */
   public AbstractResourceProvider() {

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
      String resourceJsonString = dao.read(theId.getIdPart(), theId.getResourceType());

      IParser parser = ctx.newJsonParser();

      return parser.parseResource(getResourceType(), resourceJsonString);
   }

   @Update
   public MethodOutcome update(@IdParam IdType theId, @ResourceParam IBaseResource resource) {
      IParser parser = ctx.newJsonParser();
      String resourceJsonString = parser.encodeResourceToString(resource);
      String resourceType = resource.fhirType();

      dao.update(theId.getIdPart(), resourceJsonString, resourceType);

      return new MethodOutcome();
   }

   @Create()
   public MethodOutcome create(@ResourceParam IBaseResource resource) {
      resource.setId(dao.getNextId(resource.fhirType()));

      IParser parser = ctx.newJsonParser();
      String resourceJsonString = parser.encodeResourceToString(resource);

      dao.create(resourceJsonString);

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(resource.getIdElement());

      return retVal;
   }


   @Delete()
   public MethodOutcome delete(@IdParam IdType theId) {
      //TODO return on error and on success, and exceptions when not found
      dao.delete(theId.getIdPart(), theId.getResourceType());

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(theId);

      // otherwise, delete was successful
      return retVal; // can also return MethodOutcome
   }
}
