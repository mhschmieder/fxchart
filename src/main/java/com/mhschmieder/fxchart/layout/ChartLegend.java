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
package com.mhschmieder.fxchart.layout;

import com.mhschmieder.fxchart.chart.ChartLegendItem;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.layout.TilePane;

/**
 * A Chart Legend that displays a list of Legend Items with symbols in a box.
 * <p>
 * NOTE: This class is pulled in from Sun's private API (
 * com.sun.javafx.charts.Legend) as that goes away starting with Java 11.
 */
public class ChartLegend extends TilePane {

    private static final int GAP = 5;

    /**
     * The Legend Items to display in this Legend
     */
    private final ObjectProperty< ObservableList< ChartLegendItem > > items;
    /**
     * The Legend Items should be laid out vertically in columns rather than
     * horizontally in rows.
     */
    private final BooleanProperty vertical;
    protected ListChangeListener< ChartLegendItem > itemsListener;

    @SuppressWarnings( "nls" )
    public ChartLegend() {
        super( GAP, GAP );

        items = new ObjectPropertyBase< ObservableList< ChartLegendItem > >() {
            ObservableList< ChartLegendItem > oldItems = null;

            @Override
            public Object getBean() {
                return ChartLegend.this;
            }

            @Override
            public String getName() {
                return "items";
            }

            @Override
            protected void invalidated() {
                if ( oldItems != null ) {
                    oldItems.removeListener( itemsListener );
                }
                getChildren().clear();
                final ObservableList< ChartLegendItem > newItems = get();
                if ( newItems != null ) {
                    newItems.addListener( itemsListener );
                    for ( final ChartLegendItem item : newItems ) {
                        getChildren().add( item.label );
                    }
                }
                oldItems = get();
                requestLayout();
            }
        };

        itemsListener = change -> {
            getChildren().clear();
            for ( final ChartLegendItem item : getItems() ) {
                getChildren().add( item.label );
            }
            if ( isVisible() ) {
                requestLayout();
            }
        };

        vertical = new BooleanPropertyBase( false ) {
            @Override
            public Object getBean() {
                return ChartLegend.this;
            }

            @Override
            public String getName() {
                return "vertical";
            }

            @Override
            protected void invalidated() {
                setOrientation( get()
                                ? Orientation.VERTICAL
                                : Orientation.HORIZONTAL );
            }
        };

        setTileAlignment( Pos.CENTER_LEFT );
        setItems( FXCollections.< ChartLegendItem >observableArrayList() );

        getStyleClass().setAll( "chart-legend" );
    }

    public final ObservableList< ChartLegendItem > getItems() {
        return items.get();
    }

    public final void setItems( final ObservableList< ChartLegendItem > value ) {
        itemsProperty().set( value );
    }

    public final ObjectProperty< ObservableList< ChartLegendItem > > itemsProperty() {
        return items;
    }

    @Override
    protected double computePrefWidth( final double forHeight ) {
        // Legend prefWidth is zero if there are no Legend Items.
        return ( getItems().isEmpty() )
               ? 0
               : super.computePrefWidth( forHeight );
    }

    @Override
    protected double computePrefHeight( final double forWidth ) {
        // Legend prefHeight is zero if there are no Legend Items.
        return ( getItems().isEmpty() )
               ? 0
               : super.computePrefHeight( forWidth );
    }

    public final boolean isVertical() {
        return vertical.get();
    }

    public final void setVertical( final boolean value ) {
        vertical.set( value );
    }

    public final BooleanProperty verticalProperty() {
        return vertical;
    }
}
