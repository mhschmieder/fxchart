/**
 * MIT License
 *
 * Copyright (c) 2020, 2023 Mark Schmieder
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

import com.mhschmieder.fxcharttoolkit.chart.ChartUtilities;
import com.mhschmieder.physicstoolkit.ColorPalette;

import javafx.geometry.Side;

/**
 * This is a layout container for a legend used to indicate SPL values in color
 * palette based visualizations. It is agnostic towards the actual palette,
 * which is loaded as a generic image. It only cares about maintaining the
 * Title, Dynamic Range, and tick marks/labels for the SPL units and divisions.
 * <p>
 * TODO: Make an enumeration of palette types; there are several popular ones,
 * but the most common one is NASA's Jet Palette, so we use that by default.
 */
public class SplPaletteLegend extends ColorPaletteLegend {

    // Declare default constants.
    private static final double DIV_DEFAULT            = 6.0d;
    private static final int    NUMBER_OF_DIVS_DEFAULT = 7;
    private static final double DYNAMIC_RANGE_DEFAULT  = DIV_DEFAULT * NUMBER_OF_DIVS_DEFAULT;

    private static final double MAG_MAX_DEFAULT        = 0.0d;
    private static final double MAG_MIN_DEFAULT        = MAG_MAX_DEFAULT - DYNAMIC_RANGE_DEFAULT;

    public SplPaletteLegend( final boolean pNormalizeMaxToZero, final double pAspectRatio ) {
        // Always call the superclass constructor first!
        this( pNormalizeMaxToZero, pAspectRatio, ColorPalette.JET, 256 );
    }

    public SplPaletteLegend( final boolean pNormalizeMaxToZero,
                             final double pAspectRatio,
                             final ColorPalette pColorPalette,
                             final int pNumberOfPaletteColors ) {
        // Always call the superclass constructor first!
        super( "SPL (dB)",
               DIV_DEFAULT,
               NUMBER_OF_DIVS_DEFAULT,
               MAG_MIN_DEFAULT,
               MAG_MAX_DEFAULT,
               pNormalizeMaxToZero,
               pAspectRatio,
               pColorPalette,
               pNumberOfPaletteColors );
    }

    protected void makeYAxis() {
        // NOTE: This effectively normalizes us to zero at the start, so may
        // need to be reviewed in uses cases where that is not what is wanted.
        yAxis = ChartUtilities.getSplAxis( MAG_MIN_DEFAULT, MAG_MAX_DEFAULT, DIV_DEFAULT );
        yAxis.setSide( Side.LEFT );
    }

    @Override
    protected void rationalizeDivs() {
        // Recalculate the divisions to be sane based on the new Dynamic Range.
        div = ( dynamicRange <= 66.0d ) ? ( dynamicRange <= 27.0d ) ? 3.0d : 6.0d : 12d;
    }
}
