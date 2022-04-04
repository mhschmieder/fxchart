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
 * This file is part of the FxGuiToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * GuiToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxguitoolkit
 */
package com.mhschmieder.fxcharttoolkit.chart;

import java.nio.IntBuffer;

import com.mhschmieder.fxgraphicstoolkit.paint.ColorUtilities;
import com.mhschmieder.fxguitoolkit.layout.LayoutFactory;
import com.mhschmieder.physicstoolkit.PaletteUtilities;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * This is a layout container for a legend used to indicate SPL values in color
 * palette based visualizations. It is agnostic towards the actual palette,
 * which is loaded as a generic image. It only cares about maintaining the
 * Title, Dynamic Range, and tick marks/labels for the SPL units and divisions.
 * <p>
 * TODO: Make an enumeration of palette types; there are several popular ones.
 * <p>
 * TODO: Look further into whether this can be generalized beyond SPL contexts,
 * by just making a factory method that sets the title and units. On first look,
 * it appears that the audio domain context may be baked-in in several methods.
 */
public final class SplPaletteLegend extends StackPane {

    // Declare default constants.
    private static final double DIV_DEFAULT            = 6.0d;
    private static final int    NUMBER_OF_DIVS_DEFAULT = 7;
    private static final double DYNAMIC_RANGE_DEFAULT  = DIV_DEFAULT * NUMBER_OF_DIVS_DEFAULT;

    private static final double MAG_MAX_DEFAULT        = 0.0d;
    private static final double MAG_MIN_DEFAULT        = MAG_MAX_DEFAULT - DYNAMIC_RANGE_DEFAULT;

    // Declare a blanker pane for when the SPL Palette isn't applicable.
    private BorderPane          _blankerPane;

    // Declare a container for the SPL Palette layout elements.
    private VBox                _splPaletteBox;

    // Need a label for the overall legend as we no longer use a chart overlay.
    private Label               _splPaletteLabel;

    // Use an Image View to host the SPL Palette Image as a background.
    private ImageView           _splPaletteImageView;

    // Declare a generic Line Chart, as we can customize it on-the-fly.
    public NumberAxis           _yAxis;

    // Declare dynamic range and div.
    private double              _div;

    // private int _numberOfDivs;
    private double              _dynamicRange;

    // Declare minimum and maximum magnitudes (must be valid 6.0dB divs)
    private double              _magMax;
    private double              _magMin;

    /**
     * Cache the Aspect Ratio desired for the bounding box of this layout pane
     */
    private final double        _aspectRatio;

    /** Cache the flag for whether to normalize the Dynamic Range to Zero */
    private final boolean       _normalizeToZero;

