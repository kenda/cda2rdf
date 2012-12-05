package edu.leipzig.dispedia;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import org.eclipse.emf.common.util.EList;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.Patient;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.ccd.CCDPackage;
import org.openhealthtools.mdht.uml.cda.ccd.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;
import org.openhealthtools.mdht.uml.hl7.datatypes.impl.*;

/**
 * Class that converts a CDA document to RDF.
 *
 * @author Marcus Nitzschke
 */
public class CDA2RDF implements Converter {

    /**
     * A list of the patient roles set by the read method.
     */
    private EList<PatientRole> patientRoles;

    /**
     * This method reads the input which is a CDA document
     * in that case and sets the appropriate patient roles.
     * @param input The CDA document
     */
    public void read(String input){
	// deserialize input
	CCDPackage.eINSTANCE.eClass();
	ContinuityOfCareDocument ccdDocument = null;
	try{
	    ccdDocument = (ContinuityOfCareDocument) CDAUtil.load(new ByteArrayInputStream(input.getBytes("UTF-8")));
	}
	catch (Exception e) {
	    System.out.println(e);
	}
	
	// TODO check the validity of the CDA document

	// extract and set patient roles
	this.patientRoles = ccdDocument.getPatientRoles();
    }

    /**
     * This method writes the RDF output which results of
     * processing the patient roles.
     * @return The RDF document as rdf/xml
     */
    public String write(){
	// create Jena model
	Model model = ModelFactory.createDefaultModel();
	model.setNsPrefix("schema", "http://schema.org/");
	model.setNsPrefix("dispediao", "http://dispedia.de/o/");

	// HACK temporary hack for unique uris
	int i=0;
	for(PatientRole patientrole : this.patientRoles){

	    // create new resource for the patient
	    Resource patient = model.createResource("http://example.com/patient_xy"+i++);

	    // Patient name
	    PN names = patientrole.getPatient().getNames().get(0);
	    patient.addProperty(model.createProperty("http://schema.org/givenName"), names.getGivens().get(0).getText())
		   .addProperty(model.createProperty("http://schema.org/familyName"), names.getFamilies().get(0).getText());

	    // gender
	    CE gender = patientrole.getPatient().getAdministrativeGenderCode();
	    if (gender != null)
		patient.addProperty(model.createProperty("http://schema.org/gender"), gender.getCode());
	}

	// output the model as rdf/xml
	StringWriter output = new StringWriter();
	model.write(output);

	return output.toString();
    }

    public static void main(String[] args){
	CDA2RDF c2r = new CDA2RDF();
	String xml = "<?xml version='1.0' encoding='UTF-8'?><ClinicalDocument xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns='urn:hl7-org:v3' xsi:schemaLocation='urn:hl7-org:v3 CDA.xsd'>  <templateId root='2.16.840.1.113883.10.20.1'/>  <code code='34133-9' codeSystem='2.16.840.1.113883.6.1' codeSystemName='LOINC' displayName='Summarization of episode note'/>  <recordTarget>    <patientRole>      <id root='996-756-495' />      <addr use='H'><streetAddressLine>1313 Mockingbird Lane</streetAddressLine><city>Janesville</city><state>WI</state><postalCode>53545</postalCode></addr>      <patient>        <name><given>Henry</given><family>Levin</family></name>      </patient>    </patientRole></recordTarget><recordTarget> <patientRole>      <id root='925-234-523'/>      <addr use='H'><streetAddressLine>1313 Mockingbird Lane</streetAddressLine><city>Janesville</city><state>WI</state><postalCode>53545</postalCode></addr>      <patient><administrativeGenderCode code='M' codeSystem='2.16.840.1.113883.5.1'/> <name><given>Henry2</given><family>Levin2</family></name>      </patient>    </patientRole>  </recordTarget></ClinicalDocument>";
	c2r.read(xml);
	System.out.println(c2r.write());
    }

}
