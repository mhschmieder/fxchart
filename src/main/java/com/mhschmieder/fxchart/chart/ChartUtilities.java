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
 * This file is part of the FxChart Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChart Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchar
 */
package com.mhschmieder.fxchart.chart;

import com.mhschmieder.fxchart.layout.ChartLegend;
import com.mhschmieder.fxgui.util.GuiUtilities;
import com.mhschmieder.jcommons.lang.StringConstants;
import com.mhschmieder.jcommons.text.TextUtilities;
import com.mhschmieder.jcommons.util.ClientProperties;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import org.apache.commons.math3.util.FastMath;

import java.text.NumberFormat;

/**
 * This is a utility class for common chart methods, agnostic to chart type.
 */
public final class ChartUtilities {

    /**
     * The default constructor is disabled, as this is a static utilities class.
     */
    private ChartUtilities() {}
    
    /**
     * A method to find the lower bound of an axis given its upper bound, long 
     * edge dimension, and a conversion from its dimension to local coordinates.
     * <p>
     * As long as you treat the display's x-axis separately from its y-axis,
     * this method should apply to any arbitrary local units, even when they
     * are different both in type and in scale for the two axes.
     *
     * @param dimensionPx
     *            The long dimension of the axis in terms of its layout bounds
     * @param upperBound
     *            The maximum value the axis may represent in local coordinates
     * @param displayToLocalScaleFactor
     *            The multiplier to convert from display coordinates to local
     *            coordinates
     * @return The lower bound for the axis
     */
    public static double calculateLowerBound( final double dimensionPx,
                                              final double upperBound,
                                              final double displayToLocalScaleFactor ) {
        // Check edge cases.
        if ( ( displayToLocalScaleFactor <= 0.0d ) || ( dimensionPx <= 0.0d )
                || ( upperBound >= Double.POSITIVE_INFINITY ) ) {
            return 0.0d;
        }

        final double range = dimensionPx * displayToLocalScaleFactor;
        final double lowerBound = upperBound - range;

        return lowerBound;
    }

    /**
     * A method to find the upper bound of an axis given its lower bound, long 
     * edge dimension, and a conversion from its dimension to local coordinates.
     * <p>
     * As long as you treat the display's x-axis separately from its y-axis,
     * this method should apply to any arbitrary local units, even when they
     * are different both in type and in scale for the two axes.
     *
     * @param dimensionPx
     *            The long dimension of the axis in terms of its layout bounds
     * @param lowerBound
     *            The minimum value the axis may represent in local coordinates
     * @param displayToLocalScaleFactor
     *            The multiplier to convert from display coordinates to local
     *            coordinates
     * @return The upper bound for the axis
     */
    public static double calculateUpperBound( final double dimensionPx,
                                              final double lowerBound,
                                              final double displayToLocalScaleFactor ) {
        // Check edge cases.
        if ( ( displayToLocalScaleFactor <= 0.0d ) || ( dimensionPx <= 0.0d )
                || ( lowerBound <= Double.NEGATIVE_INFINITY ) ) {
            return 0.0d;
        }

        final double range = dimensionPx * displayToLocalScaleFactor;
        final double upperBound = lowerBound + range;

        return upperBound;
    }

    // Apply standardized attributes to all number-based charts.
    public static void applyNumberChartAttributes( final XYChart< Number, Number > chart,
                                                   final boolean isOverlayChart,
                                                   final boolean showLegend,
                                                   final Side legendSide ) {
        chart.setLegendVisible( showLegend );
        if ( showLegend ) {
            if ( legendSide != null ) {
                chart.setLegendSide( legendSide );
            }
        }

        // It is safer to turn off animation while the chart is initializing.
        chart.setAnimated( false );

        // NOTE: The overlay attribute could be for dual axis charts or for
        // charts with the same y-axis data range, but the underlying assumption
        // is a shared x-axis for all charts; only the bottom chart shows all.
        chart.setAlternativeRowFillVisible( !isOverlayChart );
        chart.setAlternativeColumnFillVisible( !isOverlayChart );
        chart.setHorizontalGridLinesVisible( !isOverlayChart );
        chart.setVerticalGridLinesVisible( !isOverlayChart );

        // The overlay chart shouldn't show its x-axis tick marks or labels.
        if ( isOverlayChart ) {
            final Axis< Number > xAxis = chart.getXAxis();
            xAxis.setLabel( "" );
            xAxis.setTickLabelsVisible( false );
            xAxis.setTickMarkVisible( false );

            GuiUtilities.addStylesheetAsJarResource( chart, "/css/overlay-chart.css" );
        }
    }

