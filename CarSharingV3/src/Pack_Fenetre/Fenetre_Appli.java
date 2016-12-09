package Pack_Fenetre;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import Pack_Appli.Application;
import Pack_Appli.Saving;

public class Fenetre_Appli extends JFrame{
	private static final long serialVersionUID = -3022785407645996324L;
	//On déclare les variables pour pouvoir récupérer leur valeur et les modifier
	private CityBoard board;
	private JButton startButton;
	private JButton clearButton;
	private JButton dataButton;
	private JButton helpButton;
	private JButton quitButton;
	private JComboBox savedSimuComboBox;
	private JButton displaySavedSimuButton;
	private JButton deleteSavedSimuButton;
	private JButton newSavedSimuButton;
	private JSlider speedSlider;
	private JSlider costSlider;
	private JSpinner stepSpinner;
	private JCheckBox divideCheckBox;
	private JSpinner occupantSpinner;
	private JSpinner blockSizeSpinner;
	private JCheckBox addClientCheckBox;
	private JSpinner addClientSpinner;
	private JSpinner intervalSpinner;
	private JSlider probabilitySlider;
	
	private RadioButton[] algorithmeArray;
	ButtonGroup algorithme;
	//Cette fonction permet de récupérer le numéro de l'algorithme sélectioné
	public int getAlgorithmeId(){
		int algoId = 0;
		while(!getAlgorithmeArray()[algoId].isSelected()){algoId++;}
		return algoId;
	}
	
