package business;

import components.data.Diagnosis;
import components.data.PSC;
import components.data.Patient;
import components.data.Phlebotomist;
import service.DBSingleton;

import java.util.*;

public class BusinessLayer {
    public DBSingleton dbSingleton;

    public String initialize() {
        dbSingleton = DBSingleton.getInstance();
        dbSingleton.db.initialLoad("LAMS");
        return "Database Initialized";
    }

    public Patient checkPatient(String id) throws Exception {
        List<Object> objs = dbSingleton.db.getData("Patient", "id='" + id + "'");
        if (objs.size() != 1) {
            throw new Exception("Patient error");
        }
        return (Patient) objs.get(0);
    }

    public Phlebotomist checkPhleb(String id) throws Exception {
        List<Object> objs = dbSingleton.db.getData("Phlebotomist", "id='" + id + "'");
        if (objs.size() != 1) {
            throw new Exception("Phlebotomist error");
        }
        return (Phlebotomist) objs.get(0);
    }

    public PSC checkPSC(String id) throws Exception {
        List<Object> objs = dbSingleton.db.getData("PSC", "id='" + id + "'");
        if (objs.size() != 1) {
            throw new Exception("PSC error");
        }
        return (PSC) objs.get(0);
    }

    public Diagnosis checkDiagnosis(String id) throws Exception {
        List<Object> objs = dbSingleton.db.getData("Diagnosis", "code='" + id + "'");
        if (objs.size() != 1) {
            throw new Exception("Diagnosis error");
        }
        return (Diagnosis) objs.get(0);
    }
}