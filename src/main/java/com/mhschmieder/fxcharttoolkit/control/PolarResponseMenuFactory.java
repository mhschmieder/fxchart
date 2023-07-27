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
package com.mhschmieder.fxcharttoolkit.control;

import java.util.Collection;

import org.controlsfx.control.action.Action;

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxcharttoolkit.action.PolarResponseActions;
import com.mhschmieder.fxcharttoolkit.action.PolarResponseAmplitudeScaleChoices;
import com.mhschmieder.fxcharttoolkit.action.PolarResponseLabeledActionFactory;
import com.mhschmieder.fxcharttoolkit.action.TestActions;
import com.mhschmieder.fxcharttoolkit.action.ViewActions;
import com.mhschmieder.fxguitoolkit.action.XActionGroup;
import com.mhschmieder.fxguitoolkit.action.XActionUtilities;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

/**
 * This is a factory class for generating Menus for Polar Response.
 */
public final class PolarResponseMenuFactory {

    public static ContextMenu getPolarResponseContextMenu( final ClientProperties pClientProperties,
                                                           final PolarResponseActions polarResponseActions ) {
        final Collection< Action > polarResponseContextMenuActionCollection = polarResponseActions
                .getPolarResponseContextMenuActionCollection( pClientProperties );
        final ContextMenu polarResponseContextMenu = XActionUtilities
                .createContextMenu( polarResponseContextMenuActionCollection );
        return polarResponseContextMenu;
    }

    public static MenuBar getPolarResponseMenuBar( final ClientProperties pClientProperties,
                                                   final PolarResponseActions polarResponseActions ) {
        final Collection< Action > polarResponseMenuBarActionCollection = polarResponseActions
                .getPolarResponseMenuBarActionCollection( pClientProperties );
        final MenuBar polarResponseMenuBar = XActionUtilities
                .createMenuBar( polarResponseMenuBarActionCollection );
        return polarResponseMenuBar;
    }

    public static Menu getScaleMenu( final ClientProperties pClientProperties,
                                     final PolarResponseAmplitudeScaleChoices polarResponseAmplitudeScaleChoices ) {
        final XActionGroup scaleChoiceGroup = PolarResponseLabeledActionFactory
                .getScaleChoiceGroup( pClientProperties, polarResponseAmplitudeScaleChoices );
        final Menu scaleMenu = XActionUtilities.createMenu( scaleChoiceGroup );
        return scaleMenu;
    }

    public static Menu getTestMenu( final ClientProperties pClientProperties,
                                    final TestActions testActions ) {
        final XActionGroup testActionGroup = PolarResponseLabeledActionFactory
                .getTestActionGroup( pClientProperties, testActions );
        final Menu testMenu = XActionUtilities.createMenu( testActionGroup );
        return testMenu;
    }

    public static Menu getViewMenu( final ClientProperties pClientProperties,
                                    final ViewActions viewActions ) {
        final XActionGroup viewActionGroup = PolarResponseLabeledActionFactory
                .getViewActionGroup( pClientProperties, viewActions );
        final Menu viewMenu = XActionUtilities.createMenu( viewActionGroup );
        return viewMenu;
    }
}