	//Initialisation de la fenêtre
	public Fenetre_Appli(Application app){
		//Initialisation du cityBoard (voir classe correspondante)
		setBoard(new CityBoard(app));
		
		//Initialisation des bouttons de la colonne de droite
		setStartButton(new Button("Start",app,true));
		setClearButton(new Button("Clear",app,true));
		setDataButton(new Button("Données",app,true));
		setHelpButton(new Button("Instructions",app,true));
		setQuitButton(new Button("Quitter",app,true));
		
		
		/** OBJETS RELATIFS A L'AFFICHAGE DE SIMULATIONS ENREGISTREES **/
		Saving.setSavedSimuList();
		JLabel savedSimuLabel = new JLabel("Simulations enregistrées :");
		setSavedSimuComboBox(new JComboBox());
		getSavedSimuComboBox().addItem("Dernière simulation");
		for(String s:Saving.getSavedSimuList())
			getSavedSimuComboBox().addItem(Saving.savedSimuNameOfSavedSimuString(s));
		getSavedSimuComboBox().setAlignmentX(Component.LEFT_ALIGNMENT);
		getSavedSimuComboBox().addItemListener(app);
		setDisplaySavedSimuButton(new Button("Afficher",app,false));
		setDeleteSavedSimuButton(new Button("Supprimer",app,false));
		setNewSavedSimuButton(new Button("Enregistrer / Modifier",app,true));
		JPanel savedSimuLayout = new JPanel();
		savedSimuLayout.setLayout(new BoxLayout(savedSimuLayout,BoxLayout.PAGE_AXIS));
		savedSimuLayout.add(savedSimuLabel);
		savedSimuLayout.add(getSavedSimuComboBox());
		JPanel savedSimuButtonLayout = new JPanel();
		savedSimuButtonLayout.setLayout(new BoxLayout(savedSimuButtonLayout,BoxLayout.LINE_AXIS));
		savedSimuButtonLayout.add(getDisplaySavedSimuButton());
		savedSimuButtonLayout.add(getDeleteSavedSimuButton());
		savedSimuButtonLayout.add(getNewSavedSimuButton());
		savedSimuButtonLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
		savedSimuLayout.add(savedSimuButtonLayout);
		
		
		/** OBJETS RELATIFS AU PARAMETRE DE LA FONCTION DE COUT **/
		JLabel costLabel = new JLabel("Préférence pour la satisfaction du client :");
		setCostSlider(new JSlider(SwingConstants.HORIZONTAL,0,100,0));
		getCostSlider().setMajorTickSpacing(50);
		getCostSlider().setMinorTickSpacing(10);
		getCostSlider().setPaintTicks(true);
		getCostSlider().setPaintLabels(true);
		getCostSlider().setAlignmentX(Component.LEFT_ALIGNMENT);
		JPanel costLayout = new JPanel();
		costLayout.setLayout(new BoxLayout(costLayout,BoxLayout.PAGE_AXIS));
		costLayout.add(costLabel);
		costLayout.add(getCostSlider());
			
		Dimension h1 = new Dimension(20,0);
		
		/** OBJETS RELATIFS AU CHOIX DE L'ALGORITHME **/
		JLabel algorithmeLabel = new JLabel("Algorithme :");
		algorithme = new ButtonGroup();
		JPanel algorithmeLayout = new JPanel();
		algorithmeLayout.setLayout(new BoxLayout(algorithmeLayout,BoxLayout.PAGE_AXIS));
		algorithmeLayout.add(algorithmeLabel);
		setAlgorithmeArray(new RadioButton[]{
				new RadioButton("Déterministe",algorithme,algorithmeLayout,true),
				new RadioButton("Recuit simulé",algorithme,algorithmeLayout,true),
				new RadioButton("Genetique",algorithme,algorithmeLayout,false)
		});
		setDivideCheckBox(new JCheckBox("Diviser pour mieux régner"));
		getDivideCheckBox().setEnabled(false);
		algorithmeLayout.add(getDivideCheckBox());
		JLabel stepLabel = new JLabel("Nombre d'étapes : ");
		setStepSpinner(new JSpinner(new SpinnerNumberModel(100,1,999999999,1)));
		JPanel stepLayout = new JPanel();
		stepLayout.setLayout(new BoxLayout(stepLayout,BoxLayout.LINE_AXIS));
		stepLayout.add(stepLabel);
		stepLayout.add(Box.createRigidArea(h1));
		stepLayout.add(getStepSpinner());
		stepLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
		algorithmeLayout.add(stepLayout);
		
		
		/** OBJETS RELATIFS AU CHOIX DE LA VITESSE **/
		JLabel speedLabel = new JLabel("Vitesse de la simulation :");
		setSpeedSlider(new JSlider(SwingConstants.HORIZONTAL,1,10,1));
		getSpeedSlider().setMinorTickSpacing(1);
		getSpeedSlider().setPaintTicks(true);
		getSpeedSlider().setAlignmentX(Component.LEFT_ALIGNMENT);
		getSpeedSlider().addChangeListener(app);
		JPanel speedLayout = new JPanel();
		speedLayout.setLayout(new BoxLayout(speedLayout,BoxLayout.PAGE_AXIS));
		speedLayout.add(speedLabel);
		speedLayout.add(getSpeedSlider());
		
		
		/** OBJETS RELATIFS AU NOMBRE DE PASSAGERS **/
		JLabel occupantLabel = new JLabel("Capacités des voitures :");
		setOccupantSpinner(new JSpinner(new SpinnerNumberModel(5,1,999,1)));
		JPanel occupantLayout = new JPanel();
		occupantLayout.setLayout(new BoxLayout(occupantLayout,BoxLayout.PAGE_AXIS));
		occupantLayout.add(occupantLabel);
		occupantLayout.add(getOccupantSpinner());
		occupantLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		/** OBJETS RELATIFS A LA TAILLE DES BLOCKS **/
		JLabel blockSizeLabel = new JLabel("Taille des blocks :");
		setBlockSizeSpinner(new JSpinner(new SpinnerNumberModel(0,0,getBoard().getBoardHeight(),1)));
		getBlockSizeSpinner().addChangeListener(app);
		JPanel blockSizeLayout = new JPanel();
		blockSizeLayout.setLayout(new BoxLayout(blockSizeLayout,BoxLayout.PAGE_AXIS));
		blockSizeLayout.add(blockSizeLabel);
		blockSizeLayout.add(getBlockSizeSpinner());
		blockSizeLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		/** OBJETS RELATIFS A L'AJOUT AUTOMATIQUE DES CLIENTS **/
		setAddClientCheckBox(new JCheckBox("Ajouter "));
		getAddClientCheckBox().addActionListener(app);
		setAddClientSpinner(new JSpinner(new SpinnerNumberModel(1,1,999999,1)));
		getAddClientSpinner().setEnabled(false);
		JLabel addClientLabel = new JLabel(" clients à intervalle de ");
		setIntervalSpinner(new JSpinner(new SpinnerNumberModel(5,1,999999,1)));
		getIntervalSpinner().setEnabled(false);
		JPanel addClientLayout = new JPanel();
		addClientLayout.setLayout(new BoxLayout(addClientLayout,BoxLayout.LINE_AXIS));
		addClientLayout.add(getAddClientCheckBox());
		addClientLayout.add(getAddClientSpinner());
		addClientLayout.add(addClientLabel);
		addClientLayout.add(getIntervalSpinner());
		addClientLayout.setAlignmentX(Component.LEFT_ALIGNMENT);
		setProbabilitySlider(new JSlider(SwingConstants.HORIZONTAL,0,100,0));
		getProbabilitySlider().setMajorTickSpacing(50);
		getProbabilitySlider().setMinorTickSpacing(10);
		getProbabilitySlider().setPaintTicks(true);
		getProbabilitySlider().setPaintLabels(true);
		getProbabilitySlider().setAlignmentX(Component.LEFT_ALIGNMENT);
		
		
		
		
		/** GEOMETRIE DE LA FENETRE **/
		//Colonne de gauche
		JPanel leftColumn = new JPanel();
		leftColumn.setLayout(new BoxLayout(leftColumn,BoxLayout.PAGE_AXIS));
		Dimension v1 = new Dimension(0,30);
		leftColumn.add(savedSimuLayout);
		leftColumn.add(Box.createRigidArea(v1));
		leftColumn.add(algorithmeLayout);
		leftColumn.add(Box.createRigidArea(v1));
		leftColumn.add(costLayout);
		leftColumn.add(Box.createRigidArea(v1));
		leftColumn.add(addClientLayout);
		leftColumn.add(new JLabel("Avec probabilité d'utilisation du covoiturage dynamique :"));
		leftColumn.add(getProbabilitySlider());
		
		//Colonne de droite
		JPanel rightColumn = new JPanel();
		rightColumn.setLayout(new BoxLayout(rightColumn,BoxLayout.PAGE_AXIS));
		Dimension v2 = new Dimension(0,5);
		rightColumn.add(getHelpButton());
		rightColumn.add(Box.createRigidArea(v2));
		rightColumn.add(getDataButton());
		rightColumn.add(Box.createRigidArea(v1));
		rightColumn.add(blockSizeLayout);
		rightColumn.add(Box.createRigidArea(v1));
		rightColumn.add(occupantLayout);
		rightColumn.add(Box.createRigidArea(v1));
		rightColumn.add(speedLayout);
		rightColumn.add(Box.createRigidArea(v1));
		rightColumn.add(getStartButton());
		rightColumn.add(Box.createRigidArea(v2));
		rightColumn.add(getClearButton());
		rightColumn.add(Box.createRigidArea(v2));
		rightColumn.add(getQuitButton());
		
		//Géométrie générale
		JPanel container = new JPanel();
		container.setLayout(new FlowLayout());
		container.add(leftColumn);
		container.add(getBoard());
		container.add(rightColumn);
		setContentPane(container);
		
		//Dernier paramètre
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(app);
		setTitle("Car Sharing");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  	pack();
		setLocationRelativeTo(null);
	  	setVisible(true);
	}

