package Pack_Appli;

import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import Pack_Simu.*;
import Pack_Fenetre.*;

public class Application implements ActionListener, MouseListener, ItemListener, ChangeListener, KeyEventDispatcher
{
	private Simulation simu;
	Fenetre_Appli window;
	//Par défaut la fenêtre de données n'est pas affichée
	SideWindow dataWindow = null;
	//Taille des blocs de la ville
	private int blockSize;

	//initialise la simulation et crée et ouvre la fenêtre
	Application()
	{
		setSimu(new Simulation(0,1,0,0));
		window = new Fenetre_Appli(this);
		if(Saving.getSavedSimuList().size()>0) window.getSavedSimuComboBox().setSelectedIndex(1);
		window.getSavedSimuComboBox().setSelectedIndex(0);
	}


	/** AFFICHAGE DE SIMULATIONS ENREGISTREES **/
	//Active ou désactive le bouton "Afficher"
	void setDisplaySavedSimuButton()
	{
		window.getDisplaySavedSimuButton().setEnabled(
				window.getSavedSimuComboBox().getSelectedIndex()!=0 || lastSimuArray!=null);
	}

	//Active ou désactive le bouton "Supprimer"
	void setDeleteSavedSimuButton()
	{
		window.getDeleteSavedSimuButton().setEnabled(window.getSavedSimuComboBox().getSelectedIndex()!=0);
	}

	//Affiche sur le cityboard la simulation enregistrée sélectionnée
	void displaySavedSimu()
	{
		clear();
		//On récupêre le numéro de la simulation enregistrée
		int n = window.getSavedSimuComboBox().getSelectedIndex();
		//S'il s'agit de la derniêre simulation
		int[][] t = (n==0)? lastSimuArray:
		//Sinon on récupère le tableau de la simulation
		Saving.savedSimuArrayOfSavedSimuString(Saving.getSavedSimuList().get(n-1));
		//on ajuste tous les paramètres
		window.getAlgorithmeArray()[t[0][0]].setSelected(true);
		window.getCostSlider().setValue(t[0][1]);
		window.getDivideCheckBox().setSelected(t[0][2]==1);
		window.getStepSpinner().setValue(t[0][3]);
		window.getOccupantSpinner().setValue(t[0][4]);
		window.getBlockSizeSpinner().setValue(t[0][5]);
		window.getSpeedSlider().setValue(t[0][6]);
		window.getAddClientCheckBox().setSelected(t[0][7]!=0);
		addClientEvent(t[0][7]!=0);
		if(t[0][7]!=0){
			window.getAddClientSpinner().setValue(t[0][7]);
			window.getIntervalSpinner().setValue(t[0][8]);
			window.getProbabilitySlider().setValue(t[0][9]);
		}
		//on crée les voitures et les clients de la configuration enregistrÃ©e
		for(int k=0;k<t[1].length;k=k+2)
			getSimu().newCar(t[1][k],t[1][k+1]);
		for(int l=0;l<t[2].length;l=l+2)
			getSimu().newClient(t[2][l],t[2][l+1]);
	}
	
	void newSavedSimulation()
	{
		//On récupêre le numéro de la simulation enregistrée
		int n = window.getSavedSimuComboBox().getSelectedIndex();
		//Si aucune simulation enregistrée n'est sélectionnée il s'agit d'une nouvelle
		if(n==0) Saving.newSavedSimu(simuArray(),window);
		//Sinon on modifie la simulation enregistrée
		else Saving.editSavedSimu(simuArray(),n-1,window);
	}


	/** LANCEMENT ET REMISE A ZERO DE LA SIMULATION **/
	//Ce tableau sauvegarde la simulation précédente
	int[][] lastSimuArray;

