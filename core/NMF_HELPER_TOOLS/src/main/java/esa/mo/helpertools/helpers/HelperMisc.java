/* ----------------------------------------------------------------------------
 * Copyright (C) 2015      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under the European Space Agency Public License, Version 2.0
 * You may not use this file except in compliance with the License.
 *
 * Except as expressly set forth in this License, the Software is provided to
 * You on an "as is" basis and without warranties of any kind, including without
 * limitation merchantability, fitness for a particular purpose, absence of
 * defects or errors, accuracy or non-infringement of intellectual property rights.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * ----------------------------------------------------------------------------
 */
package esa.mo.helpertools.helpers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.MALContextFactory;
import org.ccsds.moims.mo.mal.MALElementFactory;
import org.ccsds.moims.mo.mal.MALService;
import org.ccsds.moims.mo.mal.structures.Attribute;
import org.ccsds.moims.mo.mal.structures.Element;
import org.ccsds.moims.mo.mal.structures.ElementList;
import org.ccsds.moims.mo.mal.structures.Identifier;
import org.ccsds.moims.mo.mal.structures.IdentifierList;
import org.ccsds.moims.mo.mal.structures.UOctet;
import org.ccsds.moims.mo.mal.structures.UShort;

/**
 *
 * @author Cesar Coelho
 */
public class HelperMisc {

    private static final Set LOADED_PROPERTIES = new TreeSet();
    private static final String TRANSPORT_PROPERTIES_FILE = "transport.properties";
    private static final String PROVIDER_PROPERTIES_FILE = "provider.properties";
    private static final String CONSUMER_PROPERTIES_FILE = "consumer.properties";
    private static final String SHARED_BROKER_PROPERTIES = "sharedBroker.properties";
    private static final String SHARED_BROKER_URI = "sharedBrokerURI.properties";
    private static final String PROP_TRANSPORT_ID = "helpertools.configurations.provider.transportfilepath";
    private static final String SETTINGS_PROPERTY = "esa.mo.nanosatmoframework.provider.settings";

    /**
     * Clears the list of loaded property files.
     */
    public static void clearLoadedPropertiesList() {
        LOADED_PROPERTIES.clear();
    }

    /**
     * Loads in a property file and optionally searches for a contained property
     * that contains the next file to load.
     *
     * @param configFile The name of the property file to load. May be null, in
     * which case nothing is loaded.
     * @param chainProperty The property name that contains the name of the next
     * file to load.
     * @return The loaded properties or an empty list if no file loaded.
     */
    public static Properties loadProperties(final String configFile, final String chainProperty) {
        Properties topProps = new Properties();

        if (null != configFile) {
            topProps = loadProperties(ClassLoader.getSystemClassLoader().getResource(configFile), chainProperty);
        }

        return topProps;
    }