	public JComboBox getSavedSimuComboBox() {
		return savedSimuComboBox;
	}

	public void setSavedSimuComboBox(JComboBox savedSimuComboBox) {
		this.savedSimuComboBox = savedSimuComboBox;
	}

	public JButton getDisplaySavedSimuButton() {
		return displaySavedSimuButton;
	}

	public void setDisplaySavedSimuButton(JButton displaySavedSimuButton) {
		this.displaySavedSimuButton = displaySavedSimuButton;
	}

	public JButton getDeleteSavedSimuButton() {
		return deleteSavedSimuButton;
	}

	public void setDeleteSavedSimuButton(JButton deleteSavedSimuButton) {
		this.deleteSavedSimuButton = deleteSavedSimuButton;
	}

	public RadioButton[] getAlgorithmeArray() {
		return algorithmeArray;
	}

	public void setAlgorithmeArray(RadioButton[] algorithmeArray) {
		this.algorithmeArray = algorithmeArray;
	}

	public JSlider getCostSlider() {
		return costSlider;
	}

	public void setCostSlider(JSlider costSlider) {
		this.costSlider = costSlider;
	}

	public JCheckBox getDivideCheckBox() {
		return divideCheckBox;
	}

	public void setDivideCheckBox(JCheckBox divideCheckBox) {
		this.divideCheckBox = divideCheckBox;
	}

