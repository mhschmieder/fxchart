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
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.layout;

import com.mhschmieder.fxgraphicstoolkit.paint.ColorUtilities;
import com.mhschmieder.fxguitoolkit.layout.LayoutFactory;
import com.mhschmieder.jphysics.ColorPalette;
import com.mhschmieder.jphysics.PaletteUtilities;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import org.apache.commons.math3.util.FastMath;

import java.nio.IntBuffer;

/**
 * This is a layout container for a legend used to indicate values in color
 * palette based visualizations. It is agnostic towards the actual palette,
 * which is loaded as a generic image. It only cares about maintaining the
 * Title, Dynamic Range, and tick marks/labels for the units and divisions.
 * <p>
 * NOTE: This legend presumes that y-axis ticks will be monotonically spaced,
 * but that does not mean that the actual palette that is loaded must have
 * monotonically increasing color values or be symmetrically weighted. The
 * actual color palette that is hosted by this layout container, is generated
 * by the user of this layout and is decoupled from all of the logic here.
 * <p>
 * TODO: support horizontal orientation of the Palette Legend, which would
 * require flipping all of the x-axis and y-axis contexts everywhere?
 */
public abstract class ColorPaletteLegend extends StackPane {

    // Declare a blanker pane for when the Palette isn't applicable.
    private BorderPane     blankerPane;

    // Declare a container for the Palette layout elements.
    private VBox           paletteBox;

    // Need a label for the overall legend as we no longer use a chart overlay.
    private Label          paletteLabel;

    // Use an Image View to host the Palette Image as a background.
    private ImageView      paletteImageView;

    // Declare a generic Line Chart, as we can customize it on-the-fly.
    public NumberAxis      yAxis;

    // Declare dynamic range and div.
    protected double       div;

    // private int numberOfDivs;
    protected double       dynamicRange;

    // Declare minimum and maximum magnitudes (must be divisible by divs value).
    private double         magMin;
    private double         magMax;

    /**
     * Cache the flag for whether to normalize Dynamic Range max to Zero.
     */
    private final boolean  normalizeMaxToZero;

    /**
     * Cache the Aspect Ratio desired for the bounding box of this layout pane.
     */
    private final double   aspectRatio;

    // Declare the Color Palette to be used for this Legend.
    protected ColorPalette colorPalette;

    // The number of colors to use when applying the current Color Palette.
    protected int          numberOfPaletteColors;

