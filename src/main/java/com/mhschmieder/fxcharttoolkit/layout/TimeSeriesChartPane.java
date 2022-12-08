/**
 * MIT License
 *
 * Copyright (c) 2020, 2022 Mark Schmieder
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

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.commonstoolkit.util.SystemType;
import com.mhschmieder.fxcharttoolkit.chart.ChartUtilities;

import javafx.application.Platform;
import javafx.scene.chart.NumberAxis;

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
 * NOTE: This class has recently been abstracted to a higher level, for greater
 *  applicability than just Impulse Responses, but it needs some testing in
 *  real-world applications to make sure further parameterization is unneeded.
 */
public abstract class TimeSeriesChartPane extends DualAxisChartPane {

    // Keep track of which Data Set is active for this chart.
    protected int  _activeDataSet;

    // Keep track of the Time Range used for Horizontal Zoom. Note that units
    // are not specified, but must be internally consistent for these values as
    // well as any values related to tick units or tick spacing.
    protected double _startTime;
    protected double _endTime;
    
    // Minimum and maximum analysis times are integers as the time units should be
    // chosen such that this is the case. This is so that tick labels won't clip.
    // NOTE: Units must match the time range above, but negative numbers are OK.
    // TODO: Evaluate whether we should allow different bottom and top "y" units.
    public TimeSeriesChartPane( final int maximumNumberOfDataSets,
                                final String xUnitLabel,
                                final String yUnitLabel,
                                final double startTime,
                                final double endTime,
                                final ClientProperties pClientProperties ) {
        // Always call the superclass constructor first!
        super( maximumNumberOfDataSets, 
               xUnitLabel, 
               yUnitLabel, 
               yUnitLabel, 
               pClientProperties );

        try {
            initPane(xUnitLabel);
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    private final void initPane(final String xUnitLabel) {
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

    // Query whether this component is valid or not; that is, whether it
    // contains a valid data vector or not.
    public final boolean isDataSetValid() {
        return ChartUtilities.isDataSetValid( _xyChartBottom, _activeDataSet );
    }

    public final void setActiveDataSet( final int dataSetIndex ) {
        _activeDataSet = dataSetIndex;
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
