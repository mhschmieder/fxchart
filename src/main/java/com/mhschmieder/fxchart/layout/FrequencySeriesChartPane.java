/*
 * MIT License
 *
 * Copyright (c) 2020, 2025, Mark Schmieder. All rights reserved.
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
 * This file is part of the FxChart Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChart Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.layout;

import com.mhschmieder.fxacousticscontrols.action.FrequencyRangeHorizontalZoomChoices;
import com.mhschmieder.fxchart.chart.ChartUtilities;
import com.mhschmieder.fxchart.chart.FrequencySeriesAxis;
import com.mhschmieder.jacoustics.CenterFrequencies;
import com.mhschmieder.jacoustics.FrequencySignalUtilities;
import com.mhschmieder.jcommons.util.ClientProperties;
import javafx.scene.chart.NumberAxis;
import org.apache.commons.math3.util.FastMath;

public abstract class FrequencySeriesChartPane extends DualAxisChartPane {

    // These are rounded default frequencies for a practical range to use in
    // many application contexts, noting that there are now many usage domains
    // where some coverage below the typical human hearing threshold of 20 Hz is
    // needed, whether to cover sound effects in movies, animal sounds, sonar,
    // or any other sound source that is not bound by the average hearing range.
    // TODO: Add setters and/or constructor parameters for more flexibility.
    public static final double DEFAULT_LOWEST_FREQUENCY_TO_DISPLAY = 15.0d;
    public static final double DEFAULT_HIGHEST_FREQUENCY_TO_DISPLAY = 20000.0d;

    // Number of dB between vertical tics (that is, the tic increment in dB).
    public static final int    DEFAULT_VERTICAL_GRID_SPACING_LEGACY = 10;
    public static final int    DEFAULT_VERTICAL_GRID_SPACING = 6;

    // Number of vertical tics above, below, and including 0 dB.
    public static final int    DEFAULT_NUMBER_OF_VERTICAL_TICS = 7;
    public static final double DEFAULT_VERTICAL_GRID_RANGE_LEGACY
            = ( DEFAULT_NUMBER_OF_VERTICAL_TICS - 1 )
            * DEFAULT_VERTICAL_GRID_SPACING_LEGACY;
    public static final double DEFAULT_VERTICAL_GRID_RANGE
            = ( DEFAULT_NUMBER_OF_VERTICAL_TICS - 1 )
            * DEFAULT_VERTICAL_GRID_SPACING;

    // Default the maximum magnitude to 120 dB, as that covers a majority of
    // prediction scenarios and thus results in the best initial conditions.
    public static final double DEFAULT_MAG_MAX_LEGACY = 120.0d;
    public static final double DEFAULT_MAG_MIN_LEGACY  = DEFAULT_MAG_MAX_LEGACY
            - DEFAULT_VERTICAL_GRID_RANGE_LEGACY;

    // Cache the number of bins used for this chart.
    // TODO: Review whether this is redundant with asking for bins.length.
    protected int              _numberOfBins;

    // Keep track of whether we are using third octave center frequencies.
    private final boolean      _useThirdOctaveCenterFrequencies;

    // Local cache of bins passed by client. No restriction on what they are.
    public double[] bins;

    // Declare global minimum and maximum vertical axis values.
    public double              _magMax;
    public double              _magMin;

    // Declare vertical axis grid spacing.
    public int                 _verticalGridSpacing;
    public double              _verticalGridRange;

    // Cache the Frequency Index Range as it varies for Third vs. Full Octave.
    private int                _startFreqIndex;
    private int                _stopFreqIndex;

    // Cache the Frequency Value Range as it varies for Third vs. Full Octave.
    private double             _lowestFrequencyToDisplay;
    private double             _highestFrequencyToDisplay;

    public FrequencySeriesChartPane( final int numberOfBins,
                                     final int maximumNumberOfDataSets,
                                     final String xUnitLabel,
                                     final String yUnitLabel,
                                     final boolean useThirdOctaveCenterFrequencies,
                                     final double[] pBins,
                                     final boolean useLimitedFrequencyRange,
                                     final ClientProperties pClientProperties ) {
        // Always call the superclass constructor first!
        super( maximumNumberOfDataSets,
                xUnitLabel,
                yUnitLabel,
                yUnitLabel,
                pClientProperties );

        // Default to center frequencies if measurement bins not provided.
        // TODO: Add setters to changes these bins later, with a re-jig of
        //  start/stop frequencies etc.
        bins =  ( pBins != null )
                ? pBins
                : CenterFrequencies.NOMINAL_THIRD_OCTAVE_CENTER_FREQUENCIES;

        _numberOfBins = numberOfBins;
        _useThirdOctaveCenterFrequencies = useThirdOctaveCenterFrequencies;

        _magMax = DEFAULT_MAG_MAX_LEGACY;
        _magMin = DEFAULT_MAG_MIN_LEGACY;

        _verticalGridSpacing = DEFAULT_VERTICAL_GRID_SPACING_LEGACY;
        _verticalGridRange = DEFAULT_VERTICAL_GRID_RANGE_LEGACY;

        try {
            initPane( useLimitedFrequencyRange );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    private void initPane( boolean useLimitedFrequencyRange ) {
        // The x-axis can be zoomed/scaled but is set to its max range.
        final double[] centerFrequencies = _useThirdOctaveCenterFrequencies
                ? CenterFrequencies.NOMINAL_THIRD_OCTAVE_CENTER_FREQUENCIES
                : CenterFrequencies.NOMINAL_FULL_OCTAVE_CENTER_FREQUENCIES;

        // Find the start and end indices for the valid data.
        // NOTE: For expediency of getting this added to the toolkit, we are
        //  initially using a default range that goes slightly beyond hearing.
        final int[] displayableFrequencyRangeIndices = FrequencySignalUtilities
                .getClampedFrequencyRangeIndices(
                        bins,
                        useLimitedFrequencyRange,
                        DEFAULT_LOWEST_FREQUENCY_TO_DISPLAY,
                        DEFAULT_HIGHEST_FREQUENCY_TO_DISPLAY );
        _startFreqIndex = displayableFrequencyRangeIndices[ 0 ];
        _stopFreqIndex = displayableFrequencyRangeIndices[ 1 ];

        _lowestFrequencyToDisplay = bins[ _startFreqIndex ];
        _highestFrequencyToDisplay = bins[ _stopFreqIndex ];

        _xAxisBottom = ChartUtilities.getFrequencySeriesAxis(
                _lowestFrequencyToDisplay,
                _highestFrequencyToDisplay,
                centerFrequencies,
                clientProperties );
        _xAxisTop = ChartUtilities.getFrequencySeriesAxis(
                _lowestFrequencyToDisplay,
                _highestFrequencyToDisplay,
                centerFrequencies,
                clientProperties );

        // If showing third octaves, the minor ticks should be at 1/12 octave
        // intervals. Otherwise, we are showing full octaves and should show
        // minor ticks at third octave intervals.
        final int numberOfMinorTickMarks = _useThirdOctaveCenterFrequencies
                ? 4
                : 3;
        _xAxisBottom.setMinorTickCount( numberOfMinorTickMarks );
        _xAxisTop.setMinorTickCount( numberOfMinorTickMarks );

        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );
        _xAxisTop.setMinorTickVisible( false );
    }

    protected double[] getDataVec( final int dataSetIndex ) {
        // Provide default behavior to avoid downstream algorithm issues.
        return null;
    }

    public final int getVerticalGridSpacing() {
        return _verticalGridSpacing;
    }

    // Special method, used mostly for Processor-only amplitude charts (e.g.)
    // to reset to default min/max range, as otherwise the Processor-only charts
    // do not center at 0 dB.
    public final void resetMinMax( final int verticalGridSpacing ) {
        _magMax = 0.5d * ( DEFAULT_NUMBER_OF_VERTICAL_TICS - 1 ) * verticalGridSpacing;
        _magMin = -_magMax;
    }

    // Set the current Frequency Range.
    public final void setFrequencyRangeHorizontalZoomIndex( final int frequencyRangeHorizontalZoomIndex ) {
        // Set the appropriate zoom level for this Frequency Range.
        switch ( frequencyRangeHorizontalZoomIndex ) {
            case FrequencyRangeHorizontalZoomChoices.ZOOM_FULL_RANGE:
                zoomFullFrequencyRange();
                break;
            case FrequencyRangeHorizontalZoomChoices.ZOOM_LOW_FREQ:
                zoomLowFrequencies();
                break;
            case FrequencyRangeHorizontalZoomChoices.ZOOM_LOW_MID_FREQ:
                zoomLowMidFrequencies();
                break;
            case FrequencyRangeHorizontalZoomChoices.ZOOM_MID_FREQ:
                zoomMidFrequencies();
                break;
            case FrequencyRangeHorizontalZoomChoices.ZOOM_MID_HIGH_FREQ:
                zoomMidHighFrequencies();
                break;
            case FrequencyRangeHorizontalZoomChoices.ZOOM_HIGH_FREQ:
                zoomHighFrequencies();
                break;
            default:
                zoomFullFrequencyRange();
                break;
        }

        // If the Data Tracker is showing, its displayed data will now be
        // inaccurate unless regenerated from scratch using the new range.
        updateDataTracking();
    }

    // NOTE: Only the derived classes know which data set indices are in use.
    public abstract void setTraceVisible( final int dataSetIndex, final boolean traceVisible );

    public void updateAmplitudeAxisRange( final int verticalGridSpacing,
                                          final double verticalGridRange ) {
        // Make sure the maximum peak is included in the plot, but preserve the
        // Vertical Grid Range. This means recalculating a new minimum for
        // purposes of limiting the displayed Dynamic Range based on the user's
        // Vertical Scale setting.
        final double limitedMin = _magMax - verticalGridRange;

        // Find the closest tic at or above the adjusted maximum, and at or
        // above the adjusted minimum (to preserve the preferred dynamic range
        // for display).
        // TODO: Verify after checking every combination of vertical scale
        //  factors, which computation of the niceYMin is most advantageous for
        //  good tic marks.
        final int niceYMax = ( int ) FastMath.ceil(
                _magMax / verticalGridSpacing )  * verticalGridSpacing;
        final int niceYMin = ( int ) FastMath.ceil(
                limitedMin  / verticalGridSpacing ) * verticalGridSpacing;
        // int niceYMin = niceYMax - ( int )FastMath.ceil( verticalGridRange );

        _yAxisBottom.setLowerBound( niceYMin );
        _yAxisBottom.setUpperBound( niceYMax );
        ( ( NumberAxis ) _yAxisBottom ).setTickUnit( verticalGridSpacing );

        // Set the number of minor ticks to be sensible for the decibels scale.
        // NOTE: The first four scale matchings are meant to match legacy apps.
        int minorTickDivisions = 1;
        switch ( verticalGridSpacing ) {
            case 1:
                minorTickDivisions = 2; // 0.5 dB subdivision
                break;
            case 2:
                minorTickDivisions = 2; // 1 dB subdivision
                break;
            case 3:
                minorTickDivisions = 3; // 1 dB subdivision
                break;
            case 6:
                minorTickDivisions = 6; // 1 dB subdivision
                break;
            case 10:
                minorTickDivisions = 5; // 2 dB subdivision
                break;
            case 12:
                minorTickDivisions = 4; // 3 dB subdivision
                break;
            case 15:
                minorTickDivisions = 5; // 3 dB subdivision
                break;
            case 20:
                minorTickDivisions = 4; // 5 dB subdivision
                break;
            case 30:
                minorTickDivisions = 5; // 6 dB subdivision
                break;
            default:
                break;
        }
        _yAxisBottom.setMinorTickCount( minorTickDivisions );

        // Cache the new vertical grid spacing.
        _verticalGridSpacing = verticalGridSpacing;
        _verticalGridRange = verticalGridRange;
    }

    protected final void updateFrequencyAxisRange(
            final double xMin,
            final double xMax,
            final boolean useThirdOctaveCenterFrequencies ) {
        // Cache the current status of chart update animation, as we need to
        // turn it off while scaling or else we get a ghost trace due to the
        // animation running on a different thread from the main data setter.
        final boolean animateChartUpdates = isAnimateChartUpdates();
        setAnimateChartUpdates( false );

        final double[] centerFrequencies = useThirdOctaveCenterFrequencies
                ? CenterFrequencies.NOMINAL_THIRD_OCTAVE_CENTER_FREQUENCIES
                : CenterFrequencies.NOMINAL_FULL_OCTAVE_CENTER_FREQUENCIES;
        ( ( FrequencySeriesAxis ) _xAxisBottom ).setCenterFrequencies(
                centerFrequencies );

        _xAxisBottom.setLowerBound( xMin );
        _xAxisBottom.setUpperBound( xMax );

        ( ( FrequencySeriesAxis ) _xAxisTop ).setCenterFrequencies(
                centerFrequencies );

        _xAxisTop.setLowerBound( xMin );
        _xAxisTop.setUpperBound( xMax );

        // If showing third octaves, the minor ticks should be at 1/12 octave
        // intervals. Otherwise, we are showing full octaves and should show
        // minor ticks at third octave intervals.
        final int numberOfMinorTickMarks = useThirdOctaveCenterFrequencies
                ? 4
                : 3;
        _xAxisBottom.setMinorTickCount( numberOfMinorTickMarks );
        _xAxisTop.setMinorTickCount( numberOfMinorTickMarks );

        // Now it is safe to conditionally re-animate chart updates.
        // NOTE: Avoid side effects by only setting when we need to.
        if ( animateChartUpdates ) {
            setAnimateChartUpdates( animateChartUpdates );
        }
    }

    // NOTE: Only the derived classes know which data set indices are in use.
    protected void updateMinMax() {}

    protected final void updateMinMax( final double[] dataVec,
                                       final boolean cumulativeMinMax,
                                       final double[] bins,
                                       final int numberOfBins,
                                       final int minimumBinIndex,
                                       final int maximumBinIndex ) {
        // Find the minimum and maximum displayed amplitudes in the data vector.
        double magMax = Double.NEGATIVE_INFINITY;
        double magMin = Double.POSITIVE_INFINITY;
        for ( int i = _startFreqIndex; i <= _stopFreqIndex; i++ ) {
            final double dataValue = dataVec[ i ];
            magMax = FastMath.max( magMax, dataValue );
            magMin = FastMath.min( magMin, dataValue );
        }

        // Replace the max/min pair or compare to the existing pair.
        _magMax = cumulativeMinMax ? FastMath.max( _magMax, magMax ) : magMax;
        _magMin = cumulativeMinMax ? FastMath.min( _magMin, magMin ) : magMin;
    }

    /**
     * This method loops through a range of Data Sets to update the min/max
     * composite Dynamic Range. Note that the list of indices is pre-vetted
     * based on criteria outside the domain of this low-level method.
     *
     * @param dataSetIndices
     *            The indices of the Data Sets to include in the min/max
     *            criteria
     */
    public final void updateMinMax( final int[] dataSetIndices,
                                    final double[] bins,
                                    final int numberOfBins,
                                    final int minimumBinIndex,
                                    final int maximumBinIndex ) {
        // Early exit if no valid data set indices provided.
        if ( ( dataSetIndices == null ) || ( dataSetIndices.length < 1 ) ) {
            return;
        }

        boolean validDataSetLoaded = false;
        for ( final int dataSetIndex : dataSetIndices ) {
            final double[] dataVec = getDataVec( dataSetIndex );
            if ( dataVec == null ) {
                continue;
            }

            // Adjust the Dynamic Range for this trace, accounting for the
            // previous Dynamic Range if computed (i.e. a previous data set in
            // the series is valid and has been loaded), and the selected divs.
            final boolean cumulativeMinMax = validDataSetLoaded;
            updateMinMax( dataVec,
                    cumulativeMinMax,
                    bins,
                    numberOfBins,
                    minimumBinIndex,
                    maximumBinIndex );

            validDataSetLoaded = true;
        }
    }

    private void zoomFullFrequencyRange() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our default non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( true );

        updateFrequencyAxisRange(
                _lowestFrequencyToDisplay,
                _highestFrequencyToDisplay,
                _useThirdOctaveCenterFrequencies );
    }

    private void zoomLowFrequencies() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );

        updateFrequencyAxisRange(
                _lowestFrequencyToDisplay,
                200.0d,
                true );
    }

    private void zoomLowMidFrequencies() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );

        updateFrequencyAxisRange(
                60.0d, 600.0d, true );
    }

    private void zoomMidFrequencies() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );

        updateFrequencyAxisRange(
                200.0d, 2000.0d, true );
    }

    private void zoomMidHighFrequencies() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );

        updateFrequencyAxisRange(
                600.0d, 6000.0d, true );
    }

    private void zoomHighFrequencies() {
        // Currently, only full zoom benefits from showing the minor ticks. This
        // is most likely due to our non-constant-Q spacing, as we get
        // unevenness if we generate twelfth octave intervals between third
        // octave major ticks -- even if we straddle them and use two formulae.
        _xAxisBottom.setMinorTickVisible( false );

        updateFrequencyAxisRange(
                2000.0d,
                _highestFrequencyToDisplay,
                true );
    }
}