	public JSpinner getStepSpinner() {
		return stepSpinner;
	}

	public void setStepSpinner(JSpinner stepSpinner) {
		this.stepSpinner = stepSpinner;
	}

	public JSpinner getOccupantSpinner() {
		return occupantSpinner;
	}

	public void setOccupantSpinner(JSpinner occupantSpinner) {
		this.occupantSpinner = occupantSpinner;
	}

	public JSpinner getBlockSizeSpinner() {
		return blockSizeSpinner;
	}

	public void setBlockSizeSpinner(JSpinner blockSizeSpinner) {
		this.blockSizeSpinner = blockSizeSpinner;
	}

	public JSlider getSpeedSlider() {
		return speedSlider;
	}

	public void setSpeedSlider(JSlider speedSlider) {
		this.speedSlider = speedSlider;
	}

	public JCheckBox getAddClientCheckBox() {
		return addClientCheckBox;
	}

	public void setAddClientCheckBox(JCheckBox addClientCheckBox) {
		this.addClientCheckBox = addClientCheckBox;
	}

	public JSpinner getIntervalSpinner() {
		return intervalSpinner;
	}

	public void setIntervalSpinner(JSpinner intervalSpinner) {
		this.intervalSpinner = intervalSpinner;
	}

	public JSpinner getAddClientSpinner() {
		return addClientSpinner;
	}

	public void setAddClientSpinner(JSpinner addClientSpinner) {
		this.addClientSpinner = addClientSpinner;
	}

	public JSlider getProbabilitySlider() {
		return probabilitySlider;
	}

	public void setProbabilitySlider(JSlider probabilitySlider) {
		this.probabilitySlider = probabilitySlider;
	}

	public CityBoard getBoard() {
		return board;
	}

	public void setBoard(CityBoard board) {
		this.board = board;
	}

	public JButton getStartButton() {
		return startButton;
	}

	public void setStartButton(JButton startButton) {
		this.startButton = startButton;
	}

	public JButton getQuitButton() {
		return quitButton;
	}

	public void setQuitButton(JButton quitButton) {
		this.quitButton = quitButton;
	}

	public JButton getClearButton() {
		return clearButton;
	}

	public void setClearButton(JButton clearButton) {
		this.clearButton = clearButton;
	}

	public JButton getDataButton() {
		return dataButton;
	}

	public void setDataButton(JButton dataButton) {
		this.dataButton = dataButton;
	}

	public JButton getHelpButton() {
		return helpButton;
	}

	public void setHelpButton(JButton helpButton) {
		this.helpButton = helpButton;
	}

	public JButton getNewSavedSimuButton() {
		return newSavedSimuButton;
	}

	public void setNewSavedSimuButton(JButton newSavedSimuButton) {
		this.newSavedSimuButton = newSavedSimuButton;
	}
}