    public static NumberAxis getFrequencyDomainAmplitudeAxis( final double lowerBound,
                                                              final double upperBound,
                                                              final double tickUnit ) {
        final NumberAxis frequencyAmplitudeAxis =
                                                new NumberAxis( lowerBound, upperBound, tickUnit );
        frequencyAmplitudeAxis.setLabel( "Amplitude (dB)" ); //$NON-NLS-1$

        frequencyAmplitudeAxis
                .setTickLabelFormatter( new NumberAxis.DefaultFormatter( frequencyAmplitudeAxis ) );

        return frequencyAmplitudeAxis;
    }

    public static NumberAxis getFrequencyDomainGainAxis( final double lowerBound,
                                                         final double upperBound,
                                                         final double tickUnit ) {
        final NumberAxis frequencyGainAxis = new NumberAxis( lowerBound, upperBound, tickUnit );
        frequencyGainAxis.setLabel( "Gain (dB)" ); //$NON-NLS-1$

        frequencyGainAxis
                .setTickLabelFormatter( new NumberAxis.DefaultFormatter( frequencyGainAxis ) );

        return frequencyGainAxis;
    }

    public static NumberAxis getFrequencyDomainPhaseAxis( final double tickUnit,
                                                          final int minorTickCount ) {
        final NumberAxis frequencyPhaseAxis = new NumberAxis( -180d, 180d, tickUnit );

        frequencyPhaseAxis.setTickMarkVisible( true );
        frequencyPhaseAxis.setTickLabelsVisible( true );
        frequencyPhaseAxis.setMinorTickCount( minorTickCount );
        frequencyPhaseAxis.setMinorTickVisible( true );

        frequencyPhaseAxis.setLabel( "Phase (degrees)" ); //$NON-NLS-1$

        // NOTE: We might have to support other angle formats at some point.
        frequencyPhaseAxis
                .setTickLabelFormatter( new NumberAxis.DefaultFormatter( frequencyPhaseAxis,
                                                                         null,
                                                                         StringConstants.DEGREES_SYMBOL ) );

        return frequencyPhaseAxis;
    }

    @SuppressWarnings("nls")
    public static NumberAxis getFrequencyDomainSplAxis( final double lowerBound,
                                                        final double upperBound,
                                                        final double tickUnit ) {
        final NumberAxis frequencySplAxis = new NumberAxis( lowerBound, upperBound, tickUnit );
        frequencySplAxis.setLabel( "SPL (dBSPL)" );

        frequencySplAxis
                .setTickLabelFormatter( new NumberAxis.DefaultFormatter( frequencySplAxis ) );

        return frequencySplAxis;

    }

    public static NumberAxis getNormalizedAmplitudeAxis() {
        final NumberAxis normalizedAmplitudeAxis = new NumberAxis( -1d, 1.0d, 0.25d );
        normalizedAmplitudeAxis.setLabel( "Amplitude (Normalized)" ); //$NON-NLS-1$

        return normalizedAmplitudeAxis;
    }

    public static FrequencySeriesAxis getFrequencySeriesAxis( final double lowerBound,
                                                              final double upperBound,
                                                              final double[] centerFrequencies,
                                                              final ClientProperties pClientProperties ) {
        final FrequencySeriesAxis frequencySeriesAxis =
                                                      new FrequencySeriesAxis( FastMath
                                                              .round( lowerBound ),
                                                              FastMath.round( upperBound ),
                                                                               centerFrequencies,
                                                                               pClientProperties );

        return frequencySeriesAxis;
    }

