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

import java.nio.IntBuffer;

import com.mhschmieder.fxcharttoolkit.chart.ChartUtilities;
import com.mhschmieder.physicstoolkit.PaletteUtilities;

import javafx.geometry.Side;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

/**
 * This is a layout container for a legend used to indicate SPL values in color
 * palette based visualizations. It is agnostic towards the actual palette,
 * which is loaded as a generic image. It only cares about maintaining the
 * Title, Dynamic Range, and tick marks/labels for the SPL units and divisions.
 * <p>
 * TODO: Make an enumeration of palette types; there are several popular ones,
 *  but the most common one is NASA's Jet Palette, so we use that by default.
 */
public final class SplPaletteLegend extends PaletteLegend {

    // Declare default constants.
    private static final double DIV_DEFAULT            = 6.0d;
    private static final int    NUMBER_OF_DIVS_DEFAULT = 7;
    private static final double DYNAMIC_RANGE_DEFAULT  = DIV_DEFAULT * NUMBER_OF_DIVS_DEFAULT;

    private static final double MAG_MAX_DEFAULT        = 0.0d;
    private static final double MAG_MIN_DEFAULT        = MAG_MAX_DEFAULT - DYNAMIC_RANGE_DEFAULT;

    public SplPaletteLegend( final double pAspectRatio, 
                             final boolean pNormalizeToZero ) {
        // Always call the superclass constructor first!
        super( "SPL (dB)", 
               pAspectRatio, 
               pNormalizeToZero,
               DIV_DEFAULT,
               NUMBER_OF_DIVS_DEFAULT,
               MAG_MIN_DEFAULT,
               MAG_MAX_DEFAULT );
    }
    
    protected void makeYAxis() {
        // NOTE: This effectively normalizes us to zero at the start, so may
        //  need to be reviewed in uses cases where that is not what is wanted.
        yAxis = ChartUtilities.getSplAxis( MAG_MIN_DEFAULT, MAG_MAX_DEFAULT, DIV_DEFAULT );
        yAxis.setSide( Side.LEFT );
    }
    
    /**
     * Makes the SPL Palette Image in the desired ARGB Color Space.
     * 
     * @param numberOfPaletteColors
     *            The number of palette colors to use for the palette image
     * @return The newly generated palette image
     */
    @Override 
    protected Image makePaletteImage( final int numberOfPaletteColors ) {
        // Generate an SPL Palette Image with the desired number of palette
        // colors, one column per row and one row per color. We can then control
        // the scaling of this minimal palette to fit the Legend without the
        // core JavaFX imaging engine up-sampling to create intermediary colors.
        final int fitWidth = 1;
        final int heightScaleFactor = ( int ) Math
                .round( yAxis.getHeight() / numberOfPaletteColors );
        final int fitHeight = numberOfPaletteColors * heightScaleFactor;
        
        final int[] jetPalette = PaletteUtilities.generateJetPalette( numberOfPaletteColors );

        // Up-scale the source image height to more or less fit its on-screen
        // host, via duplication of the generated rows for the height scale
        // factor. This prevents the JavaFX imaging engine from up-sampling,
        // which produced undesired additional colors between those generated.
        // NOTE: We invert the y-axis order as we can't flip the Image itself.
        //  If we flip the Image View, this scale factor doesn't make it into
        //  the downstream AWT conversion used for EPS export. That's hard to do.
        final int[] jetPaletteStretched = new int[ fitHeight ];
        int jetPaletteStretchedIndex = 0;
        for ( int i = numberOfPaletteColors - 1; i >= 0; i-- ) {
            for ( int j = 0; j < heightScaleFactor; j++ ) {
                // Throw in the full-opacity alpha component, to play it safe.
                final int colorAlphaAdjusted = jetPalette[ i ] + 0xff000000;
                jetPaletteStretched[ jetPaletteStretchedIndex++ ] = colorAlphaAdjusted;
            }
        }

        final WritableImage splPaletteImage = new WritableImage( fitWidth, fitHeight );
        final PixelWriter pixelWriter = splPaletteImage.getPixelWriter();
        final WritablePixelFormat< IntBuffer > pixelFormat = PixelFormat.getIntArgbInstance();
        pixelWriter.setPixels( 0,
                               0,
                               fitWidth,
                               fitHeight,
                               pixelFormat,
                               jetPaletteStretched,
                               0,
                               fitWidth );
        
        return splPaletteImage;
    }
    
    @Override
    protected void rationalizeDivs() {
        // Recalculate the divisions to be sane based on the new Dynamic Range.
        div = ( dynamicRange <= 66f ) ? ( dynamicRange <= 27f ) ? 3.0d : 6.0d : 12d;
    }
}
