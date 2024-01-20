/**
 * MIT License
 *
 * Copyright (c) 2020, 024 Mark Schmieder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is part of the FxChartToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChartToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxcharttoolkit
 */
package com.mhschmieder.fxcharttoolkit.net;

import java.net.HttpURLConnection;

import com.mhschmieder.acousticstoolkit.FrequencyRange;
import com.mhschmieder.acousticstoolkit.RelativeBandwidth;
import com.mhschmieder.commonstoolkit.net.DataRequestParameters;
import com.mhschmieder.commonstoolkit.security.LoginCredentials;
import com.mhschmieder.commonstoolkit.util.DataUpdateType;

/**
 * This class holds the semi-volatile parameters unique to each data request.
 * <p>
 * NOTE: This approach sets us up nicely for switching to JSON vs. XML.
 */
public final class PolarDataRequestParameters extends DataRequestParameters {

    /** 
     * Cache the Acoustic Source Model to use for the prediction. 
     * <p>
     * NOTE: This should be just the model, not a full file path. This is just
     *  an example of a simple client/server call that requests polar response
     *  data from a server that is capable of producing polar data grids from
     *  a simple parameter set, whether the Acoustic Source Model represents a
     *  database key or a filename (sans file path in formation). In this
     *  context, the Acoustic Source Model should match server expectations
     *  and therefore may not match what is presented to the user in the GUI.
     *  It is up to downstream clients to write mappers to pass to this class.
     */
    private final String acousticSourceModel;

    /**
     * Declare a structure to hold the most recent Frequency Range used for
     * polar response data requests.
     */
    private final FrequencyRange frequencyRange;

    public PolarDataRequestParameters( final LoginCredentials pLoginCredentials,
                                       final String pAcousticSourceModel,
                                       final FrequencyRange pFrequencyRange ) {
        // Always call the super-constructor first!
        super( "Polar Response",
               DataUpdateType.FULL_UPDATE,
               pLoginCredentials );
        
        acousticSourceModel = pAcousticSourceModel;
        frequencyRange = pFrequencyRange;
    }

    /**
     * Adds data request properties to the HTTP Request.
     * <p>
     * NOTE: Polar Response update requests are trivial enough to tag a few 
     *  custom HTTP parameters to the URL rather than attaching a full file.
     * 
     * @param httpURLConnection The HTTP URL Connection for the Request
     */
    @Override 
    public void addDataRequestProperties( final HttpURLConnection httpURLConnection ) {
        try {
            // Load the frequency range parameters immediately, in case the user
            // changes them while the data request is processing.
            final RelativeBandwidth relativeBandwidth = frequencyRange.getRelativeBandwidth();
            final int octaveDivider = relativeBandwidth.toOctaveDivider();
            final double centerFrequency = frequencyRange.getCenterFrequency();

            httpURLConnection.setRequestProperty( "acousticSourceModel", acousticSourceModel );
            httpURLConnection.setRequestProperty( "octaveDivider",
                                                  Integer.toString( octaveDivider ) );
            httpURLConnection.setRequestProperty( "centerFrequency",
                                                  Double.toString( centerFrequency ) );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }
    }
}
