package Pack_Fenetre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

import Pack_Appli.Application;
import Pack_Simu.Car;
import Pack_Simu.Client;


//CityBoard est l'�l�ment central repr�sentant la ville
public class CityBoard extends JPanel
{
	private static final long serialVersionUID = -4239432318493357189L;
	
	/**TAILLE DU WIDGET**/
	private final int boardWidth = 25;
	private final int boardHeight = 25;
	private final int squareSize = 20;
	
	//on enregistre dans ce champ l'adresse de l'application pour acc�der aux variables de simulation
	Application app;
	
	CityBoard(Application consApp){
		Dimension boardDimension = new Dimension(getBoardWidth() * getSquareSize(),getBoardHeight() * getSquareSize());
		//La taille du Panel est fix�e � sa dimension initiale
		setMinimumSize(boardDimension);
		setMaximumSize(boardDimension);
		setPreferredSize(boardDimension);
		//Les �v�nements souris seront trait�s par la classe ci-dessous
		addMouseListener(consApp);
		app = consApp;
	}
	
	
	/** DESSIN **/
	//dessine le cityboard, appel� par la fonction repaint()
	public void paintComponent(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(Color.LIGHT_GRAY);
		for(int i = 0; i<getBoardHeight();i++)
			for(int j = 0; j<getBoardWidth(); j++)
				if(i%(app.getBlockSize()+1)!=0 && j%(app.getBlockSize()+1)!=0)
					g.fillRect(i*getSquareSize(),j*getSquareSize(),getSquareSize(),getSquareSize());
		g.setFont(new Font(g.getFont().getFontName(),Font.BOLD,2*getSquareSize()/3));
		//On dessine toutes les voitures
		for(Car car: app.getSimu().getCarList()) drawCar(g,car);
		if(app.getSimu().getNotTargettedYet() != null) drawClientPos(g,app.getSimu().getNotTargettedYet());
		for(Client cli: app.getSimu().getClientList()){
			//On dessine les positions des clients non embarqués
			if(cli.getState() == 0) drawClientPos(g,cli);
			//On dessine les destinations des clients définis et non arrivés
			if(cli.getState() < 2) drawClientTarget(g,cli);
		}
	}
	
	//Dessine une voiture au point p
	void drawCar(Graphics g, Car car)
	{
		g.setColor((car.isDoingCarSharing())?Color.RED:Color.GREEN);
		int x = car.getPos().getX()/getSquareSize()*getSquareSize();
		int y = car.getPos().getY()/getSquareSize()*getSquareSize();
		g.fillPolygon(
				new int[]{x, x + getSquareSize()/4, x + getSquareSize()/2, x + 3*getSquareSize()/4, x + getSquareSize(),
						x + getSquareSize(), x},
						new int[]{y + getSquareSize()/2,y + getSquareSize()/4,y + getSquareSize()/4,
						y+getSquareSize()/2,y + getSquareSize()/2,y + 3*getSquareSize()/4,y + 3*getSquareSize()/4}, 7);
		g.fillOval(x+1*getSquareSize()/8, y+5*getSquareSize()/8, getSquareSize()/4, getSquareSize()/4);
		g.fillOval(x+5*getSquareSize()/8, y+5*getSquareSize()/8, getSquareSize()/4, getSquareSize()/4);
		drawId(g,car.getId(),x,y);
	}

	//Dessine la position d'un client sur le trottoir
	void drawClientPos(Graphics g, Client cli){
		g.setColor(Color.BLUE);
		int x = cli.getPos()[0].getX()/getSquareSize()*getSquareSize();
		int y = cli.getPos()[0].getY()/getSquareSize()*getSquareSize();
		g.drawPolygon( new int[]{ x + getSquareSize()/4, x + 3*getSquareSize()/4, x + 3*getSquareSize()/4,
						x + getSquareSize()/2, x + getSquareSize()/4},
						new int[]{ y + getSquareSize()/4, y + getSquareSize()/4, y + getSquareSize(),
						y + 3*getSquareSize()/4, y + getSquareSize()}, 5);
		g.drawOval(x+3*getSquareSize()/8, y, getSquareSize()/4, getSquareSize()/4);
		drawId(g,cli.getId(),x,y);
	}

	//Dessine la destination d'un client
	void drawClientTarget(Graphics g, Client cli)
	{
		g.setColor((cli.isUsingCarSharing())?Color.BLUE:Color.CYAN);
		int x = cli.getPos()[1].getX()/getSquareSize()*getSquareSize();
		int y = cli.getPos()[1].getY()/getSquareSize()*getSquareSize();
		g.fillPolygon(
				new int[]{x + getSquareSize()/4, x + 3*getSquareSize()/4, x + getSquareSize()/4},
						new int[]{ y, y + getSquareSize()/4, y + getSquareSize()/2}, 3);
		g.drawLine(x + getSquareSize()/4, y, x + getSquareSize()/4,y+getSquareSize());
		drawId(g,cli.getId(),x,y);
	}
	
	//Dessine le num�ro de la voiture ou du client
	void drawId(Graphics g, int id, int x, int y){
		g.setColor(Color.BLACK);
		g.drawString(""+id,x + getSquareSize()/3, y + getSquareSize());
	}


	public int getSquareSize() {
		return squareSize;
	}


	public int getBoardWidth() {
		return boardWidth;
	}


	public int getBoardHeight() {
		return boardHeight;
	}
}
