package Pack_Appli;

//VERSION 6.3
//Dernière modification le mardi 29 novembre 2016 à 14:26 par Romain HAGEMANN
//Contact : etienne.callies@polytechnique.org
//Encodage ISO-8859-1, Java 1.6 ou 1.7
//CarSharing : n voitures, m clients, une destination et une voiture par client, covoiturage dynamique
//Dans une ville type Manhattan avec embouteillages
//Algorithme : méthode déterministe ou méthode du recuit simulé
//Fonction de cout réaliste paramétrable
//Possibilité de rajouter des clients de manière aléatoire et continue
//Possibilité de régler la probabilité qu'un usager préfère sa voiture au système de covoiturage
//Possibilité de voir en temps réel les différentes données de simulation


//Variables à privilégier :
//i : abscisse en nombre de carrés
//j : ordonnée en nombre de carrés
//k : numéro de Car sélectionnée
//l : numéro du Client sélectionné
//m : une matrice
//n : numéro de la simulation enregistrée sélectionnée
//p : un Point
//q : une ordonnée d'une matrice de passage, varie de 0 à 2*clientAlgoNumber-1,
//     pair pour les positions, impair pour les target
//t : un tableau
//x : abscisse en nombre de pixels
//y : ordonnée en nombre de pixels
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
