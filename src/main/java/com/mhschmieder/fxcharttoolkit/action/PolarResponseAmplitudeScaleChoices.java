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
import com.mhschmieder.fxguitoolkit.action.XAction;

/**
 * This is a struct-like container for Scale choices used by Polar Response.
 */
public final class PolarResponseAmplitudeScaleChoices {

    public XAction _scaleDiv5dbChoice;
    public XAction _scaleDiv6dbChoice;
    public XAction _scaleDiv10dbChoice;

    public PolarResponseAmplitudeScaleChoices( final ClientProperties pClientProperties ) {
        _scaleDiv5dbChoice =
                           PolarResponseLabeledActionFactory.getScaleDiv5dbChoice( pClientProperties );
        _scaleDiv6dbChoice =
                           PolarResponseLabeledActionFactory.getScaleDiv6dbChoice( pClientProperties );
        _scaleDiv10dbChoice = PolarResponseLabeledActionFactory
                .getScaleDiv10dbChoice( pClientProperties );
    }

    public Collection< Action > getScaleChoiceCollection() {
        final Collection< Action > scaleChoiceCollection = Arrays
                .asList( _scaleDiv5dbChoice, _scaleDiv6dbChoice, _scaleDiv10dbChoice );
        return scaleChoiceCollection;
    }

    public void setGridSpacing( final int gridSpacing ) {
        // Sync up the status of all associated controls with the current radial
        // Grid Spacing value by setting the appropriate action selected status.
        switch ( gridSpacing ) {
        case 5:
            _scaleDiv5dbChoice.setSelected( true );
            break;
        case 6:
            _scaleDiv6dbChoice.setSelected( true );
            break;
        case 10:
            _scaleDiv10dbChoice.setSelected( true );
            break;
        default:
            _scaleDiv6dbChoice.setSelected( true );
            break;
        }
    }
}
