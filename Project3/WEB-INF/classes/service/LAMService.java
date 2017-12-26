package service;

import javax.jws.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import components.data.*;
import business.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.*;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.*;


@Path("Services")
public class LAMService{
   //Create instance of the dbSingleton class in the Service folder 
   public DBSingleton dbSingleton;
   //Create instance of the Businees class in the Business folder
   private BusinessLayer business;
   @Context 
   private UriInfo context;
   
   //Initialize the connection to singleton. Use business layer to validate.
   @GET
   @Produces("application/xml")
   public String initialize(){
      
      dbSingleton = DBSingleton.getInstance();
      dbSingleton.db.initialLoad("LAMS");
      business = new BusinessLayer();
      business.initialize();
      
      String serviceXML;
      serviceXML = "<?xml version='1.0' encoding='UTF-8'?>";
      serviceXML +="<AppointmentList>";
      serviceXML +=  "<intro>Welcome to the LAMS Appointment Service</intro>";
      serviceXML +=  "<wadl>" + this.context.getBaseUri().toString() + "application.wadl</wadl>";
      serviceXML +="</AppointmentList>";

      return serviceXML;
   }
       
   //Method that gets all listed appointments.
   @Path("Appointments")
   @GET
   @Produces("application/xml")
   public String getAllAppointments(){
      //Init a connection to the singleton and generate the default xml string
      dbSingleton = DBSingleton.getInstance();
      dbSingleton.db.initialLoad("LAMS");
      String apptString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><AppointmentList>";
      //List of objects that represents the data taken from the appointment database
      List<Object> objs = dbSingleton.db.getData("Appointment", "");
      //Create a list of the appointments
      List<Appointment> appointments = new ArrayList<Appointment>();
      //Check each appointment object and add the appointment info to the list. 
      //Then create proper xml in the makeAppointmentXML method.
      if (objs.isEmpty()) {
         dbSingleton = dbSingleton.resetConnection();
      }
      for (Object obj : objs) {
         appointments.add((Appointment)obj);
      }
      for (Appointment appointment: appointments){
         apptString += makeAppointmentXML(appointment);
      }
      apptString += "</AppointmentList>";
      return apptString;
   }
   
   @Path("Appointments/{appointment}")
   @GET
   @Produces("application/xml")
   public String getAppointment(@PathParam("appointment") String apptNum){
      return getAppts(apptNum);
      // try
//          dbSingleton = DBSingleton.resetConnection();
//          List<Object> objs;
//          String aptString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><AppointmentList>";
//          Appointment appt = (Appointment) dbSingleton.db.getData("Appointment", "id='" + apptNum + "'").get(0);
//          aptString += makeAppointmentXML(appt);
//          aptString += "</AppointmentList>";
//          return aptString;
//       }catch(Exception e){
//          return "ERROR THAT APPOINTMENT DOES NOT EXIST";
//       }
   }
   
   private String getAppts(String num){
      try{
         dbSingleton = DBSingleton.resetConnection();
         List<Object> objs;
         String aptString = "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?><AppointmentList>";
         Appointment appt = (Appointment) dbSingleton.db.getData("Appointment", "id='" + num + "'").get(0);
         aptString += makeAppointmentXML(appt);
         aptString += "</AppointmentList>";
         return aptString;
      }catch(Exception e){
         
         return "ERROR THAT APPOINTMENT DOES NOT EXIST"+e.getMessage();
      }

   }
   
