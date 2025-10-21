/*
 * MIT License
 *
 * Copyright (c) 2020, 2025 Mark Schmieder
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
 * This file is part of the FxChartT Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChart Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.layout;

import com.mhschmieder.acousticstoolkit.TimeSignalUtilities;
import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxacoustics.action.AnalysisTimeHorizontalZoomChoices;
import com.mhschmieder.fxchart.chart.ChartUtilities;
import javafx.scene.chart.XYChart;
import org.apache.commons.math3.util.FastMath;

/**
 * A generalized Chart Pane container for a normalized Impulse Response.
 * <p>
 * It is general practice to normalize Impulse Responses between { -1, 1 }
 * as the time aspect of peak impulse and impulse echoes is of most concern.
 * <p>
 * NOTE: Several of the methods simply provide examples of how to write your
 *  own overrides that include your full set of data vectors. This is why
 *  this class is declared abstract, as it won't display data in most contexts.
 */
public abstract class ImpulseResponseChartPane extends TimeSeriesChartPane {

    public static final int REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX = 0;
    
    /** The sample rate used for the Impulse Response measurement, in kHz. */
    protected double _sampleRateKhz;
    
    /**
     * Cache the main Amplitude Vector locally, to avoid hard couplings with 
     * higher logic and external business logic that might change.
     */
    public double[] _referenceImpulseResponseData;

    /** Keeps track of whether the current Impulse Response data is valid. */
    protected boolean _referenceImpulseResponseValid;

    /** Keeps track of whether the Impulse Response data should be visible. */
    protected boolean _referenceImpulseResponseVisible;

    /** 
     * A delay to be applied to the overall Impulse Response, such as the
     * inherent delay of the measurement device used to record the signal,
     * or a delay to apply for purposes of centering the Peak Impulse. */ 
    protected double _delayMs;

    public ImpulseResponseChartPane( final int maximumNumberOfDatasets,
                                     final double sampleRateKhz,
                                     final double startTimeMs,
                                     final double endTimeMs,
                                     final double dataEpsilonValue,
                                     final double[] timeIncrements,
                                     final int minimumTimeRecordIndex,
                                     final int maximumTimeRecordIndex,
                                     final boolean isOverlayChart, 
                                     final ClientProperties pClientProperties ) {
        // Always call the superclass constructor first!
        super( maximumNumberOfDatasets, 
               " ms", 
               "", 
               startTimeMs,
               endTimeMs,
               dataEpsilonValue,
               timeIncrements,
               minimumTimeRecordIndex,
               maximumTimeRecordIndex,
               isOverlayChart,
               pClientProperties );
        
        // TODO: Determine whether any monotonic data spacing works as kHz.
        _sampleRateKhz = sampleRateKhz;
        
        // Default the reference Impulse Response to invalid but visible, as
        // a flat trace at zero is preferred to blank data (it is intuitive).
        _referenceImpulseResponseValid = false;
        _referenceImpulseResponseVisible = true;
        
        // The delay can be set multiple times, so it starts at zero.
        _delayMs = 0.0d;
        
        // Make sure to set the active data set to the one defined here.
        _activeDataSet = REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX;
    }

    @Override
    protected void makeYAxes() {
        // The y-axis is fully bounded statically across predictions.
        // NOTE: The IFFT Chart is by default scaled to 1.0 or -1.0, so we
        //  never zoom vertically and must therefore make sure that the 
        //  vertical scale is always set to [ -1.0, 1.0 ] so that there is
        //  no order dependency in the code.
        _yAxisBottom = ChartUtilities.getNormalizedAmplitudeAxis();
        _yAxisTop = ChartUtilities.getNormalizedAmplitudeAxis();
    }

    @Override 
    protected void makeBottomChartDataSets() {
        // Create the series we need to show here, and set their labels.
        // NOTE: We do not pre-allocate the data arrays as they can vary in size.
        addBottomDataSet( REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX );
    }
    
    @Override 
    protected void makeTopChartDataSets() {
        // Create the series we need to show here, and set their labels.
        // NOTE: We do not pre-allocate the data arrays as they can vary in size.
        addTopDataSet( REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX );
    }

