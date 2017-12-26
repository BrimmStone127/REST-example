package service;

import java.util.Set;
import javax.ws.rs.core.Application;

@javax.ws.rs.ApplicationPath("resources")
public class ApplicationConfig extends Application{
   
   @Override
   public Set<Class<?>> getClasses(){
      return getRestResourceClasses();
   }
   
   public Set<Class<?>> getRestResourceClasses(){
      Set<Class<?>> resources = new java.util.HashSet<Class<?>>();
      resources.add(service.LAMService.class);
      return resources;
   }
   
}