    public SplPaletteLegend( final double aspectRatio, final boolean normalizeToZero ) {
        // Always call the superclass constructor first!
        super();

        _aspectRatio = aspectRatio;
        _normalizeToZero = normalizeToZero;

        _div = DIV_DEFAULT;
        _dynamicRange = DYNAMIC_RANGE_DEFAULT;
        _magMax = MAG_MAX_DEFAULT;
        _magMin = MAG_MIN_DEFAULT;

        try {
            initLegend();
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Return the SPL Palette Image.
     *
     * @return The SPL Palette Image .
     */
    public Image getSplPaletteImage() {
        return _splPaletteImageView.getImage();
    }

    @SuppressWarnings("nls")
    private void initLegend() {
        // Make a placeholder blank Image View to host the SPL Palette image as
        // a background image that lines up with the chart overlay.
        // NOTE: We might need to defer the Blend Mode to image loading time,
        // and use either MULTIPLY or another mode based on dark vs. light
        // background color.
        _splPaletteImageView = new ImageView();
        _splPaletteImageView.setBlendMode( BlendMode.SRC_OVER );
        _splPaletteImageView.setCache( true );
        _splPaletteImageView.setSmooth( false );

        _yAxis = ChartUtilities.getSplAxis( MAG_MIN_DEFAULT, MAG_MAX_DEFAULT, DIV_DEFAULT );
        _yAxis.setSide( Side.LEFT );

        // Use a derived font that is bold and large like an HTML header.
        _splPaletteLabel = new Label( "SPL (dB)" );
        _splPaletteLabel.setFont( Font.font( "sans-serif", FontWeight.BOLD, 18d ) );

        // Make a group for the label so that it is easy to center.
        final Group splPaletteLabelGroup = new Group( _splPaletteLabel );
        splPaletteLabelGroup.translateXProperty().bind( widthProperty().divide( 2.0d )
                .subtract( _splPaletteLabel.widthProperty().divide( 2.0d ) ) );

        // Make a grid pane to "glue" the y-axis and palette image together.
        final GridPane splPaletteBox = new GridPane();
        splPaletteBox.add( _yAxis, 0, 0 );
        splPaletteBox.add( _splPaletteImageView, 1, 0 );

        // Enforce the Aspect ratio on the overall SPL Palette box.
        splPaletteBox.prefWidthProperty()
                .bind( splPaletteBox.heightProperty().multiply( _aspectRatio ) );

        // Place the SPL Palette Label above the image area, like a chart label.
        _splPaletteBox = new VBox();
        _splPaletteBox.getChildren().addAll( splPaletteLabelGroup, splPaletteBox );

        // Prevent the label from slamming into the chart, by providing a gap.
        _splPaletteBox.setSpacing( 6.0d );

        // Provide some padding for legibility and to avoid overlap at borders.
        _splPaletteBox.setPadding( new Insets( 6.0d ) );

        // Make sure that the SPL Palette Image and y-axis, and not the SPL
        // Palette Label, grow to fill empty space, but only when necessary.
        VBox.setVgrow( splPaletteLabelGroup, Priority.NEVER );
        VBox.setVgrow( splPaletteBox, Priority.SOMETIMES );

        // Make a blanker pane for when the SPL Palette isn't applicable.
        _blankerPane = new BorderPane();

        // Set up the stack pane for switching SPL Palette "on" and "off".
        final ObservableList< Node > contentNodes = getChildren();
        contentNodes.addAll( _splPaletteBox, _blankerPane );

        // Make sure the image height stays in sync with its axis boundary.
        Platform.runLater( () -> _splPaletteImageView.fitHeightProperty()
                .bind( _yAxis.heightProperty() ) );

        // Make sure the image width stays in sync with its aspect ratio.
        // NOTE: This also effectively stretches the single-pixel-wide original
        // palette image to replicate itself across an entire row without having
        // to make a larger source image for the ImageView host.
        Platform.runLater( () -> _splPaletteImageView.fitWidthProperty()
                .bind( _splPaletteImageView.fitHeightProperty().multiply( _aspectRatio ) ) );
    }

    /**
     * Remove the current SPL Palette Image by setting the Image View to a null
     * Image reference.
     */
    private void removeSplPaletteImage() {
        // Blank out the image in case the layout visibility isn't hidden --
        // most often in cases where we no longer have a valid correspondence
        // with a prediction response.
        _splPaletteImageView.setImage( null );
    }

    public void setForegroundFromBackground( final Color backColor ) {
        // Set the new Background first, so it sets context for CSS derivations.
        final Background background = LayoutFactory.makeRegionBackground( backColor );
        setBackground( background );

        final Color foreColor = ColorUtilities.getForegroundFromBackground( backColor );

        _blankerPane.setBackground( background );

        _splPaletteBox.setBackground( background );

        _splPaletteLabel.setBackground( background );
        _splPaletteLabel.setTextFill( foreColor );

        _yAxis.setBackground( background );
    }

    /**
     * This is a method to hide or show the SPL Palette using the Stack Pane
     * layout approach, so that exporting node snapshots won't block out blank
     * space when the SPL Palette isn't applicable (i.e., no prediction).
     *
     * @param showPalette
     *            Flag for whether to show or hide the SPL Palette
     */
    public void showSplPalette( final boolean showPalette ) {
        if ( showPalette ) {
            _splPaletteBox.setVisible( true );
            _blankerPane.setVisible( false );
        }
        else {
            _splPaletteBox.setVisible( false );
            _blankerPane.setVisible( true );
        }
    }

    /**
     * This method updates the axes from the current Dynamic Range and Scale.
     */
    protected void updateAxes() {
        if ( _normalizeToZero ) {
            _yAxis.setLowerBound( -_dynamicRange );
            _yAxis.setUpperBound( 0 );
        }
        else {
            _yAxis.setLowerBound( _magMin );
            _yAxis.setUpperBound( _magMax );
        }

        _yAxis.setTickUnit( _div );
    }

    /**
     * Updates the SPL Palette Image.
     *
     * @param numberOfPaletteColors
     *            The number of palette colors to use for the updated image
     */
    public void updateSplPaletteImage( final int numberOfPaletteColors ) {
        // Flush the overlay image resources to ensure that the new overlay
        // image is loaded vs. the cached overlay image.
        removeSplPaletteImage();

        // Generate an SPL Palette Image with the desired number of palette
        // colors, one column per row and one row per color. We can then control
        // the scaling of this minimal palette to fit the Legend without the
        // core JavaFX imaging engine up-sampling to create intermediary colors.
        final int heightScaleFactor = ( int ) Math
                .round( _yAxis.getHeight() / numberOfPaletteColors );
        final int fitHeight = numberOfPaletteColors * heightScaleFactor;
        final int fitWidth = 1;
        final int[] jetPalette = PaletteUtilities.generateJetPalette( numberOfPaletteColors );

        // Up-scale the source image height to more or less fit its on-screen
        // host, via duplication of the generated rows for the height scale
        // factor. This prevents the JavaFX imaging engine from up-sampling,
        // which produced undesired additional colors between those generated.
        // NOTE: We invert the y-axis order as we can't flip the Image itself.
        // If we flip the Image View, this scale factor doesn't make it into the
        // downstream AWT conversion used for EPS export. That's hard to do.
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

        _splPaletteImageView.setImage( splPaletteImage );

        // NOTE: We must defer this calculation or it may be off by a pixel or
        // two on first invocation while the layout elements stabilize their
        // relative positions and sizes.
        // TODO: Use bindings instead, in case the numbers change at run-time.
        Platform.runLater( () -> {
            final double imageX = _yAxis.getLayoutBounds().getMaxX() + _yAxis.getLayoutX();
            final double imageY = _yAxis.getLayoutBounds().getMinY() + _yAxis.getLayoutY();
            _splPaletteImageView.setLayoutX( imageX );
            _splPaletteImageView.setLayoutY( imageY );
        } );
    }

    public void updateSplRange( final double splMinimum, final double splMaximum ) {
        // Store the new Dynamic Range, and use it to update the labels.
        // TODO: Force the Dynamic Range itself to an increment of the chosen
        // Scale Factor?
        _magMin = Math.round( Math.min( splMinimum, splMaximum ) );
        _magMax = Math.round( Math.max( splMinimum, splMaximum ) );
        _dynamicRange = Math.abs( _magMax - _magMin );

        // Recalculate the divisions to be sane based on the new Dynamic Range.
        _div = ( _dynamicRange <= 66f ) ? ( _dynamicRange <= 27f ) ? 3.0d : 6.0d : 12d;

        // Update the axes from the current Dynamic Range and Scale Factor.
        // NOTE: Might need to rescale the SPL Palette image to fit after
        // changing the Dynamic Range.
        updateAxes();
    }

}
