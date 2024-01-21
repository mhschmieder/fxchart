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
 * FxChartToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxcharttoolkit
 */
package com.mhschmieder.fxcharttoolkit.action;

import java.util.Arrays;
import java.util.Collection;

import org.controlsfx.control.action.Action;

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxguitoolkit.action.FileActions;
import com.mhschmieder.fxguitoolkit.action.LabeledActionFactory;
import com.mhschmieder.fxguitoolkit.action.SettingsActions;
import com.mhschmieder.fxguitoolkit.action.XActionGroup;

import javafx.scene.paint.Color;

/**
 * This is a struct-like container for actions used by Polar Response.
 */
public final class PolarResponseActions {

    public FileActions     _fileActions;
    public PolarResponseViewActions     _viewActions;
    public SettingsActions _settingsActions;
    public TestActions     _testActions;

    public PolarResponseActions( final ClientProperties pClientProperties ) {
        _fileActions = new FileActions( pClientProperties );
        _viewActions = new PolarResponseViewActions( pClientProperties );
        _settingsActions = new SettingsActions( pClientProperties );
        _testActions = new TestActions( pClientProperties );
    }

    public Collection< Action > getBackgroundColorChoiceCollection() {
        // Forward this method to the Settings actions container.
        return _settingsActions.getBackgroundColorChoiceCollection();
    }

    public Collection< Action > getExportActionCollection() {
        // Forward this method to the File actions container.
        return _fileActions.getExportActionCollection( true, true );
    }

    public Collection< Action > getFileActionCollection( final ClientProperties pClientProperties ) {
        // Forward this method to the File actions container.
        return _fileActions.getFileActionCollection( pClientProperties, true, true );
    }

    public Collection< Action > getPolarResponseContextMenuActionCollection( final ClientProperties pClientProperties ) {
        final Collection< Action > polarResponseContextMenuActionCollection = _viewActions
                .getViewActionCollection( pClientProperties );

        return polarResponseContextMenuActionCollection;
    }

    public Collection< Action > getPolarResponseMenuBarActionCollection( final ClientProperties pClientProperties ) {
        final XActionGroup fileActionGroup = LabeledActionFactory
                .getFileActionGroup( pClientProperties, _fileActions, true, true );

        final XActionGroup viewActionGroup = PolarResponseLabeledActionFactory
                .getViewActionGroup( pClientProperties, _viewActions );

        final XActionGroup settingsActionGroup = LabeledActionFactory
                .getSettingsActionGroup( pClientProperties, _settingsActions, true );

        final XActionGroup testActionGroup = PolarResponseLabeledActionFactory
                .getTestActionGroup( pClientProperties, _testActions );

        final Collection< Action > polarResponseMenuBarActionCollection = Arrays
                .asList( fileActionGroup, viewActionGroup, settingsActionGroup, testActionGroup );

        return polarResponseMenuBarActionCollection;
    }

    public Collection< Action > getScaleChoiceCollection() {
        // Forward this method to the View actions container.
        return _viewActions.getScaleChoiceCollection();
    }

    public String getSelectedBackgroundColorName() {
        // Forward this method to the Settings actions container.
        return _settingsActions.getSelectedBackgroundColorName();
    }

    public Collection< Action > getSettingsActionCollection( final ClientProperties pClientProperties ) {
        // Forward this method to the File actions container.
        return _settingsActions.getSettingsActionCollection( pClientProperties, true );
    }

    public Collection< Action > getTestActionCollection() {
        // Forward this method to the Test actions container.
        return _testActions.getTestActionCollection();
    }

    public Collection< Action > getViewActionCollection( final ClientProperties pClientProperties ) {
        // Forward this method to the View actions container.
        return _viewActions.getViewActionCollection( pClientProperties );
    }

    public Collection< Action > getWindowSizeActionCollection() {
        // Forward this method to the Settings actions container.
        return _settingsActions.getWindowSizeActionCollection( true );
    }

    public Color selectBackgroundColor( final String backgroundColorName ) {
        // Forward this method to the Settings actions container.
        return _settingsActions.selectBackgroundColor( backgroundColorName );
    }

    public void setGridSpacing( final int gridSpacing ) {
        // Forward this method to the View actions container.
        _viewActions.setGridSpacing( gridSpacing );
    }
}
