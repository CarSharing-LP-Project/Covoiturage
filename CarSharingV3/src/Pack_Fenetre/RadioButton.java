package Pack_Fenetre;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class RadioButton extends JRadioButton{
	private static final long serialVersionUID = 6729994551712467626L;

	RadioButton(String buttonName, ButtonGroup buttonGroup, JPanel layout, boolean isEnable)
	{
		//Titre de la fonction de cout
		setText(buttonName);
		//Activé ou non
		setEnabled(isEnable);
		//On l'ajoute au buttonGroup qui gêre l'exclusivité de la selection
		buttonGroup.add(this);
		//On l'ajoute au layout (pour qu'il soit visible)
		layout.add(this);
	}
}
