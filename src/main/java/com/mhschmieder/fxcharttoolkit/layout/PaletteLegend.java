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

import com.mhschmieder.fxgraphicstoolkit.paint.ColorUtilities;
import com.mhschmieder.fxguitoolkit.layout.LayoutFactory;

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
 * This is a layout container for a legend used to indicate values in color
 * palette based visualizations. It is agnostic towards the actual palette,
 * which is loaded as a generic image. It only cares about maintaining the
 * Title, Dynamic Range, and tick marks/labels for the units and divisions.
 * <p>
 * NOTE: This legend presumes that y-axis ticks will be monotonically spaced,
 *  but that does not mean that the actual palette that is loaded must have
 *  monotonically increasing color values or be symmetrically weighted. The
 *  actual color palette that is hosted by this layout container, is generated
 *  by the user of this layout and is decoupled from all of the logic here.
 */
public abstract class PaletteLegend extends StackPane { 

    // Declare a blanker pane for when the Palette isn't applicable.
    private BorderPane          blankerPane;

    // Declare a container for the Palette layout elements.
    private VBox                paletteBox;

    // Need a label for the overall legend as we no longer use a chart overlay.
    private Label               paletteLabel;

    // Use an Image View to host the Palette Image as a background.
    private ImageView           paletteImageView;

    // Declare a generic Line Chart, as we can customize it on-the-fly.
    public NumberAxis           yAxis;

    // Declare dynamic range and div.
    protected double            div;

    // private int numberOfDivs;
    protected double            dynamicRange;

    // Declare minimum and maximum magnitudes (must be divisible by divs value).
    private double              magMin;
    private double              magMax;

    /**
     * Cache the Aspect Ratio desired for the bounding box of this layout pane.
     */
    private final double        aspectRatio;

    /** Cache the flag for whether to normalize the Dynamic Range to Zero. */
    private final boolean       normalizeToZero;

    public PaletteLegend( final String label, 
                          final double pAspectRatio, 
                          final boolean pNormalizeToZero,
                          final double pDiv,
                          final int pNumberOfDivs,
                          final double pMagMin,
                          final double pMagMax ) {
        // Always call the superclass constructor first!
        super();

        aspectRatio = pAspectRatio;
        normalizeToZero = pNormalizeToZero;
        
        div = pDiv;
        dynamicRange = pDiv * pNumberOfDivs;
        
        magMin = pMagMin;
        magMax = pMagMax;
        
        try {
            initLegend( label );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("nls")
    private void initLegend(final String label) {
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
        //  and use either MULTIPLY or another mode based on dark vs. light
        //  background color.
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
        //  palette image to replicate itself across an entire row without 
        //  having to make a larger source image for the ImageView host.
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
     * This method updates the axes from the current Dynamic Range and Scale.
     */
    protected void updateAxes() {
        if ( normalizeToZero ) {
            yAxis.setLowerBound( -dynamicRange );
            yAxis.setUpperBound( 0 );
        }
        else {
            yAxis.setLowerBound( magMin );
            yAxis.setUpperBound( magMax );
        }

        yAxis.setTickUnit( div );
    }

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
     * Return the Palette Image.
     *
     * @return The Palette Image .
     */
    public Image getPaletteImage() {
        return paletteImageView.getImage();
    }

    /**
     * Makes the palette image based on criteria specific to a concrete derived
     * class. Although there are some basic logical assumptions one could make,
     * such as a fit width of 1 pixel and scaling the x-axis to be consistent
     * for a single column of values, such variables need to be declared and
     * consumed in the middle of specialized palette generation algorithms.
     * 
     * @param numberOfPaletteColors
     *            The number of palette colors to use for the palette image
     * @return The newly generated palette image
     */
    protected abstract Image makePaletteImage( final int numberOfPaletteColors );

    /**
     * Updates the Palette Image.
     *
     * @param numberOfPaletteColors
     *            The number of palette colors to use for the updated image
     */
    public void updatePaletteImage( final int numberOfPaletteColors ) {
        // Flush the overlay image resources to ensure that the new overlay
        // image is loaded vs. the cached overlay image.
        removePaletteImage();
        
        // Delegate the actual palette image to the concrete derived classes.
        final Image paletteImage = makePaletteImage( numberOfPaletteColors );

        // Set the palette image on the Image View container in the layout.
        paletteImageView.setImage( paletteImage );

        // NOTE: We must defer this calculation or it may be off by a pixel or
        //  two on first invocation while the layout elements stabilize their
        //  relative positions and sizes.
        // TODO: Use bindings instead, in case the numbers change at run-time.
        Platform.runLater( () -> {
            final double imageX = yAxis.getLayoutBounds().getMaxX() + yAxis.getLayoutX();
            final double imageY = yAxis.getLayoutBounds().getMinY() + yAxis.getLayoutY();
            paletteImageView.setLayoutX( imageX );
            paletteImageView.setLayoutY( imageY );
        } );
    }

    /**
     * Remove the current Palette Image by setting the Image View to a null
     * Image reference.
     */
    private void removePaletteImage() {
        // Blank out the image in case the layout visibility isn't hidden --
        // most often in cases where we no longer have a valid correspondence
        // with a prediction response.
        paletteImageView.setImage( null );
    }

    /**
     * This is a method to hide or show the Palette using the Stack Pane
     * layout approach, so that exporting node snapshots won't block out blank
     * space when the Palette isn't applicable (i.e., no associated chart data).
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

    public void updateRange( final double minimum, final double maximum ) {
        // Store the new Dynamic Range, and use it to update the labels.
        // TODO: Force the Dynamic Range itself to an increment of the chosen
        //  Scale Factor?
        magMin = Math.round( Math.min( minimum, maximum ) );
        magMax = Math.round( Math.max( minimum, maximum ) );
        dynamicRange = Math.abs( magMax - magMin );

        // Recalculate the divisions to be sane based on the new Dynamic Range.
        rationalizeDivs();

        // Update the axes from the current Dynamic Range and Scale Factor.
        // NOTE: Might need to rescale the Palette image to fit after changing
        //  the Dynamic Range.
        updateAxes();
    }
    
    /**
     * Rationalizes the divs in case a change to min/max dynamic range does
     * not result in clean number when applying a basic formula to compute.
     * Only a concrete derived class can know the right logic to apply here.
     */
    protected abstract void rationalizeDivs();
}
