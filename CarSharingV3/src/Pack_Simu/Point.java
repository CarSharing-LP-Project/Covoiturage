package Pack_Simu;

//Cette classe représente un doublet d'entier
public class Point
{   
	private int x;
	private int y;

	Point(int xCons,int yCons){
		setX(xCons);
		setY(yCons);
	}

	public void set(int xCons, int yCons)
	{
		setX(xCons);
		setY(yCons);
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