	//renvoie le tableau de sauvegarde asocié aux paramètres et à simu
	int[][] simuArray()
	{
		return new int[][]{{window.getAlgorithmeId(),
			window.getCostSlider().getValue(),
			(window.getDivideCheckBox().isSelected())?1:0,
			(Integer) window.getStepSpinner().getValue(),
			(Integer) window.getOccupantSpinner().getValue(),
			(Integer) window.getBlockSizeSpinner().getValue(),
			window.getSpeedSlider().getValue(),
			(window.getAddClientCheckBox().isSelected())?(Integer)window.getAddClientSpinner().getValue():0,
			(Integer)window.getIntervalSpinner().getValue(),
			window.getProbabilitySlider().getValue()},
			getSimu().carIntArray(),getSimu().clientIntArray()};
	}
	
	void doAlgorithme()
	{
		//on sauvegarde la simulation avant de la modifier
		lastSimuArray = simuArray();
		//on met à jour le bouton "afficher"
		setDisplaySavedSimuButton();
		//On informe simu des paramètres d'algorithme
		getSimu().setAlgoId(window.getAlgorithmeId());
		getSimu().setCostRate(window.getCostSlider().getValue());
		getSimu().setDivide(window.getDivideCheckBox().isSelected());
		getSimu().setStepMax((Integer) window.getStepSpinner().getValue());
		getSimu().setOccupantCapacity((Integer) window.getOccupantSpinner().getValue());
		//on lance l'algorithme
		getSimu().algorithme();
	}
	
	//Arrete et efface la simulation
	void clear()
	{
		//arrête le timer
		timerStop();
		//réinitialise les variables de simulations
		setSimu(new Simulation(window.getBoard().getSquareSize()/2,
				(getBlockSize()+1)*window.getBoard().getSquareSize(),
				window.getBoard().getBoardWidth()*window.getBoard().getSquareSize(),
				window.getBoard().getBoardHeight()*window.getBoard().getSquareSize()));
		//on actualise la fenêtre
		refresh();
	}

	
	/** TIMER : DEMARRAGE, PAUSE ET EVENEMENTS **/
	Timer timer = new Timer(0,this);
	//Crée le timer à partir de la valeur du slideur et démarre
	void timerStart()
	{
		window.getStartButton().setText("Pause");
		timer = new Timer(500/window.getSpeedSlider().getValue(),this);
		timer.start();
	}
	
	//Arrête le timer
	void timerStop()
	{
		timer.stop();
		window.getStartButton().setText("Start");
	}

	//actions aux échéances du timer
	void timerEvent()
	{
		//On rajoute un client automatiquement si demandé
		addClient();
		//On calcule le parcours si la configuration a changé
		if(getSimu().isNeedAlgorithme()) doAlgorithme();
		//on bouge les voitures d'un cran
		getSimu().OneMove();
		//on actualise la fenêtre
		refresh();
	}

	//Mise à jour de la fenêtre
	void refresh()
	{
		//On redessine le cityBoard
		window.getBoard().repaint();
		//On met à jour la fenêtre de données
		if(dataWindow != null) dataWindow.setDataLabel(getSimu());
	}
	
	//Compte à rebours
	int addClientTime = -1;
	//Cette fonction gêre l'ajout automatique d'un client aléatoire
	void addClient(){
		//Si la case d'ajout automatique est sélectionnée on réinitialise le compte à rebours
		if(window.getAddClientCheckBox().isSelected() && addClientTime == -1)
			addClientTime = (Integer) window.getIntervalSpinner().getValue();
		if(addClientTime != -1){
			//Lorsque le compte à rebours est à zéro, on fait autant de fois demandé l'ajout
			if(addClientTime == 0) for(int z = 0;z<(Integer) window.getAddClientSpinner().getValue();z++){
				//On simule deux clics d'abscisse et d'ordonnée aléatoire
				getSimu().newClient(randomWidth(), randomHeight());
				getSimu().newClient(randomWidth(), randomHeight());
				//Si le voyageur ne participe pas au covoiturage
				if(getSimu().getSelectedClient() != null && Math.random()*100>window.getProbabilitySlider().getValue()){
					getSimu().getSelectedClient().setState(1);
					getSimu().getSelectedClient().setUsingCarSharing(false);
					getSimu().newCar(getSimu().getSelectedClient().getPos()[0].getX(), getSimu().getSelectedClient().getPos()[0].getY());
					getSimu().getSelectedClient().setCar(getSimu().getSelectedCar());
					getSimu().getSelectedCar().setDoingCarSharing(false);
					getSimu().getSelectedCar().getParcoursList().add(new ParcoursStep(getSimu().getSelectedClient(),1));
				}
			}
			//On décrémente le compte à rebours
			addClientTime--;
		}
}
	
