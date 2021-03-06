package org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.List;

public class SpinnerSkin<T> extends BehaviorSkinBase<Spinner<T>, SpinnerBehavior<T>> {

    private TextField textField;

    private Region incrementArrow;
    private StackPane incrementArrowButton;

    private Region decrementArrow;
    private StackPane decrementArrowButton;

    // rather than create an private enum, lets just use an int, here's the important details:
    private static final int ARROWS_ON_RIGHT_VERTICAL   = 0;
    private static final int ARROWS_ON_LEFT_VERTICAL    = 1;
    private static final int ARROWS_ON_RIGHT_HORIZONTAL = 2;
    private static final int ARROWS_ON_LEFT_HORIZONTAL  = 3;
    private static final int SPLIT_ARROWS_VERTICAL      = 4;
    private static final int SPLIT_ARROWS_HORIZONTAL    = 5;

    private int layoutMode = 0;

    public SpinnerSkin(Spinner<T> spinner) {
        super(spinner, new SpinnerBehavior<T>(spinner));

        textField = spinner.getEditor();
        getChildren().add(textField);

        updateStyleClass();
        spinner.getStyleClass().addListener((ListChangeListener<String>) c -> updateStyleClass());

        // increment / decrement arrows
        incrementArrow = new Region();
        incrementArrow.setFocusTraversable(false);
        incrementArrow.getStyleClass().setAll("increment-arrow");
        incrementArrow.setMaxWidth(Region.USE_PREF_SIZE);
        incrementArrow.setMaxHeight(Region.USE_PREF_SIZE);
        incrementArrow.setMouseTransparent(true);

        incrementArrowButton = new StackPane();
        incrementArrowButton.setFocusTraversable(false);
        incrementArrowButton.getStyleClass().setAll("increment-arrow-button");
        incrementArrowButton.getChildren().add(incrementArrow);
        incrementArrowButton.setOnMousePressed(e -> {
            getSkinnable().requestFocus();
            getBehavior().startSpinning(true);
        });
        incrementArrowButton.setOnMouseReleased(e -> getBehavior().stopSpinning());

        decrementArrow = new Region();
        decrementArrow.setFocusTraversable(false);
        decrementArrow.getStyleClass().setAll("decrement-arrow");
        decrementArrow.setMaxWidth(Region.USE_PREF_SIZE);
        decrementArrow.setMaxHeight(Region.USE_PREF_SIZE);
        decrementArrow.setMouseTransparent(true);

        decrementArrowButton = new StackPane();
        decrementArrowButton.setFocusTraversable(false);
        decrementArrowButton.getStyleClass().setAll("decrement-arrow-button");
        decrementArrowButton.getChildren().add(decrementArrow);
        decrementArrowButton.setOnMousePressed(e -> {
            getSkinnable().requestFocus();
            getBehavior().startSpinning(false);
        });
        decrementArrowButton.setOnMouseReleased(e -> getBehavior().stopSpinning());

        getChildren().addAll(incrementArrowButton, decrementArrowButton);

        // Fixes in the same vein as ComboBoxListViewSkin

        // move fake focus in to the textfield if the spinner is editable
        spinner.focusedProperty().addListener((ov, t, hasFocus) -> {
            // Fix for the regression noted in a comment in RT-29885.
            ((ComboBoxListViewSkin.FakeFocusTextField)textField).setFakeFocus(hasFocus);
        });

        spinner.addEventFilter(KeyEvent.ANY, ke -> {
            if (spinner.isEditable()) {
                // This prevents a stack overflow from our rebroadcasting of the
                // event to the textfield that occurs in the final else statement
                // of the conditions below.
                if (ke.getTarget().equals(textField)) return;

                // Fix for the regression noted in a comment in RT-29885.
                // This forwards the event down into the TextField when
                // the key event is actually received by the Spinner.
                textField.fireEvent(ke.copyFor(textField, textField));
                ke.consume();
            }
        });

        textField.focusedProperty().addListener((ov, t, hasFocus) -> {
            // Fix for RT-29885
            spinner.getProperties().put("FOCUSED", hasFocus);
            // --- end of RT-29885

            // RT-21454 starts here
            if (! hasFocus) {
                pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, false);
            } else {
                pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, true);
            }
            // --- end of RT-21454
        });

        // end of comboBox-esque fixes

        textField.focusTraversableProperty().bind(spinner.editableProperty());
    }

    private void updateStyleClass() {
        final List<String> styleClass = getSkinnable().getStyleClass();

        if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_VERTICAL)) {
            layoutMode = ARROWS_ON_LEFT_VERTICAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_LEFT_HORIZONTAL)) {
            layoutMode = ARROWS_ON_LEFT_HORIZONTAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_ARROWS_ON_RIGHT_HORIZONTAL)) {
            layoutMode = ARROWS_ON_RIGHT_HORIZONTAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_SPLIT_ARROWS_VERTICAL)) {
            layoutMode = SPLIT_ARROWS_VERTICAL;
        } else if (styleClass.contains(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL)) {
            layoutMode = SPLIT_ARROWS_HORIZONTAL;
        } else {
            layoutMode = ARROWS_ON_RIGHT_VERTICAL;
        }
    }

    @Override protected void layoutChildren(final double x, final double y,
                                            final double w, final double h) {

        final double incrementArrowButtonWidth = incrementArrowButton.snappedLeftInset() +
                snapSize(incrementArrow.prefWidth(-1)) + incrementArrowButton.snappedRightInset();

        final double decrementArrowButtonWidth = decrementArrowButton.snappedLeftInset() +
                snapSize(decrementArrow.prefWidth(-1)) + decrementArrowButton.snappedRightInset();

        final double widestArrowButton = Math.max(incrementArrowButtonWidth, decrementArrowButtonWidth);

        // we need to decide on our layout approach, and this depends on
        // the presence of style classes in the Spinner styleClass list.
        // To be a bit more efficient, we observe the list for changes, so
        // here in layoutChildren we can just react to a few booleans.
        if (layoutMode == ARROWS_ON_RIGHT_VERTICAL || layoutMode == ARROWS_ON_LEFT_VERTICAL) {
            final double textFieldStartX = layoutMode == ARROWS_ON_RIGHT_VERTICAL ? x : x + widestArrowButton;
            final double buttonStartX = layoutMode == ARROWS_ON_RIGHT_VERTICAL ? x + w - widestArrowButton : x;
            final double halfHeight = Math.floor(h / 2.0);

            textField.resizeRelocate(textFieldStartX, y, w - widestArrowButton, h);

            incrementArrowButton.resize(widestArrowButton, halfHeight);
            positionInArea(incrementArrowButton, buttonStartX, y,
                    widestArrowButton, halfHeight, 0, HPos.CENTER, VPos.CENTER);

            decrementArrowButton.resize(widestArrowButton, halfHeight);
            positionInArea(decrementArrowButton, buttonStartX, y + halfHeight,
                    widestArrowButton, h - halfHeight, 0, HPos.CENTER, VPos.BOTTOM);
        } else if (layoutMode == ARROWS_ON_RIGHT_HORIZONTAL || layoutMode == ARROWS_ON_LEFT_HORIZONTAL) {
            final double totalButtonWidth = incrementArrowButtonWidth + decrementArrowButtonWidth;
            final double textFieldStartX = layoutMode == ARROWS_ON_RIGHT_HORIZONTAL ? x : x + totalButtonWidth;
            final double buttonStartX = layoutMode == ARROWS_ON_RIGHT_HORIZONTAL ? x + w - totalButtonWidth : x;

            textField.resizeRelocate(textFieldStartX, y, w - totalButtonWidth, h);

            // decrement is always on the left
            decrementArrowButton.resize(decrementArrowButtonWidth, h);
            positionInArea(decrementArrowButton, buttonStartX, y,
                    decrementArrowButtonWidth, h, 0, HPos.CENTER, VPos.CENTER);

            // ... and increment is always on the right
            incrementArrowButton.resize(incrementArrowButtonWidth, h);
            positionInArea(incrementArrowButton, buttonStartX + decrementArrowButtonWidth, y,
                    incrementArrowButtonWidth, h, 0, HPos.CENTER, VPos.CENTER);
        } else if (layoutMode == SPLIT_ARROWS_VERTICAL) {
            final double incrementArrowButtonHeight = incrementArrowButton.snappedTopInset() +
                    snapSize(incrementArrow.prefHeight(-1)) + incrementArrowButton.snappedBottomInset();

            final double decrementArrowButtonHeight = decrementArrowButton.snappedTopInset() +
                    snapSize(decrementArrow.prefHeight(-1)) + decrementArrowButton.snappedBottomInset();

            final double tallestArrowButton = Math.max(incrementArrowButtonHeight, decrementArrowButtonHeight);

            // increment is at the top
            incrementArrowButton.resize(w, tallestArrowButton);
            positionInArea(incrementArrowButton, x, y,
                    w, tallestArrowButton, 0, HPos.CENTER, VPos.CENTER);

            // textfield in the middle
            textField.resizeRelocate(x, y + tallestArrowButton, w, h - (2*tallestArrowButton));

            // decrement is at the bottom
            decrementArrowButton.resize(w, tallestArrowButton);
            positionInArea(decrementArrowButton, x, h - tallestArrowButton,
                    w, tallestArrowButton, 0, HPos.CENTER, VPos.CENTER);
        } else if (layoutMode == SPLIT_ARROWS_HORIZONTAL) {
            // decrement is on the left-hand side
            decrementArrowButton.resize(widestArrowButton, h);
            positionInArea(decrementArrowButton, x, y,
                    widestArrowButton, h, 0, HPos.CENTER, VPos.CENTER);

            // textfield in the middle
            textField.resizeRelocate(x + widestArrowButton, y, w - (2*widestArrowButton), h);
            textField.setAlignment(Pos.CENTER);

            // increment is on the right-hand side
            incrementArrowButton.resize(widestArrowButton, h);
            positionInArea(incrementArrowButton, w - widestArrowButton, y,
                    widestArrowButton, h, 0, HPos.CENTER, VPos.CENTER);
        }
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        final double textfieldWidth = textField.prefWidth(height);
        return leftInset + textfieldWidth + rightInset;
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        double ph;
        double textFieldHeight = textField.prefHeight(width);

        if (layoutMode == SPLIT_ARROWS_VERTICAL) {
            ph = topInset + incrementArrowButton.prefHeight(width) +
                    textFieldHeight + decrementArrowButton.prefHeight(width) + bottomInset;
        } else {
            ph = topInset + textFieldHeight + bottomInset;
        }

        return ph;
    }

    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefWidth(height);
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return getSkinnable().prefHeight(width);
    }

    // Overridden so that we use the textfield as the baseline, rather than the arrow.
    // See RT-30754 for more information.
    @Override protected double computeBaselineOffset(double topInset, double rightInset, double bottomInset, double leftInset) {
        return textField.getLayoutBounds().getMinY() + textField.getLayoutY() + textField.getBaselineOffset();
    }


    /***************************************************************************
     *                                                                         *
     * Stylesheet Handling                                                     *
     *                                                                         *
     **************************************************************************/

    private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("contains-focus");
}