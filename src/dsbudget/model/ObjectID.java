package dsbudget.model;

//problem with our current XML is that it doesn't use any IDs..
//in order to be able to identify various object within the app, let's create ID
public class ObjectID {
	private Integer id;
	
	static int nextid = 0;
	static int getNextID() {
		return nextid++;
	}
	
	ObjectID()
	{
		id = ObjectID.getNextID();
	}
	public Integer getID() {
		return id;
	}
}
