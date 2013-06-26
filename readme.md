# CDA2RDF Converter (and the other way round)
## Installation
* Maven

  If you want to build the sources on your own you can use Maven to install the dependecies and build the project.
* standalone jar

  There is also a standalone jar `cda2rdf-0.1-jar-with-dependencies.jar` including all dependencies. This requires no installation.

## Usage
### Webservice

	`java -jar cda2rdf-0.1-jar-with-dependencies.jar --webservice`

The server runs by default on port 8080. It provides two endpoints:

* /cda2rdf

* /rdf2cda

It requires in both cases the parameter *input* with the given CDA or RDF document.

### CLI

	`java -jar cda2rdf-0.1-jar-with-dependencies.jar --cda2rdf --input="<cda_input>"`
	`java -jar cda2rdf-0.1-jar-with-dependencies.jar --rdf2cda --input="<rdf_input>"`

## Demo
You can take a look at the output by querying the server like this:

	curl --data "input=$(cat Arztbrief-4\ I01873200709201640.xml|sed -e "s/\"/\'/g")" http://localhost:8080/cda2rdf

or by calling the program directly, e.g.:

	java -jar cda2rdf-0.1-jar-with-dependencies.jar --cda2rdf --input="$(cat Arztbrief-4\ I01873200709201640.xml|sed -e "s/\"/\'/g")"
