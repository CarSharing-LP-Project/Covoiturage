package Pack_Appli;

//VERSION 6.3
//Derni�re modification le mardi 29 novembre 2016 � 14:26 par Romain HAGEMANN
//Contact : etienne.callies@polytechnique.org
//Encodage ISO-8859-1, Java 1.6 ou 1.7
//CarSharing : n voitures, m clients, une destination et une voiture par client, covoiturage dynamique
//Dans une ville type Manhattan avec embouteillages
//Algorithme : m�thode d�terministe ou m�thode du recuit simul�
//Fonction de cout r�aliste param�trable
//Possibilit� de rajouter des clients de mani�re al�atoire et continue
//Possibilit� de r�gler la probabilit� qu'un usager pr�f�re sa voiture au syst�me de covoiturage
//Possibilit� de voir en temps r�el les diff�rentes donn�es de simulation


//Variables � privil�gier :
//i : abscisse en nombre de carr�s
//j : ordonn�e en nombre de carr�s
//k : num�ro de Car s�lectionn�e
//l : num�ro du Client s�lectionn�
//m : une matrice
//n : num�ro de la simulation enregistr�e s�lectionn�e
//p : un Point
//q : une ordonn�e d'une matrice de passage, varie de 0 à 2*clientAlgoNumber-1,
//     pair pour les positions, impair pour les target
//t : un tableau
//x : abscisse en nombre de pixels
//y : ordonn�e en nombre de pixels
//z : index d'un tableau quelconque
//Le reste en anglais en respectant la casse helloWorld



class CarSharing
{
	//lance l'application
	public static void main(String args[])
	{
		new Application();
	} 
}
