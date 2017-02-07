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
package esa.mo.nmf.nanosatmosupervisor;

import esa.mo.nmf.nanosatmomonolithic.NanoSatMOMonolithic;
import esa.mo.nmf.NanoSatMOFrameworkProvider;
import esa.mo.helpertools.connections.ConnectionProvider;
import esa.mo.helpertools.connections.SingleConnectionDetails;
import esa.mo.helpertools.helpers.HelperMisc;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.CloseAppListener;
import esa.mo.nmf.MCRegistration;
import esa.mo.nmf.NMFException;
import static esa.mo.nmf.NanoSatMOFrameworkProvider.DYNAMIC_CHANGES_PROPERTY;
import esa.mo.platform.impl.util.PlatformServicesConsumer;
import esa.mo.sm.impl.provider.AppsLauncherProviderServiceImpl;
import esa.mo.sm.impl.util.PackageManagementBackendInterface;
import esa.mo.sm.impl.provider.PackageManagementProviderServiceImpl;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ccsds.moims.mo.mal.MALException;

/**
 * A Provider of MO services composed by COM, M&C and Platform services. Selects
 * the transport layer based on the selected values of the properties file and
 * initializes all services automatically. Provides configuration persistence,
 * therefore the last state of the configuration of the MO services will be kept
 * upon restart. Additionally, the NanoSat MO Framework implements an
 * abstraction layer over the Back-End of some MO services to facilitate the
 * monitoring of the business logic of the app using the NanoSat MO Framework.
 *
 * @author Cesar Coelho
 */
public abstract class NanoSatMOSupervisor extends NanoSatMOFrameworkProvider {

    private final PackageManagementProviderServiceImpl packageManagementService = new PackageManagementProviderServiceImpl();
    private final AppsLauncherProviderServiceImpl applicationsManagerService = new AppsLauncherProviderServiceImpl();

    /**
     * To initialize the NanoSat MO Framework with this method, it is necessary
     * to extend the MonitorAndControlAdapter adapter class. The
     * SimpleMonitorAndControlAdapter class contains a simpler interface which
     * allows sending directly parameters of the most common java types and it
     * also allows the possibility to send serializable objects.
     *
     * @param mcAdapter The adapter to connect the actions and parameters to the
     * corresponding methods and variables of a specific entity.
     * @param platformServices The Platform services consumer stubs
     * @param packageManagementBackend
     */
    public NanoSatMOSupervisor(MonitorAndControlNMFAdapter mcAdapter,
            PlatformServicesConsumer platformServices,
            PackageManagementBackendInterface packageManagementBackend) {
        ConnectionProvider.resetURILinksFile(); // Resets the providerURIs.properties file
        HelperMisc.loadPropertiesFile(); // Loads: provider.properties; settings.properties; transport.properties
        HelperMisc.setInputProcessorsProperty();
        
        // Enforce the App Name property to be HelperMisc.NMF_NMS_NAME
        System.setProperty(HelperMisc.MO_APP_NAME, HelperMisc.NMF_NMS_NAME);

        // Create provider name to be registerd on the Directory service...
        this.providerName = System.getProperty(HelperMisc.MO_APP_NAME);

        this.platformServices = platformServices;

        try {
            this.comServices.init();
            this.heartbeatService.init();
            
            // Change transport to start on both RMI and SPP
            
            this.directoryService.init(comServices);
            this.applicationsManagerService.init(comServices, directoryService);
            this.packageManagementService.init(comServices, packageManagementBackend);
            this.startMCServices(mcAdapter);
            this.initPlatformServices(comServices);
        } catch (MALException ex) {
            Logger.getLogger(NanoSatMOSupervisor.class.getName()).log(Level.SEVERE,
                    "The services could not be initialized. Perhaps there's something wrong with the Transport Layer.", ex);
            return;
        }

        // Populate the Directory service with the entries from the URIs File
        Logger.getLogger(NanoSatMOSupervisor.class.getName()).log(Level.INFO, "Populating Directory service...");
        this.directoryService.autoLoadURIsFile(NanoSatMOFrameworkProvider.NANOSAT_MO_SUPERVISOR_NAME);

        if (mcAdapter != null) {
            // Are the dynamic changes enabled?
            if ("true".equals(System.getProperty(DYNAMIC_CHANGES_PROPERTY))) {
                Logger.getLogger(NanoSatMOMonolithic.class.getName()).log(Level.INFO, 
                        "Loading previous configurations...");
                
                try {
                    this.loadConfigurations();
                } catch (NMFException ex) {
                    Logger.getLogger(NanoSatMOSupervisor.class.getName()).log(Level.SEVERE, 
                            "The configurations could not be loaded!", ex);
                }
            }

            final MCRegistration registration = new MCRegistration(comServices, mcServices.getParameterService(), 
                    mcServices.getAggregationService(), mcServices.getAlertService(), mcServices.getActionService());
            mcAdapter.initialRegistrations(registration);
        }
        
        final String primaryURI = this.directoryService.getConnection().getPrimaryConnectionDetails().getProviderURI().toString();
        
        final SingleConnectionDetails det = this.directoryService.getConnection().getSecondaryConnectionDetails();
        final String secondaryURI = (det != null) ? det.getProviderURI().toString() : null;
        this.writeCentralDirectoryServiceURI(primaryURI, secondaryURI);
        Logger.getLogger(NanoSatMOSupervisor.class.getName()).log(Level.INFO, "NanoSat MO Supervisor initialized! URI: " + primaryURI + "\n");
    }

    @Override
    public void addCloseAppListener(CloseAppListener closeAppAdapter) {
        // To do...
    }

}
