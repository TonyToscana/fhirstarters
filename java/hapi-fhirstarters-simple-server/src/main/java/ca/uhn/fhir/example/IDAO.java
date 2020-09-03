package ca.uhn.fhir.example;

//Todo: change (implement DAO pattern better)
public interface IDAO {
   String read(String id, String type);
   void create(String resource);
   void update(String id, String type, String resource);
   void patch(String id, String type, String resource);
   void delete(String id, String type);
   long retrieveEntityCount(String type);
   String getNextId(String entityType);
}
