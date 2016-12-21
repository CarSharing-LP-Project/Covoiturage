package Pack_Fenetre;

import java.awt.Dimension;

import javax.swing.JButton;

import Pack_Appli.Application;


class Button extends JButton{
	private static final long serialVersionUID = 637262569694527029L;

	Button(String buttonName,Application app,boolean isEnable){
		//Texte sur le bouton
		setText(buttonName);
		//Activé ou non
		setEnabled(isEnable);
		//On permet aux boutons de s'étirer autant que possible
		setMaximumSize(new Dimension(Short.MAX_VALUE,Short.MAX_VALUE));
		//classe ou sera envoyé le signal
		addActionListener(app);
	}
}
