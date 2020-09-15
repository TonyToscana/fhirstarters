package ca.uhn.fhir.example;

import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.PatchTypeEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.OperationOutcome;


public abstract class AbstractResourceProvider<T extends IBaseResource> implements IResourceProvider {

   protected final IDAO<T> dao = new OrionDAO<>((Class<T>) getResourceType());

   protected Class<T> inferedClass;

   /**
    * Constructor
    */
   public AbstractResourceProvider() {

   }

   @Override
   public Class<? extends IBaseResource> getResourceType() {
      if(inferedClass == null){
         inferedClass = ClassUtils.getGenericClass(getClass());
      }

      return inferedClass;
   }

   // TODO cvvicalID vs Identifier
   // TODO Implement Search operation and exceptions
   // TODO check which operations are not supported in which resources (think of a method to throw exception notsupported)
   
   // TODO update exceptions
   // TODO patch exceptions
   // TODO create exceptions
   // TODO delete exceptions

   /**
    * Simple implementation of the "read" method
    */
   @Read()
   public IBaseResource read(@IdParam IdType theId) {
      return dao.read(theId);
   }

   @Update
   public MethodOutcome update(@ResourceParam IBaseResource resource) {
      dao.update(resource);

      return new MethodOutcome();
   }

   @Patch
   public OperationOutcome patch(@IdParam IdType theId, PatchTypeEnum thePatchType, @ResourceParam String theBody) {
      if (thePatchType != PatchTypeEnum.JSON_PATCH) {
         throw new UnsupportedOperationException("Only JSON Patch is supported");
      }
      dao.patch(theId, theBody);

      OperationOutcome retVal = new OperationOutcome();
      retVal.getText().setDivAsString("<div>OK</div>");

      return retVal;
   }

   @Create()
   public MethodOutcome create(@ResourceParam IBaseResource resource) {
      dao.create(resource);

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(resource.getIdElement());

      return retVal;
   }

   @Delete()
   public MethodOutcome delete(@IdParam IdType theId) {
      dao.delete(theId);

      MethodOutcome retVal = new MethodOutcome();
      retVal.setId(theId);

      return retVal;
   }
}
