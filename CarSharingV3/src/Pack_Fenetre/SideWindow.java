package Pack_Fenetre;

import java.awt.Component;
import java.awt.GraphicsEnvironment;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import Pack_Simu.Car;
import Pack_Simu.Client;
import Pack_Simu.Simulation;

//SideWindow est ou bien la fenêtre de données ou bien la fenêtre d'instructions
public class SideWindow extends JDialog{
	private static final long serialVersionUID = -8863025054299250887L;
	
	//contient le texte affiché si fenêtre de data
	JLabel dataLabel;
	
	//l'appel de cette fonction met à jour les données affichées
	public void setDataLabel(Simulation simu){
		String text ="<html>";
		text += "Time : "+simu.getTime()+"<br><br>";
		text += "Nombre de voyageurs : "+simu.getClientList().size()+"<br><br>";
		text += "Nombre de voitures : "+simu.getCarList().size()+"<br><br>";
		text += "Moyenne des vitesses instantanées :<br>"+simu.getCarSpeedMean()+"<br><br>";
		text += "Moyenne des vitesses des trajets terminés :<br>"+simu.getClientSpeedMean()+"<br><br>";
		text += "Moyenne des vitesses des trajets en cours :<br>"+simu.getClientRealSpeedMean()+"<br><br>";
		text += "Taux de voyageurs arrivés :<br>"+simu.getArrivedRate()+"<br><br>";
		text += "Somme des distances parcourues :<br>"+simu.getDistSum()+"<br><br>";
		text += "Consommation de carburant :<br>"+simu.getConsumption()+"<br><br>";
		text += "Voitures participant au covoiturage :<br>";
		for(Car car: simu.getCarList())
		{
			if(car.isDoingCarSharing()){
				text +="Voiture n° "+car.getId()+"<br> Occupants : ";
				for(Client cli: car.getOccupantList())
					text += (cli.getState() == 1)?cli.getId()+" ":"<s>"+cli.getId()+"</s> ";
				text += "<br><br>";
			}
		}
		text += "</html>";
		dataLabel.setText(text);
	}
	
	//Initialisation de la fenêtre de côté
	public SideWindow(Fenetre_Appli window, String title){
		//définit window comme la fenêtre parente
		super(window,title);
		//la variable text contiendra le texte
		Component text = null;
		//S'il s'agit de la fenêtre d'instructions
		if(title == "Help"){
			JTextArea helpText = new JTextArea(
	"Cliquer sur le carré blanc pour placer des clients (clique gauche) " +
	"et des voitures (clique droit). " +
	"Le premier clique gauche correspond à la position du client " +
	"et le deuxième à sa destination. " +
	"Pour modifier une position, effectuer un cliquer-glisser avec le bouton" +
	" de la souris correspondant (gauche pour les clients, " +
	"droit pour les voitures). " +
	"Pour supprimer un client ou une voiture, cliquer-glisser en dehors" +
	" du carré blanc, toujours avec le bon bouton. " +
	"Pour tout effacer, cliquer sur le boutton \"Clear\". \n" +
	"\n" +
	"Cliquer sur \"Start\" pour démarrer la simulation. " +
	"Vous pouvez à tout moment mettre la simulation sur pause en cliquant " +
	"sur \"Pause\". Vous pouvez également régler la vitesse de la simulation " +
	"quand vous voulez grâce au slider de droite. Un spinner permet de régler le nombre " +
	"maximum de clients que peut transporter une voiture en même temps. " +
	"Il est également possible de régler la taille des blocks constituant la ville.\n" +
	"\n" +
	"Comme il s'agit d'un logiciel de covoiturage dynamique, " +
	"vous pouvez ajouter des clients et des voitures pendant la simulation. " +
	"Si celle-ci n'est pas sur pause, l'algorithme " +
	"recalculera automatiquement le parcours.\n" +
	"\n" +
	"Avant le démarrage d'une simulation, vous pouvez sélectionner " +
	"l'algorithme d'optimisation ainsi que le paramètre de la fonction de coût à minimiser " +
	"correspondant à la préférence pour la satisfiabilité du client.\n" +
	"\n" +
	"Vous pouvez afficher la dernière simulation démarrée en sélectionnant " +
	"\"Dernière simulation\" puis en cliquant sur le bouton \"Afficher\". " +
	"Vous avez la possibilité d'enregistrer une simulation (cliquez sur " +
	"\"Enregistrer\" lorsque \"Dernière simulation\" est sélectionnée), " +
	"que vous pouvez réafficher, modifier et supprimer à souhait.\n" +
	"\n" +
	"Pour introduire automatiquement de nouveaux usagers de la route cochez la case correspondante, " +
	"en indiquant la période ainsi que la probabilité de préférence " +
	"pour le système de covoiturage dynamique. La voiture des usagers qui n'utilisent " +
	"pas le système de covoiturage est de couleur verte et leur destination de couleur bleu cyan.\n" +
	"\n" +
	"Cliquer sur le bouton \"données\" pour visualiser quelques données numériques de la simulation.\n" +
	"\n" +
	"Les paramètres par défaut sont les paramètres sélectionnés au démarrage " +
	"de l'application. Vous pouvez les modifier, après avoir sélectionné " +
	"\"Paramètres par défaut\", puis en enregistrant.\n" +
	"\n" +
	"Les raccourcis claviers sont les suivants :\n" +
	"F1 : Start\n" +
	"F2 : Clear\n" +
	"F3 : Données\n" +
	"F4 : Quitter\n" +
	"F5 : Afficher\n" +
	"F6 : Supprimer\n" +
	"F7 : Enregistrer\n" +
	"F12 : Instructions\n"
					);
			helpText.setEditable(false);
			helpText.setLineWrap(true);
			helpText.setWrapStyleWord(true);
			text = helpText;
		}
		//S'il s'agit de la fenêtre de données
		else if(title == "Datas"){
			dataLabel = new JLabel();
			text = dataLabel;
		}
		//crée un objet contenant des barres de défilement
		JScrollPane pane = new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(pane);
		//la fenêtre doit avoir la même hauteur que window et la colargeur de window
		setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width
				-window.getWidth(),
				window.getHeight());
		//on situe la fenêtre à droite de window
		setLocation(window.getWidth(),window.getLocation().y);
		//on affiche la fenêtre
		setVisible(true);
		//on décale window à gauche
		window.setLocation(0,window.getLocation().y);
	}
}
