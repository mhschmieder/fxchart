/**
 * MIT License
 *
 * Copyright (c) 2020, 2024 Mark Schmieder
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
package com.mhschmieder.fxcharttoolkit.control;

import java.text.NumberFormat;

import com.mhschmieder.acousticstoolkit.FrequencyRange;
import com.mhschmieder.acousticstoolkit.RelativeBandwidth;
import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxacousticsgui.control.FrequencyRangeControls;
import com.mhschmieder.fxguitoolkit.control.TextSelector;

import javafx.scene.control.ToolBar;

public final class PolarResponseToolBar extends ToolBar {

    // Declare tool bar buttons for shortcuts, etc.
    public TextSelector acousticSourceModelSelector;
    public FrequencyRangeControls frequencyRangeControls;

    // Fully qualified constructor, which needs the Acoustic Source Model
    // Selector passed in, as it is dealt with generically in this GUI
    // framework for Polar Response charting, so that any acoustic source
    // category and drop-list is allowed as the data to display is the same.
    public PolarResponseToolBar( final NumberFormat numberFormat,
                                 final ClientProperties pClientProperties,
                                 final TextSelector pAcousticSourceModelSelector,
                                 final boolean useExtendedRange ) {
        // Always call the superclass constructor first!
        super();
        
        acousticSourceModelSelector = pAcousticSourceModelSelector;

        try {
            initToolBar( numberFormat, pClientProperties, useExtendedRange );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    public String getCenterFrequency() {
        return frequencyRangeControls.getCenterFrequency();
    }

    public String getAcousticalSourceModel() {
        // return acousticSourceModelSelector.getTextValue();
        return acousticSourceModelSelector.getValue();
    }

    public String getOctaveRange() {
        return frequencyRangeControls.getOctaveRange();
    }

    public RelativeBandwidth getRelativeBandwidth() {
        return frequencyRangeControls.getRelativeBandwidth();
    }

    private void initToolBar( final NumberFormat numberFormat,
                              final ClientProperties pClientProperties,
                              final boolean useExtendedRange ) {
        final int startIndexForOneOctave = useExtendedRange ? 4 : 5;
        final int startIndexForThirdOctave = useExtendedRange ? 10 : 13;
        frequencyRangeControls = new FrequencyRangeControls( numberFormat, 
                                                             pClientProperties, 
                                                             false,
                                                             useExtendedRange,
                                                             startIndexForOneOctave,
                                                             startIndexForThirdOctave );

        // Add all the Nodes to the Tool Bar.
        getItems().addAll( acousticSourceModelSelector,
                           frequencyRangeControls._relativeBandwidthSelector,
                           frequencyRangeControls._octaveRangeSelector,
                           frequencyRangeControls._centerFrequencySelector );

        // As ControlsFX overrides some settings of the regular ComboBox in its
        // SearchableComboBox, we need to assert a uniform height within the
        // Tool Bar. Otherwise, the Searchable Filter field makes it too tall.
        acousticSourceModelSelector.maxHeightProperty()
                .bind( frequencyRangeControls._relativeBandwidthSelector.heightProperty() );
    }

    public void setCenterFrequency( final String sOctaveRange, 
                                    final double centerFrequency ) {
        frequencyRangeControls.setCenterFrequency( sOctaveRange, centerFrequency );
    }

    public void setAcousticSourceModel( final String acousticSourceModel ) {
        // acousticSourceModelSelector.setTextValue( acousticSourceModel );
        acousticSourceModelSelector.setValue( acousticSourceModel );
    }

    public void setOctaveRange( final String sOctaveRange ) {
        frequencyRangeControls.setOctaveRange( sOctaveRange );
    }

    public void setRelativeBandwidth( final RelativeBandwidth relativeBandwidth ) {
        frequencyRangeControls.setRelativeBandwidth( relativeBandwidth );
    }

    public void updateCenterFrequencyForBandwidthAndOctave( final RelativeBandwidth relativeBandwidth,
                                                            final String sOctaveRange,
                                                            final double centerFrequency,
                                                            final boolean preserveSelection ) {
        frequencyRangeControls.updateCenterFrequencyForBandwidthAndOctave( relativeBandwidth,
                                                                           sOctaveRange,
                                                                           centerFrequency,
                                                                           preserveSelection );
    }

    public void updateFrequencyRange( final FrequencyRange frequencyRange ) {
        frequencyRangeControls.updateFrequencyRange( frequencyRange );
    }

    public void updateOctaveRangeForBandwidthAndFrequency( final RelativeBandwidth relativeBandwidth,
                                                           final String sOctaveRange,
                                                           final double centerFrequency ) {
        frequencyRangeControls.updateOctaveRangeForBandwidthAndFrequency( relativeBandwidth,
                                                                          sOctaveRange,
                                                                          centerFrequency );
    }
}