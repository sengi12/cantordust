package resources;

import docking.ComponentProvider;
import docking.WindowPosition;
import ghidra.framework.plugintool.PluginTool;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Hosts Cantordust inside the Ghidra tool.
 *
 * Ghidra owns the window for a ComponentProvider, so the same instance can float
 * on its own - which is how Cantordust has always opened - or be dragged into the
 * tool and docked alongside the listing. Ghidra remembers whichever the user
 * chose, and the panel picks up the tool's look and feel either way.
 */
public class CantordustProvider extends ComponentProvider {

    public static final String NAME = "Cantordust";

    private final MainInterface mainInterface;
    private final PluginTool tool;

    public CantordustProvider(PluginTool tool, MainInterface mainInterface,
                              String programName, Host host) {
        super(tool, NAME, NAME);
        this.tool = tool;
        this.mainInterface = mainInterface;

        setTitle(NAME);
        setSubTitle(programName);

        // Open detached, matching the standalone window this replaces. Docking is
        // then a drag away, rather than the only option.
        setDefaultWindowPosition(WindowPosition.WINDOW);

        // The tool cannot rebuild a script-created provider when it restores
        // itself on the next launch, so keep this out of the saved configuration.
        setTransient();

        setWindowMenuGroup(NAME);
        loadIcon(host);
    }

    private void loadIcon(Host host) {
        try (java.io.InputStream in = host.openResource("resources/icons/icon.png")) {
            java.awt.Image img = javax.imageio.ImageIO.read(in);
            if (img != null) {
                setIcon(new ImageIcon(img));
            }
        } catch (java.io.IOException | RuntimeException e) {
            // A missing or unreadable icon is not worth failing the window over.
        }
    }

    @Override
    public JComponent getComponent() {
        return mainInterface;
    }

    @Override
    public void closeComponent() {
        super.closeComponent();
        // Nothing can reopen a script-created provider from the UI, so drop it
        // from the tool instead of leaving a dead entry in the Window menu.
        tool.removeComponentProvider(this);
    }
}