    public double[] getAmplitudeVector() {
        return _referenceImpulseResponseData;
    }

    public boolean updateImpulseResponse( final int dataSetIndex, 
                                          final double[] amplitudeVector ) {
        // Replace the selected Data Set and schedule it for redisplay.
        boolean succeeded = false;
        
        switch ( dataSetIndex ) {
        case REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX:
            // Set the Reference Impulse Response directly, shifting for delay.
            succeeded = loadImpulseResponse( amplitudeVector );
            break;
        default:
            break;
        }

        if ( !succeeded ) {
            return false;
        }

        // Display the just-loaded Data Set, with or without adjustments.
        setDataSet( _xyChartBottom, dataSetIndex );

        return true;
    }

    public boolean loadImpulseResponse( final double[] amplitudeVector ) {
        // When a new Impulse Response data vector is loaded, it must be 
        // adjusted by the current delay setting.
        _referenceImpulseResponseData = TimeSignalUtilities.adjustTimeSignal( 
                amplitudeVector,
                _delayMs,
                _sampleRateKhz );

        // If we got this far, the new Impulse Response data set is valid.
        _referenceImpulseResponseValid = true;

        return true;
    }

    public void clearImpulseResponse() {
        _referenceImpulseResponseValid = false;

        clear( _xyChartBottom, REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX );
    }
    
    public boolean isReferenceImpulseResponseValid() {
        return _referenceImpulseResponseValid;
    }

    public void setTraceVisible( final int dataSetIndex, 
                                 final boolean traceVisible ) {
        boolean validDataSetIndex = false;
        switch ( dataSetIndex ) {
        case REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX:
            _referenceImpulseResponseVisible = traceVisible;
            validDataSetIndex = true;
            break;
        default:
            break;
        }
        
        // Don't set the data set unless the data set index is valid.
        if ( validDataSetIndex ) {
            setTrace( dataSetIndex );
        }
    }
    
    public void setTrace( final int dataSetIndex ) {
        // Cache the current status of chart update animation, as we turn
        // it off while clearing to avoid getting a ghost trace due to the
        // animation running on a different thread from the data setter.
        final boolean animateChartUpdates = isAnimateChartUpdates();
        setAnimateChartUpdates( false );

        setDataSet( _xyChartBottom, dataSetIndex );

        // Now it is safe to conditionally re-animate chart updates.
        // NOTE: Avoid side effects by only setting it when we need to.
        if ( animateChartUpdates ) {
            setAnimateChartUpdates( animateChartUpdates );
        }
    }

    @Override
    public void setDataSet( final XYChart< Number, Number > xyChart, 
                            final int dataSetIndex ) {
        boolean dataSetValid;
        boolean traceVisible;
        double[] amplitudeVector;

        switch ( dataSetIndex ) {
        case REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX:
            dataSetValid = _referenceImpulseResponseValid;
            traceVisible = _referenceImpulseResponseVisible;
            amplitudeVector = _referenceImpulseResponseData;
            break;
        default:
            dataSetValid = false;
            traceVisible = false;
            amplitudeVector = null;
            break;
        }

        // If the current data set isn't valid, don't show it.
        if ( !dataSetValid || !traceVisible || ( amplitudeVector == null ) ) {
            clear( _xyChartBottom, dataSetIndex );
            return;
        }

        // Show the current trace.
        setDataSet( _xyChartBottom,
                    dataSetIndex,
                    _timeIncrements,
                    amplitudeVector,
                    _minimumTimeRecordIndex,
                    _maximumTimeRecordIndex );
    }

    @Override
    public String getDataSetName( final int dataSetIndex ) {
        String dataSetName = "";
        
        switch ( dataSetIndex ) {
        case REFERENCE_IMPULSE_RESPONSE_DATA_SET_INDEX:
            dataSetName = "Impulse Response";
            break;
        default:
            break;
        }
        
        return dataSetName;
    }

    public int getPeakTimeIndex() {
        final int peakTimeIndex = TimeSignalUtilities
                .getPeakTimeIndex( _referenceImpulseResponseData );
        return peakTimeIndex;
    }

