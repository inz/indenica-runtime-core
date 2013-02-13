package testPkg;

// + XMLRootElement!
public class EventOne extends eu.indenica.events.Event {
    public EventOne() {
        super("testPkg.EventOne");
    }

    private String attr1;

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(final String attr1) {
        this.attr1 = attr1;
    }

    private int anAttribute;

    public int getAnAttribute() {
        return anAttribute;
    }

    public void setAnAttribute(final int anAttribute) {
        this.anAttribute = anAttribute;
    }
}
