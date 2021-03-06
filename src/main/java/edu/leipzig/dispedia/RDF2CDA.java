package edu.leipzig.dispedia;

import java.io.StringWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.BasicEList;
import org.openhealthtools.mdht.uml.cda.CDAFactory;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.InfrastructureRootTypeId;
import org.openhealthtools.mdht.uml.cda.Patient;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.*;

/**
 * Class that converts a dispedia specific RDF document to a
 * CDA document.
 *
 * @author Marcus Nitzschke
 */
public class RDF2CDA implements Converter {

    /**
     * A list of the patient roles set by the read method.
     */
    private EList<PatientRole> patientRoles = new BasicEList(); 
;

    /**
     * This method reads the input which is a CDA document
     * in that case and sets the appropriate patient roles.
     * @param input The RDF document
     */
    public void read(String input){

	// create a new model and parse the given rdf
	Model model = ModelFactory.createDefaultModel();
	model.read(new StringReader(input), null);

	// iterating over available resources
	ResIterator itr = model.listSubjects();
	while (itr.hasNext()){
	    Resource res = itr.next();

	    if (res.hasProperty(RDF.type, model.createResource("http://www.dispedia.de/o/Patient")) || 
		res.hasProperty(RDF.type, model.createResource("http://www.dispedia.de/o/Person"))){
		
		// create a patient object and add it to patient role
		PatientRole patientRole = CDAFactory.eINSTANCE.createPatientRole();
		Patient patient = CDAFactory.eINSTANCE.createPatient();
		patientRole.setPatient(patient);

		// name processing
		if (res.hasProperty(model.getProperty("http://schema.org/givenName")) ||
		    res.hasProperty(model.getProperty("http://schema.org/familyName"))){
		    PN name = DatatypesFactory.eINSTANCE.createPN();
		    patient.getNames().add(name);

		    if (res.hasProperty(model.getProperty("http://schema.org/givenName"))){
			String given = res.getProperty(model.getProperty("http://schema.org/givenName")).getString();
			name.addGiven(given);
		    }
		    if (res.hasProperty(model.getProperty("http://schema.org/familyName"))){
			String family = res.getProperty(model.getProperty("http://schema.org/familyName")).getString();
			name.addFamily(family);
		    }
		}

		// gender
		if (res.hasProperty(model.getProperty("http://schema.org/gender"))){
		    CE ce = DatatypesFactory.eINSTANCE.createCE();
		    patient.setAdministrativeGenderCode(ce);
		    ce.setCodeSystem("2.16.840.1.113883.5.1");

		    String gender = res.getProperty(model.getProperty("http://schema.org/gender")).getString();
		    ce.setCode(gender);
		}

		// birthdate
		if (res.hasProperty(model.getProperty("http://schema.org/birthDate"))){
		    TS birthTime = DatatypesFactory.eINSTANCE.createTS();
		    patient.setBirthTime(birthTime);

		    String birthDate = res.getProperty(model.getProperty("http://schema.org/birthDate")).getString();
		    birthDate = birthDate.replace("-", "");
		
		    birthTime.setValue(birthDate);
		}

		this.patientRoles.add(patientRole);
	    }
	}
    }

    /**
     * This method writes the CDA output which results of
     * processing the patient roles.
     * @return The CDA document
     */
    public String write(){

	// create and initialize an instance of the ContinuityOfCareDocument class
	ClinicalDocument cdaDocument = CDAFactory.eINSTANCE.createClinicalDocument();

	/* The following attributes are required by CDA/R2 spec.
	   To be complete valid the author, custodian, legalAuthenticator,
	   componentOf and component sections have to be added. But they would
	   have no useful meaning at the moment.
	*/
	//build realmCode
	CS csObj = DatatypesFactory.eINSTANCE.createCS();
	csObj.setCode("DE");
	cdaDocument.getRealmCodes().add(csObj);

	//build typeid
	InfrastructureRootTypeId typeId = CDAFactory.eINSTANCE.createInfrastructureRootTypeId();
	typeId.setRoot("2.16.840.1.113883.1.3");
	typeId.setExtension("POCD_HD000040");
	cdaDocument.setTypeId(typeId);

	// build random document id
	II id = DatatypesFactory.eINSTANCE.createII();
	String docUUID = UUID.randomUUID().toString();
	id.setRoot(docUUID);
	cdaDocument.setId(id);

	// build code
	CE code = DatatypesFactory.eINSTANCE.createCE();
	code.setCode("51897-7");
	code.setCodeSystem("2.16.840.1.113883.6.1");
	code.setCodeSystemName("LOINC");
	code.setDisplayName("Healthcare Associated Infection Report");
	cdaDocument.setCode(code);

	// build title
	ST title = DatatypesFactory.eINSTANCE.createST();
	title.addText("CDA/R2 document " + docUUID);
	cdaDocument.setTitle(title);

	// build effectiveTime
	TS etime = DatatypesFactory.eINSTANCE.createTS();
	SimpleDateFormat formatUTC = new SimpleDateFormat("yyyyMMddHHmmss");
	formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
	etime.setValue(formatUTC.format(new Date()));
	cdaDocument.setEffectiveTime(etime);

	// build confidentiality code
	CE confidentialityCode = DatatypesFactory.eINSTANCE.createCE();
	confidentialityCode.setCode("N");
	confidentialityCode.setCodeSystem("2.16.840.1.113883.5.25");
	cdaDocument.setConfidentialityCode(confidentialityCode);

	// create a patient role object and add it to the document
	for (PatientRole patientRole : this.patientRoles){
	    // build random document id
	    II patId = DatatypesFactory.eINSTANCE.createII();
	    patId.setRoot(UUID.randomUUID().toString());
	    patientRole.getIds().add(patId);

	    cdaDocument.addPatientRole(patientRole);
	}
	this.patientRoles.clear();
	
	StringWriter output = new StringWriter();
	try{
	    CDAUtil.save(cdaDocument, output);
	} catch (Exception e) {System.out.println(e);}

	return output.toString();
    }

    public static void main(String[] args){
	String rdf = "<rdf:RDF    xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'    xmlns:dispediao='http://dispedia.de/o/'    xmlns:schema='http://schema.org/' >   <rdf:Description rdf:about='http://example.com/patient_xy0'>    <schema:familyName>Levin</schema:familyName>    <schema:givenName>Henry</schema:givenName>  </rdf:Description>  <rdf:Description rdf:about='http://example.com/patient_xy1'>    <schema:gender>M</schema:gender> <schema:birthDate rdf:datatype='http://www.w3.org/2001/XMLSchema#date'>1932-09-24</schema:birthDate>   <schema:familyName>Levin2</schema:familyName>    <schema:givenName>Henry2</schema:givenName> <rdf:type rdf:resource='http://www.dispedia.de/o/Patient'/>  </rdf:Description></rdf:RDF>";
	RDF2CDA r2c = new RDF2CDA();
	r2c.read(rdf);
	System.out.println(r2c.write());
    }
}
