package net.runtime.role.evolution;

import net.runtime.role.actor.Compartment;
import net.runtime.role.helper.ClassHelper;
import net.runtime.role.helper.StringValueConverter;
import net.runtime.role.registry.RegistryManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * Created by nguonly on 1/25/16.
 */
public class EvolutionXMLParser {
    public EvolutionXMLParser(){

    }

    public void evolve(String xmlPath){
        try {
            File inputFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            RegistryManager registry = RegistryManager.getInstance();

            ////////////////////////
            // compartment tag
            ////////////////////////
            NodeList nodeList = doc.getElementsByTagName("compartment");
            for(int i=0; i<nodeList.getLength(); i++){
                //component tag
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    Element element = (Element)node;
                    String sCompId = element.getAttribute("id");
                    int compartmentId = sCompId.isEmpty()?0:Integer.parseInt(sCompId);

                    //Create compartment if it doesn't have one
                    Compartment compartment;
                    if(compartmentId<=0){
                        //compartment = Compartment.initialize(Compartment.class);
                        compartment = new Compartment();
                        compartment.activate();
                    }

                    /////////////////
                    //rebind tag
                    /////////////////
                    //System.out.println("::: bind :::");
                    NodeList bindNodes = element.getElementsByTagName("rebind");
                    for(int iBind=0; iBind<bindNodes.getLength(); iBind++){
                        Node bindNode = bindNodes.item(iBind);
                        Element bindElement = (Element) bindNode;
                        int coreId = Integer.parseInt(bindElement.getAttribute("coreId"));
                        String roleClass = bindElement.getAttribute("roleType");
//                        boolean reload = StringValueConverter.convert(bindElement.getAttribute("reload"), boolean.class);
                        System.out.format("%10s %10s %s\n", compartmentId, coreId, roleClass);

                        //Bind role to core object
                        registry.rebind(compartmentId, coreId, roleClass, null, null);

                        ///////////////////
                        //invoke tag
                        ///////////////////
                        NodeList invokeNodes = bindElement.getElementsByTagName("invoke");
                        //System.out.println("::: invoke :::: " + invokeNodes.getLength());
                        for(int iInvoke=0; iInvoke<invokeNodes.getLength(); iInvoke++){
                            Node invokeNode = invokeNodes.item(iInvoke);
                            Element invokeNodeElement = (Element)invokeNode;
                            String method = invokeNodeElement.getAttribute("method");
                            System.out.println(method);

                            //param nodes
                            NodeList paramNodes = invokeNodeElement.getElementsByTagName("param");
                            Object[] paramValues = new Object[paramNodes.getLength()];
                            Class[] paramClasses = new Class[paramNodes.getLength()];
                            for(int iParam=0; iParam<paramNodes.getLength(); iParam++){
                                Node paramNode = paramNodes.item(iParam);
                                Element paramNodeElement = (Element)paramNode;
                                paramClasses[iParam] = ClassHelper.forName(paramNodeElement.getAttribute("type"));
                                paramValues[iParam] = StringValueConverter.convert(paramNodeElement.getAttribute("value"), paramClasses[iParam]);
                            }

                            //Return type
                            Class returnType = ClassHelper.forName(invokeNodeElement.getAttribute("returnType"));
                            //Prepare method definition to invoke
                            Object core = registry.getCoreObjectMap().get(coreId);
                            //core.invoke(method, paramClasses, paramValues);
                            registry.invokeRole(null, core, method, returnType, paramClasses, paramValues);
                        }

                        //Register to watch service for bytecode change
//                        if(reload){
//                            String dir = System.getProperty("user.dir");
//                            String roleFileName = roleClass.substring(roleClass.lastIndexOf('.')+1) + ".class";
//                            String rolePath = roleClass.substring(0, roleClass.lastIndexOf('.'));
//                            Path p = Paths.get(dir + "/target/test-classes/" + rolePath.replaceAll("\\.", File.separator));
//                            FileWatcher fileWatcher = FileWatcher.getInstance();
//                            fileWatcher.register(p);
//                            fileWatcher.monitor(roleFileName);
//                        }
                    }


                    //////////////////////
                    //unbind tag
                    //////////////////////
                    NodeList unbindNodes = element.getElementsByTagName("unbind");
                    //System.out.println("::: unbind :::");
                    for(int iUnbind=0; iUnbind<unbindNodes.getLength(); iUnbind++){
                        Node unbindNode = unbindNodes.item(iUnbind);
                        Element unbindElement = (Element)unbindNode;
                        int unbindCoreId = Integer.parseInt(unbindElement.getAttribute("coreId"));
                        String unbindRole = unbindElement.getAttribute("roleType");

                        //Prepare unbind operation
                        if(unbindCoreId>0 && !unbindRole.isEmpty()) {
                            System.out.println("::: unbind coreId = " + unbindCoreId + " :::");
                            registry.unbind(unbindCoreId, unbindRole);
                        }

                    }
                }
            }
        }catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }
}
