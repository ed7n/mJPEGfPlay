// @formatter:off
package eden.mjpegfplay.view;

import eden.mjpegfplay.presenter.ApplicationInstance;
import eden.mjpegfplay.presenter.exception.MalformedSequenceException;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import static eden.mjpegfplay.model.ApplicationInformation.*;
import static eden.mjpegfplay.model.SequenceTypes.*;
import static eden.mjpegfplay.view.UIConstants.*;
import static eden.mjpegfplay.view.UserCommands.*;


/**
 *  An {@code ApplicationUI} represents the graphical application interface to
 *  the end user. Being the highest-level interface, it listens, receives, and
 *  handles user inputs while maintaining its presentation.
 *  <p>
 *  This class represents the view in the model-view-presenter architectural
 *  pattern of this application.
 *  <p>
 *  Use {@code ApplicationUIMaker} to instantiate objects of this class.
 *
 *  @author     Brendon
 *  @version    u0r3, 11/28/2018.
 *
 *  @see        ApplicationUIMaker
 */
public class ApplicationUI implements ActionListener, ChangeListener {

//~~OBJECT CONSTANTS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Application instance */
    ApplicationInstance instance;

    /** Application message decoder and decorator */
    ApplicationMessenger messenger = new ApplicationMessenger(this);

    /** Application main Frame */
    JFrame frameMain               = new JFrame();

    /** Application floating Frame */
    JFrame frameFloat              = new JFrame();

    /** Application Menu */
    ApplicationMenu menu           = new ApplicationMenu(this);

    /** Application file chooser */
    JFileChooser chooser           = new JFileChooser();

    /** Application UI LayoutManager */
    SpringLayout layout            = new SpringLayout();

    /** Application information Dialog */
    AboutDialog dialogAbout        = new AboutDialog(this.frameMain);

    /** New sequence Dialog */
    NewDialog dialogNew            = new NewDialog(this);

    /** Renderer Panel */
    JPanel panelRenderer           = new JPanel(this.layout);

    /** Interface Panel */
    JPanel panelInterface          = new JPanel(this.layout);

    /** Target Container */
    Container container            = this.panelRenderer;

    /** Interface labels */
    List<JLabel> labels;

    /** Transport JButtons */
    List<JButton> buttons;

    /** Volume slider */
    JSlider slider;

    /** Track spinner */
    JSpinner spinner;

    /** Status display panel */
    FrontPanel frontPanel          = new FrontPanel();

    /** Indicates whether this ApplicationUI is multi-windowed */
    boolean multiWindow            = false;

    /** Indicates whether a modal window is visible */
    boolean modal                  = false;


//~~CONSTRUCTORS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** To prevent uninitialized instantiations of this class */
    ApplicationUI(){}


//~~PUBLIC OBJECT METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


//~~~~EVENT HANDLERS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** @inheritDoc */
    @Override
    public void actionPerformed(ActionEvent event) {
        this.frameMain.requestFocusInWindow();

        switch (event.getActionCommand().charAt(0)) {
            case 'F':
                switch (event.getActionCommand()) {
                    case F_NEW:
                        newSequence();
                        break;
                    case F_OPEN:
                        open(SEQUENCE);
                        break;
                    case F_OPEN_FREEZING:
                        open(FREEZING_SEQUENCE);
                        break;
                    case F_OPEN_MUSIC:
                        open(MUSIC_SEQUENCE);
                        break;
                    case F_SAVE:
                        //save();
                        break;
                    case F_RELOAD:
                        reload();
                        break;
                    case F_CLOSE:
                        close();
                        break;
                    case F_QUIT:
                        System.exit(0);
                }
                break;
            case 'V':
                switch (event.getActionCommand()) {
                    case V_MULTI_WINDOW:
                        multiWindow();
                        break;
                    case V_SINGLE_WINDOW:
                        singleWindow();
                        break;
                    case V_SEQUENCE_INFORMATION:
                        sequenceInformation();
                        break;
                    case V_RENDER_STATISTICS:
                        toggleDrawStatistics();
                }
                break;
            case 'T':
                switch (event.getActionCommand()) {
                    case T_PLAY:
                        play();
                        break;
                    case T_PAUSE:
                        pause();
                        break;
                    case T_STOP:
                        stop();
                        break;
                    case T_FAST_REWIND:
                        fastRewind();
                        break;
                    case T_FAST_FORWARD:
                        fastForward();
                        break;
                    case T_STEP_BACKWARD:
                        stepBackward();
                        break;
                    case T_STEP_FORWARD:
                        stepForward();
                        break;
                    case T_JUMP_TO_START:
                        jumpToStart();
                        break;
                    case T_JUMP_TO_FRAME:
                        jump();
                        break;
                    case T_JUMP_TO_END:
                        jumpToEnd();
                        break;
                    case T_TRICKPLAY:
                        trickPlay();
                }
                break;
            case 'A':
                switch (event.getActionCommand()) {
                    case A_MUTE:
                        setMuted(true);
                        break;
                    case A_UNMUTE:
                        setMuted(false);
                        break;
                    case A_TOGGLE_MUTE:
                        toggleMuted();
                }
                String command = event.getActionCommand();

                if (       command.matches(A_VOLUME + "\\d{3}")) {
                    setAmplification(Float.parseFloat(
                        command.substring(command.length() - 3)) / 100
                    );
                } else if (command.matches(A_TRACK  + "\\d{3}")) {
                    setTrack(Integer.parseInt(
                        command.substring(command.length() - 3)
                    ));
                }
                break;
            case 'H':
                switch (event.getActionCommand()) {
                    case H_ABOUT:
                        about();
                }
        }
    }

