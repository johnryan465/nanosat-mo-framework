/* ----------------------------------------------------------------------------
 * Copyright (C) 2021      European Space Agency
 *                         European Space Operations Centre
 *                         Darmstadt
 *                         Germany
 * ----------------------------------------------------------------------------
 * System                : ESA NanoSat MO Framework
 * ----------------------------------------------------------------------------
 * Licensed under European Space Agency Public License (ESA-PL) Weak Copyleft – v2.4
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
package esa.mo.nmf.apps;

import esa.mo.nmf.CloseAppListener;
import esa.mo.nmf.MonitorAndControlNMFAdapter;
import esa.mo.nmf.NMFException;
import esa.mo.nmf.commonmoadapter.SimpleDataReceivedListener;
import esa.mo.nmf.nanosatmoconnector.NanoSatMOConnectorImpl;
import esa.mo.nmf.spacemoadapter.SpaceMOApdapterImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Space2SpaceAdapter extends MonitorAndControlNMFAdapter {

    private static final Logger LOGGER = Logger.getLogger(Space2SpaceAdapter.class.getName());
    private static final String PARAMETER_NAMES = "GPS.Latitude,GPS.Longitude,GPS.Altitude,GPS.NumberOfSatellitesInView";

    // The application's NMF provider.
    private NanoSatMOConnectorImpl connector;

    // The application's NMF consumer (consuming supervisor).
    private SpaceMOApdapterImpl spaceAppSMA;

    private List<String> parametersNames = new ArrayList<>();

    public void setConnector(NanoSatMOConnectorImpl connector)
    {
        this.connector = connector;

        // Define application behavior when closed
        this.connector.setCloseAppListener(new CloseAppListener() {
            @Override
            public Boolean onClose() {
                return Space2SpaceAdapter.this.onClose();
            }
        });
    }

    public void setSpaceAppSMA(SpaceMOApdapterImpl spaceAppSMA)
    {
        this.spaceAppSMA = spaceAppSMA;
    }

    public void fetchGPSParameters() {
        LOGGER.log(Level.SEVERE, "Registering for the following parameters: " + PARAMETER_NAMES);
        parametersNames.clear();
        for (String paramName : PARAMETER_NAMES.split(",")) {
            parametersNames.add(paramName);
        }

        // Toggle the parameters generation
        try {
            spaceAppSMA.toggleParametersGeneration(parametersNames, true);
        } catch (NMFException e0) {
            LOGGER.log(Level.SEVERE, "Error toggling the parameters generation", e0);
        }

        // Receive and log the parameter values from supervisor
        SimpleDataReceivedListener parameterListener = new SimpleDataReceivedListener() {
            @Override
            public void onDataReceived(String parameterName, Serializable data) {
                if (data == null) {
                    LOGGER.log(Level.WARNING,
                            String.format("Received null value for parameter %s", parameterName));
                    return;
                }

                String dataS = data.toString();
                LOGGER.log(Level.INFO, String.format(
                        "Received value: %s = %s", parameterName, dataS));
            }
        };

        spaceAppSMA.addDataReceivedListener(parameterListener);
        LOGGER.log(Level.INFO, "Started fetching parameters");
    }


    public Boolean onClose()
    {
        boolean success = true;
        // Stop fetching data in supervisor
        try {
            spaceAppSMA.toggleParametersGeneration(parametersNames, false);
        } catch (NMFException e0) {
            success = false;
            LOGGER.log(Level.SEVERE, "Error toggling parameters generation", e0);
        }

        // Close supervisor consumer connections
        spaceAppSMA.closeConnections();

        LOGGER.log(Level.INFO, "Closed application successfully: " + success);
        return success;
    }
}
