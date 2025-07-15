/**
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
 * This file is part of the FxChartToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChartToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchartoolkit
 */
package com.mhschmieder.fxcharttoolkit.layout;

import org.apache.commons.math3.util.FastMath;

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.commonstoolkit.util.SystemType;
import com.mhschmieder.fxcharttoolkit.chart.ChartUtilities;
import com.mhschmieder.fxcharttoolkit.control.ChartControlFactory;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;

/**
 * The Time Series Chart Pane is the abstract base class that serves as a
 * container for Time Series charts in JavaFX.
 * <p>
 * Class derivation from FX Charts is deprecated for a variety of legitimate
 * reasons, so we prefer composition to inheritance.
 * <p>
 * This class attempts to cover as much ground as possible without getting into
 * specifics that might vary between different usage contexts or application
 * domains. Even so, it is geared towards Signal Processing and Analysis.
 * <p>
 * The x-axis units are not mandated here. All that matters is that downstream
 * clients use consistent units and supply the measurement unit to display.
 * <p>
 * TODO: Make an enumeration of typical time units and pass as a variable, then
 *  convert to the displayed measurement unit string here vs. in the invoker?
 * <p>
 * NOTE: This class has recently been abstracted to a higher level, for greater
 *  applicability than just Impulse Responses, but it needs some testing in
 *  real-world applications to make sure further parameterization is unneeded.
 */
public abstract class TimeSeriesChartPane extends DualAxisChartPane {

    // Keep track of which Data Set is active for this chart.
    protected int  _activeDataSet;
    
    // Cache the original start and end time, as they are invariant in most
    // cases (though this may need to be revisited if newly loaded data sets are
    // different in size). This decoupled data set time ranges from the current
    // zoom range of the x-axis, so that the data sets don't need to be re-done.
    protected double _originalStartTime;
    protected double _originalEndTime;

    // Keep track of the Time Range used for Horizontal Zoom. Note that units
    // are not specified, but must be internally consistent for these values as
    // well as any values related to tick units or tick spacing.
    protected double _startTime;
    protected double _endTime;
    
    // Using the same units as the Time range, the data epsilon value gets used
    // most often in filtering overly large data sets of statistically 
    // insignificant neighboring data values, which also cleans up the line chart.
    // TODO: Rename, as the mathematics domain defines epsilon epsilon numbers as
    //  a collection of transfinite numbers?
    protected double _dataEpsilonValue;
    
    // The monotonically spaced time values for the increments of the x-axis.
    // NOTE: This may not be static in all downstream uses, so is not declared as
    //  such, but in the Impulse Response domain, most often it is of fixed size.
    protected double[] _timeIncrements;
    
    // Store the original minimum and maximum time record indices, to use for
    // data tracking and basic replacement of data sets with ones of the same size.
    // TODO: Determine whether this is ever useful in general cases vs. specifics
    //  of a particular downstream Impulse Response client of this class.
    protected int _minimumTimeRecordIndex;
    protected int _maximumTimeRecordIndex;
    