    public static NumberAxis getTimeSeriesAxis( final double lowerBound,
                                                final double upperBound,
                                                final double tickUnit,
                                                final String timeUnit,
                                                final boolean rotateTickLabels ) {
        final NumberAxis timeSeriesAxis = new NumberAxis( lowerBound, upperBound, tickUnit );
        timeSeriesAxis.setLabel( "Time (" + timeUnit + ")" ); //$NON-NLS-1$ //$NON-NLS-2$

        // Conditionally use rotated tick labels, as they tend to cram into each
        // other due to most time increments being more than just a couple of
        // digits. It is up to the caller to decide if time scale requires this.
        // NOTE: We experimented with tagging the unit string to the tick
        //  labels, but this made them harder to read (at least when rotated).
        if ( rotateTickLabels ) {
            timeSeriesAxis.setTickLabelRotation( 90d );
        }

        return timeSeriesAxis;
    }

    public static NumberAxis getSplAxis( final double lowerBound,
                                         final double upperBound,
                                         final double tickUnit ) {
        final NumberAxis splAxis = new NumberAxis( lowerBound, upperBound, tickUnit );

        // Can't auto-range as there are no actual data sets to plot in an SPL
        // Raster Image overlay for a Cartesian Space graphics canvas, and minor
        // ticks are not wanted for most SPL-specific features either.
        splAxis.setAutoRanging( false );
        splAxis.setMinorTickVisible( false );

        splAxis.setTickLabelFormatter( new NumberAxis.DefaultFormatter( splAxis ) );

        return splAxis;
    }

    public static String getDataPointValue( final double xValueShared,
                                            final double yValueBottom,
                                            final double yValueTop,
                                            final NumberFormat xValueSharedNumberFormat,
                                            final NumberFormat yValueBottomNumberFormat,
                                            final NumberFormat yValueTopNumberFormat,
                                            final String xUnitLabelShared,
                                            final String yUnitLabelBottom,
                                            final String yUnitLabelTop ) {
        final String dataPointValue = Double.isNaN( yValueTop )
            ? TextUtilities.getFormattedQuantityPair( xValueShared,
                                                      yValueBottom,
                                                      xValueSharedNumberFormat,
                                                      yValueBottomNumberFormat,
                                                      xUnitLabelShared,
                                                      yUnitLabelBottom )
            : TextUtilities.getFormattedQuantityTriplet( xValueShared,
                                                         yValueBottom,
                                                         yValueTop,
                                                         xValueSharedNumberFormat,
                                                         yValueBottomNumberFormat,
                                                         yValueTopNumberFormat,
                                                         xUnitLabelShared,
                                                         yUnitLabelBottom,
                                                         yUnitLabelTop );
        return dataPointValue;
    }

    public static double[] getDataSetXValues( final XYChart< Number, Number > xyChart,
                                              final int dataSetIndex ) {
        if ( !isDataSetValid( xyChart, dataSetIndex ) ) {
            return new double[ 0 ];
        }

        final ObservableList< Series< Number, Number > > chartSeriesList = xyChart.getData();
        final XYChart.Series< Number, Number > chartSeries = chartSeriesList.get( dataSetIndex );
        final ObservableList< Data< Number, Number > > chartSeriesData = chartSeries.getData();

        final int numberOfDataPoints = chartSeriesData.size();
        final double[] xValues = new double[ numberOfDataPoints ];
        for ( int dataPointIndex = 0; dataPointIndex < numberOfDataPoints; dataPointIndex++ ) {
            final Data< Number, Number > chartSeriesDataPoint =
                                                              chartSeriesData.get( dataPointIndex );
            xValues[ dataPointIndex ] = chartSeriesDataPoint.getXValue().doubleValue();
        }

        return xValues;
    }

    public static double[] getDataSetYValues( final XYChart< Number, Number > xyChart,
                                              final int dataSetIndex ) {
        if ( !isDataSetValid( xyChart, dataSetIndex ) ) {
            return new double[ 0 ];
        }

        final ObservableList< Series< Number, Number > > chartSeriesList = xyChart.getData();
        final XYChart.Series< Number, Number > chartSeries = chartSeriesList.get( dataSetIndex );
        final ObservableList< Data< Number, Number > > chartSeriesData = chartSeries.getData();

        final int numberOfDataPoints = chartSeriesData.size();
        final double[] yValues = new double[ numberOfDataPoints ];
        for ( int dataPointIndex = 0; dataPointIndex < numberOfDataPoints; dataPointIndex++ ) {
            final Data< Number, Number > chartSeriesDataPoint =
                                                              chartSeriesData.get( dataPointIndex );
            yValues[ dataPointIndex ] = chartSeriesDataPoint.getYValue().doubleValue();
        }

        return yValues;
    }

