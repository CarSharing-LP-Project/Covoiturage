package Pack_Simu;


//La classe client représente un voyageur
public class Client
{
	private int id;
	int appearanceMoment;
	private Point[] pos;
	//Index de pos :
	//0 position du client
	//1 destination du client
	int state;
	//state :
	//0 sur le trottoir
	//1 dans la voiture
	//2 arrivé
	private Car car;
	private boolean isUsingCarSharing;

	Client(int l, int time, int x, int y){
		setId(l);
		appearanceMoment = time;
		setPos(new Point[2]);
		getPos()[0] = new Point(x,y);
		setState(0);
		setUsingCarSharing(true);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public Point[] getPos() {
		return pos;
	}

	public void setPos(Point[] pos) {
		this.pos = pos;
	}

	public boolean isUsingCarSharing() {
		return isUsingCarSharing;
	}

	public void setUsingCarSharing(boolean isUsingCarSharing) {
		this.isUsingCarSharing = isUsingCarSharing;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}
}
