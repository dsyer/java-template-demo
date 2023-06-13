package demo;

import io.jstach.jstache.JStache;

@JStache(path = "index")
public class DemoModel {
	public String name;
	public long visits;

	public DemoModel(String name, long visits) {
		this.name = name;
		this.visits = visits;
	}
}
