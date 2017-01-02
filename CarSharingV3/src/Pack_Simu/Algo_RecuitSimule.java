package Pack_Simu;

import java.util.ArrayList;

public class Algo_RecuitSimule implements I_Algorithme{
	
	int clientWaitingNumber;
	int carAlgoNumber;
	ArrayList<Car> carAlgoList;
	int [][] carOccupantArray;
	ArrayList<Client> clientAlgoList;
	int clientAlgoNumber;
	int costMin;
	int [][] matriceDePassage;
	Simulation simu;
	
	private void init (int etape, double temperature) {
		/* on a deux compteurs qui �voluent au cours du temps et qui
		 * peuvent servir de condition d'arr�t du while :
		 * le numero de l'�tape en cours et une "temp�rature"
		 */
		etape = 0;
		temperature = 1000;
	}
	
	private int[][] copieMatrice() {
		//Copie de la matrice courante
		return simu.copyMatrix(matriceDePassage);
	}
	
	private int clientRandom() {
		//on choisit un client au hasard
		return (int)Math.floor(clientAlgoNumber *Math.random());
	}
	
	private int getVoitureClient(int[][] copy, int clientRandom) {
		//on trouve quelle est la voiture qui le transporte
		int car = 0;
		for(int k=0; k<carAlgoNumber; k++){
			if(copy[k][2*clientRandom+1]!=-1){
				car=k;
			}
		}
		return car;
	}
	
	public void retirerClientDeLaMatrice (int[][] copy, int pos, int target, int clientRandom, int car) {
		// on retire le client de la matrice de passage
		pos = copy[car][2*clientRandom];
		target = copy[car][2*clientRandom+1];
		copy[car][2*clientRandom]=-1;
		copy[car][2*clientRandom+1]=-1;
	}
	
	public void decalerOrdrePassageVoiturePourComblerLesTrous (int[][] copy, int car, int target, int pos) {
	// il faut d�caler les ordres de passage de la voiture pour combler les trous
		for (int q=0; q<2*clientAlgoNumber; q++){
			if(copy[car][q]>target){
				copy[car][q]=copy[car][q]-2;
			}
			else{
				if(copy[car][q]>pos){
					copy[car][q]=copy[car][q]-1;
				}
			}
		}
	}
	
	private int quelleCar() {
		// on choisit au hasard la voiture qui va le transporter
		return (int)Math.floor(Math.random()*carAlgoNumber);
	}
	
	private int coutDeCopy(int[][] copy) {
		return simu.cost(copy,carAlgoList,clientAlgoList);
	}
	
	public void testComparatifDeCout(int coutDeCopy, int[][] copy, double temperature) {
		if (coutDeCopy<=costMin){
			matriceDePassage=copy;
			costMin=coutDeCopy;
		}
		else{
			int diffDeCout = coutDeCopy-costMin;
			double r = Math.random();
			if (r<Math.exp(-((double)diffDeCout/temperature))){
				matriceDePassage=copy;
				costMin=coutDeCopy;
			}
		}
	}
	
	private void incrementeCompteur(int etape, double temperature) {
		// pour finir on incr�mente les compteurs
		etape=etape+1;
		temperature=0.99*temperature;
	} 
	
