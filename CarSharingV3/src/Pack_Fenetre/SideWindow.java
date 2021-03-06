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

//SideWindow est ou bien la fen�tre de donn�es ou bien la fen�tre d'instructions
public class SideWindow extends JDialog{
	private static final long serialVersionUID = -8863025054299250887L;
	
	//contient le texte affich� si fen�tre de data
	JLabel dataLabel;
	
	//l'appel de cette fonction met � jour les donn�es affich�es
	public void setDataLabel(Simulation simu){
		String text ="<html>";
		text += "Time : "+simu.getTime()+"<br><br>";
		text += "Nombre de voyageurs : "+simu.getClientList().size()+"<br><br>";
		text += "Nombre de voitures : "+simu.getCarList().size()+"<br><br>";
		text += "Moyenne des vitesses instantan�es :<br>"+simu.getCarSpeedMean()+"<br><br>";
		text += "Moyenne des vitesses des trajets termin�s :<br>"+simu.getClientSpeedMean()+"<br><br>";
		text += "Moyenne des vitesses des trajets en cours :<br>"+simu.getClientRealSpeedMean()+"<br><br>";
		text += "Taux de voyageurs arriv�s :<br>"+simu.getArrivedRate()+"<br><br>";
		text += "Somme des distances parcourues :<br>"+simu.getDistSum()+"<br><br>";
		text += "Consommation de carburant :<br>"+simu.getConsumption()+"<br><br>";
		text += "Voitures participant au covoiturage :<br>";
		for(Car car: simu.getCarList())
		{
			if(car.isDoingCarSharing()){
				text +="Voiture n� "+car.getId()+"<br> Occupants : ";
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
	"Cliquer sur le carr� blanc pour placer des clients (clique gauche) " +
	"et des voitures (clique droit). " +
	"Le premier clique gauche correspond �la position du client " +
	"et le deuxi�me �sa destination. " +
	"Pour modifier une position, effectuer un cliquer-glisser avec le bouton" +
	" de la souris correspondant (gauche pour les clients, " +
	"droit pour les voitures). " +
	"Pour supprimer un client ou une voiture, cliquer-glisser en dehors" +
	" du carr� blanc, toujours avec le bon bouton. " +
	"Pour tout effacer, cliquer sur le boutton \"Clear\". \n" +
	"\n" +
	"Cliquer sur \"Start\" pour d�marrer la simulation. " +
	"Vous pouvez �tout moment mettre la simulation sur pause en cliquant " +
	"sur \"Pause\". Vous pouvez �galement r�gler la vitesse de la simulation " +
	"quand vous voulez gr�ce au slider de droite. Un spinner permet de r�gler le nombre " +
	"maximum de clients que peut transporter une voiture en m�me temps. " +
	"Il est �galement possible de r�gler la taille des blocks constituant la ville.\n" +
	"\n" +
	"Comme il s'agit d'un logiciel de covoiturage dynamique, " +
	"vous pouvez ajouter des clients et des voitures pendant la simulation. " +
	"Si celle-ci n'est pas sur pause, l'algorithme " +
	"recalculera automatiquement le parcours.\n" +
	"\n" +
	"Avant le d�marrage d'une simulation, vous pouvez s�lectionner " +
	"l'algorithme d'optimisation ainsi que le param�tre de la fonction de co�t � minimiser " +
	"correspondant � la pr�f�rence pour la satisfiabilit� du client.\n" +
	"\n" +
	"Vous pouvez afficher la derni�re simulation d�marr�e en s�lectionnant " +
	"\"Derni�re simulation\" puis en cliquant sur le bouton \"Afficher\". " +
	"Vous avez la possibilit� d'enregistrer une simulation (cliquez sur " +
	"\"Enregistrer\" lorsque \"Derni�re simulation\" est s�lectionn�e), " +
	"que vous pouvez r�afficher, modifier et supprimer � souhait.\n" +
	"\n" +
	"Pour introduire automatiquement de nouveaux usagers de la route cochez la case correspondante, " +
	"en indiquant la p�riode ainsi que la probabilit� de pr�f�rence " +
	"pour le syst�me de covoiturage dynamique. La voiture des usagers qui n'utilisent " +
	"pas le syst�me de covoiturage est de couleur verte et leur destination de couleur bleu cyan.\n" +
	"\n" +
	"Cliquer sur le bouton \"donn�es\" pour visualiser quelques donn�es num�riques de la simulation.\n" +
	"\n" +
	"Les param�tres par d�faut sont les param�tres s�lectionn�s au d�marrage " +
	"de l'application. Vous pouvez les modifier, apr�s avoir s�lectionn� " +
	"\"Param�tres par d�faut\", puis en enregistrant.\n" +
	"\n" +
	"Les raccourcis claviers sont les suivants :\n" +
	"F1 : Start\n" +
	"F2 : Clear\n" +
	"F3 : Donn�es\n" +
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
		//S'il s'agit de la fen�tre de donn�es
		else if(title == "Datas"){
			dataLabel = new JLabel();
			text = dataLabel;
		}
		//cr�e un objet contenant des barres de d�filement
		JScrollPane pane = new JScrollPane(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		getContentPane().add(pane);
		//la fen�tre doit avoir la m�me hauteur que window et la colargeur de window
		setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width
				-window.getWidth(),
				window.getHeight());
		//on situe la fen�tre � droite de window
		setLocation(window.getWidth(),window.getLocation().y);
		//on affiche la fen�tre
		setVisible(true);
		//on d�cale window � gauche
		window.setLocation(0,window.getLocation().y);
	}
}
