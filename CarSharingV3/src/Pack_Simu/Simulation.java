package Pack_Simu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class Simulation{
	/** VARIABLES ET LEUR CREATION **/
	private int time;
	private boolean needAlgorithme;
	private ArrayList<Car> carList;
	int carNumber;
	int carIdCounter;
	private ArrayList<Client> clientList;
	int clientTotalNumber;
	int clientIdCounter;
	private Client notTargettedYet;
	private Car selectedCar;
	private Client selectedClient;
	int carLength;
	int streetLength;
	int cityWidth;
	int cityHeight;
	int[][][] streetArray;
	private double carSpeedMean;
	double clientSpeedSum;
	int arrivedClient;
	private double clientSpeedMean;
	private double clientRealSpeedMean;
	private double arrivedRate;
	private int distSum;
	private int consumption;

	//Initialisation des variables
	public Simulation(int cLength, int sLength, int width, int height)
	{
		setTime(0);
		setNeedAlgorithme(false);
		setCarList(new ArrayList<Car>());
		carNumber = 0;
		carIdCounter = 0;
		setClientList(new ArrayList<Client>());
		clientTotalNumber = 0;
		clientIdCounter = 0;
		setNotTargettedYet(null);
		setSelectedCar(null);
		setSelectedClient(null);
		carLength = cLength;
		streetLength = sLength;
		cityWidth = width/streetLength+1;
		cityHeight = height/streetLength+1;
		streetArray = new int[cityWidth][cityHeight][2];
		setCarSpeedMean(0);
		clientSpeedSum = 0;
		arrivedClient = 0;
		setClientSpeedMean(0);
		setClientRealSpeedMean(0);
		setArrivedRate(0);
		setDistSum(0);
		setConsumption(0);

		System.out.println();
		System.out.println("Nouvelle simulation");
	}

	public void newCar(int x,int y)
	{
		getCarList().add(new Car(carIdCounter,x,y));
		carNumber++;
		carIdCounter++;
		setSelectedCar(getCarList().get(carNumber-1));
		setNeedAlgorithme(true);
	}

	public void newClient(int x,int y)
	{
		//S'il y a un client dont la destination n'est pas encore d�finie
		if(getNotTargettedYet() != null){
			getNotTargettedYet().getPos()[1] = new Point(x, y);
			getClientList().add(getNotTargettedYet());
			clientTotalNumber++;
			setSelectedClient(getNotTargettedYet());
			setNotTargettedYet(null);
			if(dist(getSelectedClient().getPos()[0],getSelectedClient().getPos()[1])==0) deleteClient();
			else setNeedAlgorithme(true);
		} else {
			//Sinon on cr�e un nouveau client
			setNotTargettedYet(new Client(clientIdCounter,getTime(),x,y));
			clientIdCounter++;
		} 
	}

	public void deleteCar(){
		getCarList().remove(getSelectedCar());
		carNumber--;
		setSelectedCar(null);
		setNeedAlgorithme(true);
	}

	public void deleteClient(){
		if(getSelectedClient() == getNotTargettedYet()) setNotTargettedYet(null);
		else {
			if(getSelectedClient().getState() == 1) getSelectedClient().getCar().getOccupantList().remove(getSelectedClient());
			getClientList().remove(getSelectedClient());
			clientTotalNumber--;
		}
		setSelectedClient(null);
		setNeedAlgorithme(true);
	}

	//Renvoie un tableau contenant les coordonn�es des voitures
	public int[] carIntArray()
	{
		int[] t = new int[carNumber*2];
		for(int k=0;k<carNumber;k++)
		{
			t[2*k]=getCarList().get(k).getPos().getX();
			t[2*k+1]=getCarList().get(k).getPos().getY();
		}
		return t;
	}

	//Renvoie un tableau contenant les coordonn�es des clients
	public int[] clientIntArray()
	{
		int[] t = new int[clientTotalNumber*4 + 2*((getNotTargettedYet()!=null)?1:0)];
		for(int l=0;l<clientTotalNumber;l++)
		{
			t[4*l]=getClientList().get(l).getPos()[0].getX();
			t[4*l+1]=getClientList().get(l).getPos()[0].getY();
			t[4*l+2]=getClientList().get(l).getPos()[1].getX();
			t[4*l+3]=getClientList().get(l).getPos()[1].getY();
		}
		if(getNotTargettedYet()!=null){
			t[4*clientTotalNumber]=getNotTargettedYet().getPos()[0].getX();
			t[4*clientTotalNumber+1]=getNotTargettedYet().getPos()[0].getY();	
		}
		return t;
	}


	/** ALGORITHME : DETERMINATION DU PARCOURS **/
	private //Numero de l'algorithme s�lectionn�
	int algoId;
	//Numero de la fonction de co�t s�lectionn�e
	private int costRate;
	//bool�en indiquant si le crible diviser pour mieux r�gner doit �tre effectu�
	private boolean divide;
	//Nombre d'�tapes maximum s�lectionn�
	private int stepMax;
	//Nombre de passagers maximum
	private int occupantCapacity;

	//Fonction algorithme d�terminant le parcours le moins couteux
	public void algorithme(){
		//1. DEFINITION DU CADRE D'ETUDE
		//On ne s�lectionne que les voitures qui participent au covoiturage
		ArrayList<Car> carAlgoList = new ArrayList<Car>();
		for(Car car: getCarList()){
			if(car.isDoingCarSharing()) carAlgoList.add(car);
		}
		//On cherche le nombre de clients sur le trottoir
		int clientWaitingNumber = 0;
		//On cr�e la liste des clients � prendre en compte
		ArrayList<Client> clientAlgoList = new ArrayList<Client>();
		for(Client cli: getClientList()){
			if(cli.getState() == 0 && cli.isUsingCarSharing()){
				clientWaitingNumber++;
				//Les clients sur le trottoir sont rajout�s au d�but
				clientAlgoList.add(0,cli);
			}
			else if(cli.getState() == 1 && cli.isUsingCarSharing())
				//Les clients d�j� embarqu�s sont rajout�s � la fin
				clientAlgoList.add(cli);
		}

		//2. RECHERCHE DE LA MATRICE DE PASSAGE MINIMISANT LE COUT
		int[][] matriceDePassage = searchMatriceDePassage(carAlgoList,clientAlgoList,clientWaitingNumber);

		//3. CONFIGURATION DES PARCOURS DES VOITURES
		setParcours(matriceDePassage,carAlgoList,clientAlgoList);
		
		setNeedAlgorithme(false);
	}

	//fonction d�terminant la matrice de passage la moins couteuse
	//Une matrice de passage est d�finie de la mani�re suivante
	//taille : carNumber lignes, 2*clientAlgoNumber colonnes
	//dans la case (k,2*l) : l'ordre de passage de la voiture k quand elle prend le client l
	//dans la case (k,2*l+1) : l'ordre de passage de la voiture k quand elle depose le client l
	//si la voiture k ne prend ni ne depose le client l, -1 dans les cases (k,2*l) et (k,2*l+1)
	//Exemple :
	//voiture 0 : prend 1, prend 0, depose 0, depose 1
	//voiture 1 : prend 2, depose 2
	//matriceDePassage :
	//| 1| 2| 0| 3|-1|-1|
	//|-1|-1|-1|-1| 0| 1|
	int[][] searchMatriceDePassage(ArrayList<Car> carAlgoList,
			ArrayList<Client> clientAlgoList, int clientWaitingNumber)
	{
		int carAlgoNumber = carAlgoList.size();
		//Le nombre de clients de l'alogrithme
		int clientAlgoNumber = clientAlgoList.size();
		//S'il n'y a pas de voiture ou pas de client dans le cadre d'étude, on ne lance pas l'algorithme
		if(carAlgoNumber == 0 || clientAlgoNumber == 0) return new int[carAlgoNumber][0];

		/** CREATION DE LA MATRICE DE PASSAGE INITIALE **/
		int[][] matriceDePassage = new int[carAlgoNumber][2*clientAlgoNumber];
		//On cr�e dans le m�me temps le tableau des occupants de la voiture renum�rot�s
		int[][] carOccupantArray = new int[carAlgoNumber][];
		//et on cherche le plus grand nombre d'occupants
		int occupantMax = 0;
		for(int k = 0; k<carAlgoNumber;k++){
			ArrayList<Client> occupantList = carAlgoList.get(k).getOccupantList();
			int occupantNumber = occupantList.size();
			for(int q=0;q<2*clientAlgoNumber;q++)
				//Les clients sur le trottoir sont rajout�s au d�but du parcours de la voiture 0
				//On remplit le reste de la matrice avec des -1
				matriceDePassage[k][q]=(k==0 && q<2*clientWaitingNumber)?q+occupantNumber:-1;
			occupantMax = Math.max(occupantMax,occupantNumber);
			carOccupantArray[k] = new int[occupantNumber];
			for(int z = 0; z<occupantNumber; z++){
				//On r�cup�re le numero du client dans le ref�rentiel de l'algorithme
				int lAlgo = clientAlgoList.indexOf((Client)occupantList.get(z));
				//On l'ajoute dans la liste des occupants
				carOccupantArray[k][z] = lAlgo;
				//On l'ajoute dans le parcours de la voiture, � la suite des clients � prendre (car n�0)
				matriceDePassage[k][2*lAlgo+1] = z;
			}
		}
		//On cherchera le co�t minimum des matrices de passages cr��es
		int costMin = cost(matriceDePassage,carAlgoList,clientAlgoList);

		/*
        Exemple : clientWaitingNumber = 3, deux occupants dans les voitures 0 et 1
        [ 0, 1, 2, 3, 4, 5,-1,-1,-1, 6]
		[-1,-1,-1,-1,-1,-1,-1, 0,-1,-1]
		Les six premiers indices correspondent aux trois clients sur le trottoir
		Les quatre derniers aux deux clients respectivement dans les voitures 1 et 0
		 */

		//On effectue une disjonction de cas selon l'algorithme s�lectionn�
		switch(getAlgoId()) {

		/** ALGORITHME DETERMINISTE **/
		case 0:
		{
			/* Deux �tapes dans l'algorithme d�terministe :
			 * 1. On g�n�re tous les parcours possibles d'une voiture, ayant :
			 *     toBeToken clients � prendre et d�poser
			 *     occupantNumber clients d�j� embarqu�s � d�poser
			 *    Ces parcours seront stockés dans le tableau possibleParcoursArray
			 * 2. On r�partit les clients dans les voitures de toutes les fa�ons possibles
			 *     Pour chaque possibilit�, on attribue aux voitures tous les parcours possibles
			 *     selon le nombre d'occupants et de clients à charge
			 */

			//Les parcours seront stock�s dans le tableau ci-dessous :
			//possibleParcours[nombre de clients � chercher][nombre d'occupants � d�poser]
			// -> liste de parcours de type int[] de taille 2*(nombre de � chercher) + nombre d'occupants

			ArrayList<Integer[]>[][] possibleParcours = new ArrayList[clientWaitingNumber+1][];
			int toBeToken = -1;
			int possibilityMax = 1;
			//Tant que (le nombre de client � prendre < nombre de client sur le trottoir
			while( (toBeToken = toBeToken + 1) < clientWaitingNumber + 1
					//et que le nombre maximal de possibilit�s ne d�passe pas stepMax
					&& possibilityMax*(2*toBeToken+occupantMax-1)*(2*toBeToken+occupantMax)/2*carAlgoNumber
					<= getStepMax())
			{
				possibleParcours[toBeToken] = new ArrayList[occupantMax+1];
				for(int occupantNumber = 0; occupantNumber<occupantMax+1;occupantNumber++)
				{
					//Dans la boucle, on permute un tableau skipArray de fa�on lexicographique
					//Exemple toBeToken = 2, occupantNumber = 3,
					//Les quatre premi�res cases correspondent � toBeToken
					//Les trois dernières à occupantNumber
					//[0,0,0,0,0,0,0] qWhile = 6
					//[0,0,0,0,0,0,0] qWhile = 5
					//[0,0,0,0,0,1,0] qWhile = 6
					//[0,0,0,0,0,1,0] qWhile = 5
					//[0,0,0,0,0,0,0] qWhile = 4
					//[0,0,0,0,1,0,0] qWhile = 6
					//[0,0,0,0,1,0,0] qWhile = 5
					//[0,0,0,0,1,1,0] qWhile = 6
					//...
					//[0,0,0,0,2,1,0] qWhile = 6
					//[0,0,0,0,2,1,0] qWhile = 5
					//[0,0,0,0,2,0,0] qWhile = 4
					//[0,0,0,0,0,0,0] qWhile = 3
					//[0,0,0,1,0,0,0] qWhile = 6
					//...
					//[0,0,0,3,2,1,0] qWhile = 6
					//...
					//[0,0,0,0,0,0,0] qWhile = 2
					//[0,0,1,1,0,0,0] qWhile = 6
					//...
					//[0,0,3,3,2,1,0]
					//...
					//[0,0,3,3,0,0,0] qWhile = 3
					//[0,0,3,0,0,0,0] qWhile = 2
					//[0,0,0,0,0,0,0] qWhile = 1
					//[0,1,0,0,0,0,0] qWhile = 6
					//...
					//[5,5,3,3,2,1,0] qWhile = 6
					//...
					//[0,0,0,0,0,0,0] qWhile = -1

					//skipArray[q] repr�sente le num�ro des possibilit�s restantes que l'on prend dans
					//parcoursRefList initialis� � [0,1,2,...,2*toBeToken+occupantNumber-1]
					//Exemple de construction de parcours : toBeToken = 2, occupantNumber = 3
					//skipArray = [1,2,0,0,2,0,0],
					//avant de commencer : parcoursCopyList = [0,1,2,3,4,5,6], parcours []
					//q=0, skipArray[q]=1, parcoursCopyList = [0,2,3,4,5,6], parcours [1]
					//q=1, skipArray[q]=2, parcoursCopyList = [0,2,4,5,6], parcours [1,3]
					//q=2, skipArray[q]=0, parcoursCopyList = [2,4,5,6], parcours [1,3,0]
					//q=3, skipArray[q]=0, parcoursCopyList = [4,5,6], parcours [1,3,0,2]
					//q=4, skipArray[q]=2, parcoursCopyList = [4,5], parcours [1,3,0,2,6]
					//q=5, skipArray[q]=0, parcoursCopyList = [5], parcours [1,3,0,2,6,4]
					//q=6, skipArray[q]=0, parcoursCopyList = [], parcours [1,3,0,2,6,4,5]
					//Ainsi skipArray = [1,2,0,0] donnera [1,3,0,2] comme parcours

					int[] skipArray = new int[2*toBeToken+occupantNumber];
					//On cr�e la liste ordonn�e de r�f�rence
					ArrayList<Integer> parcoursRefList = new ArrayList<Integer>();
					for(int q =0;q<2*toBeToken+occupantNumber;q++) parcoursRefList.add(q);

					possibleParcours[toBeToken][occupantNumber] = new ArrayList();
					int qWhile = 2*toBeToken+occupantNumber-1;
					while(qWhile!=-1){
						//Enregistrement du parcours si permut�
						if(qWhile == 2*toBeToken+occupantNumber-1){
							//On copie le parcours de r�f�rence
							ArrayList<Integer> parcoursCopyList =
									(ArrayList<Integer>) parcoursRefList.clone();
							Integer[] t = new Integer[2*toBeToken+occupantNumber];
							boolean[] occupantIn = new boolean[2*toBeToken+occupantNumber];
							for(int q = 0; q<2*toBeToken+occupantNumber; q++){
								//On supprime les �tapes du parcours copi� selon skipArray
								int rank = (Integer) parcoursCopyList.remove(skipArray[q]);
								t[q] = rank;
								occupantIn[rank] = (q%2==0);
							}
							//le parcours ne doit pas faire pas d�passer la capacit� maximale du v�hicule
							int q = -1;
							int occupantCompt = occupantNumber;
							while((q=q+1)<2*toBeToken+occupantNumber
									//on incr�mente ou d�cr�mente le nombre d'occupants
									&& (occupantCompt=occupantCompt+((occupantIn[q])?1:-1))
									//et ce nombre doit �tre inf�rieur � la capacité maximale
									<= getOccupantCapacity());
							//Si pas de surcharge, on ajoute le parcours
							if(q==2*toBeToken+occupantNumber)
								possibleParcours[toBeToken][occupantNumber].add(t);
						}
						//On incr�mente skipArray[qWhile]
						if((skipArray[qWhile]=skipArray[qWhile]+1)
								//Si on a atteint le maximum de la case,
								== 2*toBeToken+occupantNumber-qWhile)
						{
							//On remet � 0 et on d�cale le curseur � gauche
							skipArray[qWhile]=0;
							qWhile--;
						}
						//Si on se trouve dans les clients sur le trottoir aux cases pairs (position)
						else if(qWhile<2*toBeToken && qWhile%2 == 0
								//On incr�mente aussi la case impaire (arriv�e)
								&& (skipArray[qWhile+1] = skipArray[qWhile])
								//A condition que celle-ci ne soit pas trop �lev�e
								== 2*toBeToken+occupantNumber-qWhile-1)
						{
							//Auquel cas on remet les deux cases � z�ro et on d�cale le curseur � gauche
							skipArray[qWhile+1]=0;
							skipArray[qWhile]=0;
							qWhile--;
						}
						//Si la permutation est valide, on remet le curseur tout � droite
						else qWhile = 2*toBeToken+occupantNumber-1;
					}
					possibilityMax = possibleParcours[toBeToken][occupantNumber].size();
				}
			}

			if(toBeToken <= clientWaitingNumber){
				System.out.println("Nombre maximal de clients pris en");
				System.out.println("charge par une voiture r�duit � "+(toBeToken-1));
				System.out.println("Augmenter le nombre d'�tapes");
			}

			//On cr�e d'abord un tableau contenant les puissances de 2
			int power2n = 1;
			int[] power2l = new int[clientWaitingNumber]; 
			for(int l = 0; l<clientWaitingNumber; l++){
				power2l[l] = power2n;
				power2n = 2*power2n;
			}
			int[][] carCost = new int[carAlgoNumber][power2n];
			int[][][] carLine = new int[carAlgoNumber][power2n][2*clientAlgoNumber];
			for(int k = 0; k<carAlgoNumber;k++){
				int[] toTakeArray = new int[clientWaitingNumber];
				ArrayList<Integer> toTakeList = new ArrayList<Integer>();
				int toTakeListId = 0;
				int lWhile = 0;
				while(lWhile != clientWaitingNumber)
				{
					if(lWhile == 0){
						carCost[k][toTakeListId] = -1;
						int n = toTakeList.size();
						if(n < toBeToken){
							int[] lineAux = new int[2*clientAlgoNumber];
							//On commence par remplir la ligne de -1
							for(int q = 0; q<2*clientAlgoNumber;q++)
								carLine[k][toTakeListId][q] = lineAux[q]= -1;
							//On boucle sur les parcours possibles
							for(Integer[] t :
								possibleParcours[toTakeList.size()][carOccupantArray[k].length]){
								//On compl�te la partie des clientWaiting
								for(int l =0; l<n;l++){
									int l2 = toTakeList.get(l);
									lineAux[2*l2] = t[2*l];
									lineAux[2*l2+1] = t[2*l+1];
								}
								//On compl�te la partie des occupants
								for(int l=0; l<carOccupantArray[k].length;l++){
									int l2 = carOccupantArray[k][l];
									lineAux[2*l2+1] = t[2*n+l];
								}
								//On calcule le co�t de la ligne
								int cost = cost(new int[][]{lineAux},
										new ArrayList<Car>(Arrays.asList(carAlgoList.get(k))),clientAlgoList);
								//Si le cout est petit on copie la ligne
								if(cost < carCost[k][toTakeListId] || carCost[k][toTakeListId] == -1){
									carCost[k][toTakeListId] = cost;
									carLine[k][toTakeListId] = lineAux.clone();
								}
							}
						}
						toTakeListId++;
					}
					if(toTakeArray[lWhile] == 1)
					{
						toTakeArray[lWhile] = 0;
						toTakeList.remove((Integer)lWhile);
						lWhile++;
					}
					else
					{
						toTakeArray[lWhile] = 1;
						toTakeList.add((Integer)lWhile);
						lWhile=0;
					}
				}
			}
			
			
			//on attribue aux clients une voiture dans clientCar de fa�on lexicographique am�lior�
			//Exemple clientWaitingNumber = 2, carNumber = 3,
			/*
			 * [0, 0] lWhile = 1
			 * [0, 1] lWhile = 1
			 * [0, 2] lWhile = 1
			 * [0, 2] lWhile = 2
			 * [1, 2] lWhile = 1
			 * [1, 1] lWhile = 1
			 * [1, 0] lWhile = 1
			 * [1, 0] lWhile = 2
			 * [2, 0] lWhile = 1
			 * [2, 1] lWhile = 1
			 * [2, 2] lWhile = 1
			 * [2, 2] lWhile = 2
			 * lWhile = -1
			 */
			
			int[] clientCar = new int[clientWaitingNumber];
			//ce tableau stocke les indexes des ensembles de clients associ� � chaque voiture
			int[] carPower = new int[carAlgoNumber];
			carPower[0]=power2n-1;
			int cost = 0;
			for(int k = 0; k<carAlgoNumber;k++) cost += carCost[k][carPower[k]];
			
			//Cet entier compte les voitures qui d�passent le nombre de clients restreints
			int tooManyInCar = (carCost[0][power2n-1] == -1)?1:0;
			//Ce tableau de bool�ens contient les sens d'incr�mentation pour un lexicographique am�lior�
			boolean[] lReverse = new boolean[clientWaitingNumber];
			//Variable de boucle correspondant au waitingClient dont on change la voiture
			int lWhile = clientWaitingNumber-1;
			while(lWhile != -1){
				if(lWhile == clientWaitingNumber-1 && tooManyInCar == 0 && cost < costMin){
					costMin = cost;
					for(int k = 0; k<carAlgoNumber;k++)
						matriceDePassage[k] = carLine[k][carPower[k]];
				}
				//Si on est arriv� au maximum d'incr�mentation
				if(clientCar[lWhile] == ((lReverse[lWhile])?0:carAlgoNumber-1))
				{
					//On d�cale le curseur � gauche sans oublier d'inverser le sens
					lReverse[lWhile] = !lReverse[lWhile];
					lWhile--;
				}
				//Sinon on proc�de � la permutation
				else {
					//On enl�ve le client � la liste des clients de son ancienne voiture
					int k0 = clientCar[lWhile];
					//On garde en m�moire l'ancien co�t
					int k0LastCost = carCost[k0][carPower[k0]];
					//On enl�ve le client lWhile
					carPower[k0] -= power2l[lWhile];
					//on regarde si on est sorti d'une situation interdite
					if(k0LastCost == -1 && carCost[k0][carPower[k0]] != -1)	tooManyInCar--;
					
					//On change la voiture
					clientCar[lWhile] = clientCar[lWhile] + ((lReverse[lWhile])?-1:+1);
					
					//On ajoute le client à la liste des clients de sa nouvelle voiture
					int k1 = clientCar[lWhile];
					//On garde en m�moire l'ancien co�t
					int k1LastCost = carCost[k1][carPower[k1]];
					//On rajoute le client lWhile
					carPower[k1] += power2l[lWhile];
					//on regarde si on entre dans une situation interdite
					if(k1LastCost != -1 && carCost[k1][carPower[k1]] == -1) tooManyInCar++;
					
					//On met le curseur tout à droite
					lWhile=clientWaitingNumber-1;
					
					//On recalcule le coût
					cost += carCost[k0][carPower[k0]] - k0LastCost + carCost[k1][carPower[k1]] - k1LastCost;
				}
			}
		} break;


		case 1:
		{
			/* on a deux compteurs qui �voluent au cours du temps et qui
			 * peuvent servir de condition d'arr�t du while :
			 * le numero de l'�tape en cours et une "temp�rature"
			 */
			int etape = 0;
			double temperature = 1000;
			// DEBUT DES ITERATIONS
			while (etape < getStepMax() ){
				/* on commence par cr�er une copie de la matrice de passage,
				 * sur laquelle on va effectuer des modifications al�atoires,
				 * à caract�re �l�mentaire,
				 * pour ensuite la comparer avec la matrice courante
				 */
				int[][] copy = copyMatrix(matriceDePassage);
				//on choisit un client au hasard
				int clientRandom = (int)Math.floor(clientAlgoNumber *Math.random());
				//on trouve quelle est la voiture qui le transporte
				int car = 0;
				for(int k=0; k<carAlgoNumber; k++){
					if(copy[k][2*clientRandom+1]!=-1){
						car=k;
					}
				}
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
					// on le retire de la matrice de passage
					int pos = copy[car][2*clientRandom];
					int target = copy[car][2*clientRandom+1];
					copy[car][2*clientRandom]=-1;
					copy[car][2*clientRandom+1]=-1;
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
					// à pr�sent on r�injecte le client
					// on choisit au hasard la voiture qui va le transporter
					int quelleCar = (int)Math.floor(Math.random()*carAlgoNumber);
					/* on d�termine p le nombre d'entiers positifs sur la ligne,
					 * ils vont de 0 � p-1
					 */
					int m = nombreDeMoinsUn(copy[quelleCar]);
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
						if (passagers(copy[quelleCar],x)+carOccupantArray[quelleCar].length<getOccupantCapacity()){
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
							if (passagers(copy[quelleCar],z)+carOccupantArray[quelleCar].length
									<= getOccupantCapacity()){
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
					int m = nombreDeMoinsUn(copy[car]);
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
							if (passagers(copy[car],z)+carOccupantArray[car].length<=getOccupantCapacity()){
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
				int coutDeCopy = cost(copy,carAlgoList,clientAlgoList);
				/* � pr�sent vient le test comparatif de co�t :
				 * si le nouveau co�t est plus faible, on accepte la solution ;
				 * sinon on l'accepte avec une probabilit� de
				 * exp(-(le module de la diff�rence des co�ts)/la temp�rature)
				 */
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
				// pour finir on incr�mente les compteurs
				etape=etape+1;
				temperature=0.99*temperature;
			}
		} break;

		/** ALGORITHME GENETIQUE **/
		//Clement et Thibault
		case 2: {

		} break;

		}

		//On affiche la matrice de passage dans la console ainsi que son co�t
		System.out.println("Matrice de passage :");
		printMatrix(matriceDePassage);
		System.out.println("cost : "+costMin);

		return(matriceDePassage);
	}



	/* la fonction suivante d�termine, pour un indice d'un point,
	 * le nombre de clients dans la voiture juste avant d'atteindre ce point
	 */
	int passagers ( int[] t, int indice) {
		int pass =0 ;
		int clientNumber = t.length/2;
		/*for (int l=0 ; l< clientNumber ; l++){
			if ((t[2*l]==-1)&&(t[2*l+1]>-1)){
				pass+=1;
			}
		}*/
		for (int x=0 ; x<indice ; x++ ){
			for (int q=0; q<2*clientNumber ; q++){
				if ((t[q]==x)&&(q%2==0)){
					pass+=1;
				}
				else {
					if (t[q]==x){
						pass-=1;
					}
				}
			}
		}
		return pass;
	}
	
	/* nombreDeMoinsUn d�termine le nombre de cases
	 * qui sont des -1 dans un tableau d'entiers
	 */
	int nombreDeMoinsUn(int[] t){
		int compteur = 0;
		for(int z = 0 ; z < t.length ; z++)
			if(t[z]==-1) compteur++;
		return compteur;
	}

	// copyMatrix copie une matrice dans une nouvelle matrice
	int[][] copyMatrix(int[][] m){
		int n = m.length;
		int[][] mat= new int[n][m[0].length];
		for(int x=0;x<n;x++)
			mat[x]=m[x].clone();
		return mat;
	}

	// printMatrix affiche une matrice dans la console
	void printMatrix(int[][] m){
		System.out.print("[");
		for(int x=0;x<m.length;x++)
			System.out.println(Arrays.toString(m[x]));
	}

	
	/** FONCTIONS RELATIVES AU CALCUL DU COUT **/
	//il s'agit de la distance en norme 1, nous sommes � Manhattan
	int dist( Point p1, Point p2)
	{
		return Math.abs(p1.getX()-p2.getX()) + Math.abs(p1.getY()-p2.getY());
	}

	//recherche d'index d'une valeur dans un tableau
	int searchInArray(int a, int[] t)
	{
		for(int z = 0; z<t.length; z++)
			if(t[z] == a) return z;
		return -1;
	}

	//Cette fonction calcule le cout d'un parcours d�fini par une matrice de passage
	int cost(int[][] matriceDePassage, ArrayList<Car> carAlgoList, ArrayList<Client> clientAlgoList)
	{
		int carAlgoNumber = carAlgoList.size();
		int cost = 0;
		for(int k=0;k<carAlgoNumber;k++)
		{
			int clientCost = 0;
			int bestDistSum = 0;
			int carDist = 0;
			//rank est le num�ro d'ordre de l'�tape de la voiture
			int rank = 0;
			Point p0 = null;
			int q1;
			//On cherche la ranki�me �tape
			while((q1 = searchInArray(rank,matriceDePassage[k]))!=-1)
			{
				//On r�cup�re le point correspondant � q1
				Point p1 = clientAlgoList.get(q1/2).getPos()[q1%2];
				//On ajoute la distance entre la voiture et p1 ou entre p0 et p1 si p0 non null
				carDist += dist((p0==null)?carAlgoList.get(k).getPos():p0, p1);
				if(q1%2==1){
					int bestDist = dist(clientAlgoList.get(q1/2).getPos()[0],clientAlgoList.get(q1/2).getPos()[1]);
					int realDist =
							((getTime()-clientAlgoList.get(q1/2).appearanceMoment)*carLength+carDist);
					clientCost += realDist * realDist /(bestDist*bestDist);
					bestDistSum += bestDist;
				}
				//On passe � l'�tape suivante
				rank++;
				p0 = p1;
			}
			if(bestDistSum != 0) cost += getCostRate()*clientCost
					+ (100-getCostRate())*carDist*carDist/(bestDistSum*bestDistSum);
		}
		return cost;
	}

	//Cette fonction remplit les listes parcoursList des voitures � partir d'une matriceDePassage
	//Logiquement cette fonction est appel�e lorsqu'on a trouv� la matriceDePassage qui minimise le cout
	void setParcours(int[][] matriceDePassage,ArrayList<Car> carAlgoList, ArrayList<Client> clientAlgoList)
	{
		int carAlgoNumber = carAlgoList.size();
		for(int k=0; k<carAlgoNumber; k++)
		{
			//On r�initialise le parcours
			carAlgoList.get(k).setParcoursList(new ArrayList<ParcoursStep>());
			int rank = 0;
			int q;
			//On cherche la stepNumi�me �tape dans la matrice de passage
			while((q = searchInArray(rank,matriceDePassage[k]))!=-1)
			{
				//on ajoute l'�tape au parcours de la voiture
				carAlgoList.get(k).getParcoursList().add(new ParcoursStep(clientAlgoList.get(q/2), q%2));
				rank++;
			}
		}
	}


	/** MOUVEMENTS ELEMENTAIRES **/
	public //avance les Car d'une case vers leur prochaine �tape
	void OneMove()
	{
		//On incr�mente le temps de la simulation
		setTime(getTime() + 1);
		//Nombre de voitures en circulation
		int activeCar = 0;
		//Somme des vitesses des voitures
		int speedSum = 0;
		//Somme des vitesses instantan�es des clients
		double clientRealSpeedSum = 0;
		for(Iterator<Car> it= getCarList().iterator();it.hasNext();)
		{
			Car car = it.next();
			//Si la voiture n'a pas termin� son parcours
			if(!car.getParcoursList().isEmpty()){
				activeCar++;
				int X = car.getPos().getX();
				int Y = car.getPos().getY();
				ParcoursStep step = car.getParcoursList().get(0);
				Point p = step.cli.getPos()[step.type];
				//Par d�faut la voiture est de vitesse la moiti� de sa longueur par unit� de temps
				int v = carLength/2;
				//Si la voiture se trouve dans une rue
				if(car.streetId != -1)
					v = v-v*2*carLength*(-1+
							//On calcule et on d�cr�mente le nombre de voitures dans la rue
					streetArray[(car.streetId/2)%cityWidth][(car.streetId/2)/cityWidth][car.streetId%2]--
					)/streetLength;
				//Si la vitesse est nulle, on donne une vitesse de 1 px � une probabilit� de 20%
				if(v<=0) v=(Math.random()>0.8)?1:0;
				//On incr�mente les donn�es de simulations
				speedSum += v;
				setDistSum(getDistSum() + v);
				setConsumption(getConsumption() + ((v== 0 || v == 1)?carLength/2:v));
				if(X < p.getX()) car.getPos().set((X+v>p.getX())?p.getX():X+v,Y);
				else if(X > p.getX()) car.getPos().set((X-v<p.getX())?p.getX():X-v,Y);
				else if(Y < p.getY()) car.getPos().set(X,(Y+v>p.getY())?p.getY():Y+v);
				else if(Y > p.getY()) car.getPos().set(X,(Y-v<p.getY())?p.getY():Y-v);
				else {
					//Si la voiture est arriv�e � une �tape du parcours
					//On change l'�tat du client embarqu� ou d�pos�
					step.cli.setState(step.cli.getState() + 1);
					//On l'ajoute � la liste des occupants s'il est embarqu�
					if(step.cli.getState() == 1){
						car.getOccupantList().add(step.cli);
						step.cli.setCar(car);
					}
					//Sinon on l'y enl�ve
					else{
						arrivedClient++;
						clientSpeedSum += (double)dist(step.cli.getPos()[0],step.cli.getPos()[1]) 
								/(double)(getTime()-step.cli.appearanceMoment);
						car.getOccupantList().remove(step.cli);
						getClientList().remove(step.cli);
						clientTotalNumber--;
					}
					//On supprime l'�tape du parcours de la voiture
					car.getParcoursList().remove(0);
				}
				//Si la voiture est toujours en circulation, on calcule le num�ro de sa rue
				if(!car.getParcoursList().isEmpty()){
					X = car.getPos().getX()/streetLength;
					Y = car.getPos().getY()/streetLength;
					if(car.getPos().getX() % streetLength == 0 && car.getPos().getY() % streetLength == 0)
						car.streetId = -1;
					else car.streetId = 2*Y*cityWidth+2*X+((car.getPos().getX() % streetLength == 0)?0:1);
					//On incr�mente le nombre de voitures dans la rue de la voiture
					if(car.streetId != -1) streetArray[X][Y][car.streetId%2]++;
				}
				//On supprime la voiture si elle ne fait pas de covoiturage
				else if(!car.isDoingCarSharing()){
					it.remove();
					carNumber--;
				}
			}
		}
		//CALCUL DES VITESSES MOYENNES
		setCarSpeedMean((activeCar!=0)?((double) speedSum / (double) activeCar):0);
		setClientSpeedMean((arrivedClient!=0)?(clientSpeedSum/(double)arrivedClient):0);
		for(Client cli: getClientList())
			clientRealSpeedSum += (cli.getCar() == null)?0:(double)dist(cli.getPos()[0],cli.getCar().getPos()) 
			/(double)(getTime()-cli.appearanceMoment);
		setClientRealSpeedMean((clientTotalNumber!=0)?(clientRealSpeedSum/(double)clientTotalNumber):0);
		setArrivedRate((double) arrivedClient/ (double)(arrivedClient+clientTotalNumber));
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public ArrayList<Client> getClientList() {
		return clientList;
	}

	public void setClientList(ArrayList<Client> clientList) {
		this.clientList = clientList;
	}

	public ArrayList<Car> getCarList() {
		return carList;
	}

	public void setCarList(ArrayList<Car> carList) {
		this.carList = carList;
	}

	public double getCarSpeedMean() {
		return carSpeedMean;
	}

	public void setCarSpeedMean(double carSpeedMean) {
		this.carSpeedMean = carSpeedMean;
	}

	public double getClientSpeedMean() {
		return clientSpeedMean;
	}

	public void setClientSpeedMean(double clientSpeedMean) {
		this.clientSpeedMean = clientSpeedMean;
	}

	public double getClientRealSpeedMean() {
		return clientRealSpeedMean;
	}

	public void setClientRealSpeedMean(double clientRealSpeedMean) {
		this.clientRealSpeedMean = clientRealSpeedMean;
	}

	public double getArrivedRate() {
		return arrivedRate;
	}

	public void setArrivedRate(double arrivedRate) {
		this.arrivedRate = arrivedRate;
	}

	public int getDistSum() {
		return distSum;
	}

	public void setDistSum(int distSum) {
		this.distSum = distSum;
	}

	public int getConsumption() {
		return consumption;
	}

	public void setConsumption(int consumption) {
		this.consumption = consumption;
	}

	public Client getNotTargettedYet() {
		return notTargettedYet;
	}

	public void setNotTargettedYet(Client notTargettedYet) {
		this.notTargettedYet = notTargettedYet;
	}

	public //Numero de l'algorithme s�lectionn�
	int getAlgoId() {
		return algoId;
	}

	public void setAlgoId(//Numero de l'algorithme s�lectionn�
	int algoId) {
		this.algoId = algoId;
	}

	public boolean isDivide() {
		return divide;
	}

	public void setDivide(boolean divide) {
		this.divide = divide;
	}

	public int getOccupantCapacity() {
		return occupantCapacity;
	}

	public void setOccupantCapacity(int occupantCapacity) {
		this.occupantCapacity = occupantCapacity;
	}

	public int getCostRate() {
		return costRate;
	}

	public void setCostRate(int costRate) {
		this.costRate = costRate;
	}

	public int getStepMax() {
		return stepMax;
	}

	public void setStepMax(int stepMax) {
		this.stepMax = stepMax;
	}

	public boolean isNeedAlgorithme() {
		return needAlgorithme;
	}

	public void setNeedAlgorithme(boolean needAlgorithme) {
		this.needAlgorithme = needAlgorithme;
	}

	public Client getSelectedClient() {
		return selectedClient;
	}

	public void setSelectedClient(Client selectedClient) {
		this.selectedClient = selectedClient;
	}

	public Car getSelectedCar() {
		return selectedCar;
	}

	public void setSelectedCar(Car selectedCar) {
		this.selectedCar = selectedCar;
	}
}