    // Minimum and maximum analysis times are integers as the time units should be
    // chosen such that this is the case. This is so that tick labels won't clip.
    // NOTE: Units must match the time range above, but negative numbers are OK.
    // TODO: Evaluate whether we should allow different bottom and top "y" units.
    public TimeSeriesChartPane( final int maximumNumberOfDataSets,
                                final String xUnitLabel,
                                final String yUnitLabel,
                                final double startTime,
                                final double endTime,
                                final double dataEpsilonValue,
                                final double[] timeIncrements,
                                final int minimumTimeRecordIndex,
                                final int maximumTimeRecordIndex,
                                final boolean isOverlayChart, 
                                final ClientProperties pClientProperties ) {
        // Always call the superclass constructor first!
        super( maximumNumberOfDataSets, 
               xUnitLabel, 
               yUnitLabel, 
               yUnitLabel, 
               pClientProperties );
        
        _originalStartTime = startTime;
        _originalEndTime = endTime;
        _startTime = startTime;
        _endTime = endTime;
        _dataEpsilonValue = dataEpsilonValue;
        
        _timeIncrements = timeIncrements;
        _minimumTimeRecordIndex = minimumTimeRecordIndex;
        _maximumTimeRecordIndex = maximumTimeRecordIndex;
        
        // Play it safe in case derived classes forget to set this initially.
        _activeDataSet = 0;

        try {
            initPane( xUnitLabel, isOverlayChart );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    private final void initPane( final String xUnitLabel, 
                                 final boolean isOverlayChart ) {
        // First, make the axes and initialize their values.
        initAxes( xUnitLabel );
        
        // Next, make the charts themselves.
        initChart( isOverlayChart );
        
        // Add the data sets, as they are needed for CSS rules at start time.
        initDataSets();

        // Take care of common chart layout, and data tracking elements.
        initChartLayout();
    }
    
    /**
     * Initializes the Line Chart shared by all subclasses, so must be invoked
     * after the axes have been instantiated. Only the axes differ by subclass,
     * and only the subclasses need to know legend side.
     * <p>
     * TODO: Pass an enumeration for whether to do a Line Chart or Area Chart,
     *  or possibly two enumerations for bottom and top charts?
     *
     * @param isOverlayChart
     *            Flag for whether this is an overlay chart used just for
     *            showing all four edges of the chart boundary, no extra data
     */
    protected void initChart( final boolean isOverlayChart ) {
        _xyChartBottom = ChartControlFactory.getAnalysisTimeSeriesLineChart( _xAxisBottom,
                                                                             _yAxisBottom,
                                                                             isOverlayChart,
                                                                             false,
                                                                             null );

        // NOTE: The top chart shouldn't show its tick marks or labels.
        // NOTE: Unfortunately, the overlay chart doesn't line up properly
        //  unless the legend settings also match, so we have to make the top
        //  legend transparent in the Overlay CSS, as we do for Chart Content.
        _xyChartTop = ChartControlFactory
                .getAnalysisTimeSeriesLineChart( _xAxisTop, _yAxisTop, true, false, null );

        // We do not currently title the IFFT charts.
        // NOTE: Layout gets screwed up by this, so best to leave null.
        // _xyChartBottom.setTitle( "" );
        // _xyChartTop.setTitle( "" );
    }
    
    private final void initAxes( final String xUnitLabel ) {
        // The x-axis tends to be the same across all implementation classes.
        makeXAxes(xUnitLabel);
        
        // The y-axis may vary quite a bit for time series charts, so delegate
        // to the concrete derived classes. ChartUtilities provides some choices.
        makeYAxes();

        // The overlay chart shouldn't show its y-axis tick marks or labels.
        // TODO: Find a way to parameterize this in lower-level logic.
        _yAxisTop.setLabel( "" );
        _yAxisTop.setTickLabelsVisible( false );
        _yAxisTop.setTickMarkVisible( false );

        // We do not want to show minor ticks for the top overlay axes.
        _xAxisTop.setMinorTickVisible( false );
        _yAxisTop.setMinorTickVisible( false );
    }
    
    /**
     * Makes the x axes for both bottom and top panes. Declared as final, as
     * the specifics of the x-axis initialization inform many class details.
     * 
     * @param xUnitLabel The label to use for the x-axis units
     */
    protected final void makeXAxes( final String xUnitLabel ) {
        // Strip the spaces from the unit label as we format in parentheses here.
        final String timeUnit = xUnitLabel.replaceAll( " ", "" ); //$NON-NLS-1$ //$NON-NLS-2$

        // The x-axis can be zoomed/scaled but is set to its max range.
        final double tickUnit = calculateTickUnit();
        _xAxisBottom = ChartUtilities.getTimeSeriesAxis( _startTime, 
                                                         _endTime,
                                                         tickUnit,
                                                         timeUnit,
                                                         true );
        _xAxisTop = ChartUtilities.getTimeSeriesAxis( _startTime,
                                                      _endTime,
                                                      tickUnit,
                                                      timeUnit,
                                                      true );
    }
    
    /**
     * Makes the y axes for both bottom and top panes. Delegated to the derived
     * classes as y-axis choices vary wildly across different use cases of time
     * series charts. For instance, Impulse Responses often use Normalized Y.
     */
    protected abstract void makeYAxes();
    
    // Calculate the tick unit using general heuristics of passed in ranges.
    // NOTE: It might be safer to round to the nearest divisor of 5 or 10.
    // TODO: Determine if we need more constructor parameters to perfect this.
    private final double calculateTickUnit() {
        final double timeLength = _endTime - _startTime;
                
        // NOTE: This divisor is based on previous usage of this abstract base
        //  class in the Impulse Response context, with an initial range that
        //  is symmetric about zero and goes to 1120 ms. Review the criteria
        //  as well as whether multiple criteria are needed for scaling.
        final double tickRatio = 32.0d;
        
        final double tickUnit = timeLength / tickRatio;
        
        return tickUnit;
    }
    
    /**
     * Adds the data sets to both charts, with initial empty data set lists.
     * We replace data set lists at run-time rather than making new data sets,
     * for performance reasons and to allow bindings, but also setting these
     * tight away helps the CSS tags get employed for initial sizing and LAF.
     * <p>
     * NOTE: Do not pre-allocate the data arrays if they can vary in size.
     */
    private final void initDataSets() {
        // Make the bottom chart data sets.
        makeBottomChartDataSets();
        
        // Make the top chart data sets.
        makeTopChartDataSets();
    }
    
    /**
     * Makes the data sets for the bottom chart, with initial empty lists.
     */
    protected abstract void makeBottomChartDataSets();
    
    /**
     * Makes the data sets for the top chart, with initial empty lists.
     */
    protected abstract void makeTopChartDataSets();

    // Query whether this component is valid or not; that is, whether it
    // contains a valid data vector or not.
    public final boolean isActiveDataSetValid() {
        return isDataSetValid( _activeDataSet );
    }

    public final void setActiveDataSet( final int dataSetIndex ) {
        _activeDataSet = dataSetIndex;
    }

    public boolean isDataSetValid( final int dataSetIndex ) {
        return ChartUtilities.isDataSetValid( _xyChartBottom, dataSetIndex );
    }
    
    public void setDataSet( final int dataSetIndex ) {
        setDataSet( _xyChartBottom, dataSetIndex );
    }

    /**
     * Replaces the specified data set with the given points.
     * <p>
     * NOTE: For reasons of tight-loop performance, it is the responsibility of
     *  the caller to pre-filter the data sets for any invalid values (e.g., for
     *  a normalized chart, values greater than or equal to absolute value of 1).
     *  In the absence of this, the first and last index can be used to limit the
     *  usable part of the data set to valid values, and in any case we prevent
     *  invalid values from generating NaN errors/exceptions.
     *
     * @param xyChart
     *            The chart (bottom or top) to set the data to
     * @param dataSetIndex
     *            The data set index.
     * @param x
     *            The X positions of the new points.
     * @param y
     *            The Y positions of the new points.
     * @param firstIndex
     *            The first point in the data set to use
     * @param lastIndex
     *            The last point in the data set to use
     */
    @Override
    public void setDataSet( final XYChart< Number, Number > xyChart,
                            final int dataSetIndex,
                            final double x[],
                            final double y[],
                            final int firstIndex,
                            final int lastIndex ) {
        final XYChart.Series< Number, Number > timeSeries = new Series<>();
        timeSeries.setName( getDataSetName( dataSetIndex ) );
        final ObservableList< Data< Number, Number > > timeSeriesData = timeSeries.getData();

        // NOTE: We have to deal with the first and last indices separately, to
        //  ensure that there is a line extending to the extrema in all cases.
        // NOTE: Time and amplitude units are determined by derived classes.
        int firstValidIndex = firstIndex;
        int lastValidIndex = firstIndex;
        for ( int i = firstIndex + 1; i < lastIndex; i++ ) {
            final double time = x[ i ];
            final double amplitude = y[ i ];

            // Due to issues with data sets of more than 20,000 nodes (e.g., an
            // IFFT usually has more than 50,000), filter for an epsilon range
            // to ignore "fill" data that represents the noise floor of the
            // original Frequency Series data. Due to derived classes adjusting
            // for Delay, we can't simply truncate all data before t=0.0 (ms).
            // For data sets that start at zero and have no negative time, this
            // might not be an issue, but this approach seems more universal.
            if ( FastMath.abs( amplitude - y[ i - 1 ] ) > _dataEpsilonValue ) {
                final Data< Number, Number > dataPoint 
                    = new XYChart.Data<>( time, amplitude );
                timeSeriesData.add( dataPoint );
                lastValidIndex = i;
                if ( firstValidIndex == firstIndex ) {
                    firstValidIndex = i;
                }
            }
        }

        // Add the fake first data point, to effectively flatten the wasteful
        // in-between zeroed data points.
        final Data< Number, Number > firstDataPoint 
            = new XYChart.Data<>( _originalStartTime, 0.0d );
        timeSeriesData.add( firstDataPoint );

        // Add the last real zero data point after the main time data, to
        // avoid anomalies in the trace's data connection line to the edge.
        final double lastInvalidEarlyTime = x[ firstValidIndex - 1 ];
        final Data< Number, Number > lastInvalidEarlyDataPoint 
            = new XYChart.Data<>( lastInvalidEarlyTime, 0.0d );
        timeSeriesData.add( lastInvalidEarlyDataPoint );

        // Add the first real zero data point after the main time data, to
        // avoid anomalies in the trace's data connection line to the edge.
        final double firstInvalidLateTime = x[ lastValidIndex + 1 ];
        final Data< Number, Number > firstInvalidLateDataPoint 
            = new XYChart.Data<>( firstInvalidLateTime, 0.0d );
        timeSeriesData.add( firstInvalidLateDataPoint );

        // Add the fake last data point, to effectively flatten the wasteful
        // in-between zeroed data points.
        final Data< Number, Number > lastDataPoint 
            = new XYChart.Data<>( _originalEndTime, 0.0d );
        timeSeriesData.add( lastDataPoint );

        // Replace the series as a bulk operation, to avoid excessive
        // callbacks on observable lists at the level of the visible GUI.
        setDataSet( xyChart, dataSetIndex, timeSeries );
    }

    /**
     * Returns the amplitude of the closest available data point to the x-axis
     * value at the click location. Normally this is only computable by the
     * producer of the data sets, but it should be uniform across all data
     * shown in this chart (though the server may vary in its time length).
     *
     * @param clickLocationXValue
     *            The x-axis value at the click location
     * @return The closest available data point, or NaN if not computable at
     *         this level
     */
    @Override
    public double getClosestDataPointToXValue( final double clickLocationXValue ) {
        // Loop on the full set of time increments. Stop before the final index
        // so that we always have two legal indices to look up for legitimate
        // data values.
        for ( int timeIndex = _minimumTimeRecordIndex; timeIndex <= _maximumTimeRecordIndex; timeIndex++ ) {
            final int timeIndex1 = timeIndex;
            final double xValue1 = _timeIncrements[ timeIndex1 ];
            final double timeDiff1 = clickLocationXValue - xValue1;

            final int timeIndex2 = timeIndex + 1;
            final double xValue2 = _timeIncrements[ timeIndex2 ];
            final double timeDiff2 = xValue2 - clickLocationXValue;

            if ( ( timeDiff1 >= 0.0d ) && ( timeDiff2 >= 0.0d ) ) {
                // The final detail of getting the amplitude of the closest
                // time index is delegated to an overridden method, as some
                // downstream clients, such as Impulse Response, may need to
                // apply special criteria, such as checking for Peak Impulse.
                return getAmplitudeOfClosestTimeIndex( clickLocationXValue,
                                                       xValue1,
                                                       timeDiff1,
                                                       xValue2,
                                                       timeDiff2 );
                
            }
        }

        // If no match is found, cue this with NaN, for downstream handling.
        return Double.NaN;
    }
    
    // The simplest implementation is just to see which point is closest to
    // the click location, of the two neighboring time indices on either side.
    public double getAmplitudeOfClosestTimeIndex( final double clickLocationXValue,
                                                  final double xValue1,
                                                  final double timeDiff1,
                                                  final double xValue2,
                                                  final double timeDiff2 ) {
        return ( timeDiff1 <= timeDiff2 ) ? xValue1 : xValue2;
    }


    public final void setTimeRange( final double startTime, 
                                    final double endTime ) {
        // Cache the new Time Range.
        _startTime = startTime;

        // Update the Time Axis Range with the newly cached Time Range.
        updateTimeAxisRange();
    }

    public final void updateTimeAxisRange() {
        // Cache the current status of chart update animation, as we need to
        // turn it off while scaling or else we get a ghost trace due to the
        // animation running on a different thread from the main data setter.
        final boolean animateChartUpdates = isAnimateChartUpdates();
        setAnimateChartUpdates( false );

        // TODO: Recalculate the tick unit to be proportional.
        final double tickUnit = calculateTickUnit();
        _xAxisBottom.setLowerBound( _startTime );
        _xAxisBottom.setUpperBound( _endTime );
        ( ( NumberAxis ) _xAxisBottom ).setTickUnit( tickUnit );

        _xAxisTop.setLowerBound( _startTime );
        _xAxisTop.setUpperBound( _endTime );
        ( ( NumberAxis ) _xAxisTop ).setTickUnit( tickUnit );

        // Now it is safe to conditionally re-animate chart updates.
        // NOTE: Avoid side effects by only setting animation when we need to.
        if ( animateChartUpdates ) {
            setAnimateChartUpdates( animateChartUpdates );
        }

        // If the Data Tracker is showing, its displayed data will now be
        // inaccurate unless regenerated from scratch using the new range.
        // NOTE: This is run inside a deferred thread, as the earlier call to
        //  reset the Tick Unit, uses a deferred thread inside FX Charts code,
        //  so we get the wrong data value for the current cursor position
        //  otherwise, as it is calculated partly based on the tick increments.
        // NOTE: There is so much thread-deferral inside FX Charts, that we
        //  have to do a double-nesting or we still don't get the right value
        //  back from the x-axis when querying from the cursor location.
        // NOTE: Due to how threading behaves on macOS, we must use three
        //  levels of nesting there, or the tracking is one change behind.
        if ( SystemType.MACOS.equals( clientProperties.systemType ) ) {
            Platform.runLater( () -> Platform
                    .runLater( () -> Platform.runLater( this::updateDataTracking ) ) );
        }
        else {
            Platform.runLater( () -> Platform.runLater( this::updateDataTracking ) );
        }
    }
}
