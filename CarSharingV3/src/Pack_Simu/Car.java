package Pack_Simu;

import java.util.ArrayList;


//La classe des voitures
public class Car
{
	private int id;
	private Point pos;
	//parcoursList contient les étapes du parcours
	private ArrayList<ParcoursStep> parcoursList = new ArrayList<ParcoursStep>();
	//occupantList contient les clients dans la voiture
	private ArrayList<Client> occupantList = new ArrayList<Client>();
	//indique le numéro de la rue dans laquelle la voiture se trouve
	int streetId;
	private boolean isDoingCarSharing;

	Car(int k, int x, int y){
		setId(k);
		setPos(new Point(x, y));
		streetId = -1;
		setDoingCarSharing(true);
	}

	public boolean isDoingCarSharing() {
		return isDoingCarSharing;
	}

	public void setDoingCarSharing(boolean isDoingCarSharing) {
		this.isDoingCarSharing = isDoingCarSharing;
	}

	public ArrayList<Client> getOccupantList() {
		return occupantList;
	}

	public void setOccupantList(ArrayList<Client> occupantList) {
		this.occupantList = occupantList;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Point getPos() {
		return pos;
	}

	public void setPos(Point pos) {
		this.pos = pos;
	}

	public ArrayList<ParcoursStep> getParcoursList() {
		return parcoursList;
	}

	public void setParcoursList(ArrayList<ParcoursStep> parcoursList) {
		this.parcoursList = parcoursList;
	}
}
