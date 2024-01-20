/**
 * MIT License
 *
 * Copyright (c) 2020, 2024 Mark Schmieder
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
 * Project: https://github.com/mhschmieder/fxcharttoolkit
 */
package com.mhschmieder.fxcharttoolkit.stage;

import java.awt.EventQueue;
import java.awt.RenderingHints;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.SwappedDataInputStream;

import com.mhschmieder.acousticstoolkit.FrequencyRange;
import com.mhschmieder.acousticstoolkit.FrequencySignalUtilities;
import com.mhschmieder.acousticstoolkit.RelativeBandwidth;
import com.mhschmieder.charttoolkit.layout.PolarAmplitudeChart;
import com.mhschmieder.charttoolkit.layout.SemiLogRPolarChart;
import com.mhschmieder.commonstoolkit.branding.ProductBranding;
import com.mhschmieder.commonstoolkit.io.FileStatus;
import com.mhschmieder.commonstoolkit.io.FileUtilities;
import com.mhschmieder.commonstoolkit.io.IoUtilities;
import com.mhschmieder.commonstoolkit.net.DataServerResponse;
import com.mhschmieder.commonstoolkit.net.ServerRequestProperties;
import com.mhschmieder.commonstoolkit.security.ServerLoginCredentials;
import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxcharttoolkit.action.PolarResponseActions;
import com.mhschmieder.fxcharttoolkit.concurrent.PolarDataRequestService;
import com.mhschmieder.fxcharttoolkit.control.PolarResponseMenuFactory;
import com.mhschmieder.fxcharttoolkit.control.PolarResponseToolBar;
import com.mhschmieder.fxcharttoolkit.layout.PolarResponsePane;
import com.mhschmieder.fxcharttoolkit.net.PolarDataRequestParameters;
import com.mhschmieder.fxcharttoolkit.swing.PolarResponsePanel;
import com.mhschmieder.fxguitoolkit.action.BackgroundColorChoices;
import com.mhschmieder.fxguitoolkit.control.TextSelector;
import com.mhschmieder.fxguitoolkit.dialog.DialogUtilities;
import com.mhschmieder.fxguitoolkit.stage.DataRequestStatusViewer;
import com.mhschmieder.fxguitoolkit.stage.ExtensionFilterUtilities;
import com.mhschmieder.fxguitoolkit.stage.ExtensionFilters;
import com.mhschmieder.fxguitoolkit.stage.RenderedGraphicsExportPreview;
import com.mhschmieder.fxguitoolkit.stage.XStage;
import com.mhschmieder.graphicstoolkit.GraphicsUtilities;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;

/**
 * A viewer for side-by-side horizontal and vertical polar response charts.
 * <p>
 * Clients can derive from this class to add additional domain-specific features.
 */
public class PolarResponseViewer extends XStage {


    public static final String POLAR_RESPONSE_FRAME_TITLE_DEFAULT   = "Polar Response Viewer"; //$NON-NLS-1$

    // Default window locations and dimensions.
    public static final int    POLAR_RESPONSE_VIEWER_X_DEFAULT      = 20;
    public static final int    POLAR_RESPONSE_VIEWER_Y_DEFAULT      = 20;
    public static final int    POLAR_RESPONSE_VIEWER_WIDTH_DEFAULT  = 980;
    public static final int    POLAR_RESPONSE_VIEWER_HEIGHT_DEFAULT = 680;

    // Handle the Polar Response prediction servlet's HTTP and/or authorization
    // status, and echo the formatted error response to the user if an HTTP
    // error code is detected.
    private static final boolean handleDataServerResponse( final DataServerResponse dataServerResponse ) {
        // If there were any server errors, report to the session log.
        final String serverStatusMessage = dataServerResponse.getServerStatusMessage();
        if ( serverStatusMessage != null ) {
            System.err.println( serverStatusMessage );
        }

        // If there were any servlet errors, report to the session log.
        final String servletErrorMessage = dataServerResponse.getServletErrorMessage();
        if ( servletErrorMessage != null ) {
            System.err.println( servletErrorMessage );
        }

        // Get the combined HTTP Response returned by the URL Connection.
        final String httpResponse = dataServerResponse.getHttpResponse();

        // Switch on the HTTP Response Code returned by the URL Connection.
        final int httpResponseCode = dataServerResponse.getHttpResponseCode();
        switch ( httpResponseCode ) {
        case HttpURLConnection.HTTP_OK:
            // Nothing to do; don't return a message as that means an error
            // was seen on the server.
            return true;
        case HttpURLConnection.HTTP_INTERNAL_ERROR:
            // Don't punish or confuse the user if there was an internal
            // server error, as we don't let those affect downstream data.
            // The error message was already dumped to the log.
            return true;
        case HttpURLConnection.HTTP_PRECON_FAILED:
        case HttpURLConnection.HTTP_UNAUTHORIZED:
        case HttpURLConnection.HTTP_NO_CONTENT:
        case HttpURLConnection.HTTP_NOT_FOUND:
        default:
            // Notify the user of this error via the session log.
            System.err.println( httpResponse );

            // Propagate the error to the caller to avoid loading
            // non-existent resources.
            return false;
        }
    }