    /** @inheritDoc */
    @Override
    public void stateChanged(ChangeEvent event) {
        if (       event.getSource().equals(this.slider)) {
            setAmplification((float) this.slider.getValue() / 100);
        } else if (event.getSource().equals(this.spinner)) {
            setTrack((int) this.spinner.getValue());
        }
    }


//~~~~PRESENTATION SETUP~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     *  Attaches the given {@code JComponent} for a presentation of the given
     *  title
     */
    public void initializePresentation(String title, JComponent component) {
        this.frameMain.setTitle(
            title + " - " + APPLICATION_NAME + " " + APPLICATION_VERSION
        );
        this.frameFloat.setTitle("Render - " + title);
        this.slider.setValue((int) (this.instance.getAmplification() * 100));
        this.spinner.setValue(this.instance.getTrack());
        this.frameMain.requestFocusInWindow();

        if (component == null) {
            return;
        }
        this.container.remove(BackdropComponent.INSTANCE);
        this.container.add(component, 0);

        this.layout.putConstraint(
            SpringLayout.NORTH, component,
            0,
            SpringLayout.NORTH, this.panelRenderer
        );
        this.layout.putConstraint(
            SpringLayout.SOUTH, component,
            0,
            SpringLayout.SOUTH, this.panelRenderer
        );
        this.layout.putConstraint(
            SpringLayout.WEST, component,
            0,
            SpringLayout.WEST, this.panelRenderer
        );
        this.layout.putConstraint(
            SpringLayout.EAST, component,
            0,
            SpringLayout.EAST, this.panelRenderer
        );
        this.container.validate();
        this.container.repaint();
    }