    @SuppressWarnings("nls")
    public static String getFormattedDataPoint( final Series< Number, Number > chartSeries,
                                                final double xValueShared,
                                                final double yValueBottom,
                                                final double yValueTop,
                                                final NumberFormat xValueSharedNumberFormat,
                                                final NumberFormat yValueBottomNumberFormat,
                                                final NumberFormat yValueTopNumberFormat,
                                                final String xUnitLabelShared,
                                                final String yUnitLabelBottom,
                                                final String yUnitLabelTop ) {
        final String dataSetName = chartSeries.getName();
        final String dataPointValue = getDataPointValue( xValueShared,
                                                         yValueBottom,
                                                         yValueTop,
                                                         xValueSharedNumberFormat,
                                                         yValueBottomNumberFormat,
                                                         yValueTopNumberFormat,
                                                         xUnitLabelShared,
                                                         yUnitLabelBottom,
                                                         yUnitLabelTop );
        final StringBuilder formattedDataPoint = new StringBuilder();
        formattedDataPoint.append( dataSetName );
        formattedDataPoint.append( ": " );
        formattedDataPoint.append( dataPointValue );

        return formattedDataPoint.toString();
    }

    public static boolean isChartEmpty( final XYChart< Number, Number > xyChart ) {
        // First, check first for null charts (i.e. not initialized).
        if ( xyChart == null ) {
            return true;
        }

        // Next, check first for empty charts (i.e. no data sets).
        final ObservableList< Series< Number, Number > > chartSeriesList = xyChart.getData();
        if ( ( chartSeriesList == null ) || chartSeriesList.isEmpty() ) {
            return true;
        }

        // Finally, check for all-empty data sets within the chart.
        boolean chartEmpty = true;
        for ( final Series< Number, Number > series : chartSeriesList ) {
            final ObservableList< Data< Number, Number > > data = series.getData();
            if ( ( data != null ) && !data.isEmpty() ) {
                chartEmpty = false;
                break;
            }
        }

        return chartEmpty;
    }

    /**
     * Return whether the specified data set is valid or not. Check the argument
     * to ensure that it is a valid data set index. If it is less than zero,
     * does not refer to an existing data set, or the data set is empty, return
     * false. Do not throw exceptions as this overly restricts calling contexts.
     *
     * @param xyChart
     *            The chart whose data sets must be validated
     * @param dataSetIndex
     *            The data set index.
     * @return True if the specified data set is valid; false if not.
     */
    public static boolean isDataSetValid( final XYChart< Number, Number > xyChart,
                                          final int dataSetIndex ) {
        if ( ( xyChart == null ) || ( dataSetIndex < 0 ) ) {
            return false;
        }

        final ObservableList< Series< Number, Number > > chartSeriesList = xyChart.getData();
        if ( ( chartSeriesList == null ) || ( dataSetIndex >= chartSeriesList.size() ) ) {
            return false;
        }

        final XYChart.Series< Number, Number > chartSeries = chartSeriesList.get( dataSetIndex );
        if ( chartSeries == null ) {
            return false;
        }

        final ObservableList< Data< Number, Number > > chartSeriesData = chartSeries.getData();

        return ( ( chartSeriesData != null ) && !chartSeriesData.isEmpty() );
    }

    @SuppressWarnings("nls")
    public static void updateDataSeriesLegend( final ChartLegend legend,
                                               final XYChart< Number, Number > chart ) {
        final ObservableList< ChartLegendItem > legendItems = legend.getItems();
        legendItems.clear();

        final ObservableList< Series< Number, Number > > dataSeries = chart.getData();
        if ( ( dataSeries != null ) && !dataSeries.isEmpty() ) {
            for ( int seriesIndex = 0; seriesIndex < dataSeries.size(); seriesIndex++ ) {
                final Series< Number, Number > series = dataSeries.get( seriesIndex );
                final ChartLegendItem legenditem = new ChartLegendItem( series.getName() );
                legenditem.getSymbol().getStyleClass()
                        .addAll( "chart-line-symbol", "series" + seriesIndex, "default-color" );
                legendItems.add( legenditem );
            }
        }
    }

}