    // Declare the actions.
    public PolarResponseActions                    _actions;

    // Declare the main tool bar.
    public PolarResponseToolBar                    _toolBar;

    // Declare an uninitialized pane to contain the Polar Responses.
    public PolarResponsePane                       _polarResponsePane;

    // Declare uninitialized stages.
    protected DataRequestStatusViewer    _dataRequestStatusViewer;
    protected RenderedGraphicsExportPreview _renderedGraphicsExportPreview;
    
    // Cache the Acoustic Source Model Selector so that it can be passed by
    // the constructor but valid by the time the tool bar is loaded.
    protected final TextSelector acousticSourceModelSelector;


    // Declare a Swing preview panel for Polar Response exports.
    protected PolarResponsePanel                   _polarResponsePreviewPanel;

    // Declare an unbounded byte array to hold server responses.
    // TODO: Switch to memory-mapping, for potential efficiency?
    protected byte[]                               _serverResponseData;

    // Cache the acoustic source model to use for the polar response request.
    protected String                               acousticSourceModel;

    // Declare a structure to hold the most recent frequency range used for
    // prediction.
    protected FrequencyRange                       _frequencyRange;

    // Cache the Service associated with Polar Plot update requests.
    protected PolarDataRequestService        _polarDataRequestService;

    // Cache a global reference to the most recent Login Credentials.
    private final ServerLoginCredentials       _loginCredentials;
    
    // Cache a flag for whether we use extended range or not.
    private final boolean useExtendedRange;

    /**
     * Cache the Server Request Properties, which are static except for Login.
     */
    private final ServerRequestProperties          serverRequestProperties;

