package resources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

public class MainInterface extends JPanel {
    private byte[] data;
    private byte[] fullData;
    public BitMapSlider macroSlider;
    public BitMapSlider microSlider;
    public JSlider widthSlider;
    public JSlider offsetSlider;
    public JSlider dataSlider;
    public JButton widthDownButton;
    public JButton widthUpButton;
    public JButton offsetDownButton;
    public JButton offsetUpButton;
    public JButton microUpButton;
    public JButton hilbertMapButton;
    public JButton themeButton;
    public JButton twoTupleButton;
    public JButton eightBitPerPixelBitMapButton;
    public JButton byteCloudButton;
    public JButton metricMapButton;
    public JButton oneTupleButton;
    public JButton threeTupleButton;
    public JPopupMenu popup;

    public GhidraSrc cantordust;
    public JLabel dataRange = new JLabel();
    public JLabel macroCaption = new JLabel();
    public JLabel microCaption = new JLabel();
    public JLabel macroValueHigh = new JLabel();
    public JLabel macroValueLow = new JLabel();
    public JLabel microValueHigh = new JLabel();
    public JLabel microValueLow = new JLabel();
    public JLabel widthValue = new JLabel();
    public JLabel offsetValue = new JLabel();
    public JLabel programName = new JLabel();

    public JPanel currVis = new JPanel();

    /* visualizers stored here so no duplicate visualizer instances are ever created.*/
    public HashMap<visualizerMapKeys, JPanel> visualizerPanels;

    public enum visualizerMapKeys {
        BITMAP,
        BYTECLOUD,
        METRIC,
        TWOTUPLE,
        ONETUPLE,
        THREETUPLE
    }

    public String basePath;
    public int xOffset = 0;
    /** Set while one slider is updating the other, to stop the two listeners re-entering. */
    private boolean syncingSliders = false;
    protected byte theme;
    protected Boolean dispMetricMap;

