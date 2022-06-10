import javax.swing.*;
import java.awt.*;


public class SafeLock3D extends JFrame
{

    SafeUniverse safeU = new SafeUniverse();

    public SafeLock3D()
    {
        super("Safe Lock Mechanism");

        Container c = getContentPane();
        c.setLayout(new BorderLayout());    // panel holding the 3D canvas
        c.add(safeU, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setResizable(true);                // fixed size display
        setVisible(true);
    }


    public static void  main(String[] args) {
        new SafeLock3D();
    }

}
