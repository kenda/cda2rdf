package edu.leipzig.dispedia;

import java.lang.ArrayIndexOutOfBoundsException;

import org.eclipse.jetty.server.Server;

public class CLI{

    public static void main(String[] args) throws Exception{
	for (int i = 0; i < args.length; i++){
	    if(args[i].equals("--webservice")){
                System.out.println("Starting webservice on port 8080.");
                Server server = new Server(8080);
                server.setHandler(new WebService());
 
                server.start();
                server.join();
            }
	    else if (args[i].equals("--cda2rdf")){
		try{
		    if(args[i+1].startsWith("--input")){
			String input = args[i+1].replace("--input=", "");
			CDA2RDF cda2rdf = new CDA2RDF();
			cda2rdf.read(input);
		
			String output = cda2rdf.write();
			System.out.println(output);

		    }
		    else
			System.out.println("Wrong parameter given.");

		} catch(ArrayIndexOutOfBoundsException e){
		    System.out.println("No input given.");
		}
	    }
	    else if (args[i].equals("--rdf2cda")){
		try{
		    if(args[i+1].startsWith("--input")){
			String input = args[i+1].replace("--input=", "");
			RDF2CDA rdf2cda = new RDF2CDA();
			rdf2cda.read(input);
		
			String output = rdf2cda.write();
			System.out.println(output);
		    }
		    else
			System.out.println("Wrong parameter given.");

		} catch(ArrayIndexOutOfBoundsException e){
		    System.out.println("No input given.");
		}	
	    }
	    else if (!args[i].startsWith("--input")){
		System.out.println("Usage: \n" +
				   " java -jar program.jar --webservice \t\t\t Start the webservice on port 8080\n" +
				   " java -jar program.jar --cda2rdf --input=<cda_input> \t Convert CDA document to RDF\n" +
				   " java -jar program.jar --rdf2cda --input=<rdf_input> \t Convert RDF document to CDA");
	    }
	}
    }
}
