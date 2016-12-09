package Pack_Simu;

//Cette classe représente un point de passage d'une voiture faisant du covoiturage
//Il s'agit d'un point de montée ou de descente d'un client
public class ParcoursStep
{
	Client cli;
	int type;
	//type :
	//0 position d'un client
	//1 destination d'un client

	public ParcoursStep(Client cliCons, int typeCons){
		cli = cliCons;
		type = typeCons;
	}
}