	//Ces fonctions génèrent des coordonnées aléatoirement
	int randomWidth(){return blockModulo((int) (window.getBoard().getWidth()*Math.random()));}
	int randomHeight(){return blockModulo((int) (window.getBoard().getHeight()*Math.random()));}
	
	
	/** EVENEMENTS SOURIS, BOUTONS, TIMER, LISTE, SLIDER ET CLAVIER **/
	//Actions lorsque l'on appuie sur un des bouttons de la souris
	public void mousePressed(MouseEvent event){
		int i = event.getX()/window.getBoard().getSquareSize();
		int j = event.getY()/window.getBoard().getSquareSize();
		//Clique gauche
		if (event.getButton()==MouseEvent.BUTTON1) getSimu().setSelectedClient(selectClient(i,j));
		//Clique droit
		if (event.getButton()==MouseEvent.BUTTON3) getSimu().setSelectedCar(selectCar(i,j));
	}
	
	//fonction qui à une coordonnée sur le board associe la coordonnée du carrefour proche
	int blockModulo(int x){
		return (x/window.getBoard().getSquareSize())/(getBlockSize()+1)*(getBlockSize()+1)*window.getBoard().getSquareSize();
	}
	
	int dragType = -1;
	//Actions lorsque la souris est relachée
	public void mouseReleased(MouseEvent event){
		int x = blockModulo(event.getX());
		int y = blockModulo(event.getY());
		//Si on est à l'intérieur du CityBoard, on crée ou modifie un client ou une voiture
		if(x>=0 && x<window.getBoard().getHeight() && y>=0 && y<window.getBoard().getWidth())
		{
			if(dragType == -1){
				//Clique gauche
				if (event.getButton()==MouseEvent.BUTTON1) getSimu().newClient(x,y);
				//Clique droit
				if (event.getButton()==MouseEvent.BUTTON3) getSimu().newCar(x,y);
			}
			//Si un client est sélectionné
			else if (dragType == 0 || dragType == 1) getSimu().getSelectedClient().getPos()[dragType].set(x,y);
			//Si une voiture est sélectionnée
			else if (dragType == 2) getSimu().getSelectedCar().getPos().set(x,y);
		}
		//Si on est à l'extérieur du cityBoard, on supprime la sélection
		else {
			if(dragType == 0 || dragType == 1) getSimu().deleteClient();
			else if(dragType == 2) getSimu().deleteCar();
		}
		dragType = -1;
		refresh();
	}
	
	//Pour les autres événements de souris, on ne fait rien
	public void mouseEntered(MouseEvent event){}
	public void mouseExited(MouseEvent event){}
	public void mouseClicked(MouseEvent arg0) {}

	//Renvoie le client le plus récent aux coordonnées (i,j)
	Client selectClient(int i, int j){
		if(getSimu().getNotTargettedYet() != null
				&& getSimu().getNotTargettedYet().getPos()[0].getX()/window.getBoard().getSquareSize() == i
				&& getSimu().getNotTargettedYet().getPos()[0].getY()/window.getBoard().getSquareSize() == j){
			dragType = 0;
			return getSimu().getNotTargettedYet();
		}	
		for(int l = getSimu().getClientList().size()-1;l >=0; l--)
			for(int type = 1; type >= 0; type--)
				if(getSimu().getClientList().get(l).getPos()[type].getX()/window.getBoard().getSquareSize() == i
				&& getSimu().getClientList().get(l).getPos()[type].getY()/window.getBoard().getSquareSize() == j){
					dragType = type;
					return getSimu().getClientList().get(l);
				}
		return null;
	}
	
