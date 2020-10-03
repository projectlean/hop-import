package org.apache.hop.imports.kettle;

import org.apache.commons.lang3.StringUtils;
import org.apache.hop.core.database.*;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopPluginException;
import org.apache.hop.core.plugins.IPlugin;
import org.apache.hop.imports.HopImport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KettleImport extends HopImport {

    private Document doc;

    public KettleImport(){
        super();
    }

    public KettleImport(String inputFolderName, String outputFolderName) {
        super(inputFolderName, outputFolderName);
    }

    public void importKettle(){

        FilenameFilter kettleFilter = (dir, name) -> name.endsWith(".ktr") | name.endsWith("*.kjb");
        String[] kettleFileNames = inputFolder.list(kettleFilter);

        try {

            // Walk over all ktr and kjb files we received, migrate to hpl and hwf
            Stream<Path> kettleWalk = Files.walk(Paths.get(inputFolder.getAbsolutePath()));
            List<String> result = kettleWalk.map(x -> x.toString()).filter(f -> f.endsWith(".ktr") || f.endsWith(".kjb")).collect(Collectors.toList());
            result.forEach(kettleFilename -> {
                File kettleFile = new File(kettleFilename);
                importKettleFile(kettleFile);
            });

            // TODO: kettle.properties

            // TODO: shared.xml

            // TODO: carte-config files

        } catch (IOException e) {
            e.printStackTrace();
        }

        log.logBasic("We found " + kettleFileNames.length + " kettle files. ");
    }

    public void importKettleFile(File kettleFile){

        try {
            log.logBasic("Migrating file " + kettleFile.getAbsolutePath());
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(kettleFile);

            if(kettleFile.getName().endsWith(".ktr")){
                renameNode(doc.getDocumentElement(), "pipeline");
            }else if(kettleFile.getName().endsWith(".kjb")){
                renameNode(doc.getDocumentElement(), "workflow");
            }
            importDatabaseConnections(kettleFile);
            processNode(doc.getDocumentElement());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource domSource = new DOMSource(doc);
            String outFilename = kettleFile.getAbsolutePath().replaceAll(inputFolder.getAbsolutePath(), outputFolder.getAbsolutePath()).replaceAll(".ktr", ".hpl").replaceAll(".kjb", ".hwf");
            File outFile = new File(outFilename);
            String folderName = outFile.getParent();
            Files.createDirectories(Paths.get(folderName));
            StreamResult streamResult = new StreamResult(new File(outFilename));

            transformer.transform(domSource, streamResult);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    private void importDatabaseConnections(File kettleFile){

        NodeList connectionList = doc.getElementsByTagName("connection");
        for(int i = 0; i < connectionList.getLength(); i++){
            if(connectionList.item(i).getParentNode().equals(doc.getDocumentElement())){
                Element connElement = (Element)connectionList.item(i);
                String databaseType = connElement.getElementsByTagName("type").item(0).getTextContent();
                List<IPlugin> databasePluginTypes = registry.getPlugins(DatabasePluginType.class);
                IPlugin databasePlugin = registry.findPluginWithId(DatabasePluginType.class, connElement.getElementsByTagName("type").item(0).getTextContent());
//                log.logBasic("Connection " + connElement.getElementsByTagName("name") + " is of type " + databasePlugin.getPluginType().toGenericString() + ",  " + databasePlugin.getName());

                try {
                    DatabaseMeta databaseMeta = new DatabaseMeta();
                    IDatabase iDatabase = (BaseDatabaseMeta) registry.loadClass(databasePlugin);

                    if(connElement.getElementsByTagName("name").getLength() > 0){
                        databaseMeta.setName(connElement.getElementsByTagName("name").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("server").getLength() > 0){
                        iDatabase.setHostname(connElement.getElementsByTagName("server").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("access").getLength() > 0){
                        iDatabase.setAccessType(DatabaseMeta.getAccessType(connElement.getElementsByTagName("access").item(0).getTextContent()));
                    }
                    if(connElement.getElementsByTagName("database").getLength() > 0){
                        iDatabase.setDatabaseName(connElement.getElementsByTagName("database").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("port").getLength() > 0){
                        iDatabase.setPort(connElement.getElementsByTagName("port").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("username").getLength() > 0) {
                        iDatabase.setUsername(connElement.getElementsByTagName("username").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("password").getLength() > 0){
                        iDatabase.setPassword(connElement.getElementsByTagName("password").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("servername").getLength() > 0){
                        iDatabase.setServername(connElement.getElementsByTagName("servername").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("tablespace").getLength() > 0){
                        iDatabase.setDataTablespace(connElement.getElementsByTagName("tablespace").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("data_tablespace").getLength() > 0){
                        iDatabase.setDataTablespace(connElement.getElementsByTagName("data_tablespace").item(0).getTextContent());
                    }
                    if(connElement.getElementsByTagName("index_tablespace").getLength() > 0){
                        iDatabase.setIndexTablespace(connElement.getElementsByTagName("index_tablespace").item(0).getTextContent());
                    }
                    Map<String, String> attributesMap = new HashMap<String, String>();
                    NodeList connNodeList = connElement.getElementsByTagName("attributes");
                    for(int j=0; j < connNodeList.getLength() ; j++){
                        if(connNodeList.item(j).getNodeName().equals("attributes")){
                            Node attributesNode = connNodeList.item(j);
                            for(int k=0; k < attributesNode.getChildNodes().getLength(); k++){
                                Node attributeNode = attributesNode.getChildNodes().item(k);
                                for(int l=0; l < attributeNode.getChildNodes().getLength(); l++){
                                    String code = "";
                                    String attribute = "";
                                    if(attributeNode.getChildNodes().item(l).getNodeName().equals("code")){
                                        code = attributeNode.getChildNodes().item(l).getTextContent();
                                    }
                                    if(attributeNode.getChildNodes().item(l).getNodeName().equals("attribute")){
                                        attribute = attributeNode.getChildNodes().item(l).getTextContent();
                                    }
                                    attributesMap.put(code, attribute);
                                }
                            }
                        }
                    }
                    iDatabase.setAttributes(attributesMap);
                    databaseMeta.setIDatabase(iDatabase);
                    iDatabase.setPluginId(connElement.getElementsByTagName("type").item(0).getTextContent());

                    // save every connection, effectively only saving the last connection with a given name.
                    // TODO: evaluate connections, offer choices to merge or optimize connections.
                    databaseSerializer.save(databaseMeta);
                    log.logBasic("Saved connection '" + databaseMeta.getName() + "'");
                }catch (HopPluginException e) {
                    e.printStackTrace();
                }catch(HopException e){
                    e.printStackTrace();
                }catch(NullPointerException e){
                    log.logError("Exception processing connection: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        }
    }

    private void renameNode(Element element, String newElementName){
        doc.renameNode(element, null, newElementName);
    }


    private void processNode(Node node){
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);

                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                // rename Kettle elements to Hop elements
                if(KettleConst.kettleElementReplacements.containsKey(currentNode.getNodeName())){
                    renameNode((Element)currentNode, KettleConst.kettleElementReplacements.get(currentNode.getNodeName()));
                }

                if(KettleConst.kettleElementsToRemove.containsKey(currentNode.getNodeName())){
                    if(!StringUtils.isEmpty(KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()))){
                        if(currentNode.getParentNode().getNodeName().equals(KettleConst.kettleElementsToRemove.get(currentNode.getNodeName()))){
                            currentNode.getParentNode().removeChild(currentNode);
                        }
                    }else{
                        currentNode.getParentNode().removeChild(currentNode);
                    }
                }

                if(KettleConst.kettleReplaceContent.containsKey(currentNode.getTextContent())){
                    currentNode.setTextContent(KettleConst.kettleReplaceContent.get(currentNode.getTextContent()));
                }

                processNode(currentNode);
            }
            if(currentNode.getNodeType() == Node.TEXT_NODE && !StringUtils.isEmpty(currentNode.getTextContent())){
                for(Map.Entry<String, String> entry : KettleConst.kettleReplaceInContent.entrySet()){
                    if(currentNode.getTextContent().contains(entry.getKey())){
                        currentNode.setTextContent(currentNode.getTextContent().replace(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
    }
}