    public MainInterface(byte[] mdata, GhidraSrc cd) throws IOException {
        this.data = mdata;
        this.fullData = mdata;
        this.cantordust = cd;
        visualizerPanels = new HashMap<>();

        this.dispMetricMap = false;
        this.basePath = this.cantordust.getCurrentDirectory();

        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        if(fullData.length > 26214400){
            // 0xfffff = 1048575, 25MB = 0x1900000 = 26214400 bytes
            this.data = Arrays.copyOfRange(fullData, 0, 1048575);
            int range = fullData.length - 1048575;
            dataSlider = new JSlider(1, range);
            dataSlider.setOrientation(SwingConstants.VERTICAL);
            dataSlider.setInverted(true);
            dataSlider.setValue(0);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridheight = 512;
            xOffset = 5;
            gbc.gridwidth = xOffset;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.insets = new Insets(5, 5, 5, 5);
            add(dataSlider, gbc);
        }
        cantordust.cdprint("data: "+data.length+"\n");
        macroSlider = new BitMapSlider(1, this.data.length-1, this.data, this.cantordust);
        macroSlider.setValue(1);
        macroSlider.setUpperValue(this.data.length-1);
        gbc.gridx = xOffset + 0;
        gbc.gridy = 0;
        gbc.gridheight = 512;
        gbc.gridwidth = 10;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(macroSlider, gbc);
        
        microSlider = new BitMapSlider(0, this.data.length-1, this.data, this.cantordust);
        microSlider.setValue(macroSlider.getValue());
        microSlider.setUpperValue(macroSlider.getUpperValue());
        gbc.gridx = xOffset + 10;
        add(microSlider, gbc);

        Dimension incDim = new Dimension(18, 18);
        Insets zeroIn = new Insets(0, 0, 0, 0);

        microUpButton = new JButton(">");
        microUpButton.addActionListener(new inc_micro());
        microUpButton.setPreferredSize(incDim);
        microUpButton.setMargin(zeroIn);
        microUpButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = xOffset + 19;
        gbc.gridy = 512;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(microUpButton, gbc);

        widthDownButton = new JButton("<");
        widthDownButton.addActionListener(new dec_width());
        widthDownButton.setPreferredSize(incDim);
        widthDownButton.setMargin(zeroIn);
        widthDownButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = xOffset + 20;
        add(widthDownButton, gbc);

        Dimension slideDim = new Dimension(200, 15);

        widthSlider = new JSlider(1, 1024);
        widthSlider.setValue(512);
        widthSlider.setMaximum(1024);
        widthSlider.setOrientation(SwingConstants.HORIZONTAL);
        widthSlider.setPreferredSize(slideDim);
        gbc.gridy = 512;
        gbc.gridx = xOffset + 21;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        add(widthSlider, gbc);

        widthUpButton = new JButton(">");
        widthUpButton.addActionListener(new inc_width());
        widthUpButton.setPreferredSize(incDim);
        widthUpButton.setMargin(zeroIn);
        widthUpButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = xOffset + 260;
        add(widthUpButton, gbc);

        offsetDownButton = new JButton("<");
        offsetDownButton.addActionListener(new dec_offset());
        offsetDownButton.setPreferredSize(incDim);
        offsetDownButton.setMargin(zeroIn);
        offsetDownButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = xOffset + 261;
        add(offsetDownButton, gbc);

        offsetSlider = new JSlider(1, 255);
        offsetSlider.setValue(0);
        offsetSlider.setMaximum(255);
        offsetSlider.setOrientation(SwingConstants.HORIZONTAL);
        offsetSlider.setPreferredSize(slideDim);
        gbc.gridx = xOffset + 270;
        add(offsetSlider, gbc);

        offsetUpButton = new JButton(">");
        offsetUpButton.addActionListener(new inc_offset());
        offsetUpButton.setPreferredSize(incDim);
        offsetUpButton.setMargin(zeroIn);
        offsetUpButton.setBorder(BorderFactory.createEmptyBorder());
        gbc.gridx = xOffset + 512;
        add(offsetUpButton, gbc);
        
        // Default Current Visualization: MetricMap
        currVis = new MetricMap(MetricMap.getWindowSize(), cantordust, this);
        currVis.setPreferredSize(new Dimension(512, 512));
        gbc.gridx = xOffset + 20;
        gbc.gridy = 0;
        gbc.gridheight = 512;
        gbc.gridwidth = 512;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(currVis, gbc);

        // Setup buttons and button icons
        
        Image twoTupleIcon = ImageIO.read(new File(basePath + "resources/icons/icon_2_tuple.bmp")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        twoTupleButton = new JButton(new ImageIcon(twoTupleIcon));
        twoTupleButton.addActionListener(new open_two_tuple());
        twoTupleButton.setPreferredSize(new Dimension(50, 50));
        twoTupleButton.setToolTipText("Two Tuple  (" + detachHint() + " for a new window)");
        gbc.gridx = xOffset + 532;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        add(twoTupleButton, gbc);

        Image bmpIcon = ImageIO.read(new File(basePath + "resources/icons/icon_bit_map.bmp")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        eightBitPerPixelBitMapButton = new JButton(new ImageIcon(bmpIcon));
        eightBitPerPixelBitMapButton.addActionListener(new open_8bpp_BitMap());
        eightBitPerPixelBitMapButton.setPreferredSize(new Dimension(50, 50));
        eightBitPerPixelBitMapButton.setToolTipText("Linear BitMap  (" + detachHint() + " for a new window)");
        gbc.gridy = 1;
        add(eightBitPerPixelBitMapButton, gbc);

        Image byteCloudIcon = ImageIO.read(new File(basePath + "resources/icons/icon_cloud.bmp")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        byteCloudButton = new JButton(new ImageIcon(byteCloudIcon));
        byteCloudButton.addActionListener(new open_byte_cloud());
        byteCloudButton.setPreferredSize(new Dimension(50, 50));
        byteCloudButton.setToolTipText("Byte Cloud  (" + detachHint() + " for a new window)");
        gbc.gridy = 2;
        add(byteCloudButton, gbc);
        
        Image metricMapIcon = ImageIO.read(new File(basePath + "resources/icons/icon_metricMap.png")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        metricMapButton = new JButton(new ImageIcon(metricMapIcon));
        metricMapButton.addActionListener(new open_metric_map());
        metricMapButton.setPreferredSize(new Dimension(50, 50));
        metricMapButton.setToolTipText("Metric Map  (" + detachHint() + " for a new window)");
        gbc.gridy = 3;
        add(metricMapButton, gbc);

        Image oneTupleIcon = ImageIO.read(new File(basePath + "resources/icons/icon_1_tuple.bmp")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        oneTupleButton = new JButton(new ImageIcon(oneTupleIcon));
        oneTupleButton.addActionListener(new open_one_tuple());
        oneTupleButton.setPreferredSize(new Dimension(50, 50));
        oneTupleButton.setToolTipText("One Tuple  (" + detachHint() + " for a new window)");
        gbc.gridy = 4;
        add(oneTupleButton, gbc);
        
        Image threeTupleIcon = ImageIO.read(new File(basePath + "resources/icons/icon_3_tuple.bmp")).getScaledInstance(41, 41, Image.SCALE_SMOOTH);
        threeTupleButton = new JButton(new ImageIcon(threeTupleIcon));
        threeTupleButton.addActionListener(new open_three_tuple());
        threeTupleButton.setPreferredSize(new Dimension(50, 50));
        threeTupleButton.setToolTipText("Three Tuple  (" + detachHint() + " for a new window)");
        gbc.gridy = 5;
        add(threeTupleButton, gbc);

        themeButton = new JButton();
        themeButton.addActionListener(new change_theme());
        gbc.gridy = 6;
        add(themeButton, gbc);

        // Slider captions. macroValueLow/High, widthValue and offsetValue were
        // being kept up to date on every slider move but never added to the
        // layout, so half the readouts were invisible.
        macroCaption = new JLabel("Overview  (whole file)");
        microCaption = new JLabel("Selection  (drawn range)");
        macroCaption.setHorizontalAlignment(SwingConstants.CENTER);
        microCaption.setHorizontalAlignment(SwingConstants.CENTER);

        macroSlider.setToolTipText("Overview: choose which part of the file the selection slider covers");
        microSlider.setToolTipText("Selection: choose the bytes drawn in the visualization");
        widthSlider.setToolTipText("Width of the rendered image, in pixels");
        offsetSlider.setToolTipText("Shift the data by a number of bytes before rendering");
        if(dataSlider != null){
            dataSlider.setToolTipText("Scroll the 1MB working window through a file too large to hold at once");
        }

        for(JLabel l : new JLabel[]{macroValueLow, macroValueHigh, microValueLow, microValueHigh}){
            l.setHorizontalAlignment(SwingConstants.CENTER);
        }
        updateMacroLabels();
        updateMicroLabels();

        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridy = 513;
        gbc.gridx = xOffset + 0;
        gbc.gridwidth = 10;
        add(macroCaption, gbc);
        gbc.gridx = xOffset + 10;
        add(microCaption, gbc);

        gbc.gridy = 514;
        gbc.gridwidth = 5;
        gbc.gridx = xOffset + 0;
        add(macroValueLow, gbc);
        gbc.gridx = xOffset + 5;
        add(macroValueHigh, gbc);
        gbc.gridx = xOffset + 10;
        add(microValueLow, gbc);
        gbc.gridx = xOffset + 15;
        add(microValueHigh, gbc);

        // Width and offset readouts, under the row of controls they belong to.
        widthValue.setText("Width " + hex(widthSlider.getValue()));
        offsetValue.setText("Offset " + hex(offsetSlider.getValue()));
        widthValue.setHorizontalAlignment(SwingConstants.CENTER);
        offsetValue.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 513;
        gbc.gridwidth = 239;
        gbc.gridx = xOffset + 21;
        add(widthValue, gbc);
        gbc.gridx = xOffset + 270;
        add(offsetValue, gbc);

        gbc.fill = GridBagConstraints.NONE;
         
        // Add listener to update display.
        if(dataSlider != null){
            dataSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider slider = (JSlider)e.getSource();
                    data = Arrays.copyOfRange(fullData, dataSlider.getValue(), dataSlider.getValue() + 1048575);
    
                    long minGhidraAddress1 = cantordust.getMinAddressOffset();
                    // Update text for upper and lower value of microSlider
                    long maxAddress1 = minGhidraAddress1 + dataSlider.getValue() + microSlider.getUpperValue();
                    long minAddress1 = minGhidraAddress1 + dataSlider.getValue() + macroSlider.getValue() + microSlider.getValue() - 1;
                    microValueHigh.setText(Long.toHexString(maxAddress1).toUpperCase());
                    microValueLow.setText(Long.toHexString(minAddress1).toUpperCase());
                    if(slider.getValueIsAdjusting()){
                        macroSlider.updateData(data);
                        microSlider.updateData(data);
                        macroSlider.ui.makeBitmapAsync(0, data.length);
                        microSlider.ui.makeBitmapAsync(macroSlider.getValue(), macroSlider.getUpperValue());
                    }
                }
            });
        }
        macroSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(syncingSliders){
                    return;
                }
                int lo = macroSlider.getValue();
                int hi = macroSlider.getUpperValue();
                updateMacroLabels();

                // Keep the micro selection over the same bytes, clamped into the
                // new window. The previous code rescaled it proportionally, so
                // resizing the macro window slid the selection to a different
                // part of the file; it also clamped first and then overwrote
                // that with the rescale, making the clamp dead code.
                int microLo = Math.min(Math.max(microSlider.getValue(), lo), hi);
                int microHi = Math.min(Math.max(microSlider.getUpperValue(), microLo), hi);

                // One atomic model update: setting minimum, maximum, value and
                // extent separately fired four events, each re-entering these
                // listeners, and the order decided whether a value got clamped.
                syncingSliders = true;
                try {
                    microSlider.getModel().setRangeProperties(microLo, microHi - microLo,
                            lo, hi, microSlider.getValueIsAdjusting());
                } finally {
                    syncingSliders = false;
                }
                updateMicroLabels();

                if(macroSlider.getValueIsAdjusting()) {
                    repaint();
                } else {
                    // Redraw the micro slider's strip for the window now selected.
                    microSlider.ui.makeBitmapAsync(lo, hi);
                }
            }
        });
        microSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if(syncingSliders){
                    return;
                }
                // The model already confines this slider to the macro window, so
                // re-clamping here only fought the macro listener.
                updateMicroLabels();
                if(microSlider.getValueIsAdjusting()) {
                    repaint();
                }
            }
        });
        widthSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                widthValue.setText("Width " + hex(slider.getValue()));
            }
        });
        offsetSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider slider = (JSlider) e.getSource();
                offsetValue.setText("Offset " + hex(slider.getValue()));
            }
        });

        darkTheme();
    }

    /*public changeDemo() {
        JButton decLowerButton = new JButton("decrease lower bound");
        JButton incLowerButton = new JButton("increase lower bound");
        JButton decUpperButton = new JButton("decrease upper bound");
        JButton incUpperButton = new JButton("increase upper bound");
    }*/
    
    public static int getWindowWidth() {
        return 900;
    }

    public static int getWindowHeight() {
        return 645;
    }

    public byte[] getData() {
        return this.data;
    }

    /**
     * Sets the current theme to dark
     */
    private void darkTheme() {
        this.theme = 1;
        setTheme(Color.black, Color.white, Color.darkGray);
        // The button advertises what a click will do, so in the dark theme it
        // offers the sun.
        themeButton.setIcon(new ThemeIcon(true, 16));
        themeButton.setToolTipText("Switch to the light theme");
    }

    /**
     * Sets the current theme to light
     */
    private void lightTheme() {
        this.theme = 0;
        Color c = UIManager.getColor("panelButtons.background");
        Color textColor = Color.black;
        setTheme(c, textColor, c);
        themeButton.setIcon(new ThemeIcon(false, 16));
        themeButton.setToolTipText("Switch to the dark theme");
    }

    /**
     * Sets colors of various components
     */
    private void setTheme(Color c, Color textColor, Color buttonColor) {
        this.setBackground(c);

        this.widthSlider.setBackground(c);
        this.offsetSlider.setBackground(c);
        if(this.dataSlider != null){
            this.dataSlider.setBackground(c);
        }

        this.macroSlider.setBackground(c);
        this.microSlider.setBackground(c);

        this.macroValueHigh.setForeground(textColor);
        this.macroValueLow.setForeground(textColor);
        this.microValueHigh.setForeground(textColor);
        this.microValueLow.setForeground(textColor);
        this.widthValue.setForeground(textColor);
        this.offsetValue.setForeground(textColor);

        this.widthDownButton.setBackground(c);
        this.widthDownButton.setForeground(textColor);

        this.widthUpButton.setBackground(c);
        this.widthUpButton.setForeground(textColor);

        this.offsetDownButton.setBackground(c);
        this.offsetDownButton.setForeground(textColor);

        this.offsetUpButton.setBackground(c);
        this.offsetUpButton.setForeground(textColor);

        this.dataRange.setForeground(textColor);
        this.macroCaption.setForeground(textColor);
        this.microCaption.setForeground(textColor);
        this.programName.setForeground(textColor);

        this.microUpButton.setBackground(c);
        this.microUpButton.setForeground(textColor);

        this.themeButton.setBackground(buttonColor);
        this.themeButton.setForeground(textColor);

        if(dispMetricMap) {
            currVis.setBackground(c);
        }
    }

    /** Format an offset the way Ghidra shows addresses, with an 0x prefix. */
    private static String hex(long v) {
        return "0x" + Long.toHexString(v).toUpperCase();
    }

    private void updateMacroLabels() {
        long base = cantordust.getMinAddressOffset();
        macroValueLow.setText(hex(base + macroSlider.getValue()));
        macroValueHigh.setText(hex(base + macroSlider.getUpperValue()));
    }

    private void updateMicroLabels() {
        long base = cantordust.getMinAddressOffset();
        if(dataSlider != null){
            base += dataSlider.getValue();
        }
        microValueLow.setText(hex(base + microSlider.getValue()));
        microValueHigh.setText(hex(base + microSlider.getUpperValue()));
    }

    /**
     * Modifier that opens a visualization in its own window instead of the main
     * one: Command on macOS, Control elsewhere. ActionEvent reports legacy
     * modifier bits, so the toolkit's extended mask is mapped back onto them.
     */
    private static int detachModifier() {
        int ex = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        return ((ex & InputEvent.META_DOWN_MASK) != 0) ? ActionEvent.META_MASK : ActionEvent.CTRL_MASK;
    }

    /** Label for the detach modifier, for button tooltips. */
    private static String detachHint() {
        return (detachModifier() == ActionEvent.META_MASK) ? "\u2318-click" : "Ctrl-click";
    }

    private static boolean opensInNewWindow(ActionEvent e) {
        return (e.getModifiers() & detachModifier()) != 0;
    }

    /**
     * Swap the main window's visualization for the panel stored under key,
     * building it through factory the first time it is asked for.
     */
    private void showInMainWindow(visualizerMapKeys key, Supplier<JPanel> factory) {
        currVis.setVisible(false);
        remove(currVis);
        dispMetricMap = false;
        if(!visualizerPanels.containsKey(key)) {
            visualizerPanels.put(key, factory.get());
        }
        currVis = visualizerPanels.get(key);
        currVis.setPreferredSize(new Dimension(512, 512));
        currVis.setVisible(true);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = xOffset + 20;
        gbc.gridy = 0;
        gbc.gridheight = 512;
        gbc.gridwidth = 512;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 5, 5);
        add(currVis, gbc);
        repaint();
        validate();
    }

    private class open_one_tuple implements ActionListener {
        open_one_tuple() {
        	
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                //JOptionPane.showMessageDialog(null, "test", "InfoBox: " + "test", JOptionPane.INFORMATION_MESSAGE);
                JFrame frame1 = new JFrame("1 Tuple Visualization");
                OneTupleVisualizer oneTupleVis = new OneTupleVisualizer(OneTupleVisualizer.getWindowSize(), cantordust);
                frame1.getContentPane().add(oneTupleVis);
                frame1.setSize(OneTupleVisualizer.getWindowSize(), OneTupleVisualizer.getWindowSize());
                //frame.pack();
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof OneTupleVisualizer)) {
                showInMainWindow(visualizerMapKeys.ONETUPLE, () -> new OneTupleVisualizer(OneTupleVisualizer.getWindowSize(), cantordust));
            }
        }
    }

    private class open_two_tuple implements ActionListener {
        open_two_tuple() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                //JOptionPane.showMessageDialog(null, "test", "InfoBox: " + "test", JOptionPane.INFORMATION_MESSAGE);
                JFrame frame1 = new JFrame("2 Tuple Visualization");
                TwoTupleVisualizer twoTupleVis = new TwoTupleVisualizer(TwoTupleVisualizer.getWindowSize(), cantordust);
                frame1.getContentPane().add(twoTupleVis);
                frame1.setSize(TwoTupleVisualizer.getWindowSize(), TwoTupleVisualizer.getWindowSize());
                //frame.pack();
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof TwoTupleVisualizer)) {
                showInMainWindow(visualizerMapKeys.TWOTUPLE, () -> new TwoTupleVisualizer(TwoTupleVisualizer.getWindowSize(), cantordust));
            }
        }
    }

    private class open_three_tuple implements ActionListener {
        open_three_tuple() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                JFrame frame1 = new JFrame("3 Tuple Visualization");
                ThreeTupleVisualizer threeTupleVis = new ThreeTupleVisualizer(ThreeTupleVisualizer.getWindowSize(), cantordust);
                frame1.getContentPane().add(threeTupleVis);
                frame1.setSize(ThreeTupleVisualizer.getWindowSize(), ThreeTupleVisualizer.getWindowSize());
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof ThreeTupleVisualizer)) {
                showInMainWindow(visualizerMapKeys.THREETUPLE, () -> new ThreeTupleVisualizer(ThreeTupleVisualizer.getWindowSize(), cantordust));
            }
        }
    }

    private class open_8bpp_BitMap implements ActionListener {
        open_8bpp_BitMap() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                JFrame frame1 = new JFrame("Linear Bit Map");
                BitMapVisualizer bitMapVis = new BitMapVisualizer(BitMapVisualizer.getWindowSize(), cantordust);
                frame1.getContentPane().add(bitMapVis);
                bitMapVis.setColorMapper(new EightBitPerPixelMapper(cantordust));
                frame1.setSize(BitMapVisualizer.getWindowSize(), BitMapVisualizer.getWindowSize());
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof BitMapVisualizer)) {
                showInMainWindow(visualizerMapKeys.BITMAP, () -> new BitMapVisualizer(BitMapVisualizer.getWindowSize(), cantordust));
            }
        }
    }

    private class open_byte_cloud implements ActionListener {
        open_byte_cloud() {
        	
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                JFrame frame1 = new JFrame("Byte Cloud Visualization");
                ByteCloudVisualizer byteCloudVis = new ByteCloudVisualizer(ByteCloudVisualizer.getWindowSize(), cantordust);
                frame1.getContentPane().add(byteCloudVis);
                frame1.setSize(ByteCloudVisualizer.getWindowSize(), ByteCloudVisualizer.getWindowSize());
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof ByteCloudVisualizer)) {
                showInMainWindow(visualizerMapKeys.BYTECLOUD, () -> new ByteCloudVisualizer(ByteCloudVisualizer.getWindowSize(), cantordust));
            }
        }
    }

    private class open_metric_map implements ActionListener {

        open_metric_map() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (opensInNewWindow(e)) {
                JFrame frame1 = new JFrame("Metric Map");
                MetricMap metricMap = new MetricMap(MetricMap.getWindowSize(), cantordust);
                frame1.getContentPane().add(metricMap);
                frame1.setSize(MetricMap.getWindowSize(), MetricMap.getWindowSize()+30);
                frame1.setVisible(true);
                frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            } else if (!(currVis instanceof MetricMap)) {
                showInMainWindow(visualizerMapKeys.METRIC, () -> new MetricMap(MetricMap.getWindowSize(), cantordust));
            }
        }
    }

    private class dec_width implements ActionListener {
        dec_width() {
        	
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            widthSlider.setValue(widthSlider.getValue() - 1);
        }
    }
    
    private class inc_width implements ActionListener {
        inc_width() {
        	
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            widthSlider.setValue(widthSlider.getValue() + 1);
        }
    }
    
    private class dec_offset implements ActionListener {
        dec_offset() {
        	
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            offsetSlider.setValue(offsetSlider.getValue() - 1);
        }
    }
    
    private class inc_offset implements ActionListener {
        inc_offset() {
        	
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            offsetSlider.setValue(offsetSlider.getValue() + 1);
        }
    }
    
    private class inc_micro implements ActionListener {
        inc_micro() {
        	
        }
    
        @Override
        public void actionPerformed(ActionEvent e) {
            microSlider.setValue(microSlider.getValue() + 1);
        }
    }

    private class change_theme implements ActionListener {
        change_theme() {

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(theme == 0) {
                // Swap to dark theme
                darkTheme();
            } else {
                // Swap to light theme
                lightTheme();
            }
        }
    }
}
