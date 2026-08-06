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
 * This file is part of the fxchart Library
 *
 * You should have received a copy of the MIT License along with the fxchart
 * Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart;

import com.mhschmieder.fxcontrols.model.Extents2DProperties;
import com.mhschmieder.fxgraphics.group.ChartContentGroup;
import com.mhschmieder.fxgraphics.group.GroupUtilities;
import com.mhschmieder.fxgraphics.image.ImageUtilities;
import com.mhschmieder.fxgui.util.BoundsUtilities;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This is a Group container for all the Overlay Images associated with the
 * visualization of domain-specific data in the context of a Cartesian Chart.
 * <p>
 * There is only a small amount of overlap of transform management methods with
 * the Chart Overlay Group container. Ultimately, we may be able to do away with
 * this entirely, and deal with the image and watermark strictly in pixel units,
 * as with other recently refactored elements such as the Drawing Limits.
 * <p>
 * The Watermark facility is optional but is common to many modern applications
 * to prevent nefarious misuse or manipulation of extracted overlay images.
 * <p>
 * If your application has multiple background overlays, you can derive from
 * this class and add the extra layers in your custom subclass.
 */
public class VisualizationOverlayGroup extends ChartContentGroup {

    /**
     * The watermark aspect ratio is pre-measured, to avoid thread timing issues
     * on initial load and fit width and fit height computations.
     */
    protected static final double WATERMARK_ASPECT_RATIO = 0.45d;
    /**
     * Watermark opacity level is defined a constant, in case we switch to a
     * computed ratio and in case we provide programmatic support for changing
     * its value (this then gives us our defined default value).
     */
    private static final double WATERMARK_OPACITY_DEFAULT = 0.15d;
    /**
     * Use an Image View to host the main overlay image as a background.
     */
    private final ImageView mainOverlayImageView;

    /**
     * Use an Image View to host the semi-transparent Watermark overlay.
     */
    private final ImageView watermarkImageView;

    /**
     * Declare a flag for whether to use the watermark or not.
     */
    protected boolean useWatermark;

    /**
     * Declare a variable to hold the watermark opacity value.
     */
    protected double watermarkOpacity;

