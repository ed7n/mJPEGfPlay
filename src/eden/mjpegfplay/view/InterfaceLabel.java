// @formatter:off
package eden.mjpegfplay.view;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import static eden.mjpegfplay.view.UIConstants.*;


/**
 *  A InterfaceLabel provides a non-iconified label to its target button.
 *  <p>
 *  This class is a convenient JLabel factory for transport labels.
 *
 *  @author     Brendon
 *  @version    u0r3, 11/28/2018.
 */
class InterfaceLabel extends JLabel {

//~~CONSTRUCTORS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Makes a InterfaceLabel with the given text */
    InterfaceLabel(String text) {
        super(text);
        setFont(FONT_LABEL);
        setHorizontalTextPosition(SwingConstants.CENTER);
    }
}
