import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;


public class Checkers3D extends JFrame
{
    WrapCheckers3D w3d = new WrapCheckers3D();
    public Checkers3D()
    {
        super("Checkers3D");
        Container c = getContentPane();
        c.setLayout(new BorderLayout());    // panel holding the 3D canvas
        c.add(w3d, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(false);    // fixed size display
        setVisible(true);
    }

    public static void  main(String[] args) {
        new Checkers3D();
    }

}
