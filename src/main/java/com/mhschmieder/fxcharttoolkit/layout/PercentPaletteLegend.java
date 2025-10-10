/**
 * MIT License
 *
 * Copyright (c) 2023 Mark Schmieder
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
 * GuiToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxcharttoolkit
 */
package com.mhschmieder.fxcharttoolkit.layout;

import com.mhschmieder.physicstoolkit.ColorPalette;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;

/**
 * This is a layout container for a legend used to indicate percent values in
 * color palette based visualizations. It is agnostic towards the actual
 * palette, which is generated as a pixel image. It only cares about maintaining
 * the Title, Dynamic Range, and tick marks/labels for the units and divisions.
 */
public class PercentPaletteLegend extends ColorPaletteLegend {

    // Declare default constants.
    // NOTE: This is done in case we allow something other than full range, so
    // that we can get back to the defaults, but it might be better to make
    // them all explicit, to avoid any floating-point inexactness.
    private static final double DIV_DEFAULT            = 0.1d;
    private static final int    NUMBER_OF_DIVS_DEFAULT = 10;
    private static final double DYNAMIC_RANGE_DEFAULT  = DIV_DEFAULT * NUMBER_OF_DIVS_DEFAULT;

    private static final double MAG_MAX_DEFAULT        = 1.0d;
    private static final double MAG_MIN_DEFAULT        = MAG_MAX_DEFAULT - DYNAMIC_RANGE_DEFAULT;

    public PercentPaletteLegend( final String label,
                                 final boolean pNormalizeMaxToZero,
                                 final double pAspectRatio ) {
        // Always call the superclass constructor first!
        this( label, pNormalizeMaxToZero, pAspectRatio, ColorPalette.JET, 256 );
    }

    public PercentPaletteLegend( final String label,
                                 final boolean pNormalizeMaxToZero,
                                 final double pAspectRatio,
                                 final ColorPalette pColorPalette,
                                 final int pNumberOfPaletteColors ) {
        // Always call the superclass constructor first!
        super( label,
               DIV_DEFAULT,
               NUMBER_OF_DIVS_DEFAULT,
               MAG_MIN_DEFAULT,
               MAG_MAX_DEFAULT,
               pNormalizeMaxToZero,
               pAspectRatio,
               pColorPalette,
               pNumberOfPaletteColors );
    }

    @Override
    protected void makeYAxis() {
        yAxis = new NumberAxis( MAG_MIN_DEFAULT, MAG_MAX_DEFAULT, DIV_DEFAULT );

        // Can't auto-range as there are no actual data sets to plot in a Raster
        // Image overlay for a Cartesian Space graphics canvas, and minor ticks
        // are nor wanted for most percentage-specific features either.
        yAxis.setAutoRanging( false );
        yAxis.setMinorTickVisible( false );

        yAxis.setTickLabelFormatter( new NumberAxis.DefaultFormatter( yAxis ) );

        // NOTE: It might be preferable to put the labels and ticks on the
        // right, but that's not what this does per se. More work would be
        // required, likely in the parent class, to switch the layout order of
        // the axis overlay and the image container box.
        yAxis.setSide( Side.LEFT );
    }

    @Override
    protected void rationalizeDivs() {
        // Recalculate the divisions to be sane based on the new Dynamic Range.
        div = ( dynamicRange <= 1.0d ) ? ( dynamicRange <= 0.5d ) ? 0.05d : 0.1d : 0.2d;
    }
}
