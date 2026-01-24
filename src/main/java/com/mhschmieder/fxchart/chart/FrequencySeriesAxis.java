/*
 * MIT License
 *
 * Copyright (c) 2020, 2026 Mark Schmieder. All rights reserved.
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
 * You should have received a copy of the MIT License along with the FxChart
 * Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.chart;

import com.mhschmieder.jcommons.util.ClientProperties;
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayList;
import java.util.List;

public class FrequencySeriesAxis extends LogarithmicAxis {

    protected double[] _centerFrequencies;

    public FrequencySeriesAxis( final double lowerBound,
                                final double upperBound,
                                final double[] centerFrequencies,
                                final ClientProperties pClientProperties ) {
        // Always call the superclass constructor first!
        super( lowerBound, upperBound, pClientProperties );

        _centerFrequencies = centerFrequencies;

        setLabel( "Frequency (Hz)" ); //$NON-NLS-1$
    }

    /**
     * This method is used to get the list of minor tick mark positions to
     * display on the axis. This definition is based on the number of minor
     * ticks and the logarithmic formula.
     */
    @Override
    protected List< Number > calculateMinorTickMarks() {
        final List< Number > minorTickMarksPositions = new ArrayList<>();

        final Number[] range = getRange();
        if ( range != null ) {
            final Number lowerBound = range[ 0 ];
            final Number upperBound = range[ 1 ];
            final double lowerBoundValue = lowerBound.doubleValue();
            final double upperBoundValue = upperBound.doubleValue();

            // NOTE: This refers to the octave divider vs. number of ticks.
            // NOTE: As we don't want to redundantly display the major ticks on
            // either side of the range as minor ticks, we adjust the count.
            final int minorTickMarkCount = getMinorTickCount();
            final int minorTickMarkCountAdjusted = minorTickMarkCount - 1;

            // NOTE: This is more performant than calculating this inside the
            // tight loop using the minor tick number as the numerator instead
            // of just the inverse of the minor tick mark count. In that case we
            // assign a new variable each time as a ratio of the major tick's
            // center frequency using the numerator/denominator formula vs.
            // applying a cached ratio to the previous minor tick's frequency.
            final double fractionalOctaveRatio = FastMath.pow( 2.0d, 1.0d / minorTickMarkCount );
            for ( final double centerFrequency : _centerFrequencies ) {
                if ( !Double.isNaN( centerFrequency ) && ( centerFrequency >= lowerBoundValue )
                        && ( centerFrequency <= upperBoundValue ) ) {
                    double closestFractionalOctave = centerFrequency;
                    for ( int minorTickNumber =
                                              1; minorTickNumber <= minorTickMarkCountAdjusted; minorTickNumber++ ) {
                        closestFractionalOctave *= fractionalOctaveRatio;
                        minorTickMarksPositions.add( closestFractionalOctave );
                    }
                }
            }
        }

        return minorTickMarksPositions;
    }

    /**
     * This method is used to calculate a list of all the data values for each
     * tick mark in range, represented by the second parameter. We explicitly
     * cache the desired major tick center frequencies as references in advance.
     */
    @Override
    protected List< Number > calculateTickValues( final double length, final Object range ) {
        final List< Number > tickValues = new ArrayList<>();

        if ( range != null ) {
            final Number lowerBound = ( ( Number[] ) range )[ 0 ];
            final Number upperBound = ( ( Number[] ) range )[ 1 ];
            final double lowerBoundValue = lowerBound.doubleValue();
            final double upperBoundValue = upperBound.doubleValue();

            for ( final double centerFrequency : _centerFrequencies ) {
                if ( !Double.isNaN( centerFrequency ) && ( centerFrequency >= lowerBoundValue )
                        && ( centerFrequency <= upperBoundValue ) ) {
                    tickValues.add( centerFrequency );
                }
            }
        }

        return tickValues;
    }

    // This method is only used to convert the number value to a string that
    // will be displayed under the tick mark. Here we use a number formatter, to
    // make sure the label is localized, but also deal with kHz vs. Hz.
    @SuppressWarnings("nls")
    @Override
    protected String getTickMarkLabel( final Number value ) {
        final double frequencyHz = value.doubleValue();

        final String formattedFrequency = ( frequencyHz >= 1000d )
            ? _numberFormat.format( 0.001d * frequencyHz ) + "k"
            : _numberFormat.format( frequencyHz );

        return formattedFrequency;
    }

    /**
     * This method sets the set of center frequencies to use at the major tick
     * marks. Note that it is not required that they be evenly spaced, but
     * usually they will be.
     *
     * @param centerFrequencies
     *            The center frequencies to use at the major tick marks
     */
    public final void setCenterFrequencies( final double[] centerFrequencies ) {
        _centerFrequencies = centerFrequencies;
    }

}