	//Renvoie la voiture la plus récente aux coordonnées (i,j)
	Car selectCar(int i, int j){
		for(int k = getSimu().getCarList().size()-1;k>=0; k--)
			if(getSimu().getCarList().get(k).getPos().getX()/window.getBoard().getSquareSize() == i
			&& getSimu().getCarList().get(k).getPos().getY()/window.getBoard().getSquareSize() == j){
				dragType = 2;
				return getSimu().getCarList().get(k);
			}
		return null;
	}
	
	
	/** EVENEMENTS BOUTONS, TIMER, LISTE, SLIDER ET CLAVIER **/
	//Actions au clic des boutons et aux échéances du timer
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == window.getQuitButton())
			window.dispose();
		else if(evt.getSource() == window.getStartButton())
			if(!timer.isRunning()) timerStart(); else timerStop();
		else if(evt.getSource() == window.getClearButton())
			clear();
		else if(evt.getSource() == window.getDataButton())
			dataWindow = new SideWindow(window,"Datas");
		else if(evt.getSource() == window.getHelpButton())
			new SideWindow(window,"Help");
		else if(evt.getSource() == window.getDisplaySavedSimuButton())
			displaySavedSimu();
		else if(evt.getSource() == window.getNewSavedSimuButton())
			newSavedSimulation();
		else if(evt.getSource() == window.getDeleteSavedSimuButton())
			Saving.deleteSavedSimu(window.getSavedSimuComboBox().getSelectedIndex()-1,window);
		else if(evt.getSource() == window.getAddClientCheckBox())
			addClientEvent(window.getAddClientCheckBox().isSelected());
		else if(evt.getSource() == timer)
			timerEvent();
	}

	//Actions lorsque la case d'ajout de clients est cochée ou décochée
	void addClientEvent(boolean isChecked){
		window.getAddClientSpinner().setEnabled(isChecked);
		window.getIntervalSpinner().setEnabled(isChecked);
		window.getProbabilitySlider().setEnabled(isChecked);
	}
	
	//Actions lorsqu'un élément de la liste des simulations enregistrÃ©es est sÃ©lectionnÃ©
	public void itemStateChanged(ItemEvent arg0)
	{
		if(arg0.getStateChange() == ItemEvent.SELECTED)
		{
			//On met à jour le bouton "afficher"
			setDisplaySavedSimuButton();
			//On met à jour le bouton "supprimer"
			setDeleteSavedSimuButton();
			if(window.getDisplaySavedSimuButton().isEnabled()) displaySavedSimu();
		}
	}

	public void stateChanged(ChangeEvent arg0)
	{
		//Actions lorsque le curseur du slideur a bougé
		//si le timer est en route, on le modifie sinon on ne fait rien
		if(arg0.getSource() == window.getSpeedSlider() && timer.isRunning()){
			timer.stop();
			timerStart();
		}
		//Actions lorsque la taille des blocks est modifiée
		else if(arg0.getSource() == window.getBlockSizeSpinner()){
			setBlockSize((Integer) window.getBlockSizeSpinner().getValue());
			clear();
		}
	}

	//Actions aux événements claviers
	public boolean dispatchKeyEvent(KeyEvent arg0) {
		if (arg0.getID() == KeyEvent.KEY_PRESSED)
			if(arg0.getKeyCode() == KeyEvent.VK_F1){window.getStartButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F2){window.getClearButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F3){window.getDataButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F4){window.getQuitButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F5){window.getDisplaySavedSimuButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F6){window.getDeleteSavedSimuButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F7){window.getNewSavedSimuButton().doClick();}
			else if(arg0.getKeyCode() == KeyEvent.VK_F8){}
			else if(arg0.getKeyCode() == KeyEvent.VK_F9){}
			else if(arg0.getKeyCode() == KeyEvent.VK_F10){}
		//F11 ne marche pas
			else if(arg0.getKeyCode() == KeyEvent.VK_F12){window.getHelpButton().doClick();}
		return false;
	}


	public int getBlockSize() {
		return blockSize;
	}


	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}


	public Simulation getSimu() {
		return simu;
	}


	public void setSimu(Simulation simu) {
		this.simu = simu;
	}
}
