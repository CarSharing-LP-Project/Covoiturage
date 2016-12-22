package Pack_Simu;

import java.util.ArrayList;
import java.util.Arrays;

public class Algo_Deterministe implements I_Algorithme{
	
	int clientWaitingNumber;
	int carAlgoNumber;
	int occupantMax;
	ArrayList<Car> carAlgoList;
	int [][] carOccupantArray;
	ArrayList<Client> clientAlgoList;
	int clientAlgoNumber;
	int costMin;
	int [][] matriceDePassage;
	Simulation simu;
	
	
	
	
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
				<= simu.getStepMax())
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
								<= simu.getOccupantCapacity());
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
							int cost = simu.cost(new int[][]{lineAux},
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
	}
}
