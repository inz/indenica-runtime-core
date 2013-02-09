package testPkg;

// + XMLRootElement!
public class Foo extends eu.indenica.events.Event {
	public Foo() {
		super("testPkg.Foo");
	}
	private String a1;
	
	public String getA1() {
		return a1;
	}
	
	public void setA1(final String a1) {
		this.a1 = a1;
	}
}