    public PolarResponseViewer( final ServerLoginCredentials loginCredentials,
                                final ProductBranding productBranding,
                                final ClientProperties pClientProperties,
                                final TextSelector pAcousticSourceModelSelector,
                                final String pAcousticSourceModelDefault,
                                final boolean pUseExtendedRange,
                                final ServerRequestProperties pServerRequestProperties ) {
        // Always call the superclass constructor first!
        super( POLAR_RESPONSE_FRAME_TITLE_DEFAULT,
               "polarResponse",
               false,
               false,
               true,
               productBranding,
               pClientProperties );
        
        acousticSourceModelSelector = pAcousticSourceModelSelector;

        _loginCredentials = loginCredentials;
        serverRequestProperties = pServerRequestProperties;
        
        useExtendedRange = pUseExtendedRange;

        _defaultTitle = new StringBuilder( POLAR_RESPONSE_FRAME_TITLE_DEFAULT );

        acousticSourceModel = pAcousticSourceModelDefault;
        _frequencyRange = new FrequencyRange();

        try {
            initStage();
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    // Add all of the relevant action handlers.
    @Override
    protected final void addActionHandlers() {
        // Load the action handlers for the "File" actions.
        _actions._fileActions._closeWindowAction.setEventHandler( evt -> doCloseWindow() );
        _actions._fileActions._pageSetupAction.setEventHandler( evt -> doPageSetup() );
        _actions._fileActions._printAction.setEventHandler( evt -> doPrint() );

        // Load the action handlers for the "Export" actions.
        _actions._fileActions._exportActions._exportImageGraphicsAction
                .setEventHandler( evt -> doExportImageGraphics() );
        _actions._fileActions._exportActions._exportVectorGraphicsAction
                .setEventHandler( evt -> doExportVectorGraphics() );
        _actions._fileActions._exportActions._exportRenderedGraphicsAction
                .setEventHandler( evt -> doExportRenderedGraphics() );

        // Load the action handlers for the "Scale" choices.
        _actions._viewActions._scaleChoices._scaleDiv5dbChoice.setEventHandler( evt -> doDiv5db() );
        _actions._viewActions._scaleChoices._scaleDiv6dbChoice.setEventHandler( evt -> doDiv6db() );
        _actions._viewActions._scaleChoices._scaleDiv10dbChoice
                .setEventHandler( evt -> doDiv10db() );

        // Load the action handlers for the "Background Color" choices.
        addBackgroundColorChoiceHandlers( _actions._settingsActions._backgroundColorChoices );

        // Load the action handlers for the "Window Size" actions.
        addWindowSizeActionHandlers( _actions._settingsActions._windowSizeActions );

        // Load the action handlers for the "Test" actions.
        _actions._testActions._saveServerResponseAction
                .setEventHandler( evt -> doSaveServerResponse() );
    }

    // Add the Tool Bar's event listeners.
    // TODO: Use appropriate methodology to add an action linked to both
    //  the toolbar buttons and their associated menu items, so that when one
    //  is disabled the other is as well. Is this already true of what we do?
    @Override
    protected final void addToolBarListeners() {
        // Load the event handler for the Acoustic Source Model Selector.
        _toolBar.acousticSourceModelSelector.setOnAction( evt -> {
            // Set the new Acoustic Source Model, if the user didn't cancel.
            final String newAcousticSourceModel = _toolBar.getAcousticalSourceModel();
            if ( ( newAcousticSourceModel != null ) 
                    && !newAcousticSourceModel.trim().isEmpty() ) {
                setAcousticSourceModel( newAcousticSourceModel );
            }
        } );

        // Load the event handler for the Relative Bandwidth Selector.
        _toolBar.frequencyRangeControls._relativeBandwidthSelector.setOnAction( evt -> {
            // Set the new Relative Bandwidth, if the user didn't cancel
            // the Combo Box.
            final RelativeBandwidth relativeBandwidth = _toolBar.getRelativeBandwidth();
            if ( relativeBandwidth != null ) {
                setRelativeBandwidth( relativeBandwidth );
            }
        } );

        // Load the event handler for the Octave Range Selector.
        _toolBar.frequencyRangeControls._octaveRangeSelector.setOnAction( evt -> {
            // Set the new Octave Range, if the user didn't cancel the
            // Combo Box.
            final String octaveRange = _toolBar.getOctaveRange();
            if ( ( octaveRange != null ) && !octaveRange.trim().isEmpty() ) {
                setOctaveRange( octaveRange );
            }
        } );

        // Load the event handler for the Center Frequency Selector.
        _toolBar.frequencyRangeControls._centerFrequencySelector.setOnAction( evt -> {
            // Set the new center frequency, if the user didn't cancel.
            final String sCenterFrequency = _toolBar.getCenterFrequency();
            if ( ( sCenterFrequency != null ) && !sCenterFrequency.trim().isEmpty() ) {
                final double centerFrequency = FrequencySignalUtilities
                        .expandMetricAbbreviatedFrequency( sCenterFrequency, _numberParse );
                setCenterFrequency( centerFrequency );
            }
        } );
    }

    protected final void deiconifyPolarResponse() {
        // De-iconify the Polar Response Stage, but don't bring it to the front,
        // as that interferes with the user workflow (that is, leave it in the
        // foreground or background rather than force it to one or the other,
        // unless iconified).
        if ( isIconified() ) {
            setIconified( false );
        }
    }

    public final void doDiv10db() {
        // Set polar grid spacing to 10 dB increments, and match the grid range
        // to roughly 6 increments at 60 dB (includes headroom).
        updateGridSpacing( 10 );
        updateGridRange( 60d );
    }

    public final void doDiv5db() {
        // Set polar grid spacing to 5 dB increments, and match the grid range
        // to roughly 8 increments at 40 dB (includes headroom).
        updateGridSpacing( 5 );
        updateGridRange( 40d );
    }

    public final void doDiv6db() {
        // Set polar grid spacing to 6 dB increments, and match the grid range
        // to roughly 8 increments at 48 dB (includes headroom).
        updateGridSpacing( 6 );
        updateGridRange( 48d );
    }

    public final void doExportRenderedGraphics() {
        // Update the Graphics Category for the Export Button tool tip.
        final String graphicsCategory = "Polar Response";
        _renderedGraphicsExportPreview.setGraphicsCategory( graphicsCategory );

        // Update the target Swing Component to sync with the related JavaFX
        // fields and data model settings.
        updateRenderedGraphicsExportSource();

        // Make sure the previously selected options immediately take hold.
        _renderedGraphicsExportPreview.updateView();

        // Bring up the modal Formatted Graphics Export Preview and wait.
        _renderedGraphicsExportPreview.showAndWait();

        // Detect whether the user canceled the Export action.
        if ( _renderedGraphicsExportPreview.isCanceled() ) {
            // Clean up memory from the preview window.
            cancelRenderedGraphicsExport();
        }

        // We need to reassert this window in front after dismissing a modal
        // secondary window, or else the window most recently made visible is
        // brought to front by OS-level window management methods (especially
        // when running under macOS).
        toFront();
    }

    protected final void doExportImageGraphics() {
        // Switch on export context, so we know which type of data and format to
        // save.
        final String imageCategory = "Polar Response";
        fileExportImageGraphics( imageCategory );
    }

    protected final void doExportVectorGraphics() {
        // Switch on export context, so we know which type of data and format to
        // save.
        final String graphicsCategory = "Polar Response";
        fileExportVectorGraphics( graphicsCategory );
    }

    public final void doSaveServerResponse() {
        // Invoke the common prediction method, then save the returned ZIP
        // file to disc.
        saveServerResponse();
    }

    // Hide secondary windows that depend on state (e.g. project cache).
    @Override
    public final void hideSecondaryWindows() {
        _renderedGraphicsExportPreview.setVisible( false );
    }

    @SuppressWarnings("nls")
    protected final void initStage() {
        // First have the superclass initialize its content.
        initStage( "/icons/mhschmieder/PolarResponse16.png",
                   POLAR_RESPONSE_VIEWER_WIDTH_DEFAULT,
                   POLAR_RESPONSE_VIEWER_HEIGHT_DEFAULT,
                   true );
    }

    // Load the relevant actions for this Stage.
    @Override
    protected final void loadActions() {
        // Make all of the actions.
        _actions = new PolarResponseActions( clientProperties );
    }

    @Override
    protected final void loadAllStages() {
        super.loadAllStages();

        // Instantiate the modal Prediction Request Status Viewer.
        // NOTE: This window is Application Modal vs. Window Modal, so doesn't
        //  set a Window Owner. We set to Application Modal so that the user
        //  can't change parameters in any application windows (not just the
        //  Sound Field) and thus confuse the correlation with the prediction
        //  responses. Only once the prediction is loaded is this unblocked.
        // NOTE: We switched to Window Modal as Application Modal makes the
        //  Sound Field self-dispose when in Full Screen Mode.
        _dataRequestStatusViewer = new DataRequestStatusViewer( Modality.WINDOW_MODAL,
                                                                _productBranding,
                                                                clientProperties,
                                                                "Server Connection Issues",
                                                                "/icons/mhschmieder/PolarResponse16.png",
                                                                "/icons/mhschmieder/PolarResponse16.png" );
        _windowManager.addStage( _dataRequestStatusViewer );

       // Instantiate the modal Rendered Graphics Export Preview.
        _renderedGraphicsExportPreview = new RenderedGraphicsExportPreview( getRenderedGraphicsExportAuxiliaryLabel(),
                                                                            getRenderedGraphicsExportInformationTablesLabel(),
                                                                            getRenderedGraphicsExportOptionalItemLabel(),
                                                                            _productBranding,
                                                                            clientProperties );
        _renderedGraphicsExportPreview.initOwner( this );
        _windowManager.addStage( _renderedGraphicsExportPreview );
    }

    @Override
    protected final Node loadContent() {
        // Instantiate and return the custom Content Node.
        // TODO: Pass in the angle increment in degrees, to the viewer's constructor.
        _polarResponsePane = new PolarResponsePane( POLAR_RESPONSE_VIEWER_WIDTH_DEFAULT,
                                                    POLAR_RESPONSE_VIEWER_HEIGHT_DEFAULT,
                                                    1.0d, // data resolution is one degree
                                                    clientProperties,
                                                    this,
                                                    _actions );

        final BorderPane contentPane = new BorderPane();
        contentPane.setPadding( new Insets( 5.0d ) );
        contentPane.setCenter( _polarResponsePane );
        return contentPane;
    }

    // Add the Menu Bar for this Stage.
    @Override
    protected final MenuBar loadMenuBar() {
        // Build the Menu Bar for this Stage.
        final MenuBar menuBar = PolarResponseMenuFactory.getPolarResponseMenuBar( clientProperties,
                                                                                  _actions );

        // Return the Menu Bar so the superclass can use it.
        return menuBar;
    }

    // Take care of any extensions specific to this sub-class.
    @Override
    public final FileStatus fileSaveExtensions( final File file,
                                                final File tempFile ) {
        // Pre-declare the File Save status in case of exceptions.
        FileStatus fileStatus = FileStatus.WRITE_ERROR;

        // TODO: Switch these and others to Apache Commons I/O library, which
        // has a SuffixFileFilter with accept() methods.
        final String fileName = file.getName();
        final String fileNameCaseInsensitive = fileName.toLowerCase( Locale.ENGLISH );
        try {
            // NOTE: This library doesn't have a File Mode to look at, for cases
            //  where a file extension might be targeted to multiple purposes,
            //  so we assume for now that we are saving a server response.
            if ( FilenameUtils.isExtension( fileNameCaseInsensitive, "zip" ) ) {
                // Save the server response (a ZIP file cached earlier to a
                // local byte array) directly to disc.
                //
                // Chain a ZipOutputStream to a BufferedOutputStream to a
                // FileOutputStream, for better performance and to properly
                // deflate ZIP entries.
                try ( final FileOutputStream fileOutputStream =
                                                              new FileOutputStream( tempFile );
                        final BufferedOutputStream bufferedOutputStream =
                                                                        new BufferedOutputStream( fileOutputStream );
                        final ZipOutputStream zipOutputStream =
                                                              new ZipOutputStream( bufferedOutputStream ) ) {
                    fileStatus = IoUtilities.saveByteArrayToZip( _serverResponseData,
                                                                 _productBranding,
                                                                 clientProperties.locale,
                                                                 zipOutputStream );
                }
            }
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }

        return fileStatus;
    }

    // Load all of the User Preferences for this Stage.
    // TODO: Make a class with get/set methods for user preferences, a la
    //  Listing 3.3 on p. 37 of "More Java Pitfalls" (Wiley), and including
    //  static default values for better modularity.
    @Override
    public final void loadPreferences() {
        // Get the user node for this package/class, so that we get the
        // preferences specific to this frame and user.
        final Preferences prefs = Preferences.userNodeForPackage( getClass() );

        final String sRelativeBandwidth = prefs.get( "relativeBandwidth",
                                                     FrequencyRange.RELATIVE_BANDWIDTH_DEFAULT );
        final String sOctaveRange = prefs.get( "octaveRange",
                                               FrequencyRange.OCTAVE_RANGE_WIDE_DEFAULT );
        final double centerFrequencyDefault = FrequencyRange
                .getNominalCenterFrequencyDefaultForOctaveRange( sOctaveRange, false );
        final double centerFrequency = prefs.getDouble( "centerFrequency", centerFrequencyDefault );

        // Create and set the visualization parameters.
        final String backgroundColor = prefs
                .get( "backgroundColor", BackgroundColorChoices.DEFAULT_BACKGROUND_COLOR_NAME );
        final int gridSpacing = prefs.getInt( "gridSpacing",
                                              SemiLogRPolarChart.DEFAULT_GRID_SPACING );

        // Load the Default Directory from User Preferences.
        final File defaultDirectory = FileUtilities.loadDefaultDirectoryPreferences( prefs );

        // Forward the preferences data from the stored preferences to the
        // common preferences handler.
        updatePreferences( backgroundColor,
                           RelativeBandwidth.fromPresentationString( sRelativeBandwidth ),
                           sOctaveRange,
                           centerFrequency,
                           gridSpacing,
                           defaultDirectory );
    }

    private final void loadServerResponse() {
        // Update the prediction response from the server-loaded data.
        final String loadDataStatusMessage = loadServerResponseDataFromZip();

        // If the file is write-protected, or is denied access by a security
        // manager, report the error and exit this prediction handler.
        if ( loadDataStatusMessage != null ) {
            // Alert the user that there were problems with the file read.
            DialogUtilities.showFileReadErrorAlert( loadDataStatusMessage );
            return;
        }
        
        // De-iconify the Polar Response Stage.
        deiconifyPolarResponse();
    }

    // This method updates prediction response data from a cached ZIP stream
    // already loaded from the server.
    // TODO: Make sure all zip load/save methods have an error exit for
    //  closing the original ZIP stream.
    protected final String loadServerResponseDataFromZip() {
        // Cycle through the ZIP stream until all of the responses are loaded.
        //
        // Chain a ZipInputStream to a ByteArrayInputStream to the byte array
        // copied from the original servlet response stream, to inflate the ZIP
        // entries.
        //
        // NOTE: We suppress warnings on the SwappedDataInputStream used for
        //  ENDIAN-aware loading of server-side binary data, as the compiler
        //  isn't smart enough to know that using try-with-resources inside the
        //  ZIP block will prematurely close the ZIP stream due to how the ZIP
        //  API deals with wrapping ZIP entries around the whole ZIP file itself
        //  (thus closing a ZIP entry closes the ZIP file, if one isn't careful).
        //  This is all due to the Apache I/O library not having been rewritten
        //  yet for Java 7, so it doesn't implement the methods the compiler
        //  looks for to determine awareness of resource handling.
        String statusMessage = null;
        try ( final ByteArrayInputStream byteArrayInputStream =
                                                              new ByteArrayInputStream( _serverResponseData );
                final ZipInputStream zipInputStream = new ZipInputStream( byteArrayInputStream ) ) {
            ZipEntry zipEntry = null;
            while ( ( zipEntry = zipInputStream.getNextEntry() ) != null ) {
                final String name = zipEntry.getName();
                if ( "PolarResponseHz.bin".equalsIgnoreCase( name ) ) { //$NON-NLS-1$
                    // Update the screen graphics to reflect the prediction
                    // response.
                    //
                    // Write the horizontal polar response back to the Polar
                    // Plot graphics panel.
                    //
                    // Chain a SwappedDataInputStream to BufferedInputStream to
                    // a ZipInputStream, for better performance and
                    // platform-independent floating-point reads.
                    @SuppressWarnings("resource") final SwappedDataInputStream littleEndianInputStream =
                                                                                                       new SwappedDataInputStream( new BufferedInputStream( zipInputStream ) );
                    if ( !updateHorizontalPolarResponse( littleEndianInputStream,
                                                         acousticSourceModel,
                                                         _frequencyRange.getRelativeBandwidth(),
                                                         _frequencyRange.getCenterFrequency() ) ) {
                        statusMessage =
                                      "Missing, Incomplete, or Invalid Horizontal Polar Response."; //$NON-NLS-1$
                    }
                }
                else if ( "PolarResponseVt.bin".equalsIgnoreCase( name ) ) { //$NON-NLS-1$
                    // Update the screen graphics to reflect the prediction
                    // response.
                    //
                    // Write the vertical polar response back to the Polar
                    // Plot graphics panel.
                    //
                    // Chain a SwappedDataInputStream to a BufferedInputStream
                    // to a ZipInputStream, for better performance and
                    // platform-independent floating-point reads.
                    @SuppressWarnings("resource") final SwappedDataInputStream littleEndianInputStream =
                                                                                                       new SwappedDataInputStream( new BufferedInputStream( zipInputStream ) );
                    if ( !updateVerticalPolarResponse( littleEndianInputStream,
                                                       acousticSourceModel,
                                                       _frequencyRange.getRelativeBandwidth(),
                                                       _frequencyRange.getCenterFrequency() ) ) {
                        statusMessage = "Missing, Incomplete, or Invalid Vertical Polar Response."; //$NON-NLS-1$
                    }
                }

                // Close the current ZIP entry to prepare to read the next one.
                zipInputStream.closeEntry();
            }
        }
        catch ( final NullPointerException npe ) {
            npe.printStackTrace();
            statusMessage = "Missing, Incomplete, or Invalid Response Data."; //$NON-NLS-1$
        }
        catch ( final ZipException ze ) {
            ze.printStackTrace();
            statusMessage = "Response Data Zip File Corrupt or Incorrect Format."; //$NON-NLS-1$
        }
        catch ( final IOException ioe ) {
            ioe.printStackTrace();
            statusMessage = "File Read Error: Response Data Zip File Not Loaded."; //$NON-NLS-1$
        }

        return statusMessage;
    }

    // Add the Tool Bar for this Stage.
    @Override
    public final ToolBar loadToolBar() {
        // Build the Tool Bar for this Stage.
        _toolBar = new PolarResponseToolBar( _numberFormat, 
                                             clientProperties,
                                             acousticSourceModelSelector,
                                             useExtendedRange );

        // Return the Tool Bar so the superclass can use it.
        return _toolBar;
    }

    protected final void makePredictPolarResponseService() {
        // Execute the Data Update Request via a Reusable Service.
        _polarDataRequestService = new PolarDataRequestService( serverRequestProperties,
                                                                clientProperties,
                                                                _dataRequestStatusViewer );

        // Set the callbacks for handling success, failure, and cancellation.
         _polarDataRequestService.setOnSucceeded( t -> {
            // Handle any errors that we marked for post-processing.
            final DataServerResponse dataServerResponse = _polarDataRequestService
                    .getValue();
            final boolean sawErrors = !handleDataServerResponse( dataServerResponse );
            if ( !sawErrors ) {
                // Cache the Server Response Data before post-processing.
                _serverResponseData = dataServerResponse.getServerResponseData();
                if ( _serverResponseData == null ) {
                    return;
                }
    
                // Update the server-loaded prediction response data.
                loadServerResponse();
            }
        } );
    }

    // This common update handler, called by any of the tool bar drop lists in
    // the Polar Response Stage, effectively forks the thread that does the work
    // of the polar response data update request.
    protected final void updatePolarResponse() {
        // NOTE: Everything needs to be reset if a prediction is invoked.
        _polarResponsePane.resetVisualizations();

        // Make sure the prediction parameter sources are up to date.
        final String acousticSourceModelForDataRequest = getAcousticSourceModelForDataRequest();
        final PolarDataRequestParameters polarDataRequestParameters = new PolarDataRequestParameters( _loginCredentials,
                                                                                                      acousticSourceModelForDataRequest,
                                                                                                      _frequencyRange );
        _polarDataRequestService.setDataRequestParameters( polarDataRequestParameters );

        // Restart the Service as this also cancels old tasks and then resets.
        try {
            _polarDataRequestService.restart();
        }
        catch ( final IllegalStateException ise ) {
            ise.printStackTrace();
        }
    }
    
    /**
     * Returns the Acoustic Source Model name in the form that the data request
     * server expects, which may not match the version cached in this GUI class.
     * <p>
     * NOTE: Downstream clients should override this method to provide domain
     *  specific mapping logic between presentation names and data lookup names.
     * 
     * @return The Acoustic Source Model name in the form that the data request
     *         server expects
     */
    protected String getAcousticSourceModelForDataRequest() {
        return acousticSourceModel;
    }

    // Prepare the stage and its subsidiary components for input.
    @Override
    protected final void prepareForInput( final MenuBar menuBar ) {
        // Now it is safe to make legacy AWT/Swing content, without dangerous
        // side effects on startup timing of JavaFX stuff.
        EventQueue.invokeLater( () -> {
            // Make the main Swing layout panel once only at startup.
            // TODO: Pass the angle increment to the viewer's constructor.
            _polarResponsePreviewPanel =
                                       new PolarResponsePanel( POLAR_RESPONSE_VIEWER_WIDTH_DEFAULT,
                                                               POLAR_RESPONSE_VIEWER_HEIGHT_DEFAULT,
                                                               1.0d ); // data increment is one degree

            // Get Rendering Hints that aim for quality rendering of geometry.
            final RenderingHints renderingHints = GraphicsUtilities.getRenderingHintsForCharting();
            _polarResponsePreviewPanel.setRenderingHints( renderingHints );

            // Set the background to white, as EPS is paper-oriented.
            _polarResponsePreviewPanel.setForegroundFromBackground( java.awt.Color.WHITE );

            // Set the Graphics Export Source in the Graphics Export Preview
            // window, as it is made once and only needs to update its display.
            _renderedGraphicsExportPreview
                    .setRenderedGraphicsExportSource( _polarResponsePreviewPanel );
        } );

        // Make the reusable service for Polar Response prediction requests.
        makePredictPolarResponseService();

        // It is safer to set the general input preparation last, as method
        // overload may otherwise access uninitialized variables.
        super.prepareForInput( menuBar );
    }

    // Save all of the non-login user preferences for this frame.
    // TODO: Make a class with get/set methods for user preferences, a la
    //  Listing 3.3 on p. 37 of "More Java Pitfalls" (Wiley).
    @Override
    public final void savePreferences() {
        // Get the user node for this package/class, so that we get the
        // preferences specific to this frame and user.
        final Preferences prefs = Preferences.userNodeForPackage( getClass() );

        final String backgroundColor = _actions.getSelectedBackgroundColorName();
        prefs.put( "backgroundColor", backgroundColor );

        final RelativeBandwidth relativeBandwidth = _frequencyRange.getRelativeBandwidth();
        prefs.put( "relativeBandwidth", relativeBandwidth.toPresentationString() );
        final String octaveRange = _frequencyRange.getOctaveRange();
        prefs.put( "octaveRange", octaveRange );
        final double centerFrequency = _frequencyRange.getCenterFrequency();
        prefs.putDouble( "centerFrequency", centerFrequency );

        final int gridSpacing = _polarResponsePane.getGridSpacing();
        prefs.putInt( "gridSpacing", gridSpacing );

        // Save the Default Directory to User Preferences.
        FileUtilities.saveDefaultDirectoryPreferences( _defaultDirectory, prefs );
    }

    // This is a wrapper to ensure that all server response save actions are
    // treated uniformly. This version of the method uses JavaFX.
    private final void saveServerResponse() {
        // Throw up a file chooser for the server response filename.
        final String title = "Save Server Response As";
        final List< ExtensionFilter > extensionFilterAdditions = ExtensionFilterUtilities
                .getZipExtensionFilters();

        // Save a server response file using the selected filename.
        fileSaveAs( this,
                    title,
                    _defaultDirectory,
                    extensionFilterAdditions,
                    ExtensionFilters.ZIP_EXTENSION_FILTER,
                    null );
    }

    // Update the current frequency range to match the new octave range choice.
    public final void setCenterFrequency( final double centerFrequency ) {
        // Cache the current center frequency value.
        _frequencyRange.setCenterFrequency( centerFrequency );

        // The Center Frequency Selector serves as a short-cut to running a new
        // polar response data update request.
        updatePolarResponse();
    }

    @Override
    public final void setForegroundFromBackground( final Color backColor ) {
        // Take care of general styling first, as that also loads shared
        // variables.
        super.setForegroundFromBackground( backColor );

        // Forward this method to the Polar Response Pane.
        _polarResponsePane.setForegroundFromBackground( backColor );
    }

    public final void setFrequencyRange( final FrequencyRange frequencyRange ) {
        // Cache the new Frequency Range settings.
        // NOTE: If we disable 1/6 octave and beyond, we must rectify those
        //  values to 1/3 octave in case of loading older CSV and XML project
        //  file. Extended range clients can still use high resolution.
        //final String relativeBandwidth = frequencyRange.getRelativeBandwidth();
        _frequencyRange.setFrequencyRange( frequencyRange );

        // Sync all of the relevant GUI components to the new data model.
        _toolBar.updateFrequencyRange( frequencyRange );
    }

    public final void setGridRange( final double gridRange ) {
        // Sync up the traces with the new grid range.
        _polarResponsePane.setGridRange( gridRange );
    }

    public final void setGridSpacing( final int gridSpacing ) {
        // Sync up the Radio Button Menu Items with the current radial grid
        // spacing value.
        _actions.setGridSpacing( gridSpacing );

        // Sync up the traces with the new radial grid spacing.
        _polarResponsePane.setGridSpacing( gridSpacing );
    }

    public final void setAcousticSourceModel( final String pAcousticSourceModel ) {
        // Cache the current acoustic source model.
        acousticSourceModel = pAcousticSourceModel;

        // The Acoustic Source Model Selector serves as a short-cut to running
        // a new Polar Response data update request.
        updatePolarResponse();
    }

    // Update the current Frequency Range to match the new Octave Range choice.
    public final void setOctaveRange( final String sOctaveRange ) {
        // Cache the current Octave Range value.
        _frequencyRange.setOctaveRange( sOctaveRange );

        // Modify the Center Frequency drop-list for the current Relative
        // Bandwidth and Octave Range.
        // NOTE: This must be done on a deferred thread to avoid confusion of
        //  state during nested updated between related Frequency Range controls.
        final RelativeBandwidth relativeBandwidth = _frequencyRange.getRelativeBandwidth();
        final double centerFrequency = _frequencyRange.getCenterFrequency();
        Platform.runLater( () -> _toolBar
                .updateCenterFrequencyForBandwidthAndOctave( relativeBandwidth,
                                                             sOctaveRange,
                                                             centerFrequency,
                                                             true ) );
    }

    // Update the current Frequency Range to match the new Relative Bandwidth
    // choice.
    public final void setRelativeBandwidth( final RelativeBandwidth relativeBandwidth ) {
        // Cache the current Relative Bandwidth value.
        _frequencyRange.setRelativeBandwidth( relativeBandwidth );

        // Modify the Octave Range drop-list if necessary, and match the choice
        // to include the current Center Frequency when possible, using simple
        // range-checking vs. exact string-based matches as the supplied values
        // can differ based on the Relative Bandwidth (high vs. low resolution).
        // NOTE: The update to the Center Frequency list falls out of any
        //  callback generated by a change to the Octave Range, but we avoid
        //  side effects by trying to preserve the previous Octave Range.
        // NOTE: This must be done on a deferred thread to avoid confusion of
        //  state during nested updates between related Frequency Range controls.
        final String sOctaveRange = _frequencyRange.getOctaveRange();
        final double centerFrequency = _frequencyRange.getCenterFrequency();
        Platform.runLater( () -> {
            _toolBar.updateOctaveRangeForBandwidthAndFrequency( relativeBandwidth,
                                                                sOctaveRange,
                                                                centerFrequency );
            final String sOctaveRangeCorrected = _toolBar.getOctaveRange();
            _toolBar.updateCenterFrequencyForBandwidthAndOctave( relativeBandwidth,
                                                                 sOctaveRangeCorrected,
                                                                 centerFrequency,
                                                                 true );
        } );
    }

    @Override
    protected final void updateRenderedGraphicsExportSource() {
        // Grab the non-persistent values that need to be converted/forwarded.
        final float gridRange = _polarResponsePane.getGridRange();
        final int gridSpacing = _polarResponsePane.getGridSpacing();
        final double[] horizontalData = _polarResponsePane.getHorizontalPolarAmplitudeData();
        final double[] verticalData = _polarResponsePane.getVerticalPolarAmplitudeData();

        EventQueue.invokeLater( () -> {
            // Reset the non-persistent references that might have changed.
            _polarResponsePreviewPanel.setGridRange( gridRange );
            _polarResponsePreviewPanel.setGridSpacing( gridSpacing );

            // Update prediction response data from the visualization source.
            _polarResponsePreviewPanel
                    .updateHorizontalPolarResponse( horizontalData,
                                                    acousticSourceModel,
                                                    _frequencyRange.getRelativeBandwidth(),
                                                    _frequencyRange.getCenterFrequency() );
            _polarResponsePreviewPanel
                    .updateVerticalPolarResponse( verticalData,
                                                  acousticSourceModel,
                                                  _frequencyRange.getRelativeBandwidth(),
                                                  _frequencyRange.getCenterFrequency() );

            // Make sure the new settings and data are shown right away.
            _polarResponsePreviewPanel.repaint();
        } );
    }

    protected void updateGridRange( final double gridRange ) {
        // Sync up the plots with the current grid range value.
        setGridRange( gridRange );
    }

    protected void updateGridSpacing( final int gridSpacing ) {
        // Sync up the Radio Button Menu Items with the current radial grid
        // spacing value.
        setGridSpacing( gridSpacing );
    }

    public final boolean updateHorizontalPolarResponse( final SwappedDataInputStream inputStream,
                                                        final String loudspeakerModel,
                                                        final RelativeBandwidth relativeBandwidth,
                                                        final double centerFrequency ) {
        // Load the Horizontal Polar Response from the server input stream.
        // NOTE: To close the plot curve, we repeat the on-axis 0-degree data
        //  at 360 degrees, on the server and therefore in the loaded response.
        final int numberOfDataPoints = _polarResponsePane.getNumberOfPolarDataPoints();
        final double[] amplitude = new double[ numberOfDataPoints ];
        final boolean succeeded = IoUtilities
                .loadIntoDoubleArray( inputStream,
                                      amplitude,
                                      0,
                                      numberOfDataPoints,
                                      PolarAmplitudeChart.DEFAULT_AMPLITUDE );
        if ( !succeeded ) {
            return false;
        }

        // Update the Horizontal Polar Response Chart.
        _polarResponsePane.updateHorizontalPolarResponse( amplitude,
                                                          loudspeakerModel,
                                                          relativeBandwidth,
                                                          centerFrequency );

        return true;
    }

    // Update all of the user preferences for this stage.
    // TODO: Make a preferences object instead, with get/set methods, which can
    //  be set from CSV, XML, or stored user preferences?
    private final void updatePreferences( final String backgroundColorName,
                                          final RelativeBandwidth relativeBandwidth,
                                          final String sOctaveRange,
                                          final double centerFrequency,
                                          final int gridSpacing,
                                          final File defaultDirectory ) {
        // Set the Background Color for most layout content.
        final Color backgroundColor = _actions.selectBackgroundColor( backgroundColorName );
        setForegroundFromBackground( backgroundColor );

        _frequencyRange = new FrequencyRange( relativeBandwidth, sOctaveRange, centerFrequency );
        setFrequencyRange( _frequencyRange );

        // Set the appropriate grid spacing for polar response radial scale.
        setGridSpacing( gridSpacing );

        // Reset the default directory for local file operations.
        setDefaultDirectory( defaultDirectory );
    }

    public final boolean updateVerticalPolarResponse( final SwappedDataInputStream inputStream,
                                                      final String loudspeakerModel,
                                                      final RelativeBandwidth relativeBandwidth,
                                                      final double centerFrequency ) {
        // Load the Vertical Polar Response from the server input stream.
        // NOTE: To close the plot curve, we repeat the on-axis 0-degree data
        //  at 360 degrees, on the server and therefore in the loaded response.
        final int numberOfDataPoints = _polarResponsePane.getNumberOfPolarDataPoints();
        final double amplitude[] = new double[ numberOfDataPoints ];
        final boolean succeeded = IoUtilities
                .loadIntoDoubleArray( inputStream, amplitude, 0, numberOfDataPoints, 0.0d );
        if ( !succeeded ) {
            return false;
        }

        // Update the Vertical Polar Response Chart.
        _polarResponsePane.updateVerticalPolarResponse( amplitude,
                                                        loudspeakerModel,
                                                        relativeBandwidth,
                                                        centerFrequency );

        return true;
    }
}