    public double getPeakTimeMs() {
        final double peakTimeMs = TimeSignalUtilities
                .getPeakTimeMs( _referenceImpulseResponseData,
                                _timeIncrements );
        return peakTimeMs;
    }
    
    // NOTE: Not used at the moment. Investigate why (no _peakTimeMs variable).
    public void setPeakTimeMs( final double peakTimeMs ) {
        //_peakTimeMs = peakTimeMs;
    }

    public double updatePeakTimeMs() {
        final double peakTimeMs = getPeakTimeMs();
        setPeakTimeMs( peakTimeMs );
        return peakTimeMs;
    }

    /*
     * Applies additional criteria to determine the closest data point to the
     * click location, so that Peak Impulse takes precedence over closest
     * match. This is because it is difficult to pick the peak impulse as it
     * is instantaneous and the overall data set could be large, tightly spaced.
     */
    @Override
    public double getAmplitudeOfClosestTimeIndex( final double clickLocationXValue,
                                                  final double xValue1,
                                                  final double timeDiff1,
                                                  final double xValue2,
                                                  final double timeDiff2 ) {
        final double peakTimeMs = getPeakTimeMs();
        final double diff3 = FastMath.abs( peakTimeMs - clickLocationXValue );

        // Due to low resolution of mouse tracking, we need to do some tricks
        // to see if we are near the Peak Impulse so that it can be tracked
        // and displayed. This doesn't cover echoes though.
        
        // NOTE: We calculate a fudge factor to account for the differential
        //  of the resolution of mouse clicks vs. the data resolution, but this
        //  may be subject to screen resolution, specific mouse models, OS and
        //  Java updates, and whether the Impulse Response chart is zoomed.
        final double mouseClickResolutionMs = ( _endTime - _startTime ) / 15000.0d;
        final double sampleIncrementMs = 1.0d / _sampleRateKhz;
        final double timeEstimateAccuracyMs = mouseClickResolutionMs / sampleIncrementMs;
        
        final double diff4 = diff3 - timeEstimateAccuracyMs;

        if ( timeDiff1 <= timeDiff2 ) {
            return ( diff4 <= timeDiff1 ) ? peakTimeMs : xValue1;
        }
        
        return ( diff4 <= timeDiff2 ) ? peakTimeMs : xValue2;
    }

    public double getDelayMs() {
        return _delayMs;
    }
    
    public void setDelayMs( final double delayMs ) {
        // Account for the already existent delay (which may be zero).
        final double delayOffsetMs = delayMs - _delayMs;

        // Adjust the reference Impulse Response by the new delay offset.
        _referenceImpulseResponseData = TimeSignalUtilities
                .adjustTimeSignal( 
                        _referenceImpulseResponseData,
                        delayOffsetMs,
                        _sampleRateKhz );

        // Cache the new delay.
        _delayMs = delayMs;
    }
    
    /**
     * Sets the Analysis Time Index, assuming monotonically spaced units.
     * <p>
     * This also adjusts the overall Time Window to accommodate the new index.
     * 
     * @param analysisTimeIndex The Analysis Time to apply to the Time Window
     */
    public final void setAnalysisTimeIndex( final int analysisTimeIndex ) {
        // Get the Analysis Time Edge from the lookup index.
        final int analysisTimeEdgeMs = AnalysisTimeHorizontalZoomChoices
                .getAnalysisTimeEdgeMs( analysisTimeIndex );
        setAnalysisTimeEdgeMs( analysisTimeEdgeMs );
    }

    /**
     * Sets the Analysis Time Edge (ms), assuming monotonically spaced units.
     * <p>
     * This also adjusts the overall Time Window to accommodate the new edge.
     * 
     * @param analysisTimeEdgeMs The Analysis Edge to apply to the Time Window
     */
   public final void setAnalysisTimeEdgeMs( final double analysisTimeEdgeMs ) {
        // Set the Analysis Time Window symmetrically centered at zero using
        // both the negative and positive sign of the Analysis Time Edge.
        final double startTimeMs = -analysisTimeEdgeMs;
        final double endTimeMs = analysisTimeEdgeMs;
        setTimeRange( startTimeMs, endTimeMs );
    }
}