    public ColorPaletteLegend( final String label,
                               final double pDiv,
                               final int pNumberOfDivs,
                               final double pMagMin,
                               final double pMagMax,
                               final boolean pNormalizeMaxToZero,
                               final double pAspectRatio,
                               final ColorPalette pColorPalette,
                               final int pNumberOfPaletteColors ) {
        // Always call the superclass constructor first!
        super();

        aspectRatio = pAspectRatio;
        normalizeMaxToZero = pNormalizeMaxToZero;

        div = pDiv;
        dynamicRange = pDiv * pNumberOfDivs;

        magMin = pMagMin;
        magMax = pMagMax;

        colorPalette = pColorPalette;
        numberOfPaletteColors = pNumberOfPaletteColors;

        try {
            initLegend( label );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("nls")
    private void initLegend( final String label ) {
        // Use a derived font that is bold and large like an HTML header.
        paletteLabel = new Label( label );
        paletteLabel.setFont( Font.font( "sans-serif", FontWeight.BOLD, 18d ) );

        // Make a group for the label so that it is easy to center.
        final Group paletteLabelGroup = new Group( paletteLabel );
        paletteLabelGroup.translateXProperty().bind( widthProperty().divide( 2.0d )
                .subtract( paletteLabel.widthProperty().divide( 2.0d ) ) );

        // Make y-axis via concrete derived classes that know units and range.
        makeYAxis();

        // Make a placeholder blank Image View to host the Palette image as a
        // background image that lines up with the chart overlay.
        // NOTE: We might need to defer the Blend Mode to image loading time,
        // and use either MULTIPLY or another mode based on dark vs. light
        // background color.
        paletteImageView = new ImageView();
        paletteImageView.setBlendMode( BlendMode.SRC_OVER );
        paletteImageView.setCache( true );
        paletteImageView.setSmooth( false );

        // Make a grid pane to "glue" the y-axis and palette image together.
        final GridPane palettePane = new GridPane();
        palettePane.add( yAxis, 0, 0 );
        palettePane.add( paletteImageView, 1, 0 );

        // Enforce the Aspect ratio on the overall Palette pane.
        palettePane.prefWidthProperty()
                .bind( palettePane.heightProperty().multiply( aspectRatio ) );

        // Place the Palette Label above the image area, like a chart label.
        paletteBox = new VBox();
        paletteBox.getChildren().addAll( paletteLabelGroup, palettePane );

        // Prevent the label from slamming into the chart, by providing a gap.
        paletteBox.setSpacing( 6.0d );

        // Provide some padding for legibility and to avoid overlap at borders.
        paletteBox.setPadding( new Insets( 6.0d ) );

        // Make sure that the SPL Palette Image and y-axis, and not the SPL
        // Palette Label, grow to fill empty space, but only when necessary.
        VBox.setVgrow( paletteLabelGroup, Priority.NEVER );
        VBox.setVgrow( palettePane, Priority.SOMETIMES );

        // Make a blanker pane for when the Palette isn't applicable.
        blankerPane = new BorderPane();

        // Set up the stack pane for switching Palette "on" and "off".
        final ObservableList< Node > contentNodes = getChildren();
        contentNodes.addAll( paletteBox, blankerPane );

        // Make sure the image height stays in sync with its axis boundary.
        Platform.runLater( () -> paletteImageView.fitHeightProperty()
                .bind( yAxis.heightProperty() ) );

        // Make sure the image width stays in sync with its aspect ratio.
        // NOTE: This also effectively stretches the single-pixel-wide original
        // palette image to replicate itself across an entire row without
        // having to make a larger source image for the ImageView host.
        Platform.runLater( () -> paletteImageView.fitWidthProperty()
                .bind( paletteImageView.fitHeightProperty().multiply( aspectRatio ) ) );
    }

    /**
     * Makes the y axis for the Palette Legend labels. Delegated to the derived
     * classes as y-axis choices vary wildly across different use cases, which
     * might be SPL's, a normalized range, percentages, or anything else.
     */
    protected abstract void makeYAxis();

    /**
     * Updates the axes using the current Dynamic Range and Scale.
     */
    protected void updateAxes() {
        if ( normalizeMaxToZero ) {
            yAxis.setLowerBound( -dynamicRange );
            yAxis.setUpperBound( 0 );
        }
        else {
            yAxis.setLowerBound( magMin );
            yAxis.setUpperBound( magMax );
        }

        yAxis.setTickUnit( div );
    }

    /**
     * Shows or hides the Color Palette Legend using the Stack Pane layout
     * approach, so that exporting node snapshots won't block out blank space
     * when the Palette isn't applicable (i.e., no associated chart data).
     *
     * @param paletteVisible
     *            Flag for whether to show (set visible) or hide the Palette
     */
    public void showPalette( final boolean paletteVisible ) {
        if ( paletteVisible ) {
            paletteBox.setVisible( true );
            blankerPane.setVisible( false );
        }
        else {
            paletteBox.setVisible( false );
            blankerPane.setVisible( true );
        }
    }

    /**
     * Sets the supplied background color hierarchically through this layout and
     * and makes sure that foreground colors are visible against that background
     * by applying an HSV appropriate match (e.g., white foreground for black).
     * 
     * @param backColor
     *            The background color to use for this layout pane
     */
    public void setForegroundFromBackground( final Color backColor ) {
        // Set the new Background first, so it sets context for CSS derivations.
        final Background background = LayoutFactory.makeRegionBackground( backColor );
        setBackground( background );

        final Color foreColor = ColorUtilities.getForegroundFromBackground( backColor );

        blankerPane.setBackground( background );

        paletteBox.setBackground( background );

        paletteLabel.setBackground( background );
        paletteLabel.setTextFill( foreColor );

        yAxis.setBackground( background );
    }
    
    /**
     * Sets the label that is atop this legend.
     * 
     * @param label The string to use to replace the legend's label
     */
    public final void setLabel( final String label ) {
        paletteLabel.setText( label );
    }
    
    /**
     * Returns the label that is atop this legend.
     * 
     * @return The label that is atop this legend
     */
    public final String getLabel() {
        return paletteLabel.getText();
    }

    /**
     * Sets the Color Palette and Number of Palette Colors and updates the
     * legend.
     * <p>
     * This is a convenience method to avoid interim updates to the color image
     * that is hosted by the legend, when more than one characteristic changes.
     * 
     * @param pColorPalette
     *            The new Color Palette to use for the Legend.
     * @param pNumberOfPaletteColors
     *            The number of palette colors to apply to the Color Palette
     */
    public final void setPalette( final ColorPalette pColorPalette,
                                  final int pNumberOfPaletteColors ) {
        colorPalette = pColorPalette;
        numberOfPaletteColors = pNumberOfPaletteColors;

        // Replace the current image in the Legend so it matches the new Color
        // Palette choice and the requested Number of Palette Colors.
        updatePaletteImage();
    }

    /**
     * Sets a new Color Palette to use for the Legend, and updates the Legend.
     * 
     * @param pColorPalette
     *            The new Color Palette to use for the Legend.
     */
    public final void setColorPalette( final ColorPalette pColorPalette ) {
        colorPalette = pColorPalette;

        // Replace the current image in the Legend so it matches the new Color
        // Palette choice.
        updatePaletteImage();
    }

    /**
     * Returns the current Color Palette in use.
     * 
     * @return The current Color Palette in use
     */
    public final ColorPalette getColorPalette() {
        return colorPalette;
    }

    /**
     * Sets the number of palette colors to use when generating an image for the
     * Legend that is based on the current Color Palette choice, and updates the
     * Legend with a newly generated image corresponding to the Color Palette.
     * 
     * @param pNumberOfPaletteColors
     *            The number of palette colors to apply to the Color Palette
     */
    public final void setNumberOfPaletteColors( final int pNumberOfPaletteColors ) {
        numberOfPaletteColors = pNumberOfPaletteColors;

        // Replace the current image in the Legend so it matches the new number
        // of colors.
        updatePaletteImage();
    }

    /**
     * Returns the number of palette colors being applied to the Color Palette.
     * 
     * @return The number of palette colors to apply to the Color Palette
     */
    public final int getNumberOfPaletteColors() {
        return numberOfPaletteColors;
    }

    /**
     * Updates the Dynamic Range to match a new min/max pair.
     * 
     * @param minimum
     *            The new minimum value to map in the Legend
     * @param maximum
     *            The new maximum value to map in the Legend
     */
    public void updateRange( final double minimum, final double maximum ) {
        // Store the new Dynamic Range, and use it to update the labels.
        // TODO: Force the Dynamic Range itself to an increment of the chosen
        // Scale Factor?
        magMin = FastMath.round( FastMath.min( minimum, maximum ) );
        magMax = FastMath.round( FastMath.max( minimum, maximum ) );
        dynamicRange = FastMath.abs( magMax - magMin );

        // Recalculate the divisions to be sane based on the new Dynamic Range.
        rationalizeDivs();

        // Update the axes from the current Dynamic Range and Scale Factor.
        // NOTE: Might need to rescale the Palette image to fit after changing
        // the Dynamic Range.
        updateAxes();
    }

    /**
     * Rationalizes the divs in case a change to min/max dynamic range does
     * not result in clean number when applying a basic formula to compute.
     * Only a concrete derived class can know the right logic to apply here.
     */
    protected abstract void rationalizeDivs();

    /**
     * Return the Palette Image.
     *
     * @return The Palette Image .
     */
    public Image getPaletteImage() {
        return paletteImageView.getImage();
    }

    /**
     * Updates the Palette Image based on current Color Palette and Number
     * of Palette Colors, along with any additional criteria.
     */
    private void updatePaletteImage() {
        // Flush the overlay image resources to ensure that the new overlay
        // image is loaded vs. the cached overlay image.
        removePaletteImage();

        // Delegate the actual palette image to the concrete derived classes.
        final Image paletteImage = makePaletteImage();

        // Set the palette image on the Image View container in the layout.
        paletteImageView.setImage( paletteImage );

        // NOTE: We must defer this calculation or it may be off by a pixel or
        // two on first invocation while the layout elements stabilize their
        // relative positions and sizes.
        // TODO: Use bindings instead, in case the numbers change at run-time.
        Platform.runLater( () -> {
            final double imageX = yAxis.getLayoutBounds().getMaxX() + yAxis.getLayoutX();
            final double imageY = yAxis.getLayoutBounds().getMinY() + yAxis.getLayoutY();
            paletteImageView.setLayoutX( imageX );
            paletteImageView.setLayoutY( imageY );
        } );
    }

    /**
     * Removes the current Palette Image by setting the Image View to a null
     * Image reference.
     */
    private void removePaletteImage() {
        // Blank out the image in case the layout visibility isn't hidden --
        // most often in cases where we no longer have a valid correspondence
        // with a chart series or a Cartesian Space prediction response overlay.
        paletteImageView.setImage( null );
    }

    /**
     * Makes the Palette Image in the desired ARGB Color Space.
     * <p>
     * NOTE: This is called by updatePaletteImage(), which must be called by
     * application code that uses this class. It needs to be called every time
     * the criteria for switching color ranges changes, which may be just once.
     *
     * @return The newly generated Palette Image
     */
    private Image makePaletteImage() {
        // Generate the palette as an array of packed RGB integer values.
        final int[] palette = generatePalette();
        if ( palette == null ) {
            return null;
        }

        // Generate a Palette Image with the desired number of palette colors,
        // one column per row and one row per color. We can then control the
        // scaling of this minimal palette to fit the Legend without the core
        // JavaFX imaging engine up-sampling to create intermediary colors.
        // NOTE: We make sure there is always at least one pixel to display no
        // matter how small the layout container gets, to avoid run-time errors.
        final int fitWidth = 1;
        final int heightScaleFactor = ( int ) FastMath
                .max( 1, FastMath.round( yAxis.getHeight() / numberOfPaletteColors ) );
        final int fitHeight = numberOfPaletteColors * heightScaleFactor;

        // Up-scale the source image height to more or less fit its on-screen
        // host, via duplication of the generated rows for the height scale
        // factor. This prevents the JavaFX imaging engine from up-sampling,
        // which produced undesired additional colors between those generated.
        // NOTE: We invert the y-axis order as we can't flip the Image itself.
        // If we flip the Image View, this scale factor doesn't make it into
        // the downstream AWT conversion used for EPS export. That's hard.
        final int[] paletteStretched = new int[ fitHeight ];
        int jetPaletteStretchedIndex = 0;
        for ( int i = numberOfPaletteColors - 1; i >= 0; i-- ) {
            for ( int j = 0; j < heightScaleFactor; j++ ) {
                // Throw in the full-opacity alpha component, to play it safe.
                final int colorAlphaAdjusted = palette[ i ] + 0xff000000;
                paletteStretched[ jetPaletteStretchedIndex++ ] = colorAlphaAdjusted;
            }
        }

        // TODO: Look into switching to PixelFormat.getByteBgraInstance() and
        //  change the pixel writing loops to match this format, as it is said
        //  to be 3x faster than PixelWriter.getIntArgbInstance()? Only if the
        //  raw data conforms to this format though, and maybe only if on macOS.
        final WritableImage paletteImage = new WritableImage( fitWidth, fitHeight );
        final PixelWriter pixelWriter = paletteImage.getPixelWriter();
        final WritablePixelFormat< IntBuffer > pixelFormat = PixelFormat.getIntArgbInstance();
        pixelWriter
                .setPixels( 0, 0, fitWidth, fitHeight, pixelFormat, paletteStretched, 0, fitWidth );

        return paletteImage;
    }

    /**
     * Returns a packed color integer array of RGB values to use for the Color
     * Palette in the Legend, using the current Color Palette choice.
     * 
     * @return A packed color integer array of RGB values to use for the Palette
     */
    protected int[] generatePalette() {
        int[] palette = null;

        switch ( colorPalette ) {
        case CUSTOM:
            palette = generateCustomPalette();
            break;
        case JET:
            palette = PaletteUtilities.generateJetPalette( numberOfPaletteColors );
            break;
        default:
            break;
        }

        return palette;
    }

    /**
     * Returns a custom packed color integer array of RGB values to use for the
     * Color Palette in the Legend.
     * <p>
     * This method should be overridden by classes that need specialized Color
     * Palettes that aren't covered by the current palette generators in the
     * jphysics, bearing in mind that only the common Jet Palette is
     * currently provided by that toolkit.
     * <p>
     * As most classes won't need a specialized palette, we don't want to force
     * this method to be overridden and implemented, so the default returns
     * null. Downstream logic knows to avoid null palettes.
     * <p>
     * In general, if other standardized palettes get implemented by the
     * jphysics later on, this method is still needed for special cases,
     * such as when an asymmetric color map is needed. This most often happens
     * with statistical charting, where specific threshold values may mark
     * sudden shifts in the color map.
     * 
     * @return A packed color integer array of RGB values to use for the Palette
     */
    protected int[] generateCustomPalette() {
        return null;
    }
}