	{
		int etape = 0;
		double temperature = 0;
		init(etape,temperature);
		
		// DEBUT DES ITERATIONS
		while (etape < simu.getStepMax() ){
			
			/* on commence par cr�er une copie de la matrice de passage,
			 * sur laquelle on va effectuer des modifications al�atoires,
			 * à caract�re �l�mentaire,
			 * pour ensuite la comparer avec la matrice courante
			 */
			
			int[][] copy = copieMatrice();
			int clientRandom = clientRandom();
			int car = getVoitureClient(copy,clientRandom);
			
			/* 2 cas dans l'�tat actuel des choses :
			 * soit le client est toujours sur le trottoir,
			 * soit il est dans une voiture
			 */
			
			if (clientRandom < clientWaitingNumber) {
				/* si le client est toujours sur le trottoir,
				 * l'id�e est de le sortir de la matrice
				 * et de le r�injecter dedans � un endroit al�atoire
				 * mais n�anmoins compatible avec occupantMax
				 */
				
				int pos = 0;
				int target = 0;
				
				retirerClientDeLaMatrice(copy, pos, target, clientRandom, car);
				
				decalerOrdrePassageVoiturePourComblerLesTrous(copy, car, target, pos);
				
				// à pr�sent on r�injecte le client

				int quelleCar = quelleCar();
				
				/* on d�termine p le nombre d'entiers positifs sur la ligne,
				 * ils vont de 0 � p-1
				 */
				
				int m = simu.nombreDeMoinsUn(copy[quelleCar]);
				int p = 2*clientAlgoNumber-m;
				/* on choisit un ordre de passage al�atoire pour
				 * la position de d�part du client, sachant
				 * qu'il doit �tre compris entre 0 et p
				 * et doit respecter la condition occupantCapacity
				 */
				/* on commence donc par lister les indices entre 0 et p
				 * compatibles avec occupantCapacity
				 */
				ArrayList<Integer> compatiblePos = new ArrayList<Integer>();
				for (int x=0 ; x<=p ; x++){
					if (simu.passagers(copy[quelleCar],x)+carOccupantArray[quelleCar].length<simu.getOccupantCapacity()){
						compatiblePos.add(x);
					}
				}
				/* une fois la liste faite,
				 * il reste � y prendre un �l�ment al�atoire
				 */
				int lenPos = compatiblePos.size();
				int randPos = compatiblePos.get((int)Math.floor(lenPos*Math.random()));
				/* on d�cale d'un cran les ordres de passage
				 * sup�rieurs à randPos
				 */
				for(int q =0 ; q<2*clientAlgoNumber ; q++){
					if(copy[quelleCar][q]>=randPos){
						copy[quelleCar][q]=copy[quelleCar][q]+1;
					}
				}
				copy[quelleCar][2*clientRandom]=randPos;
				/* on choisit un ordre de passage al�atoire pour
				 * la position cible du client, sachant
				 * qu'il doit �tre compris entre randPos+1 et p+1
				 * et respecter la condition occupantCapacity
				 */
				/* à partir de randPos+1 on liste donc tous les
				 * indices compatibles dans l'ordre croissant
				 * jusqu'� trouver un indice incompatible ou
				 * jusqu'� arriver � p+1
				 */
				boolean critere = true;
				ArrayList<Integer> compatibleTarget = new ArrayList<Integer>();
				for (int z = randPos+1 ; z <= p+1 ; z++){
					if (critere){
						if (simu.passagers(copy[quelleCar],z)+carOccupantArray[quelleCar].length
								<= simu.getOccupantCapacity()){
							compatibleTarget.add(z);
						}
						else {
							critere = false;
						}
					}
				}
				/* une fois la liste faite,
				 * il reste � y prendre un �l�ment al�atoire
				 */
				int lenTarget = compatibleTarget.size();
				int randTarget = compatibleTarget.get((int)Math.floor(lenTarget*Math.random()));
				/* on d�cale d'un cran les ordres de passage
				 * sup�rieurs � randTarget
				 */
				for(int q =0 ; q<2*clientAlgoNumber ; q++){
					if(copy[quelleCar][q]>=randTarget){
						copy[quelleCar][q]=copy[quelleCar][q]+1;
					}
				}
				copy[quelleCar][2*clientRandom+1]=randTarget;
			}
			else {
				/* si le client est d�j� dans une voiture
				 * (le cas o� le client est arriv� n'appara�t pas car il
				 * a alors �t� sorti de clientArray),
				 * alors il ne bouge pas de voiture, on change simplement sa
				 * destination d'indice
				 */
				//on retire sa destination de la matrice de passage
				int target = copy[car][2*clientRandom+1];
				copy[car][2*clientRandom+1]=-1;
				// il faut d�caler les ordres de passage de la voiture pour combler les trous
				for (int q=0; q<2*clientAlgoNumber; q++){
					if(copy[car][q]>target){
						copy[car][q]=copy[car][q]-1;
					}
				}
				// à pr�sent on r�injecte la destination
				/* on d�termine p le nombre d'entiers positifs sur la ligne,
				 * ils vont de 0 � p-1
				 */
				int m = simu.nombreDeMoinsUn(copy[car]);
				int p = 2*clientAlgoNumber-m;
				/* on choisit un ordre de passage al�atoire pour
				 * la destination du client, sachant
				 * qu'il doit �tre compris entre 0 et p
				 * et respecter la condition occupantMax
				 */
				/* � partir de 0 on liste donc tous les
				 * indices compatibles dans l'ordre croissant
				 * jusqu'� trouver un indice incompatible ou
				 * jusqu'� arriver à p
				 */
				boolean critere = true;
				ArrayList<Integer> compatibleTarget = new ArrayList<Integer>();
				for (int z = 0 ; z <= p ; z++){
					if (critere){
						if (simu.passagers(copy[car],z)+carOccupantArray[car].length<=simu.getOccupantCapacity()){
							compatibleTarget.add(z);
						}
						else {
							critere = false;
						}
					}
				}
				/* une fois la liste faite,
				 * il reste � y prendre un �l�ment al�atoire
				 */
				int lenTarget = compatibleTarget.size();
				int randTarget = compatibleTarget.get((int)Math.floor(lenTarget*Math.random()));
				/* on décale d'un cran les ordres de passage
				 * sup�rieurs � randTarget
				 */
				for(int q =0 ; q<2*clientAlgoNumber ; q++){
					if(copy[car][q]>=randTarget){
						copy[car][q]=copy[car][q]+1;
					}
				}
				copy[car][2*clientRandom+1]=randTarget;
			}
			
			int coutDeCopy = coutDeCopy(copy);
			
			/* � pr�sent vient le test comparatif de co�t :
			 * si le nouveau co�t est plus faible, on accepte la solution ;
			 * sinon on l'accepte avec une probabilit� de
			 * exp(-(le module de la diff�rence des co�ts)/la temp�rature)
			 */
			
			testComparatifDeCout(coutDeCopy, copy, temperature);
			
			incrementeCompteur(etape, temperature);
		}
	}
}
