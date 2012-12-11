package edu.leipzig.dispedia;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebService extends AbstractHandler{

    private CDA2RDF cda2rdf;
    private RDF2CDA rdf2cda;
    
    public WebService(){
	this.cda2rdf = new CDA2RDF();
	this.rdf2cda = new RDF2CDA();
    }
    
    public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException
    {
	String input = request.getParameter("input");
	
	if (target.equals("/cda2rdf")){
	    if (input == null){
		response.setStatus(500);
		response.getWriter().print("Error: input parameter missing");
	    }
	    else {
		this.cda2rdf.read(input);
		String output = this.cda2rdf.write();
	    
		response.setContentType("application/rdf+xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(output);
	    }
	    baseRequest.setHandled(true);

	}
	else if (target.equals("/rdf2cda")){
	    if (input == null){
		response.setStatus(500);
		response.getWriter().print("Error: input parameter missing");
	    }
	    else {
		this.rdf2cda.read(input);
		String output = this.rdf2cda.write();
	    
		response.setContentType("text/xml;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().print(output);
	    }
	    baseRequest.setHandled(true);
	}
	else {
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}
    }

    public static void main(String[] args) throws Exception{
	Server server = new Server(8080);
	server.setHandler(new WebService());
 
	server.start();
	server.join();
    }
}