    /**
     *  Detaches the presentation {@code JComponent} and readies this {@code
     *  ApplicationUI} for another
     */
    public void returnToStandby() {
        this.frameFloat.setTitle(
            "Render - " + APPLICATION_NAME + " " + APPLICATION_VERSION
        );
        this.frameMain.setTitle(APPLICATION_NAME + " " + APPLICATION_VERSION);
        this.layout.removeLayoutComponent(this.container.getComponent(0));
        this.container.remove(0);
        this.container.add(BackdropComponent.INSTANCE);
        this.container.validate();
        this.container.repaint();
    }


//~~~~ACCESSORS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /** Returns the {@code FrontPanel} of this {@code ApplicationUI} */
    public FrontPanelInterface getFrontPanel() {
        return this.frontPanel;
    }


//~~PRIVATE OBJECT METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~


//~~~~USER COMMANDS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void newSequence() {
        enterModal();
        this.dialogNew.show();
        dismissModal();
    }

    private void open(String type) {
        if (!this.instance.isLoaded()) {
            this.frontPanel.setText0("OPEN");
            this.frontPanel.call();
        }
        enterModal();

        this.chooser.setApproveButtonToolTipText(this.instance.isLoaded() ?
            "WARNING: This will close the currently loaded sequence." : null
        );
        this.chooser.setDialogTitle("Load " + type + " Directory");
        int in = this.chooser.showDialog(this.frameMain, "Load");
        dismissModal();

        if (in == JFileChooser.APPROVE_OPTION) {
            open(
                this.chooser.getSelectedFile().getPath() + File.separator,
                type
            );
        } else {
            if (!this.instance.isLoaded()) {
                this.frontPanel.setText0("");
                this.frontPanel.setText1("NO SEQUENCE");
                this.frontPanel.call();
            }
        }
    }

    private void open(String path, String type) {
        try {
            this.instance.open(path, type);
            return;
        } catch (MalformedSequenceException e) {
            this.messenger.sayMalformedSequenceException(e);
        } catch (Exception e) {
            this.messenger.sayException(e);
        }
        if (!this.instance.isLoaded()) {
            this.frontPanel.setText0("");
            this.frontPanel.setText1("NO SEQUENCE");
            this.frontPanel.call();
        }
    }

    private void reload() {
        if (!this.instance.isLoaded()) {
            return;
        }
        try {
            this.instance.reload();
        } catch (MalformedSequenceException e) {
            this.messenger.sayMalformedSequenceException(e);
        } catch (Exception e) {
            this.messenger.sayException(e);
        }
    }

    private void close() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.close();
    }

    private void multiWindow() {
        if (this.multiWindow) {
            return;
        }
        this.frameMain.getContentPane().setPreferredSize(SIZE_INTERFACE);
        this.frameMain.setMinimumSize(SIZE_INTERFACE);
        this.frameMain.setResizable(false);
        this.frameFloat.getContentPane().add(this.container.getComponent(0), 0);
        this.frameFloat.setLocation(this.frameMain.getLocation());
        this.frameFloat.setSize(SIZE_RENDER);
        this.frameMain.validate();
        this.frameMain.pack();
        this.frameFloat.validate();
        this.frameFloat.pack();
        this.frameFloat.setVisible(true);

        if (this.frameMain.getY() < 0)
        {
            this.frameFloat.setLocation(
                this.frameMain.getX(),
                0
            );
            this.frameMain.setLocation(
                this.frameFloat.getX(),
                this.frameFloat.getY() + this.frameFloat.getHeight()
            );
        }
        else if (this.frameMain.getY() + this.frameMain.getHeight() >
                 Toolkit.getDefaultToolkit().getScreenSize().getHeight())
        {
            this.frameMain.setLocation(
                this.frameMain.getX(),
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() -
                this.frameMain.getHeight()
            );
            this.frameFloat.setLocation(
                this.frameMain.getX(),
                this.frameMain.getY() - this.frameFloat.getHeight()
            );
        }
        else
        {
            this.frameFloat.setLocation(this.frameMain.getLocation());

            this.frameMain.setLocation(
                this.frameFloat.getX(),
                this.frameFloat.getY() + this.frameFloat.getHeight()
            );
        }
        this.container = this.frameFloat.getContentPane();
        this.multiWindow = true;
        this.frontPanel.setFloating(true);
        this.frontPanel.call();
    }

    private void singleWindow() {
        if (!this.multiWindow) {
            return;
        }
        this.panelRenderer.add(this.container.getComponent(0), 0);
        this.frameMain.getContentPane().setPreferredSize(SIZE_FRAME);
        this.frameMain.setMinimumSize(SIZE_FRAME);
        this.frameMain.setResizable(true);
        this.frameMain.validate();
        this.frameMain.pack();
        this.frameFloat.setVisible(false);

        if (this.frameMain.getY() - this.frameFloat.getHeight() < 0)
        {
            this.frameMain.setLocation(
                this.frameMain.getX(),
                0
            );
        }
        else if (this.frameMain.getY() + this.frameFloat.getHeight() >
                 Toolkit.getDefaultToolkit().getScreenSize().getHeight())
        {
            this.frameMain.setLocation(
                this.frameMain.getX(),
                (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() -
                this.frameMain.getHeight()
            );
        }
        this.container = this.panelRenderer;
        this.multiWindow = false;
        this.frontPanel.setFloating(false);
        this.frontPanel.call();
    }

    private void sequenceInformation() {
        if (!this.instance.isLoaded()) {
            this.messenger.sayInfo("Sequence Information",
                "There is no sequence loaded in memory."
            );
            return;
        }
        this.messenger.sayInfo("Sequence Information",
            "Name: "    + this.instance.getName()            + "\n"    +
            "Range: ["  + this.instance.getStart()           + ", "    +
                          this.instance.getEnd()             + "], "   +
                          this.instance.getLength()          + "\n"    +
            "Rate: "    + this.instance.getRate()            + " /s\n" +
            "Length: "  + this.instance.getLengthSecond()    + "s\n"   +
            "Elapsed: " + this.instance.getElapsedSecond()   + "s ("   +
                   (100 * this.instance.getElapsedPercent()) + "%)\n"
        );
    }

    private void toggleDrawStatistics() {
        BackdropComponent.INSTANCE.toggleDrawStatistics();
        this.instance.toggleDrawStatistics();
    }

    private void play() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.play();
    }

    private void pause() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.pause();
    }

    private void stop() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.stop();
    }

    private void fastRewind() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.fastRewind();
    }

    private void fastForward() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.fastForward();
    }

    private void stepBackward() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.stepBackward();
    }

    private void stepForward() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.stepForward();
    }

    private void jumpToStart() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.jumpToStart();
    }

    private void jumpToEnd() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.jumpToEnd();
    }

    private void jump() {
        if (!this.instance.isLoaded()) {
            return;
        }
        int in = this.messenger.askInteger(
            "Jump To Frame", "Input frame number"
        );
        if (in != Integer.MIN_VALUE && !jump(in)) {
            this.messenger.sayError("Jump Error", "Invalid frame number.");
        }
    }

    private boolean jump(int frame) {
        return this.instance.jump(frame);
    }

    private void trickPlay() {
        if (!this.instance.isLoaded()) {
            return;
        }
        this.instance.trickPlay();
    }

    private void setAmplification(float amplification) {
        if (amplification < 0) {
            amplification = 0;
        } else if (amplification > 2) {
            amplification = 2;
        }
        this.instance.setAmplification(amplification);

        if (!this.slider.getValueIsAdjusting()) {
            this.slider.setValue((int) (this.instance.getAmplification() * 100));
        }
    }

    private void setMuted(boolean muted) {
        this.instance.setMuted(muted);
    }

    private boolean toggleMuted() {
        return this.instance.toggleMuted();
    }

    private void setTrack(int track) {
        if (!this.instance.isLoaded() || track < 0 || track > Byte.MAX_VALUE) {
            return;
        }
        this.instance.setTrack(track);

        if (track != 0) {
            this.spinner.setValue(this.instance.getTrack());
        }
    }

    private void about() {
        enterModal();
        this.dialogAbout.show();
        dismissModal();
    }