   @Path("Appointments")
   @PUT
   @Consumes({"text/xml","application/xml"})
   @Produces("application/xml")
   public String addAppointment(String inxml){  
      //Taking in a string - xml
      //create dom object from xml so I can parse.
      //Create default strings and objects
      String strDate = "";
      String strTime = "";
      String strPatient = "";
      String strPsc = "";
      String strPhleb = "";
      String newPhysicianId = "";
      String newPscId = "";
      boolean badXML = false;
      Patient patient = null;
      Phlebotomist phleb = null;
      PSC psc = null;
           
      try{
         //Use document builder to convert input string to xml then using document builder to parse the xml
         DocumentBuilder newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         Document parse = newDocumentBuilder.parse(new ByteArrayInputStream(inxml.getBytes()));
         String rootElement = parse.getDocumentElement().getNodeName();
         NodeList nList = parse.getElementsByTagName(rootElement);
         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               //Assign string values to each of the parsed xml pieces
               strDate = eElement.getElementsByTagName("date").item(0).getTextContent();
               strTime = eElement.getElementsByTagName("time").item(0).getTextContent();
               strPatient = eElement.getElementsByTagName("patientId").item(0).getTextContent();
               strPsc = eElement.getElementsByTagName("pscId").item(0).getTextContent();
               strPhleb = eElement.getElementsByTagName("phlebotomistId").item(0).getTextContent();
               NodeList nList2 = parse.getElementsByTagName("allLabTest");
               for (int temp2 = 0; temp2 < nList2.getLength(); temp++) {
                  
               }
               //Use the business class to validate the string values
               try{
                  business = new BusinessLayer();
                  patient = business.checkPatient(strPatient);
                  psc = business.checkPSC(strPsc);
                  phleb = business.checkPhleb(strPhleb);
               }catch(Exception e){
                  return "Error " + e.getMessage();
               }
            }
         }
      }catch(Exception ex){
         ex.printStackTrace();
      }
      
      //Generate appointid from last appoint. Add other values from xml. 
      //appointmentNumber = Integer.toString(numstr);
      String appointmentNumber = getAppointmentId();
      Appointment newAppt = new Appointment(appointmentNumber, java.sql.Date.valueOf(strDate), java.sql.Time.valueOf(strTime));

      //extra steps here due to persistence api and join, need to create objects in list
      //TODO: Figure out reading xml for tests
      List<AppointmentLabTest> tests = new ArrayList<AppointmentLabTest>();
      AppointmentLabTest test = new AppointmentLabTest("800","86900","292.9");
      test.setDiagnosis((Diagnosis)dbSingleton.db.getData("Diagnosis", "code='292.9'").get(0));
      test.setLabTest((LabTest)dbSingleton.db.getData("LabTest","id='86900'").get(0));
      tests.add(test);
      newAppt.setAppointmentLabTestCollection(tests);
      
      //Create the new appointment information off of the previously edited object
      //if(!badXML){
         newAppt.setPatientid(patient);
         newAppt.setPhlebid(phleb);
         newAppt.setPscid(psc);
         List<Object> objs = dbSingleton.db.getData("Appointment", "");
         //return ""+dbSingleton.db.addData(newAppt);
         
         return getAppts(appointmentNumber);
     //}else{
       //  return "<?xml version='1.0' encoding='UTF-8' standalone='no'?><AppointmentList> <error>ERROR:Appointment is not available</error> </AppointmentList>"; 
     //}
   }//end of getAppointment
   
   public String getAppointmentId(){
      String appointmentNumber = "700";
      int numstr;
      while(true){
         if(!checkAppointment(appointmentNumber)){
            break;
         }
         numstr = Integer.parseInt(appointmentNumber);
         numstr += 10;
         appointmentNumber = Integer.toString(numstr);
      }
      return appointmentNumber;
    }
    
    public boolean checkAppointment(String apptNum){
      try{
         dbSingleton = DBSingleton.getInstance();
         List<Object> objs;
         Appointment appt = (Appointment) dbSingleton.db.getData("Appointment", "id='" + apptNum + "'").get(0);
         return true;
      }catch(Exception e){
         return false;
      }
   }
   
   //Method makes the xml for getAllAppointments and getAppointment methods
   public String makeAppointmentXML(Appointment appointment){
      String apptString = "<appointment date=\"" + appointment.getApptdate() + "\" id=\"" + appointment.getId() + "\" time=\"";
      apptString += appointment.getAppttime() + "\">";
      apptString += "<uri>" + this.context.getBaseUri().toString() + "Appointments/"+appointment.getId() + "</uri>";
   
      Patient patient = appointment.getPatientid();
      apptString +=  "<patient id=\"" + patient.getId() +"\"><uri/><name>" + patient.getName() + "</name><address>" + patient.getAddress();
      apptString +=  "</address><insurance>" + patient.getInsurance() + "</insurance><dob>" + patient.getDateofbirth() + "</dob></patient>";
       
      Phlebotomist phleb = appointment.getPhlebid();
      apptString += "<phlebotomist id=\""+ phleb.getId() +"\"><uri/><name>"+ phleb.getName() +"</name></phlebotomist>";
       
      PSC psc = appointment.getPscid();
      apptString += "<psc id=\"" + psc.getId() + "\"><uri/><name>" + psc.getName() +"</name></psc>";
      
      apptString += "<allLabTests>";
      List<AppointmentLabTest> list = appointment.getAppointmentLabTestCollection();
      for(AppointmentLabTest temp: list){
         LabTest lab = temp.getLabTest();
         apptString += "<uri/><appointmentLabTest appointmentId=\""+ appointment.getId() +"\" dxcode=\"" + temp.getDiagnosis().getCode();
         apptString += "\" labTestId=\"" + lab.getId() +"\"/>";
      }
      apptString += "</allLabTests></appointment>";
      return apptString;
   }
}