    /**
     * Loads the properties for the consumer
     *
     * @throws java.net.MalformedURLException
     */
    public static void loadConsumerProperties() throws MalformedURLException {

        final Properties sysProps = System.getProperties();
        final File file = new File(System.getProperty("provider.properties", CONSUMER_PROPERTIES_FILE));

        if (file.exists()) {
            sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "provider.properties"));
        } else {
            Logger.getLogger(HelperMisc.class.getName()).log(Level.INFO,
                    "The file " + file.getName() + " does not exist.");

        }

        System.setProperties(sysProps);
    }

    /**
     * Loads in a property file and optionally searches for a contained property
     * that contains the next file to load.
     *
     * @param url The URL of the property file to load. May be null, in which
     * case nothing is loaded.
     * @param chainProperty The property name that contains the name of the next
     * file to load.
     * @return The loaded properties or an empty list if no file loaded.
     */
    @SuppressWarnings("unchecked")
    public static Properties loadProperties(final java.net.URL url, final String chainProperty) {
        final Properties topProps = new Properties();

//        if ((null != url) && (!LOADED_PROPERTIES.contains(url.toString()))) {
        if (null != url) {
            try {
                final Properties myProps = new Properties();
                myProps.load(url.openStream());

                final Properties subProps = loadProperties(myProps.getProperty(chainProperty), chainProperty);

                String loadingString = (LOADED_PROPERTIES.contains(url.toString())) ? "Reloading properties " + url.toString() : "Loading properties " + url.toString();

                Logger.getLogger(HelperMisc.class.getName()).log(Level.INFO, loadingString);
                topProps.putAll(subProps);
                topProps.putAll(myProps);
                LOADED_PROPERTIES.add(url.toString());
            } catch (IOException ex) {
                Logger.getLogger(HelperMisc.class.getName()).log(Level.WARNING,
                        "Failed to load properties " + url , ex);
            }
        }

        return topProps;
    }

    /**
     * Loads the provider properties file
     */
    public static void loadPropertiesFile() {
        HelperMisc.loadPropertiesFile(false);
    }

    /**
     * Loads the provider properties file and the properties for the shared
     * broker
     *
     * @param useSharedBroker Flag that determines if the properties in the
     * SHARED_BROKER_PROPERTIES file will be read
     */
    public static void loadPropertiesFile(Boolean useSharedBroker) {

        // Were they loaded already?
        String propAreLoaded = System.getProperty("PropertiesLoadedFlag");
        if (propAreLoaded != null) {
            if (System.getProperty("PropertiesLoadedFlag").equals("true")) {
                return;
            }
        }

        try {
            final java.util.Properties sysProps = System.getProperties();

            File file = new File(System.getProperty("provider.properties", PROVIDER_PROPERTIES_FILE));
            if (file.exists()) {
                sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "provider.properties"));
            }

            file = new File(System.getProperty(SETTINGS_PROPERTY, "settings.properties"));
            if (file.exists()) {
                sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "settings.properties"));
            }

            String transport_file_path = TRANSPORT_PROPERTIES_FILE;
            String trans_path_prop = System.getProperty(PROP_TRANSPORT_ID);

            if (trans_path_prop != null) {
                transport_file_path = trans_path_prop;
            }

            file = new File(System.getProperty("transport.properties", transport_file_path));
            if (file.exists()) {
                sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "transport.properties"));
            }

            if (useSharedBroker) {
                file = new File(System.getProperty("sharedBroker.properties", SHARED_BROKER_PROPERTIES));
                if (file.exists()) {
                    sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "sharedBroker.properties"));
                }

                file = new File(System.getProperty("sharedBrokerURI.properties", SHARED_BROKER_URI));
                if (file.exists()) {
                    sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), "sharedBrokerURI.properties"));
                }
            }

            System.setProperties(sysProps);
            System.setProperty("PropertiesLoadedFlag", "true");

        } catch (MalformedURLException ex) {
            Logger.getLogger(HelperMisc.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Loads in the properties of a file.
     *
     * @param propertiesFileName The name of the property file to load.
     */
    public static void loadThisPropertiesFile(final String propertiesFileName) {

        final java.util.Properties sysProps = System.getProperties();

        File file = new File(propertiesFileName);
        if (file.exists()) {
            try {
                sysProps.putAll(HelperMisc.loadProperties(file.toURI().toURL(), PROVIDER_PROPERTIES_FILE));
            } catch (MalformedURLException ex) {
                Logger.getLogger(HelperMisc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.setProperties(sysProps);

    }

    /**
     * Checks if an attribute is an Identifier, String or URI MAL data type
     *
     * @param obj The attribute
     * @return True if the object can be read as a string
     */
    public static boolean isStringAttribute(Attribute obj) {

        Integer shortFormPart = obj.getTypeShortForm();

        if (shortFormPart == 6) { // Identifier
            return true;
        }
        if (shortFormPart == 15) { // String
            return true;
        }
        if (shortFormPart == 18) { // URI
            return true;
        }

        return false;
    }

    /**
     * Generates the corresponding MAL Element List from a certain MAL Element
     *
     * @param obj The MAL Element
     * @return The MAL Element List
     */
    public static ElementList element2elementList(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        if (obj instanceof Element) {
            long l = ((Element) obj).getShortForm();
            long ll = (-((l) & 0xFFFFFFL)) & 0xFFFFFFL + (l & 0xFFFFFFFFFF000000L);

            MALElementFactory eleFact = MALContextFactory.getElementFactoryRegistry().lookupElementFactory(ll);

            if (eleFact == null) {
                Logger.getLogger(HelperMisc.class.getName()).log(Level.SEVERE, "The element could not be found in the MAL ElementFactory! The object type is: '" + obj.getClass().getSimpleName() + "'. Maybe the service Helper for this object was not initialized. Try initializing the Service Helper of this object.");
            }

            return (ElementList) eleFact.createElement();
        } else {
            return HelperAttributes.generateElementListFromJavaType(obj);
        }

    }

    /**
     * Generates the corresponding MAL Element from a certain MAL Element List
     *
     * @param obj The MAL Element List
     * @return The MAL Element
     */
    public static Element elementList2element(ElementList obj) throws Exception {
        if (obj == null) {
            return null;
        }
        long l = obj.getShortForm();
        long ll = (-((l) & 0xFFFFFFL)) & 0xFFFFFFL + (l & 0xFFFFFFFFFF000000L);

        MALElementFactory eleFact = MALContextFactory.getElementFactoryRegistry().lookupElementFactory(ll);

        if (eleFact == null) {
            Logger.getLogger(HelperMisc.class.getName()).log(Level.SEVERE, "The element could not be found in the MAL ElementFactory! The object type is: '" + obj.getClass().getSimpleName() + "'. Maybe the service Helper for this object was not initialized. Try initializing the Service Helper of this object.");
        }

        return (Element) eleFact.createElement();

    }

    /**
     * Generates the domain field in an IdentifierList from a String separated
     * by dots
     *
     * @param domainId The domain Id
     * @return The domain
     */
    public static IdentifierList domainId2domain(String domainId) {
        if (domainId == null) {
            return new IdentifierList();
        }

        IdentifierList output = new IdentifierList();
        String[] parts = domainId.split("\\.");
        for (String part : parts) {
            output.add(new Identifier(part));
        }

        return output;
    }

    /**
     * Generates the domain string from an IdentifierList
     *
     * @param domain The domain
     * @return The domain Id
     */
    public static String domain2domainId(IdentifierList domain) {
        if (domain == null) {
            return null;
        }
        if (domain.isEmpty()) {
            return "";
        }
        String domainId = "";
        for (Identifier subdomain : domain) {
            domainId += subdomain.getValue() + ".";
        }

        // Remove the last dot and return the string
        return domainId.substring(0, domainId.length() - 1);
    }

    /**
     * Finds the service name from the area, areaVersion and service numbers
     *
     * @param area Area of the service
     * @param areaVersion Area version of the service
     * @param service Service number
     * @return The name of the service
     */
    public static String serviceKey2name(UShort area, UOctet areaVersion, UShort service) {
        MALService malSer = MALContextFactory.lookupArea(
                area,
                areaVersion
        ).getServiceByNumber(service);

        return malSer.getName().toString();
    }

}