//~~~~MISCELLANEOUS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private void enterModal() {
        this.frontPanel.setGui(true);
        this.frontPanel.call();
        this.modal = true;
    }

    private void dismissModal() {
        this.frontPanel.setGui(false);
        this.frontPanel.call();
        this.modal = false;
    }


//~~INNER CLASSES~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    /**
     *  A FloatingWindowListener reverts the application back to being
     *  single-windowed when the floating Frame is closed
     */
    class FloatingWindowListener extends WindowAdapter {

//~~~~~~PUBLIC OBJECT METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /** @inheritDoc */
        @Override
        public void windowClosing(WindowEvent event) {
            singleWindow();
        }
    }


    /**
     *  A TransportKeyListener provides convenient keyboard shortcuts to
     *  transport controls
     */
    class TransportKeyListener extends KeyAdapter {

//~~~~~~PUBLIC OBJECT METHODS~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        /** @inheritDoc */
        @Override
        public void keyPressed(KeyEvent event) {
            switch (event.getKeyCode()) {
                case KeyEvent.VK_A:
                    trickPlay();
                    break;
                case KeyEvent.VK_X:
                    play();
                    break;
                case KeyEvent.VK_Z:
                    pause();
                    break;
                case KeyEvent.VK_Q:
                    stop();
                    break;
                case KeyEvent.VK_S:
                    fastRewind();
                    break;
                case KeyEvent.VK_D:
                    fastForward();
                    break;
                case KeyEvent.VK_LEFT:
                    stepBackward();
                    break;
                case KeyEvent.VK_RIGHT:
                    stepForward();
                    break;
                case KeyEvent.VK_OPEN_BRACKET:
                    jumpToStart();
                    break;
                case KeyEvent.VK_BACK_SLASH:
                    jump();
                    break;
                case KeyEvent.VK_CLOSE_BRACKET:
                    jumpToEnd();
                    break;
                case KeyEvent.VK_EQUALS:
                    setAmplification(instance.getAmplification() + 0.1f);
                    break;
                case KeyEvent.VK_MINUS:
                    setAmplification(instance.getAmplification() - 0.1f);
                    break;
                case KeyEvent.VK_0:
                    toggleMuted();
                    break;
                case KeyEvent.VK_UP:
                    setTrack(instance.getTrack() + 1);
                    break;
                case KeyEvent.VK_DOWN:
                    if (instance.getTrack() != 1) {
                        setTrack(instance.getTrack() - 1);
                    }
            }
        }
    }
}