    /**
     * This is the full constructor, when all parameters are known.
     *
     * @param pUseWatermark                Flag that indicates if the Watermark
     *                                     should be visible
     * @param jarRelativeWatermarkFilename The JAR-relative file name for the
     *                                     Watermark Image file
     */
    public VisualizationOverlayGroup( final boolean pUseWatermark,
                                      final String jarRelativeWatermarkFilename ) {
        // Always call the superclass constructor first!
        super();

        mainOverlayImageView = new ImageView();
        watermarkImageView = new ImageView();

        useWatermark = pUseWatermark;
        watermarkOpacity = WATERMARK_OPACITY_DEFAULT;

        try {
            initialize( jarRelativeWatermarkFilename );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Sets up the configuration of the Visualization Overlay Group elements.
     *
     * @param jarRelativeWatermarkFilename The JAR-relative file name for the
     *                                     Watermark Image file
     */
    private final void initialize( final String jarRelativeWatermarkFilename ) {
        // Cache the Watermark Image, so that it can be displayed and scaled.
        if ( useWatermark ) {
            loadWatermark( jarRelativeWatermarkFilename );
        }

        // Make a placeholder blank Image View to host the main overlay image
        // as a background image that lines up with the chart overlay.
        // NOTE: We always want this to act as a background image, so ask for
        //  SRC_OVER Blend Mode so that the chart overlay won't paint opaquely
        //  over the background.
        // NOTE: We might need to defer the Blend Mode to image loading time,
        //  and use either MULTIPLY or another mode based on dark vs. light
        //  background color.
        // _imageView.setBlendMode( BlendMode.SRC_OVER );
        // _imageView.setCache( true );
        mainOverlayImageView.setSmooth( false );
        mainOverlayImageView.setScaleY( -1.0d );

        final ObservableList< Node > nodes = getChildren();
        nodes.add( mainOverlayImageView );
        if ( useWatermark ) {
            nodes.add( watermarkImageView );
        }

        // Initialize the persistent shared attributes of this Visualization
        // Overlay Group, which is application managed and is not directly
        // interactive at this time.
        GroupUtilities.initDecoratorNodeGroup( this );
    }

    /**
     * Load the watermark and set up its fixed attributes.
     *
     * @param jarRelativeWatermarkFilename The JAR-relative file name for the
     *                                     Watermark Image file
     */
    protected final void loadWatermark( final String jarRelativeWatermarkFilename ) {
        // Background-load the Watermark Image as a JAR-resident resource,
        // then place it into an Image View container, so it can be displayed
        // and scaled.
        ImageUtilities.updateImageView( watermarkImageView,
                                        jarRelativeWatermarkFilename,
                                        true );

        // Remember that we have to flip the y-axis for Cartesian Space vs.
        // Screen Coordinates, for any raster image that we load into the
        // associated visual elements node group.
        watermarkImageView.setScaleY( -1.0d );

        // Traditionally, we have set watermark opacity to 15%.
        watermarkImageView.setOpacity( watermarkOpacity );

        // Choose and set a Blend Mode such that the watermark isn't stark and
        // dominant -- especially in areas of high energy SPL values.
        watermarkImageView.setBlendMode( BlendMode.EXCLUSION );

        // Hide the watermark until the main overlay image is loaded.
        watermarkImageView.setVisible( false );

        Platform.runLater( () -> {
            // Center the watermark vertically, to minimize the chance that a
            // narrow bounding box (extents) will result in a watermark that
            // lies completely outside the main area of data magnitude in the
            // associated overlay image for an application visualization layer.
            watermarkImageView.layoutXProperty()
                              .bind( mainOverlayImageView.layoutXProperty() );
            watermarkImageView.layoutYProperty()
                              .bind( mainOverlayImageView.layoutYProperty()
                                                         .add( ( mainOverlayImageView.fitHeightProperty()
                                                                                     .subtract(
                                                                                             watermarkImageView.fitHeightProperty() )
                                                                                     .multiply(
                                                                                             0.5d ) ) ) );
        } );
    }

    /*
     * Export the raw overlay image (but not the surrounding context) to a file
     * using the specified image format.
     */
    public final boolean exportMainOverlayImage( final OutputStream outputStream,
                                                 final String imageFormat ) {
        boolean succeeded = false;
        try {
            final Image image = getMainOverlayImage();
            final BufferedImage bufferedImage = SwingFXUtils.fromFXImage( image,
                                                                          null );
            succeeded = ImageIO.write( bufferedImage,
                                       imageFormat,
                                       outputStream );

            // Cleanup.
            outputStream.flush();
        }
        catch ( final IOException ioe ) {
            ioe.printStackTrace();
            return false;
        }

        return succeeded;
    }

    /**
     * Returns the Main Overlay Image.
     *
     * @return The Main Overlay Image.
     */
    public final Image getMainOverlayImage() {
        return mainOverlayImageView.getImage();
    }

    public final void setMainOverlayImage( final Image image ) {
        mainOverlayImageView.setImage( image );
    }

    /**
     * Returns the Main Overlay Image View.
     *
     * @return The Main Overlay Image View.
     */
    public final ImageView getMainOverlayImageView() {
        return mainOverlayImageView;
    }

    /**
     * @return True if the Main Overlay Image is non-null
     */
    public final boolean isMainOverlayImageValid() {
        final Image mainOverlayImage = getMainOverlayImage();
        return mainOverlayImage != null;
    }

    /**
     * Loads a raw overlay image from a supplied input stream.
     *
     * @param inputStream The input stream containing the raw overlay image
     * @param extents     The Plane extents (in user units) used for the overlay
     *                    image
     * @return True if the input stream was non-null
     */
    public final boolean loadMainOverlayImage( final InputStream inputStream,
                                               final Extents2DProperties extents ) {
        // Avoid throwing unnecessary exceptions by not attempting to open bad
        // input streams.
        if ( inputStream == null ) {
            return false;
        }

        // Convert image bounds to pixels, as those are the units for images.
        final Bounds imageBounds = BoundsUtilities.boundsFromExtents( extents );
        final Bounds imageBoundsPx = getVenueToDisplayTransform().transform(
                imageBounds );

        // Apply the Aspect Ratio during Main Overlay Image Loading, and match
        // it to the current chart overlay bounds by settings its dimensions to
        // match the axes dimensions and its position to match the axis offsets.
        // NOTE: We might need to switch to a PixelReader/PixelWriter upscaler
        //  at some point, if the Image constructor ever changes its behavior.
        // TODO: Find a way to bind the chart and/or axes to the image, and
        //  hand it just the width along with -1 for height so the Aspect Ratio
        //  gets used. Currently, some downstream uses display a bit too wide.
        final Image image = ImageUtilities.loadImageFromStream( inputStream,
                                                                true,
                                                                -1d,
                                                                imageBoundsPx.getWidth(),
                                                                imageBoundsPx.getHeight(),
                                                                false );
        setMainOverlayImage( image );

        // Adjust the location and size to match the bounds of the container.
        adjustMainOverlayImage( extents );

        return true;
    }

    /**
     * Adjust the raw overlay image for the provided clipping boundary.
     *
     * @param extents The extents (in user units) used for the overlay image
     */
    public final void adjustMainOverlayImage( final Extents2DProperties extents ) {
        // Adjust the location and size to match the bounds of the container.
        final double imageWidth = extents.getWidth();
        final double imageHeight = extents.getHeight();
        mainOverlayImageView.setLayoutX( extents.getX() );
        mainOverlayImageView.setLayoutY( extents.getY() );
        mainOverlayImageView.setFitWidth( imageWidth );
        mainOverlayImageView.setFitHeight( imageHeight );

        // Make the watermark visible when a new overlay image is loaded.
        if ( useWatermark ) {
            final double imageAspectRatio = imageHeight / imageWidth;
            if ( imageAspectRatio < 1.0d ) {
                if ( imageAspectRatio >= WATERMARK_ASPECT_RATIO ) {
                    watermarkImageView.setFitWidth( imageWidth );
                    watermarkImageView.setFitHeight(
                            imageWidth * WATERMARK_ASPECT_RATIO );
                }
                else {
                    watermarkImageView.setFitHeight( imageHeight );
                    watermarkImageView.setFitWidth(
                            imageHeight / WATERMARK_ASPECT_RATIO );
                }
            }
            else {
                // NOTE: We already know the watermark ratio is width-biased.
                watermarkImageView.setFitWidth( imageWidth );
                watermarkImageView.setFitHeight(
                        imageWidth * WATERMARK_ASPECT_RATIO );
            }

            watermarkImageView.setVisible( true );
        }
    }

    /**
     * Removes the Main Overlay Image by setting the Image View to a null Image
     * reference.
     */
    public final void removeMainOverlayImage() {
        // Blank out the image in case the layout visibility isn't hidden --
        // most often in cases where we no longer have a valid correspondence
        // with a prediction response.
        mainOverlayImageView.setImage( null );

        // Make the watermark invisible when the Main Overlay Image is removed.
        if ( useWatermark ) {
            watermarkImageView.setVisible( false );
        }
    }
}
