package ca.uhn.fhir.example;

public interface IDAO {
   String read(String id, String type);
   void create(String resource);
   void delete(String id, String type);
   long retrieveEntityCount(String type);
}
